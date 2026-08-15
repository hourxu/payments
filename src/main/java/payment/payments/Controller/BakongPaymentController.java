package payment.payments.Controller;

import io.github.tongbora.bakong.service.BakongService;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.payments.Service.OrderPaymentService;
import payment.payments.Service.PaymentStatusStoreHolder;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class BakongPaymentController {

    private final BakongService bakongService;
    private final OrderPaymentService orderPaymentService;
    private final PaymentStatusStoreHolder statusHolder;

    @PostMapping("/generate-qr")
    public KHQRResponse<KHQRData> generateQR(@RequestBody Map<String, Object> body) {
        String orderId = (String) body.get("orderId");
        double amount = Double.parseDouble(body.get("amount").toString());
        return orderPaymentService.generateQrForOrder(orderId, amount);
    }

    @PostMapping("/qr-image")
    public ResponseEntity<byte[]> getQRImage(@RequestBody KHQRData qrData) {
        byte[] image = bakongService.getQRImage(qrData);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
    }

    @GetMapping("/status/{orderId}")
    public Map<String, String> getStatus(@PathVariable String orderId) {
        String status = statusHolder.getStore().getOrDefault(orderId, "UNKNOWN");
        return Map.of("orderId", orderId, "status", status);
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId) {
        statusHolder.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }
}