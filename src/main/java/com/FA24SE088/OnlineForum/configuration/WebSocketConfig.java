package com.FA24SE088.OnlineForum.configuration;

import com.FA24SE088.OnlineForum.utils.DataHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(getUserHandShakeHandler(), "/websocket")
//                .setHandshakeHandler(new UserHandshakeHandler())
                .setAllowedOrigins("*");
    }

    @Bean
    DataHandler getUserHandShakeHandler() {
        return new DataHandler();
    }
}


