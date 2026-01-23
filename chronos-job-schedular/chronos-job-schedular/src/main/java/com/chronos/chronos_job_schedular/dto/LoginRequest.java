package com.chronos.chronos_job_schedular.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/*
 * =========================================================
 * LoginRequest (Data Transfer Object)
 * =========================================================
 *
 * ROLE:
 * -----
 * LoginRequest DTO is used to RECEIVE login credentials
 * (email and password) from the client (Postman / Frontend).
 *
 * It represents the REQUEST body for the login API:
 *   POST /api/auth/login
 *
 * WHY USE A DTO?
 * --------------
 * ✔ Prevents exposing User entity directly
 * ✔ Allows validation at API boundary
 * ✔ Keeps controller logic clean
 * ✔ Protects internal database structure
 *
 * INTEGRATION:
 * ------------
 * ✔ Works with @Valid annotation in controller
 * ✔ Validation errors handled by GlobalExceptionHandler
 *
 * SAMPLE REQUEST JSON:
 * --------------------
 * {
 *   "email": "user@example.com",
 *   "password": "password123"
 * }
 */

@Getter
@Setter
/*
 * Lombok Annotations:
 * ------------------
 * @Getter → generates getter methods for all fields
 * @Setter → generates setter methods for all fields
 *
 * Benefits:
 * ---------
 * ✔ Reduces boilerplate code
 * ✔ Improves readability
 * ✔ Keeps DTO concise
 */
public class LoginRequest {

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
     * ✔ Validates email format (RFC compliant)
     * ✔ Prevents invalid values like "abc@", "123"
     *
     * @NotBlank:
     * ----------
     * ✔ Prevents null
     * ✔ Prevents empty string ""
     * ✔ Prevents whitespace-only values "   "
     *
     * VALIDATION FLOW:
     * ----------------
     * - Triggered by @Valid in controller
     * - If invalid → 400 BAD REQUEST
     * - Error message returned to client
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
     * ✔ Ensures password is not null
     * ✔ Prevents empty or whitespace-only passwords
     *
     * SECURITY NOTES:
     * ---------------
     * ✔ Password is received in raw form
     * ✔ NEVER stored directly in database
     * ✔ Validated using BCryptPasswordEncoder
     *
     * AUTH FLOW:
     * ----------
     * 1️⃣ Client sends raw password
     * 2️⃣ Controller compares using BCrypt
     * 3️⃣ JWT token generated if valid
     */
    @NotBlank(message = "Password is required")
    private String password;
}
