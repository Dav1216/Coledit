package com.coledit.backend.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.coledit.backend.entities.Note;
import com.coledit.backend.services.NoteService;

@Controller
public class WebSocketDocumentController {

    private final NoteService noteService;

    public WebSocketDocumentController(NoteService noteService) {
        this.noteService = noteService;
    }

    @MessageMapping("/wsUpdateNote")
    @SendTo("/topic/noteUpdates")
    public Note updateNoteContent(Note message) {
        System.out.println("here");
        return noteService.updateNote(message.getNoteId().toString(), message);
    }
}
