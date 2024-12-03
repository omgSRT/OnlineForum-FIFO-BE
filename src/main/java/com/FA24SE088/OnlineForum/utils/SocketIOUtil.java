package com.FA24SE088.OnlineForum.utils;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class SocketIOUtil {
    SocketIOServer socketIOServer;
    private final Map<UUID, String> accountSessionIdMap;

    public void sendEventToAllClientInAServer(String event, Object data) {
        if (socketIOServer != null) {
            socketIOServer.getBroadcastOperations().sendEvent(event, data);
        } else {
            throw new IllegalArgumentException("Server not found");
        }
    }
//
//    public void sendEventToOneClientInAServer(UUID clientSessionId, String event, Object data) {
//        if (socketIOServer != null) {
//            SocketIOClient client = socketIOServer.getClient(clientSessionId);
//            if (client != null) {
//                client.sendEvent(event, data);
//            } else {
//                throw new IllegalArgumentException("Client with session ID '" + clientSessionId + "' not found in server");
//            }
//        } else {
//            throw new IllegalArgumentException("Server not found");
//        }
//    }


    public void sendEventToOneClientInAServer(UUID accountId, String event, Object data) {
        String sessionId = accountSessionIdMap.get(accountId);
        if (sessionId != null) {
            SocketIOClient client = socketIOServer.getClient(UUID.fromString(sessionId));
            if (client != null) {
                client.sendEvent(event, data);
            } else {
                throw new IllegalArgumentException("Client with session ID '" + sessionId + "' not found");
            }
        } else {
            throw new IllegalArgumentException("Account ID '" + accountId + "' not found in session map");
        }
    }
}
