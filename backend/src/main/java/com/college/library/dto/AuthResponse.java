package com.college.library.dto;

import com.college.library.entity.Role;

public class AuthResponse {

    private boolean success;
    private String message;
    private Long id;
    private String name;
    private String email;
    private Role role;

    public AuthResponse() {
    }

    public AuthResponse(boolean success, String message, Long id, String name, String email, Role role) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
