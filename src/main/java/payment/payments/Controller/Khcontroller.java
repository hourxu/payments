package payment.payments.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import payment.payments.DTO.GenerateQRResquest;
import payment.payments.Service.KhqrService;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class Khcontroller {
    private final KhqrService khqrService;
    @PostMapping("/generateQR")
    public ResponseEntity<String>GenerateQR(@RequestBody GenerateQRResquest resquest){
        String result=khqrService.Generateqr(resquest);
        return ResponseEntity.ok(result);
    }
}
