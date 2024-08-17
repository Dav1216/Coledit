package com.coledit.backend.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

import com.coledit.backend.dtos.NoteDTO;
import com.coledit.backend.dtos.UserDTO;
import com.coledit.backend.entities.Note;
import com.coledit.backend.entities.User;
import com.coledit.backend.services.NoteService;
import com.coledit.backend.services.UserService;


@RestController
@RequestMapping("/note")
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    @Autowired
    public NoteController(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    /**
     * Handles POST requests to create a new note.
     * 
     * @param note the note to create.
     * @return a ResponseEntity containing the created note.
     */
    
    @PostMapping("/create")
    @ConditionalOnProperty(name = "note.controller.enabled", havingValue = "true", matchIfMissing = false)
    public ResponseEntity<NoteDTO> createNote(@RequestBody NoteDTO noteDTO) {
        Note createdNote = noteService.createNote(convertToNote(noteDTO));

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToNoteDTO(createdNote));
    }
    
    
    @PostMapping("/create/{userEmail}")
    public ResponseEntity<NoteDTO> createNoteWithUserEmail(@PathVariable(value = "userEmail") String userEmail, @RequestBody NoteDTO noteDTO) {
        noteDTO.setOwner(userService.getUserByEmail(userEmail).getUserId());
        Note createdNote = noteService.createNote(convertToNote(noteDTO));

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToNoteDTO(createdNote));
    }

    /**
     * Handles GET requests to retrieve a specific note by its ID.
     * 
     * @param id the ID of the note to retrieve.
     * @return a ResponseEntity containing the note with the specified ID.
     */
    @GetMapping("/get/{id}")
    @ConditionalOnProperty(name = "note.controller.enabled", havingValue = "true", matchIfMissing = false)
    public ResponseEntity<NoteDTO> getNoteById(@PathVariable(value = "id") String id) {
        Note note = noteService.getNoteById(id);
        if (note != null) {
            return ResponseEntity.ok(convertToNoteDTO(note));
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
    @ConditionalOnProperty(name = "note.controller.enabled", havingValue = "true", matchIfMissing = false)
    public ResponseEntity<List<NoteDTO>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes.stream().map(NoteController::convertToNoteDTO).toList());
    }

    /**
     * Handles PUT requests to update an existing note by its ID.
     * 
     * @param id      the ID of the note to update.
     * @param newNote the new details of the note.
     * @return a ResponseEntity containing the updated note.
     */
    @PutMapping("/update/{id}")
    @ConditionalOnProperty(name = "note.controller.enabled", havingValue = "true", matchIfMissing = false)
    public ResponseEntity<NoteDTO> updateNote(@PathVariable(value = "id") String id, @RequestBody NoteDTO newNote) {
        Note updatedNote = noteService.updateNote(id, convertToNote(newNote));
        if (updatedNote != null) {
            return ResponseEntity.ok(convertToNoteDTO(updatedNote));
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
    public ResponseEntity<List<UserDTO>> getCollaborators(@PathVariable(value = "noteId") String noteId) {
        List<User> collaborators = noteService.getNoteCollaborators(noteId);
        if (collaborators != null) {
            return ResponseEntity.ok(collaborators.stream().map(UserController::convertToUserDTO).toList());
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
    public ResponseEntity<NoteDTO> addCollaborator(@RequestParam String noteId, @RequestParam String userEmail) {
        Note updatedNote = noteService.addCollaborator(noteId, userEmail);
        if (updatedNote != null) {
            return ResponseEntity.ok(convertToNoteDTO(updatedNote));
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
    public ResponseEntity<NoteDTO> removeCollaborator(@RequestParam String noteId, @RequestParam String userEmail) {
        Note updatedNote = noteService.removeCollaborator(noteId, userEmail);
        if (updatedNote != null) {
            return ResponseEntity.ok(convertToNoteDTO(updatedNote));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles GET requests to retrieve all notes owned or collaborated on by a
     * specific user.
     * 
     * @param userId the ID of the user.
     * @return a ResponseEntity containing a list of notes.
     */
    @GetMapping("/getByUserEmail/{userEmail}")
    public ResponseEntity<List<NoteDTO>> getNotesByUser(@PathVariable String userEmail) {
        List<NoteDTO> notesDTO = noteService.getNotesByUserEmail(userEmail).stream()
                .map(NoteController::convertToNoteDTO)
                .toList();

        if (notesDTO != null && !notesDTO.isEmpty()) {
            return ResponseEntity.ok(notesDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public static NoteDTO convertToNoteDTO(Note note) {
        return NoteDTO.builder()
                .noteId(note.getNoteId())
                .title(note.getTitle())
                .content(note.getContent())
                .owner(note.getOwner().getUserId())
                .collaborators(note.getCollaborators().stream().map(User::getUserId).toList())
                .build();
    }

    public Note convertToNote(NoteDTO noteDTO) {
        User owner = userService.getUserByUUID(noteDTO.getOwner());
        List<User> collaborators = userService.getUsersByUserIds(noteDTO.getCollaborators());

        return Note.builder()
                .noteId(noteDTO.getNoteId())
                .title(noteDTO.getTitle())
                .content(noteDTO.getContent())
                .owner(owner)
                .collaborators(collaborators)
                .build();
    }
}
