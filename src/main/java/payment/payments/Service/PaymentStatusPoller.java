package payment.payments.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentStatusPoller {

    private final KhqrService khqrService;
    private final PaymentStatusStoreHolder statusHolder;

    @Value("${khqr.polling.enabled:false}")
    private boolean pollingEnabled;

    // Off by default: check-transaction endpoint was returning 404.
    // Relying on webhook (Khcontroller.handleCallback) instead.
    // Set khqr.polling.enabled=true once the endpoint issue is confirmed fixed.
    @Scheduled(fixedRate = 5000)
    public void pollPendingPayments() {
        if (!pollingEnabled) {
            return;
        }

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