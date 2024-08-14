package com.coledit.backend.configs;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.coledit.backend.helpers.FilterHelper;
import com.coledit.backend.services.JwtService;
import com.coledit.backend.services.NoteService;
import com.coledit.backend.wrappers.RequestWrapper;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final NoteService noteService;

    public JwtAuthorizationFilter(JwtService jwtService, NoteService noteService) {
        this.jwtService = jwtService;
        this.noteService = noteService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {
        // filterChain.doFilter(request, response);
        request = new RequestWrapper((HttpServletRequest) request);
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = FilterHelper.getJwString(request);
        String userEmail = null;

        if (jwt != null) {
            userEmail = jwtService.extractUsername(jwt);
        }

        // Proceeding with authorization checks based on the request method and URI
        switch (request.getMethod()) {

            case "POST":
                if (requestURI.startsWith("/note/create")) {
                    String email = extractPathVarFromRequest(requestURI, 3);

                    if (!email.equals(userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                if ("/note/addCollaborator".equals(requestURI)) {
                    Map<String, String[]> queryParams = request.getParameterMap();
                    String[] noteIds = queryParams.get("noteId");
                    String[] userEmails = queryParams.get("userEmail");

                    if (noteIds == null || userEmails == null || noteIds.length == 0 || userEmails.length == 0) {
                        response.setStatus(HttpStatus.BAD_REQUEST.value()); // Missing required parameters
                        return;
                    }
                    String noteId = noteIds[0];

                    if (!noteService.isNoteIdAccessiblByUserEmail(noteId, userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                break;

            case "GET", "UPDATE", "DELETE":
                // athorization for accessing a collaborative edditing session with WebSocket
                if (requestURI.startsWith("/document")) {
                    String noteId = extractPathVarFromRequest(requestURI, 2);
                    if (!noteService.isNoteIdAccessiblByUserEmail(noteId, userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                if (requestURI.startsWith("/note/get/") || requestURI.startsWith("/note/update/")
                        || requestURI.startsWith("/note/delete/")) {
                    String noteId = extractPathVarFromRequest(requestURI, 3);
                    if (!noteService.isNoteIdAccessiblByUserEmail(noteId, userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                if (requestURI.startsWith("/note/getByUserEmail/")) {
                    String email = extractPathVarFromRequest(requestURI, 3);
                    if (!email.equals(userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                if (requestURI.startsWith("/note/getCollaborators/")) {
                    String noteId = extractPathVarFromRequest(requestURI, 3);

                    if (!noteService.isNoteIdAccessiblByUserEmail(noteId, userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                if (requestURI.startsWith("/note/removeCollaborator")) {
                    Map<String, String[]> queryParams = request.getParameterMap();
                    String[] noteIds = queryParams.get("noteId");
                    String[] userIds = queryParams.get("userId");

                    if (noteIds == null || userIds == null || noteIds.length == 0 || userIds.length == 0) {
                        response.setStatus(HttpStatus.BAD_REQUEST.value()); // Missing required parameters
                        return;
                    }
                    String noteId = noteIds[0];

                    if (!noteService.isNoteIdAccessiblByUserEmail(noteId, userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                if (requestURI.startsWith("/note/delete")) {
                    String noteId = extractPathVarFromRequest(requestURI, 3);

                    if (!noteService.isNoteIdAccessiblByUserEmail(noteId, userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                break;
        }

        // If everything checks out, proceed with the filter chain
        filterChain.doFilter(request, response);
    }

    private String extractPathVarFromRequest(String requestURI, int number) {
        String[] parts = requestURI.split("/");

        if (number == 3 && parts.length >= 4) {
            return parts[3];
        }

        if (number == 2 && parts.length >= 3) {
            return parts[2];
        }

        return null;
    }
}