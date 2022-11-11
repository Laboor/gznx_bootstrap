package com.gznx.config;

import com.gznx.websocket.LogHandshakeInterceptor;
import com.gznx.websocket.LogMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 支持websocket的浏览器
        registry.addHandler(logMessageHandler(), "/webSocketServer").setAllowedOrigins("*").addInterceptors(new LogHandshakeInterceptor());
        // 不支持websocket的浏览器
        registry.addHandler(logMessageHandler(), "/sockjs/webSocketServer").setAllowedOrigins("*").addInterceptors(new LogHandshakeInterceptor()).withSockJS();
    }

    @Bean
    public WebSocketHandler logMessageHandler() {
        return new LogMessageHandler();
    }
}
