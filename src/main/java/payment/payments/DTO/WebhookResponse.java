package payment.payments.DTO;


public class WebhookResponse {
    private boolean received;
    private String error;

    public WebhookResponse(boolean received, String error) {
        this.received = received;
        this.error = error;
    }

    public static WebhookResponse ok() {
        return new WebhookResponse(true, null);
    }

    public static WebhookResponse error(String message) {
        return new WebhookResponse(false, message);
    }

    public boolean isReceived() { return received; }
    public String getError() { return error; }
}
