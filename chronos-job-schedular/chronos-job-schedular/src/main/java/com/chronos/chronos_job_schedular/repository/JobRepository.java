package com.chronos.chronos_job_schedular.repository;

import com.chronos.chronos_job_schedular.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * JobRepository
 *
 * Purpose:
 * --------
 * This repository interface handles all database operations
 * related to the Job entity.
 *
 * Why use JpaRepository?
 * ---------------------
 * - Provides built-in CRUD operations (save, findById, delete, etc.)
 * - Eliminates the need to write boilerplate DAO code
 * - Integrates automatically with Hibernate and JPA
 *
 * This repository is used by:
 * ---------------------------
 * - JobService (business logic layer)
 * - Scheduler/Controller layers to fetch and manage jobs
 */

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    /*
     * Find jobs by status (case-insensitive)
     *
     * Purpose:
     * --------
     * Retrieves all jobs that match the given status
     * ignoring letter case.
     *
     * Example statuses:
     * -----------------
     * - PENDING
     * - RUNNING
     * - SUCCESS
     * - FAILED
     *
     * Why IgnoreCase?
     * ---------------
     * - Prevents issues due to case mismatches
     * - Allows flexible queries (e.g., "pending", "PENDING")
     *
     * Example:
     * --------
     * jobRepository.findByStatusIgnoreCase("pending");
     */
    List<Job> findByStatusIgnoreCase(String status);

    /*
     * Find jobs by job type (case-insensitive)
     *
     * Purpose:
     * --------
     * Fetches all jobs based on their execution type.
     *
     * Common job types:
     * -----------------
     * - ONE_TIME
     * - RECURRING
     *
     * Why IgnoreCase?
     * ---------------
     * - Ensures consistent query behavior
     * - Avoids database case-sensitivity issues
     *
     * Example:
     * --------
     * jobRepository.findByJobTypeIgnoreCase("recurring");
     */
    List<Job> findByJobTypeIgnoreCase(String jobType);
}
