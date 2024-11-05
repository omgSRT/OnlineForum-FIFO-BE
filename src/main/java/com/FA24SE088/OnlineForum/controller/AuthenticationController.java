package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.*;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.Otp;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.AccountService;
import com.FA24SE088.OnlineForum.service.AuthenticateService;
import com.FA24SE088.OnlineForum.utils.EmailUtil;
import com.FA24SE088.OnlineForum.utils.OtpUtil;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;


@RestController
@RequestMapping(path = "/authenticate")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    AuthenticateService authenticateService;
    AccountService accountService;
    EmailUtil emailUtil;
    OtpUtil otpUtil;
    RedisTemplate<String, String> redisTemplate;

    @Operation(summary = "Login", description = "new admin account: admin1234 \n" +
            "password: admin1234")
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Validated @RequestBody AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .entity(authenticateService.authenticated(request))
                .build();
    }

    @PostMapping("/sign-up")
    @Transactional
    public ApiResponse<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.create(request);
        Otp otp = otpUtil.generateOtp(request.getEmail());
        emailUtil.sendToAnEmail(
                response.getEmail(),
                "Mã OTP của bạn là: " + otp.getOtpEmail(),
                "Mã OTP xác thực tài khoản",
                null);
        return ApiResponse.<AccountResponse>builder()
                .entity(response)
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<AccountResponse> verifyOtp(@RequestBody OtpRequest request) {
        otpUtil.verifyOTP(request.getEmail(), request.getOtp());
        return ApiResponse.<AccountResponse>builder()
                .entity(accountService.verifyAccount(request.getEmail()))
                .build();
    }

//    @PostMapping("/resend-otp")
//    public ApiResponse<Otp> resendOtp(@RequestParam String email) {
//        otpUtil.resendOtp(email);
//        emailUtil.sendToAnEmail(email,
//                "Mã OTP của bạn là: " + otpResponse.getOtpEmail(),
//                "Mã OTP xác thực tài khoản",
//                null);
//        return ApiResponse.<Otp>builder()
//                .entity(otpResponse)
//                .build();
//    }


    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticated(@RequestBody IntrospectRequest token) throws ParseException, JOSEException {
        var result = authenticateService.introspectJWT(token);
        return ApiResponse.<IntrospectResponse>builder()
                .entity(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticateService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshAccessTokenResponse> generateNewAccessToken(String refreshToken, String username) {
        return ApiResponse.<RefreshAccessTokenResponse>builder()
                .entity(authenticateService.generateNewAccessTokenFromRefreshToken(refreshToken, username))
                .build();
    }

    @PostMapping("/forget-password")
    public ApiResponse<Void> forgotPassword(String email){
        return authenticateService.forgetPassword(email).thenApply(v ->
                ApiResponse.<Void>builder()
                        .message(SuccessReturnMessage.SEND_SUCCESS.getMessage())
                        .entity(null)
                        .build()
        ).join();
    }
    @PutMapping("/change-password")
    public ApiResponse<AccountResponse> changePassword(String email,
                                                       @RequestBody @Valid AccountChangePasswordRequest request){
        return authenticateService.changePassword(email, request).thenApply(accountResponse ->
                ApiResponse.<AccountResponse>builder()
                        .message(SuccessReturnMessage.CHANGE_SUCCESS.getMessage())
                        .entity(accountResponse)
                        .build()
        ).join();
    }
    @Operation(summary = "Resend OTP Email For Forget Password")
    @PostMapping("/resend-otp/forget-password")
    public ApiResponse<Void> resendOtpForForgetPassword(@RequestParam String email) {
        String emailBody = "<html>"
                + "<body>"
                + "<p><strong>FIFO Password Reset</strong></p>"
                + "<p>We heard that you lost your FIFO password. Sorry about that!</p>"
                + "<p>Don't worry! Enter This OTP To Reset Your Password: " +otpUtil.generateOtp(email).getOtpEmail()+ " </p>"
                + "</body>"
                + "</html>";
        emailUtil.sendToAnEmailWithHTMLEnabled(email,
                emailBody,
                "Please Reset Your Password",
                null);
        return ApiResponse.<Void>builder()
                .message(SuccessReturnMessage.SEND_SUCCESS.getMessage())
                .build();
    }
    @PostMapping("/verify-otp/forget-password")
    public ApiResponse<AccountResponse> verifyOtpForForgetPassword(@RequestBody OtpRequest request) {
        otpUtil.verifyOTPForForgetPassword(request.getEmail(), request.getOtp());
        return ApiResponse.<AccountResponse>builder()
                .message(SuccessReturnMessage.VERIFY_SUCCESS.getMessage())
                .entity(accountService.verifyAccount(request.getEmail()))
                .build();
    }
}
