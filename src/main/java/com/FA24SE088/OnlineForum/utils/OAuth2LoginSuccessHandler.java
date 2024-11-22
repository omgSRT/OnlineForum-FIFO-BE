package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.dto.response.AuthenticationResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.service.AuthenticateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AuthenticateService authenticateService;
    @Autowired
    UnitOfWork unitOfWork;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        UUID accountId = (UUID) ((DefaultOAuth2User) authentication.getPrincipal()).getAttributes().get("accountId");
        Account account = unitOfWork.getAccountRepository()
                .findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        String token = authenticateService.generateToken(account, 1);
        String refreshToken = authenticateService.generateToken(account, 365);

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();

        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(authResponse));
        response.getWriter().flush();
    }
}
