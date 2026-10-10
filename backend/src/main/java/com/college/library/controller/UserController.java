package com.college.library.controller;

import com.college.library.entity.User;
import com.college.library.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lists registered students (for librarians to select when issuing books).
     */
    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getStudents() {
        List<User> students = userService.getAllStudents();
        List<Map<String, Object>> response = students.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("email", s.getEmail());
            map.put("phone", s.getPhone());
            map.put("active", s.isActive());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
