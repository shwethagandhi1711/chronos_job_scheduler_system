package com.chronos.chronos_job_schedular.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/*
 * =========================================================
 * RegisterRequest (Data Transfer Object)
 * =========================================================
 *
 * ROLE:
 * -----
 * RegisterRequest DTO is used to RECEIVE user registration
 * details from the client (Postman / Frontend).
 *
 * This DTO acts as the REQUEST body for:
 *   POST /api/auth/register
 *
 * WHY USE A DTO?
 * --------------
 * ✔ Prevents exposing internal User entity
 * ✔ Enables input validation at API boundary
 * ✔ Improves security and maintainability
 * ✔ Keeps controller and service layers clean
 *
 * INTEGRATION:
 * ------------
 * ✔ Used with @Valid annotation in AuthController
 * ✔ Validation errors handled via GlobalExceptionHandler
 *
 * SAMPLE REQUEST JSON:
 * --------------------
 * {
 *   "name": "Shwetha T",
 *   "email": "shwetha@example.com",
 *   "password": "password123",
 *   "role": "ROLE_ADMIN"
 * }
 */

@Getter
@Setter
/*
 * Lombok Annotations:
 * ------------------
 * @Getter → Automatically generates getter methods
 * @Setter → Automatically generates setter methods
 *
 * BENEFITS:
 * ---------
 * ✔ Reduces boilerplate code
 * ✔ Improves readability
 * ✔ Keeps DTO concise
 */
public class RegisterRequest {

    /*
     * =====================================================
     * NAME FIELD
     * =====================================================
     */

    /*
     * User full name
     *
     * @NotBlank:
     * ----------
     * ✔ Ensures name is not null
     * ✔ Prevents empty string ""
     * ✔ Prevents whitespace-only values "   "
     *
     * VALIDATION FLOW:
     * ----------------
     * - Triggered by @Valid in controller
     * - On failure → 400 BAD REQUEST
     * - Error message returned to Postman
     */
    @NotBlank(message = "Name is required")
    private String name;

    /*
     * =====================================================
     * EMAIL FIELD
     * =====================================================
     */

    /*
     * User email address
     *
     * @Email:
     * -------
     * ✔ Validates email format
     * ✔ Rejects invalid patterns (abc@, 123)
     *
     * @NotBlank:
     * ----------
     * ✔ Prevents null or empty values
     *
     * BUSINESS RULES:
     * ----------------
     * ✔ Email must be UNIQUE
     * ✔ Duplicate emails checked in service layer
     *
     * ERROR HANDLING:
     * ---------------
     * ✔ Validation errors → 400 BAD REQUEST
     * ✔ Duplicate email → Custom exception
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    /*
     * =====================================================
     * PASSWORD FIELD
     * =====================================================
     */

    /*
     * User password (RAW password from client)
     *
     * @NotBlank:
     * ----------
     * ✔ Ensures password is provided
     *
     * SECURITY FLOW:
     * ---------------
     * 1️⃣ Raw password received from client
     * 2️⃣ Encrypted using BCryptPasswordEncoder
     * 3️⃣ Encrypted password stored in database
     *
     * IMPORTANT:
     * ----------
     * ❌ Plain text password is NEVER stored
     * ❌ Password is NEVER returned in responses
     */
    @NotBlank(message = "Password is required")
    private String password;

    /*
     * =====================================================
     * ROLE FIELD
     * =====================================================
     */

    /*
     * User role
     *
     * @NotBlank:
     * ----------
     * ✔ Role must be provided
     *
     * EXPECTED VALUES:
     * ----------------
     * ✔ ROLE_ADMIN
     * ✔ ROLE_EMPLOYEE
     *
     * PURPOSE:
     * --------
     * ✔ Used by Spring Security for authorization
     * ✔ Controls access to secured APIs
     *
     * EXAMPLES:
     * ---------
     * - ROLE_ADMIN → Can delete jobs
     * - ROLE_EMPLOYEE → Read-only access
     */
    @NotBlank(message = "Role is required")
    private String role;
}
