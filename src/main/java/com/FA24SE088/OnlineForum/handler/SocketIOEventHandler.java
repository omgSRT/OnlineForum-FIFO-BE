package com.FA24SE088.OnlineForum.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketIOEventHandler {
    private final Map<String, SocketIOClient> clients = new ConcurrentHashMap<>();
    private final Map<UUID, String> accountSession = new ConcurrentHashMap<>();

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();

        String authHeader = client.getHandshakeData().getHttpHeaders().get("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No valid JWT provided.");
            client.disconnect();
            return;
        }

        String token = authHeader.substring(7);
        try {
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String accountId = claims.getStringClaim("accountId");

            if (accountId != null) {
                UUID accountUuid = UUID.fromString(accountId);
                accountSession.put(accountUuid, sessionId);
                clients.put(sessionId, client);

                System.out.println("User connected: " + accountUuid);
                System.out.println("Client connected: " + sessionId);
            } else {
                System.out.println("No accountId claim in the JWT");
                client.disconnect();

            }
        } catch (ParseException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
            client.disconnect();
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();

        accountSession.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(sessionId))
                .map(Map.Entry::getKey)
                .findFirst().ifPresent(accountSession::remove);
        clients.remove(sessionId);

        System.out.println("Client disconnected: " + sessionId);
    }
}
