package com.chronos.chronos_job_schedular.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 * CustomAccessDeniedHandler
 *
 * Purpose:
 * --------
 * This class handles authorization failures in Spring Security.
 * It is triggered when an authenticated user tries to access
 * a resource without sufficient permissions (roles/authorities).
 *
 * Example Scenario:
 * -----------------
 * - User is logged in
 * - User has role USER
 * - User tries to delete a job (ADMIN-only operation)
 * - This handler sends a custom 403 Forbidden response
 *
 * Why create a custom handler?
 * ----------------------------
 * - Provides meaningful and consistent error responses
 * - Avoids default HTML error pages
 * - Suitable for REST APIs (JSON response)
 */

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /*
     * handle()
     *
     * This method is automatically invoked by Spring Security
     * when an AccessDeniedException occurs.
     *
     * Parameters:
     * -----------
     * @param request
     *  - The incoming HTTP request
     *  - Contains request details like URL, headers, etc.
     *
     * @param response
     *  - Used to send a custom HTTP response back to the client
     *
     * @param accessDeniedException
     *  - Thrown when the user lacks required authority/role
     *
     * Throws:
     * -------
     * IOException
     *  - Thrown if writing the response fails
     */

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        /*
         * Set HTTP status code to 403 (Forbidden)
         *
         * Meaning:
         * --------
         * - The user is authenticated
         * - But does not have permission to perform this action
         */
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        /*
         * Set response content type to JSON
         *
         * Reason:
         * -------
         * - REST APIs should return structured JSON responses
         * - Allows frontend clients to parse the message easily
         */
        response.setContentType("application/json");

        /*
         * Write a custom JSON error message to the response body
         *
         * IMPORTANT:
         * ----------
         * - Do not expose sensitive security details
         * - Keep the message clear and user-friendly
         */
        response.getWriter().write("""
            {
              "message": "Access denied. Only ADMIN can delete jobs"
            }
        """);
    }
}
