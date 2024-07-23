package com.coledit.backend.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coledit.backend.entities.Note;
import com.coledit.backend.entities.User;
import com.coledit.backend.exceptions.UserNotFoundException;
import com.coledit.backend.repositories.NoteRepository;
import com.coledit.backend.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public Note createNote(Note note) {
        return noteRepository.save(note);
    }

    public Note getNoteById(String id) {
        Optional<Note> note = noteRepository.findById(UUID.fromString(id));
        return note.orElse(null);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    @Transactional
    public Note updateNote(String id, Note newNote) {
        Optional<Note> existingNote = noteRepository.findById(UUID.fromString(id));
        if (existingNote.isPresent()) {
            Note noteToUpdate = existingNote.get();
            noteToUpdate.setContent(newNote.getContent());
            noteToUpdate.setOwner(newNote.getOwner());
            noteToUpdate.setCollaborators(newNote.getCollaborators());
            return noteRepository.save(noteToUpdate);
        }
        return null;
    }

    @Transactional
    public boolean deleteNoteById(String id) {
        if (noteRepository.existsById(UUID.fromString(id))) {
            noteRepository.deleteById(UUID.fromString(id));
            return true;
        }
        return false;
    }

    @Transactional
    public Note addCollaborator(String noteId, String userId) {
        Optional<Note> noteOptional = noteRepository.findById(UUID.fromString(noteId));
        Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
        if (noteOptional.isPresent() && userOptional.isPresent()) {
            Note note = noteOptional.get();
            User user = userOptional.get();
            note.getCollaborators().add(user);
            return noteRepository.save(note);
        }
        return null;
    }

    @Transactional
    public Note removeCollaborator(String noteId, String userId) {
        Optional<Note> noteOptional = noteRepository.findById(UUID.fromString(noteId));
        Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
        if (noteOptional.isPresent() && userOptional.isPresent()) {
            Note note = noteOptional.get();
            User user = userOptional.get();
            note.getCollaborators().remove(user);
            return noteRepository.save(note);
        }
        return null;
    }

    public List<Note> getNotesByUserEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        User user = userOptional.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        List<Note> ownedNotes = noteRepository.findByOwner(user);
        List<Note> collaboratedNotes = noteRepository.findByCollaboratorsContaining(user);
        ownedNotes.addAll(collaboratedNotes);
        return ownedNotes;
    }

    public boolean isNoteIdAccessiblByUserEmail(String noteId, String userEmail) {
        List<Note> acccessibleNotes = getNotesByUserEmail(userEmail);
        return acccessibleNotes.stream().anyMatch(note -> note.getNoteId().toString().equals(noteId));
    }
}