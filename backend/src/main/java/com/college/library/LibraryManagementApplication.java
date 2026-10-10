package com.college.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the College Library Management System.
 * B.Tech Technical Training (TT) Session Project.
 */
@SpringBootApplication
public class LibraryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" College Library Management System is running successfully!");
        System.out.println(" Open your browser at: http://localhost:8085");
        System.out.println("==================================================================");
    }
}
