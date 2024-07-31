package com.coledit.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coledit.backend.entities.Note;
import com.coledit.backend.entities.User;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByOwner(User user);
    List<Note> findByCollaboratorsContaining(User user);
    
    
    @Query("SELECT u FROM Note n JOIN n.collaborators u WHERE n.noteId = :noteId")
    List<User> findCollaboratorsByNoteId(@Param("noteId") UUID noteId);
}