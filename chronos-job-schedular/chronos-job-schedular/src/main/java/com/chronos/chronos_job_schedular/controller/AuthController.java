package com.chronos.chronos_job_schedular.controller;

import com.chronos.chronos_job_schedular.dto.*;
import com.chronos.chronos_job_schedular.entity.User;
import com.chronos.chronos_job_schedular.exception.InvalidCredentialsException;
import com.chronos.chronos_job_schedular.repository.UserRepository;
import com.chronos.chronos_job_schedular.security.JwtUtil;
import com.chronos.chronos_job_schedular.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/*
 * =========================================================
 * AuthController
 * =========================================================
 *
 * PURPOSE:
 * --------
 * Handles USER AUTHENTICATION and AUTHORIZATION.
 *
 * Responsibilities:
 * -----------------
 * ✔ User Registration
 * ✔ User Login
 * ✔ Password Validation
 * ✔ JWT Token Generation
 *
 * This controller is the ENTRY POINT for all auth-related APIs.
 */

@RestController
@RequestMapping("/api/auth")
/*
 * Base URL:
 * ---------
 * All authentication APIs will start with:
 *   /api/auth
 *
 * Examples:
 *   POST /api/auth/register
 *   POST /api/auth/login
 */
public class AuthController {

    /*
     * =====================================================
     * DEPENDENCY INJECTION
     * =====================================================
     */

    // Handles business logic like duplicate email check, user save, etc.
    @Autowired
    private UserService userService;

    // Used to fetch user details directly from database
    @Autowired
    private UserRepository userRepository;

    // Encrypts passwords and validates encrypted passwords
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Utility class to generate and validate JWT tokens
    @Autowired
    private JwtUtil jwtUtil;

    /*
     * =====================================================
     * REGISTER API
     * =====================================================
     */

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {

        /*
         * @RequestBody:
         * --------------
         * Accepts JSON input from Postman / Frontend.
         *
         * @Valid:
         * -------
         * Triggers validation annotations in RegisterRequest.
         * Example:
         *  - @NotNull
         *  - @Email
         *  - @NotBlank
         *
         * If validation fails → Spring returns 400 Bad Request
         */

        // Create a new User entity
        User user = new User();

        // Map DTO values to Entity
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        /*
         * IMPORTANT:
         * ----------
         * Password encryption is usually done in the service layer
         * using BCryptPasswordEncoder before saving to DB.
         */
        user.setPassword(request.getPassword());

        // Assign role (ADMIN / EMPLOYEE)
        user.setRole(request.getRole());

        /*
         * Service Layer Responsibilities:
         * --------------------------------
         * ✔ Check if email already exists
         * ✔ Encrypt password
         * ✔ Save user to database
         * ✔ Throw exception if duplicate email found
         */
        userService.register(user);

        // Success response sent to Postman
        return "User registered successfully";
    }

    /*
     * =====================================================
     * LOGIN API
     * =====================================================
     */

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {

        /*
         * @Valid:
         * -------
         * Ensures email and password are present.
         * If missing → validation error returned to Postman.
         */

        // Fetch user by email from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        /*
                         * Thrown when email does not exist.
                         * Same message is used to avoid revealing
                         * whether email or password is incorrect.
                         */
                        new InvalidCredentialsException("Invalid email or password")
                );

        /*
         * PASSWORD VALIDATION:
         * --------------------
         * Compares:
         *  - Raw password from request
         *  - Encrypted password from database
         */
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        /*
         * JWT TOKEN GENERATION:
         * --------------------
         * Token contains:
         *  ✔ Email
         *  ✔ Role (ADMIN / EMPLOYEE)
         *
         * This token is used in:
         * Authorization header for secured APIs
         */
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        /*
         * Return token wrapped inside AuthResponse DTO
         * Example response:
         * {
         *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
         * }
         */
        return new AuthResponse(token);
    }
}
