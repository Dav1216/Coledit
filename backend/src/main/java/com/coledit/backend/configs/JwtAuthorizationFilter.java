package com.coledit.backend.configs;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.coledit.backend.entities.Note;
import com.coledit.backend.helpers.FilterHelper;
import com.coledit.backend.services.JwtService;
import com.coledit.backend.services.NoteService;
import com.coledit.backend.wrappers.RequestWrapper;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        request = new RequestWrapper((HttpServletRequest) request);
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/auth")) {
            filterChain.doFilter(request, response);
        }

        String jwt = FilterHelper.getJwString(request);
        String userEmail = null;

        if (jwt != null) {
            userEmail = jwtService.extractUsername(jwt);
        }
      

        // Proceed with authorization checks based on the request method and URI
        switch (request.getMethod()) {
            case "POST":
                if ("/note/create".equals(requestURI)) {
                    Note note = extractNoteFromBody(request); // Implement this method to parse Note from request body

                    if (!note.getOwner().getEmail().equals(userEmail)) {
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
                if (requestURI.startsWith("/note/get/") || requestURI.startsWith("/note/update/")
                        || requestURI.startsWith("/note/delete/")) {
                    String noteId = extractPathVarFromRequest(requestURI);
                    if (!noteService.isNoteIdAccessiblByUserEmail(noteId, userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                if (requestURI.startsWith("/note/getByUserEmail/")) {
                    String email = extractPathVarFromRequest(requestURI);
                    if (!email.equals(userEmail)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        return;
                    }
                }

                if ("/note/removeCollaborator".equals(requestURI)) {
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

                break;
        }

        // If everything checks out, proceed with the filter chain
        filterChain.doFilter(request, response);
    }

    public Note extractNoteFromBody(HttpServletRequest request) throws java.io.IOException {
        // Read the request body without modifying the request
        String body = ((HttpServletRequest) request).getReader().lines()
        .collect(Collectors.joining(System.lineSeparator()));
        System.out.println(body);
        // Deserialize the JSON string into a Note object
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Note note = null;
        try {
             note = objectMapper.readValue(body, Note.class);
        } catch (JacksonException e) {
            System.out.println(e);
        }
      

        return note;
    }

    private String extractPathVarFromRequest(String requestURI) {
        String[] parts = requestURI.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        return null;
    }
}