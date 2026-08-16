package payment.payments.Service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentStatusStoreHolder {
    private final Map<String, String> store = new ConcurrentHashMap<>();        // orderId -> status
    private final Map<String, String> md5Store = new ConcurrentHashMap<>();     // orderId -> md5
    private final Map<String, Long> registeredAt = new ConcurrentHashMap<>();   // orderId -> timestamp (ms)

    public Map<String, String> getStore() {
        return store;
    }

    public Map<String, String> getMd5Store() {
        return md5Store;
    }

    public Map<String, Long> getRegisteredAt() {
        return registeredAt;
    }

    public void registerOrder(String orderId, String md5) {
        store.put(orderId, "PENDING");
        md5Store.put(orderId, md5);
        registeredAt.put(orderId, System.currentTimeMillis());
    }

    public void cancelOrder(String orderId) {
        store.put(orderId, "CANCELLED");
    }

    // Optional cleanup: fully remove very old entries so the maps don't grow forever.
    // Call this occasionally (e.g. from the poller) once orders are PAID/EXPIRED/CANCELLED.
    public void remove(String orderId) {
        store.remove(orderId);
        md5Store.remove(orderId);
        registeredAt.remove(orderId);
    }
}