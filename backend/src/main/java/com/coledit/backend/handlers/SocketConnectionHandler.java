package com.coledit.backend.handlers;

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

import com.coledit.backend.entities.Note;
import com.coledit.backend.entities.WSUpdateNotification;
import com.coledit.backend.services.NoteService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Socket-Connection Configuration class 
@Component
public class SocketConnectionHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> documentSessions = new ConcurrentHashMap<>();
    private final Map<String, String> latestDocumentContent = new ConcurrentHashMap<>();
    private NoteService noteService;

    @Autowired
    public SocketConnectionHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    // This method is executed when client tries to connect
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        String documentId = getDocumentId(session);
        documentSessions.computeIfAbsent(documentId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);

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
                    latestDocumentContent.remove(documentId); 
                }
                documentSessions.remove(documentId);
            }
        }

        System.out.println("Session " + session.getId() + " disconnected from document " + documentId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload().toString());
        String newContent = null;

        if (jsonMessage.has("type") && jsonMessage.get("type").asText().equals("heartbeat")) {
            return; // Do nothing for heartbeat type
        }
        // Check if the message has the expected structure for updating notes
        if (jsonMessage.has("type") && jsonMessage.has("payload") &&
                jsonMessage.get("type").asText().equals("updateNote")) {

            try {
                JsonNode payloadNode = jsonMessage.get("payload");
                newContent = objectMapper.convertValue(payloadNode, String.class);
            } catch (RuntimeException e) {
                System.err.println("Error converting payload to Note object: " + e.getMessage());
                return; // Early return if conversion fails
            }
        } else {
            System.out.println("Received message does not have the expected 'update' type.");
            return; // Early return for unexpected types
        }

        String documentId = getDocumentId(session);
        latestDocumentContent.put(documentId, newContent); 

        List<WebSocketSession> sessions = documentSessions.get(documentId);

        WSUpdateNotification notification = WSUpdateNotification.builder().type("updateNotification")
                .payload(newContent).build();
        String jsonNotification = objectMapper.writeValueAsString(notification);

        if (sessions != null) {
            for (WebSocketSession webSocketSession : sessions) {
                if (session != webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(jsonNotification));
                }
            }
        }
    }

    private String getDocumentId(WebSocketSession session) {
        // Extract document ID from the session URI
        String uri = session.getUri().toString();
        return uri.substring(uri.lastIndexOf('/') + 1);
    }
}
