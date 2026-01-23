package com.chronos.chronos_job_schedular.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * SecurityConfig
 *
 * Purpose:
 * --------
 * Central Spring Security configuration class.
 *
 * Responsibilities:
 * -----------------
 * - Configure authentication and authorization rules
 * - Register JWT authentication filter
 * - Enforce stateless session management
 * - Configure password encoding
 * - Enable role-based and method-level security
 *
 * This class replaces:
 * --------------------
 * - WebSecurityConfigurerAdapter (deprecated)
 */

@Configuration
// Marks this class as a Spring Security configuration component
@EnableMethodSecurity
// Enables method-level security annotations like:
// @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig {

    /*
     * CustomAccessDeniedHandler
     *
     * Purpose:
     * --------
     * Handles 403 (FORBIDDEN) errors when:
     * - User is authenticated
     * - But does not have required role
     *
     * Example:
     * --------
     * EMPLOYEE trying to delete a job (ADMIN-only)
     */
    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    /*
     * JwtAuthenticationFilter
     *
     * Purpose:
     * --------
     * - Intercepts every incoming request
     * - Extracts JWT from Authorization header
     * - Validates token
     * - Sets authentication in SecurityContext
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /*
     * BCryptPasswordEncoder Bean
     *
     * Purpose:
     * --------
     * Encrypts passwords before storing in database
     *
     * Why BCrypt?
     * -----------
     * - One-way hashing algorithm
     * - Adaptive (slow by design)
     * - Protects against brute-force & rainbow table attacks
     *
     * Used in:
     * --------
     * - User registration
     * - Login password verification
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * securityFilterChain()
     *
     * Purpose:
     * --------
     * Defines the complete Spring Security filter configuration.
     *
     * Controls:
     * ---------
     * - CSRF protection
     * - Session policy
     * - Endpoint authorization
     * - Custom filters
     * - Exception handling
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                /*
                 * Disable CSRF protection
                 *
                 * Reason:
                 * -------
                 * - JWT-based authentication is stateless
                 * - No cookies or server-side sessions
                 * - CSRF is only needed for session-based auth
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * Stateless session management
                 *
                 * Meaning:
                 * --------
                 * - Spring Security will NOT create HTTP session
                 * - Every request must carry JWT token
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /*
                 * Authorization rules
                 *
                 * Defines who can access which endpoints
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Public endpoints
                         *
                         * Accessible without authentication:
                         * - User registration
                         * - User login
                         */
                        .requestMatchers("/api/auth/**").permitAll()

                        /*
                         * Admin-only endpoints
                         *
                         * Example:
                         * --------
                         * DELETE job API
                         */
                        .requestMatchers("/api/jobs/delete/**").hasRole("ADMIN")

                        /*
                         * Shared access endpoints
                         *
                         * Accessible by:
                         * - ADMIN
                         * - EMPLOYEE
                         */
                        .requestMatchers("/api/jobs/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        /*
                         * Any other endpoint
                         *
                         * Requires authentication
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * Exception handling configuration
                 *
                 * Handles:
                 * --------
                 * - 403 Forbidden errors
                 * - Role-based access denial
                 */
                .exceptionHandling(ex ->
                        ex.accessDeniedHandler(customAccessDeniedHandler)
                )

                /*
                 * Register JWT Authentication Filter
                 *
                 * IMPORTANT:
                 * ----------
                 * Filter order matters!
                 *
                 * This filter runs BEFORE:
                 * UsernamePasswordAuthenticationFilter
                 *
                 * So JWT validation happens early in the chain
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        /*
         * Build and return the configured security filter chain
         */
        return http.build();
    }
}
