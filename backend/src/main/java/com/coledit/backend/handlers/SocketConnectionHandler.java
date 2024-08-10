package com.coledit.backend.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;

import com.coledit.backend.entities.WSUpdateNotification;
import com.coledit.backend.helpers.StringMerger;
import com.coledit.backend.services.NoteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Socket-Connection Configuration class 
@Component
public class SocketConnectionHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> documentSessions = new ConcurrentHashMap<>();
    private final Map<String, String> latestDocumentContent = new ConcurrentHashMap<>();
    private final Map<String, List<String>> latestDocumentVariants = new ConcurrentHashMap<>();
    private final Map<String, Integer> documentVersionCounter = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private NoteService noteService;

    @Autowired
    public SocketConnectionHandler(NoteService noteService, ObjectMapper objectMapper) {
        this.noteService = noteService;
        this.objectMapper = objectMapper;
    }

    // This method is executed when client tries to connect
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        String documentId = getDocumentId(session);
        documentSessions.computeIfAbsent(documentId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);
        List<WebSocketSession> sessions = documentSessions.get(documentId);

        String newContent = latestDocumentContent.get(documentId);
        String jsonNotification = createJsonNotification(newContent, documentVersionCounter.get(documentId));

        if (sessions != null && newContent != null) {
            for (WebSocketSession webSocketSession : sessions) {
                if (session == webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(jsonNotification));
                    break;
                }
            }
        }

        System.out.println("Session " + session.getId() + " connected to document " + documentId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        String documentId = getDocumentId(session);
        List<WebSocketSession> sessions = documentSessions.get(documentId);

        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                String latestContent = latestDocumentContent.getOrDefault(documentId, "");
                if (!latestContent.isEmpty()) {
                    noteService.updateNoteContent(documentId, latestContent);
                }
                latestDocumentContent.remove(documentId);
                documentSessions.remove(documentId);
                Integer currentValue = documentVersionCounter.get(documentId);

                if (currentValue != null) {
                    documentVersionCounter.put(documentId, 0);
                }
            }
        }

        System.out.println("Session " + session.getId() + " disconnected from document " + documentId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload().toString());

        if (isHeartbeatMessage(jsonMessage)) {
            return; // Do nothing for heartbeat type
        }

        String newContent = extractNoteContent(jsonMessage);
        if (newContent == null) {
            return; // Early return if the message doesn't have the expected structure
        }

        Integer version = extractNoteVersion(jsonMessage);
        if (version == null) {
            return; // Early return if the message doesn't have the expected structure
        }

        String documentId = getDocumentId(session);
        List<String> latestVariants = latestDocumentVariants.computeIfAbsent(documentId,
                k -> Collections.synchronizedList(new ArrayList<>()));
        latestVariants.add(newContent);

        synchronized (latestVariants) {
            if (version >= documentVersionCounter.getOrDefault(documentId, 0)) {
                String latestMergedVersion = StringMerger.mergeVariants(
                        latestDocumentContent.getOrDefault(documentId, ""),
                        latestVariants);
                updateLatestDocumentContent(documentId, latestMergedVersion);
                latestVariants.clear();
                broadcastUpdateNotification(session, documentId, latestMergedVersion);
            }
        }
    }

    private boolean isHeartbeatMessage(JsonNode jsonMessage) {
        return jsonMessage.has("type") && jsonMessage.get("type").asText().equals("heartbeat");
    }

    private String extractNoteContent(JsonNode jsonMessage) {
        if (jsonMessage.has("type") && jsonMessage.has("payload") &&
                jsonMessage.get("type").asText().equals("updateNote")) {
            try {
                JsonNode payloadNode = jsonMessage.get("payload");
                return objectMapper.convertValue(payloadNode, String.class);

            } catch (RuntimeException e) {
                System.err.println("Error converting payload to String object: " + e.getMessage());
            }
        } else {
            System.out.println("Received message does not have the expected 'updateNote' type.");
        }
        return null;
    }

    private Integer extractNoteVersion(JsonNode jsonMessage) {
        if (jsonMessage.has("type") && jsonMessage.has("version") &&
                jsonMessage.get("type").asText().equals("updateNote")) {
            try {
                JsonNode payloadNode = jsonMessage.get("version");
                return objectMapper.convertValue(payloadNode, Integer.class);

            } catch (RuntimeException e) {
                System.err.println("Error converting version to Integer object: " + e.getMessage());
            }
        } else {
            System.out.println("Received message does not have the expected 'updateNote' type.");
        }
        return null;
    }

    private void updateLatestDocumentContent(String documentId, String newContent) {
        latestDocumentContent.put(documentId, newContent);

        Integer currentValue = documentVersionCounter.getOrDefault(documentId, 0);
        documentVersionCounter.put(documentId, currentValue + 1);
    }

    private void broadcastUpdateNotification(WebSocketSession session, String documentId, String newContent)
            throws IOException {
        List<WebSocketSession> sessions = documentSessions.get(documentId);

        String jsonNotification = createJsonNotification(newContent,
                documentVersionCounter.getOrDefault(documentId, 0));

        if (sessions != null) {
            for (WebSocketSession webSocketSession : sessions) {
                if (session != webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(jsonNotification));
                }
            }
        }
    }

    private String createJsonNotification(String newContent, Integer version) throws JsonProcessingException {
        WSUpdateNotification notification = WSUpdateNotification.builder()
                .type("updateNotification")
                .payload(newContent)
                .version(version)
                .build();

        return objectMapper.writeValueAsString(notification);
    }

    private String getDocumentId(WebSocketSession session) {
        // Extract document ID from the session URI
        String uri = session.getUri().toString();
        return uri.substring(uri.lastIndexOf('/') + 1);
    }
}
