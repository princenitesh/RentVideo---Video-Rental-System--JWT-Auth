package com.rentvideo.rentvideo.controller;

import com.rentvideo.rentvideo.dto.UserCreationDTO;
import com.rentvideo.rentvideo.dto.UserDTO;
import com.rentvideo.rentvideo.exception.ResourceNotFoundException;
import com.rentvideo.rentvideo.exception.UserAlreadyExistsException;
import com.rentvideo.rentvideo.model.Role;
import com.rentvideo.rentvideo.model.User;
import com.rentvideo.rentvideo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOS = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOS);
    }

    // Admin only
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(convertToDto(user));
    }

    // Public endpoint for self-registration
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        try {
            User newUser = new User();
            newUser.setUsername(userCreationDTO.getUsername());
            newUser.setPassword(userCreationDTO.getPassword());
            newUser.setEmail(userCreationDTO.getEmail());
            User registeredUser = userService.registerNewUser(newUser);
            return new ResponseEntity<>(convertToDto(registeredUser), HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // Admin or self (if authenticated user matches ID)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.username == @userService.getUserById(#id).username")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        User userDetails = new User();
        userDetails.setEmail(userDTO.getEmail()); // Only email can be updated here
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    // Admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Admin only
    @PutMapping("/{userId}/roles/assign/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> assignRoleToUser(@PathVariable Long userId, @PathVariable Role.RoleName roleName) {
        User updatedUser = userService.assignRoleToUser(userId, roleName);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    // Admin only
    @PutMapping("/{userId}/roles/remove/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> removeRoleFromUser(@PathVariable Long userId, @PathVariable Role.RoleName roleName) {
        User updatedUser = userService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    private UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return userDTO;
    }
}