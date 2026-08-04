package payment.payments.DTO;

import lombok.Data;

@Data
public class KhqrResponse {
    private String qrImage;
    private String qrString;
    private String transactionId;
    private String status;
}
