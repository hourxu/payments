package payment.payments.Service;

import io.github.tongbora.bakong.service.BakongService;
import io.github.tongbora.bakong.dto.BakongRequest;
import io.github.tongbora.bakong.dto.BakongResponse;
import io.github.tongbora.bakong.dto.CheckTransactionRequest;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final BakongService bakongService;
    private final PaymentStatusStoreHolder statusHolder;

    public KHQRResponse<KHQRData> generateQrForOrder(String orderId, double amount) {
        BakongRequest request = new BakongRequest(
                null, amount, null, null, null, null,
                null, 15, orderId, null, null, null,
                "Order " + orderId, null, null, null
        );

        KHQRResponse<KHQRData> response = bakongService.generateQR(request);
        statusHolder.registerOrder(orderId, response.getData().getMd5());
        return response;
    }

    public byte[] generateQrImage(KHQRData qrData) {
        return bakongService.getQRImage(qrData);
    }

    public boolean isOrderPaid(String md5) {
        BakongResponse response = bakongService.checkTransactionByMD5(new CheckTransactionRequest(md5));
        return response.responseCode() == 0;
    }
}