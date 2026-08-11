package payment.payments.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentStatusPoller {

    private final KhqrService khqrService;
    private final PaymentStatusStoreHolder statusHolder;

    // Disabled: check-transaction endpoint returns 404, not usable.
    // Relying on webhook (Khcontroller.handleCallback) instead.
     @Scheduled(fixedRate = 5000)
    public void pollPendingPayments() {
        Map<String, String> store = statusHolder.getStore();
        for (Map.Entry<String, String> entry : store.entrySet()) {
            String orderId = entry.getKey();
            String status = entry.getValue();
            if ("PENDING".equals(status)) {
                boolean paid = khqrService.isTransactionPaid(orderId);
                if (paid) {
                    store.put(orderId, "PAID");
                }
            }
        }
    }
    
}