package com.rentvideo.rentvideo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Correct import for AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Needed for JWT filter

// NOTE: Remove unused imports if they cause issues:
// import com.rentvideo.model.Role; // Not directly used in SecurityConfig
// import com.rentvideo.repository.UserRepository; // Not directly used in SecurityConfig
// import com.rentvideo.service.CustomUserDetailsService; // Only implicitly used by AuthenticationManagerBuilder

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize
public class SecurityConfig {

    // Removed CustomUserDetailsService and CustomBasicAuthenticationEntryPoint from direct injection
    // as they are primarily used by AuthenticationManager and replaced by JWT specifics.
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    // Adjusted constructor to inject JWT specific components
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtRequestFilter jwtRequestFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Use AuthenticationConfiguration to get the AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)) // Use JWT EntryPoint
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session for JWT
            .authorizeHttpRequests(authorize -> authorize
                // Allow these specific authentication endpoints without authentication
                .requestMatchers("/api/auth/register", "/api/auth/login", "/h2-console/**").permitAll()
                // You might also want to allow GET for all videos publicly if applicable
                // .requestMatchers(HttpMethod.GET, "/api/videos", "/api/videos/{id}").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        // Add our custom JWT filter BEFORE Spring Security's default UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // For H2 console to work with Spring Security (remove in production if not needed)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}