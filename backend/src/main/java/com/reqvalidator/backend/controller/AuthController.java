package com.reqvalidator.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reqvalidator.backend.dto.UserResponse;
import com.reqvalidator.backend.model.User;
import com.reqvalidator.backend.repository.UserRepository;
import com.reqvalidator.backend.security.JwtService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;

        this.passwordEncoder =
                new BCryptPasswordEncoder();
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody User user) {

        // Check whether email already exists
        Optional<User> existingUser =
                userRepository.findByEmail(
                        user.getEmail()
                );

        if (existingUser.isPresent()) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            "An account with this email already exists."
                    );
        }

        // Validate required fields
        if (user.getName() == null
                || user.getName().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Name is required.");
        }

        if (user.getEmail() == null
                || user.getEmail().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required.");
        }

        if (user.getPassword() == null
                || user.getPassword().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Password is required.");
        }

        // Normalize email
        user.setEmail(
                user.getEmail()
                        .trim()
                        .toLowerCase()
        );

        // Hash password before saving
        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        User savedUser =
                userRepository.save(user);

        // Never return the password/hash
        UserResponse response =
                new UserResponse(
                        savedUser.getId(),
                        savedUser.getName(),
                        savedUser.getEmail()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User loginUser) {

        if (loginUser.getEmail() == null
                || loginUser.getEmail().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required.");
        }

        if (loginUser.getPassword() == null
                || loginUser.getPassword().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Password is required.");
        }

        String email =
                loginUser.getEmail()
                        .trim()
                        .toLowerCase();

        Optional<User> userOptional =
                userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Invalid email or password."
                    );
        }

        User user =
                userOptional.get();

        // Check BCrypt password
        boolean passwordMatches =
                passwordEncoder.matches(
                        loginUser.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Invalid email or password."
                    );
        }

        // Generate JWT
        String token =
                jwtService.generateToken(
                        user.getEmail()
                );

        // Safe user response
        UserResponse userResponse =
                new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );

        // Return token + safe user data
        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Login successful"
        );

        response.put(
                "token",
                token
        );

        response.put(
                "user",
                userResponse
        );

        return ResponseEntity.ok(
                response
        );
    }
}