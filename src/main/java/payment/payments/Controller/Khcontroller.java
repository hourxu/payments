package payment.payments.Controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.payments.DTO.GenerateQRResquest;
import payment.payments.DTO.PaymentStatusStore;
import payment.payments.DTO.WebhookResponse;
import payment.payments.Service.KhqrService;
import payment.payments.Service.PaymentStatusStoreHolder;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class Khcontroller {

    private static final Logger log = LoggerFactory.getLogger(Khcontroller.class);

    private final KhqrService khqrService;
    private final PaymentStatusStoreHolder statusHolder;

    @PostMapping("/generateQR")
    public ResponseEntity<String> GenerateQR(@RequestBody GenerateQRResquest resquest){
        String result = khqrService.Generateqr(resquest);
        statusHolder.getStore().put(resquest.getOrderId(), "PENDING");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/webhook")
    public ResponseEntity<WebhookResponse> handleCallback(@RequestBody Map<String, Object> payload) {
        try {
            String reqTime = String.valueOf(payload.get("req_time"));
            String transactionId = (String) payload.get("transaction_id");
            String amount = String.valueOf(payload.get("amount"));
            String status = (String) payload.get("status");
            String hash = (String) payload.get("hash");

            if (transactionId == null || status == null || hash == null) {
                log.warn("Webhook payload missing required fields: {}", payload);
                return ResponseEntity.badRequest()
                        .body(WebhookResponse.error("missing required fields"));
            }

            boolean valid = khqrService.verifyCallbackHash(reqTime, transactionId, amount, status, hash);
            if (!valid) {
                log.warn("Webhook signature invalid for transaction {}", transactionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(WebhookResponse.error("Invalid signature"));
            }

            // match the same case-insensitive check used in KhqrService.isTransactionPaid
            if (status.equalsIgnoreCase("paid")
                    || status.equalsIgnoreCase("success")
                    || status.equalsIgnoreCase("completed")) {
                statusHolder.getStore().put(transactionId, "PAID");
                log.info("Transaction {} marked PAID via webhook", transactionId);
            }

            return ResponseEntity.ok(WebhookResponse.ok());
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(WebhookResponse.error("processing failed"));
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentStatusStore> checkstatus(@PathVariable String orderId) {
        String status = statusHolder.getStore().getOrDefault(orderId, "PENDING");
        return ResponseEntity.ok(new PaymentStatusStore(orderId, status));
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId) {
        statusHolder.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }
}