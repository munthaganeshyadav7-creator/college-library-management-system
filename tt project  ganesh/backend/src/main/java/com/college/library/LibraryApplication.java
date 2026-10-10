package com.college.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the College Library Management System.
 * B.Tech Technical Training (TT) Session Project.
 */
@SpringBootApplication
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        System.out.println("===============================================================");
        System.out.println(" College Library Management System started successfully!");
        System.out.println(" Web Application URL: http://localhost:8080");
        System.out.println(" REST API Base URL:  http://localhost:8080/api");
        System.out.println("===============================================================");
    }
}
