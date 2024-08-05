package com.coledit.backend.configs;

import com.coledit.backend.handlers.SocketConnectionHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// web socket connections is handled  
// by this class 
@Configuration
@EnableWebSocket
public class WebSocketConfig
        implements WebSocketConfigurer {

    @Value("${custom.hostname}")
    private String hostname;

    private final SocketConnectionHandler socketConnectionHandler;

    @Autowired // Injects the SocketConnectionHandler bean managed by Spring
    public WebSocketConfig(SocketConnectionHandler socketConnectionHandler) {
        this.socketConnectionHandler = socketConnectionHandler;
    }

    // Overriding a method which register the socket
    // handlers into a Registry
    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
                .addHandler(socketConnectionHandler, "/document/*")
                .setAllowedOrigins(
                        "https://" + hostname);
    }
}