package payment.payments.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import payment.payments.DTO.GenerateQRResquest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class KhqrService {

    @Value("${khqr.profile-id}")
    private String profileId;

    @Value("${khqr.api-key}")
    private String apiKey;

    @Value("${khqr.base-url}")
    private String baseUrl;

    @Value("${khqr.callback-url}")
    private String callbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String Generateqr(GenerateQRResquest resquest) {
        String transactionId = resquest.getOrderId();
        String amount = String.valueOf(resquest.getAmount());
        String successUrl = "https://yourdomain.com/payment/success";
        String remark = "Order " + transactionId;
        String hash = generateHash(apiKey, transactionId, amount, successUrl, remark);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("transaction_id", transactionId);
        param.add("amount", amount);
        param.add("success_url", successUrl);
        param.add("remark", remark);
        param.add("callback_url", callbackUrl);
        param.add("hash", hash);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(param, headers);
        String url = baseUrl + "/" + profileId + "/payment-gateway/v1/payments/qr-api";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody();
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.out.println("KHQR API error status: " + e.getStatusCode());
            System.out.println("KHQR API error body: " + e.getResponseBodyAsString());
            throw new RuntimeException("KHQR gateway rejected request: " + e.getResponseBodyAsString(), e);
        }
    }

    private String generateHash(String secret, String id, String amount, String successUrl, String remark) {
        String raw = secret + id + amount + successUrl + remark;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(raw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to generate hash", e);
        }
    }

    public boolean verifyCallbackHash(String reqTime, String transactionId, String amount, String status, String receivedHash) {
        String raw = apiKey + reqTime + transactionId + amount + status;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(receivedHash);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Callback hash verification failed", e);
        }
    }

    // ==== NEW: used by the scheduled poller as a webhook backup ====
    public boolean isTransactionPaid(String transactionId) {
        String hash = generateSimpleHash(apiKey, transactionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("transaction_id", transactionId);
        param.add("hash", hash);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(param, headers);
        String url = baseUrl + "/" + profileId + "/payment-gateway/v1/payments/check-trans";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String body = response.getBody();
            System.out.println("check-transaction response for " + transactionId + ": " + body);

            if (body == null) return false;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);
            String status = root.path("data").path("status").asText("");

            return status.equalsIgnoreCase("paid") || status.equalsIgnoreCase("success") || status.equalsIgnoreCase("completed");
        } catch (Exception e) {
            System.out.println("check-transaction failed for " + transactionId + ": " + e.getMessage());
            return false;
        }
    }
    // ==== END NEW ====

    private String generateSimpleHash(String secret, String id) {
        String raw = secret + id;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(raw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to generate hash", e);
        }
    }
}