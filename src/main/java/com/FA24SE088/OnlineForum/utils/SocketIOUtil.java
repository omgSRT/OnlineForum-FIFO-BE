package com.FA24SE088.OnlineForum.utils;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

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


    public void sendEventToOneClientInAServer(UUID accountId, String event,String message, Object data) {
        String sessionId = accountSessionIdMap.get(accountId);
        if (sessionId != null) {
            SocketIOClient client = socketIOServer.getClient(UUID.fromString(sessionId));
            if (client != null) {
                client.sendEvent(event,message,data);
            } else {
                throw new IllegalArgumentException("Client with session ID '" + sessionId + "' not found");
            }
        }
    }
}
