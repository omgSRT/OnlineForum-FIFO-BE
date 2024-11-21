package com.FA24SE088.OnlineForum.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketIOEventHandler {
    private final Map<String, SocketIOClient> clients = new ConcurrentHashMap<>();

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        clients.put(sessionId, client);
        System.out.println("Client connected: " + sessionId);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        clients.remove(sessionId);
        System.out.println("Client disconnected: " + sessionId);
    }

    @OnEvent("message")
    public void onMessage(SocketIOClient client, String message) {
        System.out.println("Received message: " + message);
        client.sendEvent("response", "Message received: " + message);
    }
}
