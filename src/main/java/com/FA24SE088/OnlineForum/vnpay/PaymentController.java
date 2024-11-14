package com.FA24SE088.OnlineForum.vnpay;

import com.FA24SE088.OnlineForum.dto.request.MCRequest;
import com.FA24SE088.OnlineForum.dto.response.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/vn-pay")
    public ResponseObject<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
        return new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createVnPayPayment(request));
    }
//    @GetMapping("/vn-pay-callback")
//    public ResponseObject<PaymentDTO.VNPayResponse> payCallbackHandler(HttpServletRequest request) {
//        String status = request.getParameter("vnp_ResponseCode");
//        if (status.equals("00")) {
//            paymentService.handleVnPayCallback(request);
//            return new ResponseObject<>(HttpStatus.OK, "Success", new PaymentDTO.VNPayResponse("00", "Success", ""));
//        } else {
//            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Failed", null);
//        }
//    }


    @PostMapping("/buyPoints")
    public ResponseEntity<PaymentDTO.VNPayResponse> buyPoints(HttpServletRequest request, @RequestBody MCRequest mcRequest) {
        PaymentDTO.VNPayResponse response = paymentService.buyPoints(request, mcRequest.getMonkeyCoinPackId(), mcRequest.getRedirectUrl());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vn-pay-callback")
    public RedirectView payCallbackHandler(HttpServletRequest request) {
        return paymentService.handleVnPayCallback(request);

    }

}
