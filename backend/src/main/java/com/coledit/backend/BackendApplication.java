package com.coledit.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.coledit.backend.repositories.NoteRepository;
import com.coledit.backend.repositories.UserRepository;
import com.coledit.backend.entities.Note;
import com.coledit.backend.entities.User;
import com.coledit.backend.services.NoteService;
import com.coledit.backend.services.UserService;

import jakarta.annotation.Resource;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
public class BackendApplication {
    @Resource
    UserService userService;
    @Resource
    NoteService noteService;
    @Resource
    NoteRepository noteRepository;
    @Resource
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner demoData() {
        return args -> {
            noteRepository.deleteAll();
            userRepository.deleteAll();

            // Create User objects
            User user1 = User.builder()
                    .email("user1@example.com")
                    .hashPassword("pass1")
                    .build();

            User user2 = User.builder()
                    .email("user2@example.com")
                    .hashPassword("pass2")
                    .build();

            // Save users to the database
            user1 = userService.addUser(user1);
            user2 = userService.addUser(user2);

            // Create Note objects
            Note note1 = Note.builder()
                    .title("Note 1")
                    .content("Content of Note 1")
                    .owner(user1)
                    .build();

            Note note2 = Note.builder()
                    .title("Note 2")
                    .content("Content of Note 2")
                    .owner(user2)
                    .build();

            // Save notes to the database
            note1 = noteService.createNote(note1);
            note2 = noteService.createNote(note2);

            noteService.addCollaborator(note1.getNoteId().toString(), user2.getEmail());
            noteService.addCollaborator(note2.getNoteId().toString(), user1.getEmail());
        };
    }
}
