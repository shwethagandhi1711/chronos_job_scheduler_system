package com.chronos.chronos_job_schedular.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
 * JwtAuthenticationFilter
 *
 * Purpose:
 * --------
 * This filter intercepts every incoming HTTP request and checks
 * whether a valid JWT token is present in the Authorization header.
 *
 * If the token is valid:
 * - User is authenticated
 * - User role is extracted
 * - Authentication is stored in SecurityContext
 *
 * If the token is missing or invalid:
 * - Request continues without authentication
 *
 * Why OncePerRequestFilter?
 * ------------------------
 * - Ensures this filter executes only once per request
 * - Prevents duplicate authentication processing
 *
 * This filter works with:
 * ----------------------
 * - JwtUtil (token validation & extraction)
 * - Spring Security filter chain
 * - Role-based authorization (@PreAuthorize, hasRole)
 */

@Component
// Registers this filter as a Spring-managed bean
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /*
     * JwtUtil
     *
     * Utility class responsible for:
     * - Validating JWT signature
     * - Checking token expiration
     * - Extracting email and role from token
     */
    @Autowired
    private JwtUtil jwtUtil;

    /*
     * doFilterInternal()
     *
     * This method is automatically executed by Spring Security
     * for every incoming HTTP request.
     *
     * Parameters:
     * -----------
     * @param request
     *  - Incoming HTTP request
     *
     * @param response
     *  - HTTP response object
     *
     * @param filterChain
     *  - Allows request to continue through remaining filters
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        /*
         * Extract Authorization header from the request
         *
         * Expected format:
         * ----------------
         * Authorization: Bearer <JWT_TOKEN>
         */
        String header = request.getHeader("Authorization");

        /*
         * Check if Authorization header exists
         * and starts with "Bearer "
         */
        if (header != null && header.startsWith("Bearer ")) {

            /*
             * Remove "Bearer " prefix to get actual JWT token
             */
            String token = header.substring(7);

            /*
             * Validate JWT token
             *
             * Validation includes:
             * -------------------
             * - Signature verification
             * - Expiration check
             */
            if (jwtUtil.validateToken(token)) {

                /*
                 * Extract user information from token
                 */
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                /*
                 * Create UsernamePasswordAuthenticationToken
                 *
                 * Parameters:
                 * -----------
                 * - Principal: email (authenticated user identity)
                 * - Credentials: null (password not needed)
                 * - Authorities: role extracted from token
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(() -> role)
                        );

                /*
                 * Attach additional request-related details
                 * (IP address, session ID, etc.)
                 */
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                /*
                 * Store authentication in SecurityContext
                 *
                 * Why this is important?
                 * ----------------------
                 * - Marks the user as authenticated
                 * - Enables role-based access control
                 * - Allows @PreAuthorize and hasRole() to work
                 */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }

        /*
         * Continue execution of remaining filters
         * and eventually reach the controller
         */
        filterChain.doFilter(request, response);
    }
}
