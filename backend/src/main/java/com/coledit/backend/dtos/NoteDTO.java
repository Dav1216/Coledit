package com.coledit.backend.dtos;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteDTO {
    private UUID noteId;
    private String title;
    private String content;
    private UUID owner;
    private List<UUID> collaborators;
}