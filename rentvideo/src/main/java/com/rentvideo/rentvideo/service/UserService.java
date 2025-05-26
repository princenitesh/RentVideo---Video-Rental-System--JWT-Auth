package com.rentvideo.rentvideo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentvideo.rentvideo.exception.ResourceNotFoundException;
import com.rentvideo.rentvideo.exception.UserAlreadyExistsException;
import com.rentvideo.rentvideo.model.Role; // IMPORTANT: Add this import
import com.rentvideo.rentvideo.model.User;
import com.rentvideo.rentvideo.repository.RoleRepository;
import com.rentvideo.rentvideo.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // --- NEW METHOD ADDED HERE ---
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    // --- END OF NEW METHOD ---


    @Transactional
    public User registerNewUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign default role (e.g., ROLE_USER)
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_USER not found. Please ensure it's in the database."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setEmail(userDetails.getEmail());
        // Do not allow direct password update here, have a separate endpoint for that
        // user.setPassword(passwordEncoder.encode(userDetails.getPassword())); // If password needs to be updated

        // You might want to update roles as well, but typically handled by admin
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public User assignRoleToUser(Long userId, Role.RoleName roleName) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Transactional
    public User removeRoleFromUser(Long userId, Role.RoleName roleName) {
        User user = getUserById(userId);
        Role roleToRemove = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.getRoles().removeIf(role -> role.getName().equals(roleToRemove.getName()));
        return userRepository.save(user);
    }
}