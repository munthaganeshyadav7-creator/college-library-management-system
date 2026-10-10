package com.college.library.config;

import com.college.library.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Static resources & HTML pages
                .requestMatchers(
                    "/", "/index.html", "/login.html", "/register.html", "/books.html",
                    "/student-dashboard.html", "/my-borrowings.html",
                    "/librarian-dashboard.html", "/manage-books.html", "/issue-return.html",
                    "/admin-dashboard.html", "/reports.html",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico"
                ).permitAll()

                // Public REST endpoints
                .requestMatchers(HttpMethod.POST, "/api/register", "/api/login", "/api/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/**", "/api/books").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/dashboard").hasAnyRole("LIBRARIAN", "ADMIN")

                // Authenticated user status check
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()

                // Student actions
                .requestMatchers(HttpMethod.GET, "/api/borrow/my").hasRole("STUDENT")

                // Borrowing: Student can borrow, Librarian/Admin can issue
                .requestMatchers(HttpMethod.POST, "/api/borrow").hasAnyRole("STUDENT", "LIBRARIAN", "ADMIN")

                // Librarian / Admin operations
                .requestMatchers(HttpMethod.PUT, "/api/borrow/*/return", "/api/borrow/*/pay-penalty").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/borrow").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/borrow/overdue").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/students").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/books").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/books/*").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/*").hasAnyRole("LIBRARIAN", "ADMIN")

                // Admin & Librarian Reports & Analytics
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/penalties").hasAnyRole("ADMIN", "LIBRARIAN")

                // Any other request must be authenticated
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized. Please log in.\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Access Denied. Insufficient role permissions.\"}");
                })
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.onrender.com",
                "https://*.github.io",
                "*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
