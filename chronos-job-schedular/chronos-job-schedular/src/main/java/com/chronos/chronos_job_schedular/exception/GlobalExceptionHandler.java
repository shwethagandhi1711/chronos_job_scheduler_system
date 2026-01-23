package com.chronos.chronos_job_schedular.exception;

import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*
 * =========================================================
 * GLOBAL EXCEPTION HANDLER
 * =========================================================
 *
 * Purpose:
 * --------
 * This class provides centralized exception handling
 * for the entire application.
 *
 * Any exception thrown from:
 *  - Controller
 *  - Service
 *  - Validation layer
 *
 * will be intercepted here and converted into
 * a clean HTTP response.
 *
 * Why use @RestControllerAdvice?
 * ------------------------------
 * 1️⃣ Centralized error handling
 *    - No need for try-catch in controllers
 *
 * 2️⃣ Consistent API error responses
 *    - Same structure & HTTP codes everywhere
 *
 * 3️⃣ Security
 *    - Prevents exposing stack traces or DB errors
 *
 * 4️⃣ Clean controller code
 *    - Business logic only, no error clutter
 *
 * IMPORTANT:
 * ----------
 * - Full stack trace is logged on server (IntelliJ)
 * - Only user-friendly messages are sent to client
 */

@RestControllerAdvice
// Applies this exception handler to ALL @RestController classes
public class GlobalExceptionHandler {

    // =========================================================
    // 1️⃣ FIELD VALIDATION ERRORS (@Valid)
    // =========================================================

    /*
     * Handles validation errors triggered by @Valid annotation
     *
     * This exception is thrown automatically by Spring when:
     *  - DTO validation fails
     *  - Constraints like @NotBlank, @Email are violated
     *
     * Used for:
     *  - RegisterRequest
     *  - LoginRequest
     *
     * Example Input Error:
     * --------------------
     * {
     *   "email": "",
     *   "password": ""
     * }
     *
     * Example Response:
     * -----------------
     * {
     *   "email": "Email is required",
     *   "password": "Password is required"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        /*
         * Map to store validation errors
         * Key   -> Field name
         * Value -> Validation message
         */
        Map<String, String> errors = new HashMap<>();

        /*
         * Loop through all validation errors
         * collected during request binding
         */
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {

            // Example:
            // email -> "Invalid email format"
            errors.put(error.getField(), error.getDefaultMessage());
        }

        /*
         * Return:
         *  - HTTP 400 (Bad Request)
         *  - Field-specific validation messages
         */
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // =========================================================
    // 2️⃣ DUPLICATE EMAIL EXCEPTION
    // =========================================================

    /*
     * Handles DuplicateEmailException
     *
     * Thrown when:
     *  - User tries to register with an email
     *    that already exists in DB
     *
     * Prevents exposing:
     *  - SQLIntegrityConstraintViolationException
     *  - Database-specific error details
     *
     * Example Response:
     * -----------------
     * {
     *   "message": "Email already exists"
     * }
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(
            DuplicateEmailException ex) {

        // Response body
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        // Return HTTP 400 (Bad Request)
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // =========================================================
    // 3️⃣ INVALID LOGIN CREDENTIALS
    // =========================================================

    /*
     * Handles InvalidCredentialsException
     *
     * Thrown when:
     *  - Email does not exist
     *  - Password does not match
     *
     * Security Best Practice:
     * -----------------------
     * - Same error message for both cases
     * - Prevents attackers from guessing valid emails
     *
     * Example Response:
     * -----------------
     * {
     *   "message": "Invalid email or password"
     * }
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        // Authentication error response
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        // Return HTTP 401 (Unauthorized)
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // =========================================================
    // 4️⃣ GENERIC RUNTIME EXCEPTION (FALLBACK)
    // =========================================================

    /*
     * Handles all uncaught RuntimeExceptions
     *
     * Acts as a safety net for:
     *  - Unexpected business logic failures
     *  - NullPointerException
     *  - IllegalStateException
     *
     * NOTE:
     * -----
     * - Should be placed LAST
     * - More specific handlers are matched first
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException ex) {

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        // Return HTTP 400 (Bad Request)
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
