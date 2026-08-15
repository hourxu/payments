package payment.payments.Service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentStatusStoreHolder {
    private final Map<String, String> store = new ConcurrentHashMap<>();       // orderId -> status
    private final Map<String, String> md5Store = new ConcurrentHashMap<>();    // orderId -> md5

    public Map<String, String> getStore() {
        return store;
    }

    public Map<String, String> getMd5Store() {
        return md5Store;
    }

    public void registerOrder(String orderId, String md5) {
        store.put(orderId, "PENDING");
        md5Store.put(orderId, md5);
    }

    public void cancelOrder(String orderId) {
        store.put(orderId, "CANCELLED");
    }
}