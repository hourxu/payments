package payment.payments.DTO;

public class PaymentStatusStore{
    private String status; // "PAID" or "PENDING"
    private String orderId;

    public PaymentStatusStore(String orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public String getStatus() { return status; }
    public String getOrderId() { return orderId; }
}