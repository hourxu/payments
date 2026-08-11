package payment.payments.Service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentStatusStoreHolder {
    private final Map<String, String> store = new ConcurrentHashMap<>();

    public Map<String, String> getStore() {
        return store;
    }

    public void cancelOrder(String orderId) {
        store.put(orderId, "CANCELLED");
    }
}