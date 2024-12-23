package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.*;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.AccountService;
import com.FA24SE088.OnlineForum.service.AuthenticateService;
import com.FA24SE088.OnlineForum.utils.EmailUtil;
import com.FA24SE088.OnlineForum.utils.OtpUtil;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    OtpUtil otpUtil;

    @Operation(summary = "Login", description = "new admin account: admin1234 \n" +
            "password: admin1234")
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Validated @RequestBody AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .entity(authenticateService.authenticated(request))
                .build();
    }

    @GetMapping("/login-Google")
    public ApiResponse<String> loginGG() {
        return ApiResponse.<String>builder()
                .entity("https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?response_type=code&client_id=765797941898-1j31g2v6eoa94ktultp59putq41ivksk.apps.googleusercontent.com&scope=email%20profile&state=c-r0rpiAmWP12lY8B6MjWCkpRA6xoCaWCf3R3SmWRcQ%3D&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin%2Foauth2%2Fcode%2Fgoogle&service=lso&o2v=2&ddm=1&flowName=GeneralOAuthFlow")
                .build();
    }

    @GetMapping("/callback")
    public ApiResponse<AccountResponse> callbackLoginGoogle(@AuthenticationPrincipal OAuth2User oAuth2User, HttpServletRequest request) {
        AccountResponse response = accountService.callbackLoginGoogle(oAuth2User);
        return ApiResponse.<AccountResponse>builder()
                .message(SuccessReturnMessage.LOGIN_SUCCESS.getMessage())
                .entity(response)
                .build();
    }

    @PostMapping("/sign-up")
    @Transactional
    public ApiResponse<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.create(request);
        return ApiResponse.<AccountResponse>builder()
                .entity(response)
                .build();
    }

    @PostMapping("/resend-otp")
    public ApiResponse<String> resendOtp(@RequestParam String email) {
        String otpResponse = otpUtil.resendOtp(email);
        return ApiResponse.<String>builder()
                .entity(otpResponse)
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<AccountResponse> verifyOtp(@RequestBody OtpRequest request) {
        otpUtil.verifyOTPRedis(request.getEmail(), request.getOtp());
        return ApiResponse.<AccountResponse>builder()
                .message(SuccessReturnMessage.VERIFY_SUCCESS.getMessage())
                .entity(accountService.verifyAccount(request.getEmail()))
                .build();
    }

//    @PostMapping("/resend-otp")
//    public ApiResponse<Otp> resendOtp(@RequestParam String email) {
//        otpUtil.resendOtp(email);
//
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


    @PostMapping("/refresh")
    public ApiResponse<RefreshAccessTokenResponse> generateNewAccessToken(String refreshToken, String username) {
        return ApiResponse.<RefreshAccessTokenResponse>builder()
                .entity(authenticateService.generateNewAccessTokenFromRefreshToken(refreshToken, username))
                .build();
    }

    @PostMapping("/forget-password")
    public ApiResponse<Void> forgotPassword(String email) {
        return authenticateService.forgetPassword(email).thenApply(v ->
                ApiResponse.<Void>builder()
                        .message(SuccessReturnMessage.SEND_SUCCESS.getMessage())
                        .entity(null)
                        .build()
        ).join();
    }

    @PutMapping("/change-password")
    public ApiResponse<AccountResponse> changePassword(String email,
                                                       @RequestBody @Valid AccountChangePasswordRequest request) {
        return authenticateService.changePassword(email, request).thenApply(accountResponse ->
                ApiResponse.<AccountResponse>builder()
                        .message(SuccessReturnMessage.CHANGE_SUCCESS.getMessage())
                        .entity(accountResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Resend OTP Email For Forget Password")
    @PostMapping("/resend-otp/forget-password")
    public ApiResponse<String> resendOtpForForgetPassword(@RequestParam String email) {
        otpUtil.resendOtpForgotPassword(email);
        return ApiResponse.<String>builder()
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
