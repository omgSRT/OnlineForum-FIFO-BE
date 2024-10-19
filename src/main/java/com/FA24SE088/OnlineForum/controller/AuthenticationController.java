package com.FA24SE088.OnlineForum.controller;
//

import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.request.AuthenticationRequest;
import com.FA24SE088.OnlineForum.dto.request.IntrospectRequest;
import com.FA24SE088.OnlineForum.dto.request.LogoutRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.service.AccountService;
import com.FA24SE088.OnlineForum.service.AuthenticateService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping(path = "/authenticate")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticateService authenticateService;
    private final AccountService accountService;

    @Operation(summary = "Login", description = "new admin account: admin1234 \n" +
            "password: admin1234")
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Validated @RequestBody AuthenticationRequest request){
        return ApiResponse.<AuthenticationResponse>builder()
                .entity(authenticateService.authenticated(request))
                .build();
    }

    @PostMapping("/sign-up")
    public ApiResponse<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.create(request);
//        String otp = otpService.generateOTP(request.getEmail());
//        emailService.sendOtpEmail(request.getEmail(), "Mã OTP xác thực tài khoản", "Mã OTP của bạn là: " + otp);
        return ApiResponse.<AccountResponse>builder()
                .entity(response)
                .build();
    }

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
    public ApiResponse<RefreshAccessTokenResponse> generateNewAccessToken(String refreshToken, String username){
        return ApiResponse.<RefreshAccessTokenResponse>builder()
                .entity(authenticateService.generateNewAccessTokenFromRefreshToken(refreshToken, username))
                .build();
    }
}
