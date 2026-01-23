package com.chronos.chronos_job_schedular.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/*
 * ============================
 * USER ENTITY
 * ============================
 *
 * Purpose:
 * --------
 * This class represents the "users" table in the database.
 *
 * It stores:
 *  - User authentication details (email, password)
 *  - Authorization details (role)
 *
 * Used in:
 *  - Login & registration
 *  - JWT authentication
 *  - Role-based access control (RBAC)
 *  - Hibernate / JPA ORM mapping
 */

@Entity
// Marks this class as a JPA entity so Hibernate maps it to a database table

@Table(
        name = "users",
        // Specifies the database table name as "users"

        indexes = {
                /*
                 * Index on email column
                 *
                 * Why?
                 * - Email is frequently used for login
                 * - Improves query performance
                 * - Enforces uniqueness at DB level
                 */
                @Index(name = "idx_email", columnList = "email", unique = true)
        }
)
@Getter
@Setter
// Lombok automatically generates getters and setters for all fields

@NoArgsConstructor
// Required by JPA (Hibernate uses it internally)

@AllArgsConstructor
// Generates a constructor with all fields (useful for testing / DTO mapping)
public class User {

    /* ============================
     * PRIMARY KEY
     * ============================
     */

    /*
     * Unique identifier for each user
     *
     * @Id:
     *  - Marks this field as primary key
     *
     * @GeneratedValue:
     *  - Database auto-generates value
     *  - IDENTITY = AUTO_INCREMENT (MySQL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ============================
     * USER NAME
     * ============================
     */

    /*
     * Name of the user
     *
     * @NotBlank:
     *  - Prevents null, empty, or whitespace-only values
     *
     * Used for:
     *  - Display in UI
     *  - Profile information
     */
    @NotBlank(message = "Name is required")
    private String name;

    /* ============================
     * USER EMAIL
     * ============================
     */

    /*
     * Email address of the user
     *
     * @Email:
     *  - Validates proper email format
     *
     * @NotBlank:
     *  - Email must be provided
     *
     * @Column(unique = true):
     *  - Ensures no duplicate emails in DB
     *
     * Used as:
     *  - Login username
     *  - JWT subject
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    /* ============================
     * USER PASSWORD
     * ============================
     */

    /*
     * Encrypted password
     *
     * @NotBlank:
     *  - Password cannot be empty
     *
     * SECURITY NOTE:
     *  - Password is stored as BCrypt hash
     *  - Plain text passwords are NEVER saved
     */
    @NotBlank(message = "Password is required")
    private String password;

    /* ============================
     * USER ROLE
     * ============================
     */

    /*
     * Role assigned to the user
     *
     * @NotBlank:
     *  - Role must be present
     *
     * Common values:
     *  - ROLE_ADMIN
     *  - ROLE_EMPLOYEE
     *
     * Used for:
     *  - Spring Security authorization
     *  - Access control to APIs
     */
    @NotBlank(message = "Role is required")
    @Column(nullable = false)
    private String role;
}
