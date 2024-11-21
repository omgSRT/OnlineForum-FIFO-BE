package com.FA24SE088.OnlineForum.configuration;

import com.FA24SE088.OnlineForum.handler.SocketIOEventHandler;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfiguration {
    //link -> ws://localhost:16453/socket.io/?EIO=4&transport=websocket
    @Bean
    public SocketIOServer socketIOServer(SocketIOEventHandler socketIOEventHandler){
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(null);
        config.setPort(16453);
        config.setUpgradeTimeout(10000);

        SocketIOServer server = new SocketIOServer(config);
        server.addListeners(socketIOEventHandler);

        return server;
    }
}
