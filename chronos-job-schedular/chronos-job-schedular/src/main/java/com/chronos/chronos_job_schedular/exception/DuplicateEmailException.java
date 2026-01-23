package com.chronos.chronos_job_schedular.exception;

/*
 * ================================
 * DUPLICATE EMAIL EXCEPTION
 * ================================
 *
 * Purpose:
 * --------
 * This is a custom runtime exception.
 * It is thrown when a user tries to register
 * using an email address that already exists
 * in the database.
 *
 * Why create a custom exception?
 * ------------------------------
 * 1️⃣ Improves code readability
 *    - Clearly tells what went wrong
 *
 * 2️⃣ Separates business logic errors
 *    - Avoids exposing database / SQL errors
 *
 * 3️⃣ Works cleanly with GlobalExceptionHandler
 *    - Allows sending meaningful error messages
 *      to the client (Postman / UI)
 *
 * 4️⃣ Interview-friendly design
 *    - Shows understanding of exception hierarchy
 *
 * Example Scenario:
 * -----------------
 * Existing email in DB:
 *   admin@chronos.com
 *
 * User tries to register again with same email →
 * DuplicateEmailException is thrown
 *
 * Client Response:
 * ----------------
 * HTTP Status: 400 (Bad Request)
 * Message: "Email already exists"
 */

public class DuplicateEmailException extends RuntimeException {

    /*
     * ================================
     * CONSTRUCTOR
     * ================================
     *
     * @param message
     *  - Custom error message explaining the issue
     *  - This message is passed to the parent
     *    RuntimeException class
     *
     * Why RuntimeException?
     * ---------------------
     * - Unchecked exception
     * - No need to explicitly catch or declare
     * - Ideal for business rule violations
     */
    public DuplicateEmailException(String message) {
        super(message);
        // Calls RuntimeException constructor
        // Stores the message internally
    }
}
