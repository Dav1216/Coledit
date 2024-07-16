package com.coledit.backend.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "notes")
public class Note {
    @Id
    @Column(name = "note_id")
    private UUID noteId;
    private String content;

    
    public Note() {
        this.noteId = UUID.randomUUID();
    }
}
