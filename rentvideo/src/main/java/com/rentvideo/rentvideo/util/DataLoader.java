package com.rentvideo.rentvideo.util;

import com.rentvideo.rentvideo.model.Role;
import com.rentvideo.rentvideo.model.User;
import com.rentvideo.rentvideo.model.Video;
import com.rentvideo.rentvideo.repository.RoleRepository;
import com.rentvideo.rentvideo.repository.UserRepository;
import com.rentvideo.rentvideo.repository.VideoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VideoRepository videoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, RoleRepository roleRepository, VideoRepository videoRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.videoRepository = videoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create roles if they don't exist
        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(null, Role.RoleName.ROLE_USER));
        }
        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(null, Role.RoleName.ROLE_ADMIN));
        }

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).get();
        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).get();

        // Create admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("adminpass")); // Encode password
            adminUser.setEmail("admin@example.com");
            Set<Role> adminRoles = new HashSet<>(Arrays.asList(userRole, adminRole));
            adminUser.setRoles(adminRoles);
            userRepository.save(adminUser);
        }

        // Create regular user
        if (userRepository.findByUsername("user").isEmpty()) {
            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setPassword(passwordEncoder.encode("userpass")); // Encode password
            regularUser.setEmail("user@example.com");
            Set<Role> userRoles = new HashSet<>(Arrays.asList(userRole));
            regularUser.setRoles(userRoles);
            userRepository.save(regularUser);
        }

        // Create some videos
        if (videoRepository.count() == 0) {
            videoRepository.save(new Video(null, "The Matrix", "Lana Wachowski", "Sci-Fi", 1999, 5, 2.99));
            videoRepository.save(new Video(null, "Inception", "Christopher Nolan", "Sci-Fi", 2010, 3, 3.49));
            videoRepository.save(new Video(null, "Pulp Fiction", "Quentin Tarantino", "Crime", 1994, 2, 2.79));
            videoRepository.save(new Video(null, "The Shawshank Redemption", "Frank Darabont", "Drama", 1994, 4, 2.50));
            videoRepository.save(new Video(null, "Avatar", "James Cameron", "Sci-Fi", 2009, 6, 3.99));
        }
    }
}