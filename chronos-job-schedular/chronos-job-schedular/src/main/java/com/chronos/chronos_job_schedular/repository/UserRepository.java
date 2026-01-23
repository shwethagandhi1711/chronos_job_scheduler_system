package com.chronos.chronos_job_schedular.repository;

import com.chronos.chronos_job_schedular.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * UserRepository
 *
 * Purpose:
 * --------
 * This repository interface is responsible for handling
 * all database interactions related to the User entity.
 *
 * It acts as a Data Access Layer (DAL) between:
 * - Service layer (business logic)
 * - Database layer (tables)
 *
 * Why use JpaRepository?
 * ---------------------
 * JpaRepository provides:
 * - Basic CRUD operations (save, findById, delete, etc.)
 * - Pagination and sorting support
 * - Automatic query generation using method names
 *
 * JpaRepository<User, Long> means:
 * -------------------------------
 * - User → Entity class mapped to the users table
 * - Long → Type of primary key (userId)
 *
 * This repository is commonly used in:
 * -----------------------------------
 * - UserService (registration & validation logic)
 * - AuthService / AuthController (login authentication)
 */

public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * Find a user by email address
     *
     * Why email?
     * ----------
     * - Email is a unique identifier for users
     * - Used as username during login
     *
     * Why return Optional<User>?
     * --------------------------
     * - Prevents NullPointerException
     * - Forces the caller to handle the case
     *   where the user does not exist
     *
     * Common use cases:
     * -----------------
     * 1. Login:
     *    - Verify if a user exists with given email
     * 2. Registration:
     *    - Check for duplicate email before saving
     *
     * Spring Data JPA auto-generates this query:
     * ------------------------------------------
     * SELECT * FROM users WHERE email = ?
     *
     * Example usage:
     * --------------
     * Optional<User> user = userRepository.findByEmail("admin@chronos.com");
     */
    Optional<User> findByEmail(String email);
}
