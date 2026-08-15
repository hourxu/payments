package payment.payments.Service;

import io.github.tongbora.bakong.service.BakongService;
import io.github.tongbora.bakong.dto.BakongRequest;
import io.github.tongbora.bakong.dto.BakongResponse;
import io.github.tongbora.bakong.dto.CheckTransactionRequest;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final BakongService bakongService;
    private final PaymentStatusStoreHolder statusHolder;

    public KHQRResponse<KHQRData> generateQrForOrder(String orderId, double amount) {
        String shortId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;

        BakongRequest request = new BakongRequest(
                KHQRCurrency.USD, amount, null, null, null, null,
                null, 15, shortId, null, null, null,
                "Order " + shortId, null, null, null
        );

        KHQRResponse<KHQRData> response = bakongService.generateQR(request);

        if (response.getKHQRStatus().getCode() != 0 || response.getData() == null) {
            throw new RuntimeException("QR generation failed: " + response.getKHQRStatus().getMessage());
        }

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