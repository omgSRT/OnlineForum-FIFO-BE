package com.FA24SE088.OnlineForum.utils;

import com.sun.security.auth.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    private final Logger LOG = LoggerFactory.getLogger(UserHandshakeHandler.class);

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String email = null;
        try {
            String query = request.getURI().getQuery();
            if (query != null) {
                String[] pair = query.split("=");
                email = pair[1];
            }
            LOG.info("User with email '{}' opened the page", email);

        } catch (Exception ex) {
            logger.error("ERROR IN USER HANDSHAKE HANDLER: {}", ex);
        }
        return new UserPrincipal(email);
    }
}
