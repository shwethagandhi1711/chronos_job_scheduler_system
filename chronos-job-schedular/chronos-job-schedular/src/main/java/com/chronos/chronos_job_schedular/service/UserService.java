package com.chronos.chronos_job_schedular.service;

import com.chronos.chronos_job_schedular.entity.User;
import com.chronos.chronos_job_schedular.exception.DuplicateEmailException;
import com.chronos.chronos_job_schedular.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/*
 * ============================
 * UserService
 * ============================
 *
 * Purpose:
 * --------
 * This service layer class handles all business logic
 * related to user management operations.
 *
 * It acts as a bridge between:
 * -----------------------------
 * Controller  ➝  Service  ➝  Repository
 *
 * Key Responsibilities:
 * ---------------------
 * ✔ Enforce business rules
 * ✔ Prevent duplicate user registration
 * ✔ Encrypt passwords before persisting
 * ✔ Interact with the repository layer
 *
 * Why Service Layer?
 * ------------------
 * - Keeps controllers thin and clean
 * - Centralizes business logic
 * - Makes code easier to test and maintain
 * - Avoids duplication of logic
 */
@Service   // Marks this class as a Spring-managed service component
public class UserService {

    /*
     * UserRepository
     *
     * Purpose:
     * --------
     * - Provides database access for User entity
     * - Handles CRUD operations on users table
     *
     * Injected by Spring using Dependency Injection
     */
    @Autowired
    private UserRepository userRepository;

    /*
     * BCryptPasswordEncoder
     *
     * Purpose:
     * --------
     * - Hashes passwords using BCrypt algorithm
     * - Adds salt automatically for better security
     * - Protects passwords even if database is compromised
     *
     * NOTE:
     * -----
     * Plain-text passwords are NEVER stored in the database
     */
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /*
     * ============================
     * register()
     * ============================
     *
     * Purpose:
     * --------
     * Registers a new user into the system.
     *
     * Input:
     * ------
     * @param user
     * - User entity containing:
     *   • name
     *   • email
     *   • password (plain text from request)
     *
     * Output:
     * -------
     * @return User
     * - Persisted user object with encrypted password
     *
     * Business Rules Enforced:
     * ------------------------
     * 1️⃣ Email must be unique
     * 2️⃣ Password must be encrypted before saving
     */
    public User register(User user) {

        /*
         * Step 1: Validate email uniqueness
         *
         * - findByEmail() returns Optional<User>
         * - If email already exists, throw custom exception
         * - Prevents duplicate records and DB constraint errors
         */
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException(
                    "Email already exists"
            );
        }

        /*
         * Step 2: Encrypt password
         *
         * - BCrypt automatically applies:
         *   ✔ Salting
         *   ✔ Hashing
         * - Converts plain password into secure hash
         */
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        /*
         * Step 3: Save user to database
         *
         * - Encrypted password is stored
         * - Returns persisted User entity
         */
        return userRepository.save(user);
    }
}
