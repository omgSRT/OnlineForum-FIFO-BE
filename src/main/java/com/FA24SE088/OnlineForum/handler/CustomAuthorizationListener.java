package com.FA24SE088.OnlineForum.handler;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.HandshakeData;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

public class CustomAuthorizationListener implements AuthorizationListener {
    @Override
    public AuthorizationResult getAuthorizationResult(HandshakeData handshakeData) {
        String authHeader = handshakeData.getHttpHeaders().get("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Unauthorized handshake attempt. Rejecting connection.");
            return AuthorizationResult.FAILED_AUTHORIZATION;
        }

        String token = authHeader.substring(7);
        if (!isTokenValid(token)) {
            System.out.println("Invalid token. Rejecting connection.");
            return AuthorizationResult.FAILED_AUTHORIZATION;
        }

        return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
    }

    private boolean isTokenValid(String token) {
        try {
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
            String accountId = signedJWT.getJWTClaimsSet().getStringClaim("accountId");
            return accountId != null && !accountId.isEmpty();
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }
}
