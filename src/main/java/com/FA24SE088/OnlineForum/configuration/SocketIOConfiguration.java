package com.FA24SE088.OnlineForum.configuration;

import com.FA24SE088.OnlineForum.exception.CustomExceptionListener;
import com.FA24SE088.OnlineForum.handler.CustomAuthorizationListener;
import com.FA24SE088.OnlineForum.handler.CustomJsonSupportHandler;
import com.FA24SE088.OnlineForum.handler.SocketIOEventHandler;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.InputStream;

@Configuration
public class SocketIOConfiguration {
    @Value("${server.protocol-method}")
    private String protocolMethod;
    private SocketIOServer socketIOServer;

    @EventListener(ApplicationReadyEvent.class)
    public void startSocketIO() {
        socketIOServer.start();
        System.out.println("Socket.IO server started on "
                + socketIOServer.getConfiguration().getHostname()
                + ":" + socketIOServer.getConfiguration().getPort());
    }

    @PreDestroy
    public void stopSocketIO() {
        socketIOServer.stop();
        System.out.println("Socket.IO server stopped");
    }

    //link -> ws://localhost:16234/socket.io/?EIO=4&transport=websocket
    //link -> wss://fifoforumonline.click:16234/socket.io/?EIO=4&transport=websocket
    @Bean
    public SocketIOServer socketIOServer(SocketIOEventHandler socketIOEventHandler, CustomExceptionListener customExceptionListener) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setPort(16234);
        config.setUpgradeTimeout(15000);
        config.setAllowCustomRequests(true);
//        config.setPingInterval(5000);
//        config.setPingTimeout(10000);

        config.setExceptionListener(customExceptionListener);
        config.setAuthorizationListener(new CustomAuthorizationListener());
        //set socket.io to accept Date-related data
        config.setJsonSupport(new CustomJsonSupportHandler());

        if(protocolMethod.equalsIgnoreCase("https")){
            config.setHostname("fifoforumonline.click");
            InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream("keystore.p12");
            if (keystoreStream == null) {
                throw new IllegalStateException("Keystore file not found!");
            }
            config.setKeyStore(keystoreStream);
            config.setKeyStoreFormat("PKCS12");
            config.setKeyStorePassword("password");
        }
        else{
            config.setHostname(null);
        }

        SocketIOServer server = new SocketIOServer(config);
        server.addListeners(socketIOEventHandler);
        this.socketIOServer = server;

        return server;
    }
}
