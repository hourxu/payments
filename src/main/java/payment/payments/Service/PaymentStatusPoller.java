package payment.payments.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentStatusPoller {

    private final OrderPaymentService orderPaymentService;
    private final PaymentStatusStoreHolder statusHolder;

    @Value("${bakong.polling.enabled:true}")
    private boolean pollingEnabled;

    // How long a PENDING order is allowed to sit before we stop polling it.
    // Matches the 15-minute QR expiry you set in BakongRequest.
    private static final long EXPIRY_MS = 15 * 60 * 1000;

    // Polling interval bumped way down from 15s to stay well within Bakong's
    // 100 requests/day limit. Adjust based on how many concurrent PENDING
    // orders you expect: (86400 / intervalSeconds) * avgPendingOrders <= 100.
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void pollPendingPayments() {
        if (!pollingEnabled) {
            return;
        }

        Map<String, String> store = statusHolder.getStore();
        Map<String, String> md5Store = statusHolder.getMd5Store();
        Map<String, Long> registeredAt = statusHolder.getRegisteredAt();

        for (Map.Entry<String, String> entry : store.entrySet()) {
            String orderId = entry.getKey();
            String status = entry.getValue();

            if (!"PENDING".equals(status)) {
                continue;
            }

            // Expire stale orders instead of polling them forever.
            Long ts = registeredAt.get(orderId);
            if (ts != null && System.currentTimeMillis() - ts > EXPIRY_MS) {
                store.put(orderId, "EXPIRED");
                System.out.println("Order expired, stopped polling: orderId=" + orderId);
                continue;
            }

            String md5 = md5Store.get(orderId);
            if (md5 == null) continue;

            try {
                boolean paid = orderPaymentService.isOrderPaid(md5);
                if (paid) {
                    store.put(orderId, "PAID");
                }
            } catch (Exception e) {
                // If we've hit the daily rate limit, stop this whole run early —
                // no point burning further calls on other orders in the same tick.
                if (e.getMessage() != null && e.getMessage().contains("Daily request limit")) {
                    System.out.println("Bakong daily rate limit hit — pausing polling until next tick.");
                    return;
                }
                System.out.println("check-transaction failed for orderId=" + orderId + ": " + e.getMessage());
            }
        }
    }
}