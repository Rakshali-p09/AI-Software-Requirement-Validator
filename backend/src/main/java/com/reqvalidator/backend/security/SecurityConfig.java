package com.reqvalidator.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> {})

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .exceptionHandling(exception ->
                exception
                    .authenticationEntryPoint(
                        authenticationEntryPoint()
                    )
                    .accessDeniedHandler(
                        accessDeniedHandler()
                    )
            )

            .authorizeHttpRequests(auth -> auth

                // Public authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Public LLM health-check endpoint
                .requestMatchers("/api/llm/health").permitAll()

                // Everything else requires JWT authentication
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {

            response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
            );

            response.setCharacterEncoding("UTF-8");

            String jsonResponse =
                    "{"
                    + "\"timestamp\":\""
                    + java.time.LocalDateTime.now()
                    + "\","
                    + "\"status\":401,"
                    + "\"error\":\"Unauthorized\","
                    + "\"message\":\"Authentication required.\""
                    + "}";

            response.getWriter().write(jsonResponse);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
            );

            response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
            );

            response.setCharacterEncoding("UTF-8");

            String jsonResponse =
                    "{"
                    + "\"timestamp\":\""
                    + java.time.LocalDateTime.now()
                    + "\","
                    + "\"status\":403,"
                    + "\"error\":\"Forbidden\","
                    + "\"message\":\"You do not have permission to access this resource.\""
                    + "}";

            response.getWriter().write(jsonResponse);
        };
    }
}