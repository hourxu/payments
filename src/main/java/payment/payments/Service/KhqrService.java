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

    private final RestTemplate restTemplate = new RestTemplate();

    public String Generateqr(GenerateQRResquest resquest) {
        String transactionId = resquest.getOrderId();
        String amount = String.valueOf(resquest.getAmount());
        String successUrl = "https://yourdomain.com/payment/success"; // TODO: replace with your real success URL
        String remark = "Order " + transactionId; // TODO: customize as needed

        String hash = generateHash(apiKey, transactionId, amount, successUrl, remark);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("transaction_id", transactionId);
        param.add("amount", amount);
        param.add("success_url", successUrl);
        param.add("remark", remark);
        param.add("hash", hash);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(param, headers);
        String url = baseUrl + "/" + profileId + "/payment-gateway/v1/payments/qr-api-khqrcc";
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
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
}