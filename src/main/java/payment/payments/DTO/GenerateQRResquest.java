package payment.payments.DTO;

import lombok.Data;

@Data
public class GenerateQRResquest {
    private String orderId;
    private Double amount ;
}
