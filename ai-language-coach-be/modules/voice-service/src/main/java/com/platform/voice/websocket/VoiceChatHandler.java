package com.platform.voice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.util.JwtUtils;
import com.platform.voice.dto.UserContext;
import com.platform.voice.dto.VoiceMessage;
import com.platform.voice.model.ConversationState;
import com.platform.voice.service.ConversationService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VoiceChatHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(VoiceChatHandler.class);

    private final JwtUtils jwtUtils;
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;
    private final Counter audioMessageCounter;
    private final Counter textMessageCounter;
    private final Counter sessionCounter;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, UserContext> sessionContexts = new ConcurrentHashMap<>();

    public VoiceChatHandler(JwtUtils jwtUtils, ConversationService conversationService, MeterRegistry meterRegistry) {
        this.jwtUtils = jwtUtils;
        this.conversationService = conversationService;
        this.objectMapper = new ObjectMapper();
        this.audioMessageCounter = Counter.builder("voice.websocket.audio.messages")
                .description("Number of audio messages received")
                .register(meterRegistry);
        this.textMessageCounter = Counter.builder("voice.websocket.text.messages")
                .description("Number of text messages received")
                .register(meterRegistry);
        this.sessionCounter = Counter.builder("voice.websocket.sessions")
                .description("Number of WebSocket sessions created")
                .register(meterRegistry);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
        sessionCounter.increment();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            VoiceMessage voiceMessage = objectMapper.readValue(message.getPayload(), VoiceMessage.class);

            switch (voiceMessage.type()) {
                case START_SESSION -> handleStartSession(session, voiceMessage);
                case END_SESSION -> handleEndSession(session, voiceMessage);
                case HEARTBEAT -> handleHeartbeat(session, voiceMessage);
                case TEXT -> handleTextMessage(session, voiceMessage);
                case AUDIO -> handleAudioMessage(session, voiceMessage);
                default -> log.warn("Unknown message type: {}", voiceMessage.type());
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to parse message: {}", e.getMessage());
            sendError(session, "Invalid message format");
        }
    }

    private void handleStartSession(WebSocketSession session, VoiceMessage message) throws IOException {
        String token = message.content();
        if (token == null || !jwtUtils.validateToken(token)) {
            sendError(session, "Invalid or missing authentication token");
            return;
        }

        String userId = jwtUtils.extractUsername(token);
        String tenantId = jwtUtils.extractTenantId(token);

        UserContext context = UserContext.builder()
                .userId(userId)
                .tenantId(tenantId != null ? tenantId : "1")
                .planType(message.planType() != null ? message.planType() : "FREE")
                .targetLanguage(message.targetLanguage() != null ? message.targetLanguage() : "Czech")
                .targetLevel(message.targetLevel() != null ? message.targetLevel() : "A1")
                .nativeLanguage("en")
                .build();

        sessionContexts.put(session.getId(), context);

        conversationService.startSession(context);

        sendMessage(session, VoiceMessage.builder()
                .type(VoiceMessage.MessageType.RESPONSE)
                .content("Session started successfully")
                .sessionId(session.getId())
                .build());

        log.info("Session started for user {} on session {}", userId, session.getId());
    }

    private void handleEndSession(WebSocketSession session, VoiceMessage message) throws IOException {
        UserContext context = sessionContexts.get(session.getId());
        if (context != null) {
            conversationService.endSession(session.getId());
            sessionContexts.remove(session.getId());
        }

        sendMessage(session, VoiceMessage.builder()
                .type(VoiceMessage.MessageType.RESPONSE)
                .content("Session ended")
                .sessionId(session.getId())
                .build());

        session.close();
        log.info("Session ended for session {}", session.getId());
    }

    private void handleHeartbeat(WebSocketSession session, VoiceMessage message) throws IOException {
        conversationService.keepAlive(session.getId());

        sendMessage(session, VoiceMessage.builder()
                .type(VoiceMessage.MessageType.HEARTBEAT)
                .content("pong")
                .sessionId(session.getId())
                .timestamp(System.currentTimeMillis())
                .build());
    }

    private void handleTextMessage(WebSocketSession session, VoiceMessage message) throws IOException {
        UserContext context = sessionContexts.get(session.getId());
        if (context == null) {
            sendError(session, "Session not started");
            return;
        }

        textMessageCounter.increment();

        String response = conversationService.processText(message.content(), context);

        sendMessage(session, VoiceMessage.builder()
                .type(VoiceMessage.MessageType.RESPONSE)
                .content(response)
                .sessionId(session.getId())
                .targetLanguage(context.targetLanguage())
                .build());

        log.info("Text message processed for session {}", session.getId());
    }

    private void handleAudioMessage(WebSocketSession session, VoiceMessage message) throws IOException {
        UserContext context = sessionContexts.get(session.getId());
        if (context == null) {
            sendError(session, "Session not started");
            return;
        }

        audioMessageCounter.increment();

        byte[] audioData = message.content().getBytes();

        var state = conversationService.processAudio(audioData, context);

        String lastUserMessage = state.getMessages().stream()
                .filter(m -> "user".equals(m.getRole()))
                .reduce((first, second) -> second)
                .map(ConversationState.ConversationMessage::getContent)
                .orElse("");

        sendMessage(session, VoiceMessage.builder()
                .type(VoiceMessage.MessageType.TRANSCRIPTION)
                .content(lastUserMessage)
                .sessionId(session.getId())
                .build());

        String response = conversationService.processText(lastUserMessage, context);

        sendMessage(session, VoiceMessage.builder()
                .type(VoiceMessage.MessageType.RESPONSE)
                .content(response)
                .sessionId(session.getId())
                .targetLanguage(context.targetLanguage())
                .build());

        log.info("Audio message processed for session {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} with status {}", session.getId(), status);
        sessions.remove(session.getId());
        sessionContexts.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    private void sendMessage(WebSocketSession session, VoiceMessage message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        sendMessage(session, VoiceMessage.builder()
                .type(VoiceMessage.MessageType.ERROR)
                .content(error)
                .build());
    }
}