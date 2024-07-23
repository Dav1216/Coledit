package com.coledit.backend.repositories;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coledit.backend.entities.Note;
import com.coledit.backend.entities.User;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByOwner(User user);
    List<Note> findByCollaboratorsContaining(User user);
    
}