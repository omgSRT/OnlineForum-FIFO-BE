package com.FA24SE088.OnlineForum.vnpay;


import com.FA24SE088.OnlineForum.configuration.VNPAYConfig;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.OrderPointStatus;
import com.FA24SE088.OnlineForum.enums.WebsocketEventName;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.OrderPointMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.VNPayUtil;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    SocketIOServer socketIOServer;
    ObjectMapper objectMapper;

    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        long amount = 1000000L;
        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig("");
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

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public PaymentDTO.VNPayResponse buyPoints(HttpServletRequest request, UUID monkeyCoinPackId, String redirectUrl) {
        MonkeyCoinPack monkeyCoinPack = unitOfWork.getMonkeyCoinPackRepository().findById(monkeyCoinPackId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_INVALID));

        long amount = monkeyCoinPack.getPrice() * 100L;

        Account account = getCurrentUser();
        Wallet wallet = unitOfWork.getWalletRepository().findById(account.getWallet().getWalletId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_EXIST));
        OrderPoint orderPoint = new OrderPoint();
        orderPoint.setWallet(wallet);
        orderPoint.setMonkeyCoinPack(monkeyCoinPack);
        orderPoint.setAmount(amount / 100.0);
        orderPoint.setOrderDate(new Date());
        orderPoint.setMethod("VNPay");
        orderPoint.setStatus("PENDING");
        unitOfWork.getOrderPointRepository().save(orderPoint);

        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig(redirectUrl);
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


    public RedirectView handleVnPayCallback(HttpServletRequest request) throws JsonProcessingException {

        String status = request.getParameter("vnp_ResponseCode");
        String orderId = request.getParameter("vnp_TxnRef");
        String returnUrl = request.getParameter("returnUrl"); //returnUrl

        OrderPoint orderPoint = unitOfWork.getOrderPointRepository().findById(UUID.fromString(orderId))
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_POINT_NOT_FOUND));

        String redirectUrl;

        if ("00".equals(status)) {
            orderPoint.setStatus(OrderPointStatus.SUCCESS.name());
            unitOfWork.getOrderPointRepository().save(orderPoint);

            Wallet wallet = orderPoint.getWallet();
            wallet.setBalance(wallet.getBalance() + orderPoint.getMonkeyCoinPack().getPoint());
            unitOfWork.getWalletRepository().save(wallet);

            String messageJson = objectMapper.writeValueAsString(orderPoint);
            Notification notification = Notification.builder()
                    .title("Load money into the wallet successfully")
                    .message(messageJson)
                    .isRead(false)
                    .build();
            unitOfWork.getNotificationRepository().save(notification);
            socketIOServer.getBroadcastOperations().sendEvent(WebsocketEventName.NOTIFICATION.name(), notification);
            redirectUrl = returnUrl;
        } else {
            orderPoint.setStatus(OrderPointStatus.FAILED.name());
            unitOfWork.getOrderPointRepository().save(orderPoint);

            redirectUrl = returnUrl;
        }
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectUrl);
        return redirectView;
    }


}
