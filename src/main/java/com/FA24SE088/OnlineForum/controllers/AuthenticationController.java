package com.FA24SE088.OnlineForum.controllers;
//

import com.FA24SE088.OnlineForum.dto.requests.AuthenticationRequest;
import com.FA24SE088.OnlineForum.dto.requests.IntrospectRequest;
import com.FA24SE088.OnlineForum.dto.requests.LogoutRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.AuthenticationResponse;
import com.FA24SE088.OnlineForum.dto.response.IntrospectResponse;
import com.FA24SE088.OnlineForum.services.AuthenticateService;
import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping(path = "/authenticate")
@Slf4j
public class AuthenticationController {
    @Autowired
    private AuthenticateService authenticateService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request){
        return ApiResponse.<AuthenticationResponse>builder()
                .entity(authenticateService.authenticated(request))
                .build();
    }
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenicated(@RequestBody IntrospectRequest token) throws ParseException, JOSEException {
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
}
