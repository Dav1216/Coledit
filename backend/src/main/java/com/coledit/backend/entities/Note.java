package com.coledit.backend.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notes")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "noteId")
public class Note {

  @Id
  @Column(name = "note_id")
  @JsonView(View.Summary.class)
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID noteId;
  @JsonView(View.Summary.class)
  private String title;
  @JsonView(View.Summary.class)
  private String content;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonView(View.Summary.class)
  private User owner;

  @ManyToMany
  @JoinTable(name = "user_and_note", joinColumns = @JoinColumn(name = "note_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  @Builder.Default
  @JsonView(View.Summary.class)
  private List<User> collaborators = new ArrayList<>();

  @Override
  public String toString() {
    return "Note{" +
        "noteId='" + noteId + '\'' +
        ", title='" + title + '\'' +
        ", content='" + content + '\'' +
        ", ownerUserId='" + owner.getUserId() + '\'' + // Print only userId instead of entire User object
        '}';
  }
}
