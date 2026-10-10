package com.college.library.service;

import com.college.library.dto.AuthResponse;
import com.college.library.dto.LoginRequest;
import com.college.library.dto.RegisterRequest;
import com.college.library.dto.UserDto;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.repository.UserRepository;
import com.college.library.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling user registration, authentication, and user lookups.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Public student registration endpoint.
     * Enforces STUDENT role only.
     */
    @Transactional
    public AuthResponse registerStudent(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with email '" + req.getEmail() + "' already exists");
        }

        User student = new User(
                req.getName().trim(),
                req.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(req.getPassword()),
                req.getPhone() != null ? req.getPhone().trim() : null,
                Role.STUDENT
        );

        User saved = userRepository.save(student);

        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name(), saved.getName(), saved.getId());
        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    /**
     * User login authentication with BCrypt verification.
     */
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, req.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated. Please contact administrator.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getName(), user.getId());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    /**
     * Get user profile by email.
     */
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToDto(user);
    }

    /**
     * List all registered students.
     */
    public List<UserDto> getAllStudents() {
        return userRepository.findByRole(Role.STUDENT).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
