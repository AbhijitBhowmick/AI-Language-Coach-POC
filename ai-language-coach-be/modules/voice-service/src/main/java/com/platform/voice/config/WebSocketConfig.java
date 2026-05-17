package com.platform.voice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.platform.voice.websocket.VoiceChatHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoiceChatHandler voiceChatHandler;

    public WebSocketConfig(VoiceChatHandler voiceChatHandler) {
        this.voiceChatHandler = voiceChatHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceChatHandler, "/api/v1/voice/chat")
                .setAllowedOrigins("*");
    }
}