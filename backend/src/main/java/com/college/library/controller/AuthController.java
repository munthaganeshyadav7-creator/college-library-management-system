package com.college.library.controller;

import com.college.library.dto.ApiResponse;
import com.college.library.dto.AuthResponse;
import com.college.library.dto.LoginRequest;
import com.college.library.dto.RegisterRequest;
import com.college.library.entity.User;
import com.college.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    /**
     * Public Registration for students.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User student = userService.registerStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Registration successful! You can now log in.", student.getEmail()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * User Login for all roles (STUDENT, LIBRARIAN, ADMIN).
     * Validates credentials, sets session security context, and returns role info.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            String email = request.getEmail().trim().toLowerCase();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            User user = userService.findByEmail(email);

            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "Login successful! Welcome, " + user.getName(),
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password. Please verify your credentials."));
        }
    }

    /**
     * Invalidates current user session.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    /**
     * Returns details of currently authenticated user.
     */
    @GetMapping("/auth/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User is not authenticated"));
        }

        try {
            User user = userService.findByEmail(authentication.getName());
            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "Authenticated",
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("User not found"));
        }
    }
}
