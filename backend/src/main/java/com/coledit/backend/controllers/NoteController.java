package com.coledit.backend.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coledit.backend.entities.Note;
import com.coledit.backend.entities.User;
import com.coledit.backend.services.NoteService;

@RestController
@RequestMapping("/note")
public class NoteController {
    
    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Handles POST requests to create a new note.
     * 
     * @param note the note to create.
     * @return a ResponseEntity containing the created note.
     */
    @PostMapping("/create")
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        Note createdNote = noteService.createNote(note);
  
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

    /**
     * Handles GET requests to retrieve a specific note by its ID.
     * 
     * @param id the ID of the note to retrieve.
     * @return a ResponseEntity containing the note with the specified ID.
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable(value = "id") String id) {
        Note note = noteService.getNoteById(id);
        if (note != null) {
            return ResponseEntity.ok(note);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles GET requests to retrieve all notes.
     * 
     * @return a ResponseEntity containing a list of all notes.
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    /**
     * Handles PUT requests to update an existing note by its ID.
     * 
     * @param id      the ID of the note to update.
     * @param newNote the new details of the note.
     * @return a ResponseEntity containing the updated note.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable(value = "id") String id, @RequestBody Note newNote) {
        Note updatedNote = noteService.updateNote(id, newNote);
        if (updatedNote != null) {
            return ResponseEntity.ok(updatedNote);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles DELETE requests to delete a specific note by its ID.
     * 
     * @param id the ID of the note to delete.
     * @return a ResponseEntity containing a confirmation message.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteNoteById(@PathVariable(value = "id") String id) {
        boolean isDeleted = noteService.deleteNoteById(id);
        if (isDeleted) {
            return ResponseEntity.ok("Note deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/getCollaborators/{noteId}")
    public ResponseEntity<List<User>> getCollaborators(@PathVariable(value = "noteId") String noteId) {
        List<User> collaborators = noteService.getNoteCollaborators(noteId);
        if (collaborators != null) {
            return ResponseEntity.ok(collaborators);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles POST requests to add a collaborator to a note.
     * 
     * @param noteId the ID of the note.
     * @param userId the ID of the user to add as a collaborator.
     * @return a ResponseEntity containing the updated note.
     */
    @PostMapping("/addCollaborator")
    public ResponseEntity<Note> addCollaborator(@RequestParam String noteId, @RequestParam String userEmail) {
        Note updatedNote = noteService.addCollaborator(noteId, userEmail);
        if (updatedNote != null) {
            return ResponseEntity.ok(updatedNote);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles Delete requests to remove a collaborator from a note.
     * 
     * @param noteId the ID of the note.
     * @param userId the ID of the user to remove as a collaborator.
     * @return a ResponseEntity containing the updated note.
     */
    @DeleteMapping("/removeCollaborator")
    public ResponseEntity<Note> removeCollaborator(@RequestParam String noteId, @RequestParam String userEmail) {
        Note updatedNote = noteService.removeCollaborator(noteId, userEmail);
        if (updatedNote != null) {
            return ResponseEntity.ok(updatedNote);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles GET requests to retrieve all notes owned or collaborated on by a specific user.
     * 
     * @param userId the ID of the user.
     * @return a ResponseEntity containing a list of notes.
     */
    @GetMapping("/getByUserEmail/{userEmail}")
    public ResponseEntity<List<Note>> getNotesByUser(@PathVariable String userEmail) {
        List<Note> notes = noteService.getNotesByUserEmail(userEmail);
        if (notes != null && !notes.isEmpty()) {
            return ResponseEntity.ok(notes);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
