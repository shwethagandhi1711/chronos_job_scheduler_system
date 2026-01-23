package com.chronos.chronos_job_schedular.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/*
 * JwtUtil
 *
 * Purpose:
 * --------
 * Central utility class for handling JWT (JSON Web Token) operations.
 *
 * Responsibilities:
 * -----------------
 * - Generate JWT token after successful login
 * - Validate incoming JWT tokens
 * - Extract user information (email, role) from token
 *
 * Used By:
 * --------
 * - AuthController → for generating token during login
 * - JwtAuthenticationFilter → for validating and extracting user data
 *
 * Security Note:
 * --------------
 * JWT is stateless → server does NOT store session data.
 * Every request is authenticated using the token itself.
 */

@Component
// Makes this class available as a Spring-managed bean
public class JwtUtil {

    /*
     * SECRET_KEY
     *
     * Purpose:
     * --------
     * Used to digitally sign the JWT token.
     *
     * IMPORTANT RULE:
     * ---------------
     * - HS256 algorithm requires minimum 256-bit key
     * - That means at least 32 characters
     *
     * Production Best Practice:
     * -------------------------
     * - Store in environment variables
     * - Never hard-code secrets in real projects
     */
    private static final String SECRET_KEY =
            "chronos_chronos_chronos_chronos_secret_key_123";

    /*
     * EXPIRATION_TIME
     *
     * Purpose:
     * --------
     * Defines how long the JWT token remains valid.
     *
     * Value:
     * ------
     * 24 hours = 24 × 60 × 60 × 1000 ms
     */
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /*
     * getSigningKey()
     *
     * Purpose:
     * --------
     * Converts SECRET_KEY into a cryptographic Key object.
     *
     * Why required?
     * -------------
     * - JJWT library requires a Key object for signing & verification
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /*
     * generateToken()
     *
     * Purpose:
     * --------
     * Creates a signed JWT token after successful authentication.
     *
     * Parameters:
     * -----------
     * @param email
     *  - Stored as JWT subject (unique user identity)
     *
     * @param role
     *  - Stored as custom claim
     *  - Used later for authorization (ADMIN / USER)
     *
     * Returns:
     * --------
     * - Signed JWT token string
     */
    public String generateToken(String email, String role) {

        return Jwts.builder()

                /*
                 * setSubject()
                 * ------------
                 * Stores primary identity of the user
                 * Example: admin@chronos.com
                 */
                .setSubject(email)

                /*
                 * claim()
                 * -------
                 * Adds custom data to the token
                 * Here we store user role
                 */
                .claim("role", role)

                /*
                 * setIssuedAt()
                 * -------------
                 * Timestamp when token was generated
                 */
                .setIssuedAt(new Date())

                /*
                 * setExpiration()
                 * ----------------
                 * Token becomes invalid after this time
                 */
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )

                /*
                 * signWith()
                 * ----------
                 * Signs token using:
                 * - Secret key
                 * - HS256 algorithm
                 */
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)

                /*
                 * compact()
                 * ----------
                 * Generates final JWT string
                 */
                .compact();
    }

    /*
     * extractEmail()
     *
     * Purpose:
     * --------
     * Extracts email (subject) from JWT token.
     *
     * Used during:
     * -------------
     * - Authentication process
     * - Setting SecurityContext
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /*
     * extractRole()
     *
     * Purpose:
     * --------
     * Extracts user role from JWT token.
     *
     * Used during:
     * -------------
     * - Role-based authorization
     * - hasRole(), @PreAuthorize
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /*
     * validateToken()
     *
     * Purpose:
     * --------
     * Validates whether JWT token is:
     * - Properly signed
     * - Not expired
     *
     * Returns:
     * --------
     * true  → valid token
     * false → invalid / expired / tampered token
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token); // parsing triggers validation
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * extractClaims()
     *
     * Purpose:
     * --------
     * Parses JWT token and extracts all claims.
     *
     * Internal Method:
     * ----------------
     * Used by:
     * - extractEmail()
     * - extractRole()
     * - validateToken()
     *
     * Throws exception if:
     * --------------------
     * - Token is expired
     * - Token signature is invalid
     */
    private Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // verify signature
                .build()
                .parseClaimsJws(token)          // parse JWT
                .getBody();                     // extract payload
    }
}
