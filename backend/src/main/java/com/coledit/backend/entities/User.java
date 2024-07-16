package com.coledit.backend.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userId;

    private String userName;
    private String email;
    private String hashPassword;
    private String roles;

    @OneToMany
    @JoinColumn(name = "note_id")
    private Set<Note> notes;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
         if (this.getRoles() != null && !this.getRoles().isEmpty()) {
            // Split the roles string, map them to SimpleGrantedAuthority, and collect them
            // into a list
            return Stream.of(this.roles.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            // Safe default if roles are null
            return Collections.emptyList();
        }
    }

    @Override
    public String getPassword() {
        return this.hashPassword;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
