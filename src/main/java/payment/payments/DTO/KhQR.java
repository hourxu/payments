package payment.payments.DTO;

import java.util.Map;

public class KhQR {
    private long req_time;
    private String merchantBakongId;
    private String transaction_id;
    private String amount;
    private String status;
    private String items;
    private String custom_fields;
    private Map<String, Object> payment_details; // still generic, since shape is undocumented
    private String hash;
}
