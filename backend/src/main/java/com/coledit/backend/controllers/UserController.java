package com.coledit.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coledit.backend.dtos.UserDTO;
import com.coledit.backend.entities.Note;
import com.coledit.backend.entities.User;
import com.coledit.backend.services.NoteService;
import com.coledit.backend.services.UserService;

/**
 * UserController is a REST controller that handles HTTP requests for
 * user-related operations.
 * It uses the UserService to perform the actual business logic.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final NoteService noteService;

    // Constructor injection of the UserService
    @Autowired
    public UserController(UserService userService, NoteService noteService) {
        this.userService = userService;
        this.noteService = noteService;
    }

    /**
     * Handles GET requests to retrieve a specific user by their ID.
     * 
     * @param id the ID of the user to retrieve.
     * @return a ResponseEntity containing the user with the specified ID.
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(convertToUserDTO(userService.getUser(id)));
    }

    /**
     * Handles GET requests to retrieve a specific user by their email.
     * 
     * @param email the email of the user to retrieve.
     * @return a ResponseEntity containing the user with the specified email, or a
     *         not found response if the user does not exist.
     */
    @GetMapping("/getByEmail/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(convertToUserDTO(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles GET requests to retrieve all users.
     * 
     * @return a ResponseEntity containing a list of all users.
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users.stream().map(UserController::convertToUserDTO).toList());
    }

    /**
     * Handles PUT requests to update an existing user by their ID.
     * 
     * @param id      the ID of the user to update.
     * @param newUser the new details of the user.
     * @return a ResponseEntity containing the updated user.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable(value = "id") String id, @RequestBody UserDTO newUser) {
        return ResponseEntity.ok(convertToUserDTO(userService.updateUser(id, convertToUser(newUser))));
    }

    /**
     * Handles PUT requests to update an existing user by their email.
     * 
     * @param email   the email of the user to update.
     * @param newUser the new details of the user.
     * @return a ResponseEntity containing the updated user.
     */
    @PutMapping("/updateByEmail/{email}")
    public ResponseEntity<UserDTO> updateUserByEmail(@PathVariable String email, @RequestBody User newUser) {
        return ResponseEntity.ok(convertToUserDTO(userService.updateUserByEmail(email, newUser)));
    }

    /**
     * Handles POST requests to add a new user.
     * 
     * @param user the user to add.
     * @return a ResponseEntity containing the added user.
     */
    @PostMapping(path = "/add")
    public ResponseEntity<UserDTO> addUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(convertToUserDTO(userService.addUser(convertToUser(userDTO))));
    }

    /**
     * Handles DELETE requests to delete a specific user by their ID.
     * 
     * @param id the ID of the user to delete.
     * @return a ResponseEntity containing a confirmation message.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(userService.deleteUserById(id));
    }

    public static UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .ownedNotes(user.getOwnedNotes().stream().map(Note::getNoteId).toList())
                .collaboratedNotes(user.getCollaboratedNotes().stream().map(Note::getNoteId).toList())
                .build();
    }

    public User convertToUser(UserDTO userDTO) {
        List<Note> ownedNotes = noteService.getOwnedNotes(userDTO.getUserId());
        List<Note> collaboratedNotes = noteService.getCollaboratedNotes(userDTO.getUserId());

        return User.builder()
                .userId(userDTO.getUserId())
                .email(userDTO.getEmail())
                .ownedNotes(ownedNotes)
                .collaboratedNotes(collaboratedNotes)
                .build();
    }
}
