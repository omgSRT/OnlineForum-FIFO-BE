package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataHandler extends TextWebSocketHandler {
    private final Logger LOG = LoggerFactory.getLogger(DataHandler.class);

    // To store active WebSocket sessions by userId
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // To store user IDs by WebSocketSession
    private final Map<WebSocketSession, String> userSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    UnitOfWork unitOfWork;

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        LOG.info("Received message: " + message.getPayload());
        // Handle message if needed
    }

    public void sendGlobal(Object messageObject) {
        try {
            String messageJson = objectMapper.writeValueAsString(messageObject); // Convert object to JSON string
            for (WebSocketSession session : sessions.values()) {
                try {
                    session.sendMessage(new TextMessage(messageJson));
                } catch (Exception e) {
                    LOG.error("Failed to send message to session: " + session.getId(), e);
                }
            }
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert message object to JSON", e);
        }
    }

    public void sendToUser(UUID accountId, Object messageObject) {
        try {
            String messageJson = objectMapper.writeValueAsString(messageObject); // Convert object to JSON string
            WebSocketSession session = sessions.get(accountId);
            if (session != null) {
                try {
                    session.sendMessage(new TextMessage(messageJson));
                } catch (Exception e) {
                    LOG.error("Failed to send message to user: " + accountId, e);
                }
            } else {
                LOG.warn("No session found for user: " + accountId);
            }
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert message object to JSON", e);
        }
    }

//    public void sendToUser(UUID accountId, Object messageObject) {
//        try {
//            Account account = unitOfWork.getAccountRepository().findById(accountId).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
//            String messageJson = objectMapper.writeValueAsString(messageObject); // Convert object to JSON string
//            WebSocketSession session = sessions.get(account.getEmail());
//            if (session != null) {
//                try {
//                    session.sendMessage(new TextMessage(messageJson));
//                } catch (Exception e) {
//                    LOG.error("Failed to send message to user: " + accountId, e);
//                }
//            } else {
//                LOG.warn("No session found for user: " + accountId);
//            }
//        } catch (JsonProcessingException e) {
//            LOG.error("Failed to convert message object to JSON", e);
//        }
//    }

//    public void sendToUser2(UUID accountId, Object object) {
//        String eventKey = "sendNotification";
//        Map<String, Object> messageObject = new HashMap<>();
//        messageObject.put("event", eventKey);
//        messageObject.put("data", object);
//
//        try {
//            String messageJson = objectMapper.writeValueAsString(messageObject);
//            WebSocketSession session = sessions.get(accountId); // Giả sử các session được map bởi account ID
//            if (session != null && session.isOpen()) {
//                session.sendMessage(new TextMessage(messageJson));
//            }
//        } catch (Exception e) {
//            LOG.error("Failed to send notification to user: " + accountId, e);
//        }
//    }

//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        String userId = getUserIdFromSession(session);
//        if (userId != null) {
//            sessions.put(userId, session); // Add session to the map
//            userSessions.put(session, userId); // Track userId by session
//        }
//        LOG.info("Connection established: " + session.getId() + " for user: " + userId);
//    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = getUserIdFromSession(session);
        if (email != null) {
            sessions.put(email, session); // Add session to the map
            userSessions.put(session, email); // Track userId by session
        }
        LOG.info("Connection established: " + session.getId() + " for user: " + email);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = userSessions.remove(session); // Remove userId by session
        if (userId != null) {
            sessions.remove(userId); // Remove session from the map
        }
        LOG.info("Connection closed: " + session.getId() + " for user: " + userId);
    }

//        private String getUserIdFromSession(WebSocketSession session) {
//        // Extract userId from the WebSocketSession (e.g., from query parameters or headers)
//        // This is a placeholder implementation; replace with actual extraction logic
//        String query = session.getUri().getQuery();
//        LOG.error(query);
//        return query != null ? query.split("userid=")[1] : null;
//    }
    private String getUserIdFromSession(WebSocketSession session) {
        // Extract email from the WebSocketSession (query parameters)
        String query = session.getUri().getQuery();
        if (query != null && query.contains("email=")) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2 && "email".equals(keyValue[0])) {
                    return keyValue[1]; // Return email instead of userId
                }
            }
        }
        LOG.error("No email found in the query: " + query);
        return null;
    }

}
