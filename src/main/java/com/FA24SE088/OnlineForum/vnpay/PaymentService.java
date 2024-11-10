package com.FA24SE088.OnlineForum.vnpay;


import com.FA24SE088.OnlineForum.configuration.VNPAYConfig;
import com.FA24SE088.OnlineForum.dto.response.OrderPointResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.OrderPoint;
import com.FA24SE088.OnlineForum.entity.Pricing;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.FA24SE088.OnlineForum.enums.OrderPointStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.OrderPointMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VNPAYConfig vnPayConfig;
    @Autowired
    UnitOfWork unitOfWork;
    @Autowired
    OrderPointMapper orderPointMapper;

    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        long amount = 1000000L;
        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return PaymentDTO.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }
//    public String getServerUrl() {
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        if (request == null) {
//            throw new RuntimeException() ;
//        }
//        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
//    }

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public PaymentDTO.VNPayResponse buyPoints(HttpServletRequest request, UUID pricingId, String redirectUrl) {
        Pricing pricing = unitOfWork.getPricingRepository().findById(pricingId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_INVALID));

        long amount = pricing.getPrice() * 100L;

        Account account = getCurrentUser();
        Wallet wallet = unitOfWork.getWalletRepository().findById(account.getWallet().getWalletId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_EXIST));
        OrderPoint orderPoint = new OrderPoint();
        orderPoint.setWallet(wallet);
        orderPoint.setPricing(pricing);
        orderPoint.setAmount(amount / 100.0);
        orderPoint.setOrderDate(new Date());
//        orderPoint.setUrlRedirect(redirectUrl);
        orderPoint.setStatus("PENDING");
        unitOfWork.getOrderPointRepository().save(orderPoint);

        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        vnpParamsMap.put("vnp_TxnRef", orderPoint.getOrderId().toString());

        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return PaymentDTO.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl)
                .build();
    }


//    public OrderPointResponse handleVnPayCallback(HttpServletRequest request) {
//
//        String status = request.getParameter("vnp_ResponseCode");
//        String orderId = request.getParameter("vnp_TxnRef");
//
//        OrderPoint orderPoint = unitOfWork.getOrderPointRepository().findById(UUID.fromString(orderId))
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_POINT_NOT_FOUND));
//
//        if ("00".equals(status)) {
//            orderPoint.setStatus(OrderPointStatus.SUCCESS.name());
//            OrderPoint orderPoint1 = unitOfWork.getOrderPointRepository().save(orderPoint);
//
//            Wallet wallet = orderPoint.getWallet();
//            wallet.setBalance(wallet.getBalance() + orderPoint.getPricing().getPoint());
//            unitOfWork.getWalletRepository().save(wallet);
//            return orderPointMapper.toOderPointResponse(orderPoint1);
//        } else {
//            orderPoint.setStatus(OrderPointStatus.FAILED.name());
//            OrderPoint orderPoint2 = unitOfWork.getOrderPointRepository().save(orderPoint);
//            return orderPointMapper.toOderPointResponse(orderPoint2);
//        }
//    }



    public RedirectView handleVnPayCallback(HttpServletRequest request) {

        String status = request.getParameter("vnp_ResponseCode");
        String orderId = request.getParameter("vnp_TxnRef");
//        String url = request.getParameter("vnp_RedirectUrl");

        String returnUrl = request.getParameter("returnUrl");

        OrderPoint orderPoint = unitOfWork.getOrderPointRepository().findById(UUID.fromString(orderId))
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_POINT_NOT_FOUND));

        String redirectUrl;

        if ("00".equals(status)) {
            orderPoint.setStatus(OrderPointStatus.SUCCESS.name());
            OrderPoint orderPoint1 = unitOfWork.getOrderPointRepository().save(orderPoint);

            Wallet wallet = orderPoint.getWallet();
            wallet.setBalance(wallet.getBalance() + orderPoint.getPricing().getPoint());
            unitOfWork.getWalletRepository().save(wallet);

            // URL khi thanh toán thành công
//            redirectUrl = orderPoint.getUrlRedirect();
            redirectUrl = returnUrl;
        } else {
            orderPoint.setStatus(OrderPointStatus.FAILED.name());
            OrderPoint orderPoint2 = unitOfWork.getOrderPointRepository().save(orderPoint);

            // URL khi thanh toán thất bại
            redirectUrl = orderPoint.getUrlRedirect();
        }

        // Thực hiện redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectUrl);
        return redirectView;
    }

}
