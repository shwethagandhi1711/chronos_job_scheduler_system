package com.chronos.chronos_job_schedular.exception;

/*
 * =========================================================
 * INVALID CREDENTIALS EXCEPTION
 * =========================================================
 *
 * Purpose:
 * --------
 * This custom runtime exception represents an authentication
 * failure caused by invalid login credentials.
 *
 * It is thrown during the login process when:
 *  - The provided email does not exist in the system
 *  - OR the password does not match the stored (encrypted) password
 *
 * Why use a custom exception?
 * ---------------------------
 * 1️⃣ Improves code readability
 *    - Makes authentication failures explicit
 *
 * 2️⃣ Separates business errors from system errors
 *    - Avoids generic RuntimeException usage
 *
 * 3️⃣ Enables centralized handling
 *    - GlobalExceptionHandler maps this exception to HTTP 401
 *
 * 4️⃣ Enhances security
 *    - Prevents leaking authentication logic or DB details
 *
 * Security Best Practice:
 * -----------------------
 * The SAME error message is returned for:
 *  - Invalid email
 *  - Invalid password
 *
 * This prevents attackers from:
 *  - Enumerating valid email accounts
 *  - Performing credential-stuffing attacks
 *
 * Typical Usage Flow:
 * -------------------
 * AuthController
 *      ↓
 * AuthService
 *      ↓
 * throw new InvalidCredentialsException("Invalid email or password");
 *      ↓
 * GlobalExceptionHandler
 *      ↓
 * HTTP 401 Unauthorized (JSON response)
 *
 * Sample API Response:
 * --------------------
 * {
 *   "message": "Invalid email or password"
 * }
 */

public class InvalidCredentialsException extends RuntimeException {

    /*
     * Constructor
     *
     * @param message:
     * ----------------
     * Custom, user-friendly error message.
     *
     * This message:
     *  - Is passed to the RuntimeException superclass
     *  - Is returned to the client via GlobalExceptionHandler
     *  - Should never expose internal system details
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
