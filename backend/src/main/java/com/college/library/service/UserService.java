package com.college.library.service;

import com.college.library.dto.RegisterRequest;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new student account.
     * Enforces that public registration is strictly for the STUDENT role.
     */
    @Transactional
    public User registerStudent(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setActive(true);

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public List<User> getAllStudents() {
        return userRepository.findByRoleOrderByNameAsc(Role.STUDENT);
    }
}
