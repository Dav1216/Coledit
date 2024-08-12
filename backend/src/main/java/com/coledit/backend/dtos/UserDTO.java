package com.coledit.backend.dtos;

import java.util.List;
import java.util.UUID;

import com.coledit.backend.entities.Note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
        private UUID userId;
        private String email;
        private List<UUID> ownedNotes;
        private List<UUID> collaboratedNotes;
}
