package com.coledit.backend.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// Socket-Connection Configuration class 
public class SocketConnectionHandler extends TextWebSocketHandler {

     private final Map<String, List<WebSocketSession>> documentSessions = new ConcurrentHashMap<>();

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
                documentSessions.remove(documentId);
            }
        }

        System.out.println("Session " + session.getId() + " disconnected from document " + documentId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
        
        String documentId = getDocumentId(session);
        List<WebSocketSession> sessions = documentSessions.get(documentId);

        if (sessions != null) {
            for (WebSocketSession webSocketSession : sessions) {
                if (session != webSocketSession) {
                    webSocketSession.sendMessage(message);
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
