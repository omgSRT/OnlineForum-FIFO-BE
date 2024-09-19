package com.GSU24SE43.ConstructionDrawingManagement.controller;
//
import com.GSU24SE43.ConstructionDrawingManagement.dto.request.AuthenticationRequest;
import com.GSU24SE43.ConstructionDrawingManagement.dto.request.IntrospectRequest;
import com.GSU24SE43.ConstructionDrawingManagement.dto.request.LogoutRequest;
import com.GSU24SE43.ConstructionDrawingManagement.dto.response.ApiResponse;
import com.GSU24SE43.ConstructionDrawingManagement.dto.response.AuthenticationResponse;
import com.GSU24SE43.ConstructionDrawingManagement.dto.response.IntrospectResponse;
import com.GSU24SE43.ConstructionDrawingManagement.repository.AccountRepository;
import com.GSU24SE43.ConstructionDrawingManagement.service.AuthenticateService;
import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.text.ParseException;

@RestController
@RequestMapping(path = "/authenticate")
@Slf4j
public class AuthenticationController {
    @Autowired
    private AuthenticateService authenticateService;
//    @Autowired
//    private AccountRepository accountRepository;

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
