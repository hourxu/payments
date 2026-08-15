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
    @Scheduled(fixedRate = 15000)
    public void pollPendingPayments() {
        if (!pollingEnabled) {
            return;
        }

        Map<String, String> store = statusHolder.getStore();
        Map<String, String> md5Store = statusHolder.getMd5Store();

        for (Map.Entry<String, String> entry : store.entrySet()) {
            String orderId = entry.getKey();
            String status = entry.getValue();

            if ("PENDING".equals(status)) {
                String md5 = md5Store.get(orderId);
                if (md5 == null) continue;

                try {
                    boolean paid = orderPaymentService.isOrderPaid(md5);
                    if (paid) {
                        store.put(orderId, "PAID");
                    }
                } catch (Exception e) {
                    System.out.println("check-transaction failed for orderId=" + orderId + ": " + e.getMessage());
                }
            }
        }
    }
}