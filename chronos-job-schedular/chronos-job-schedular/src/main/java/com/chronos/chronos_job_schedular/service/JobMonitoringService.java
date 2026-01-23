package com.chronos.chronos_job_schedular.service;

import com.chronos.chronos_job_schedular.entity.Job;
import com.chronos.chronos_job_schedular.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * ============================
 * JobMonitoringService
 * ============================
 *
 * This is a SERVICE layer class.
 *
 * Role of this class:
 * -------------------
 * ✔ Acts as a business logic layer
 * ✔ Fetches job data from the database using JobRepository
 * ✔ Processes that data
 * ✔ Returns summarized statistics for monitoring purposes
 *
 * Typical usage:
 * --------------
 * - Admin dashboard
 * - Monitoring REST APIs
 * - Job status overview screen
 */
@Service   // Marks this class as a Spring-managed service component
public class JobMonitoringService {

    /*
     * JobRepository dependency
     * ------------------------
     * This repository communicates with the database.
     *
     * It provides CRUD methods such as:
     * - findAll()
     * - save()
     * - deleteById()
     *
     * Spring automatically injects the implementation
     * using Dependency Injection.
     */
    @Autowired
    private JobRepository jobRepository;

    /*
     * ============================
     * getJobStats()
     * ============================
     *
     * Purpose:
     * --------
     * Collects various job-related statistics from the database.
     *
     * Statistics calculated:
     * ----------------------
     * ✔ Total number of jobs
     * ✔ Jobs grouped by STATUS:
     *      - PENDING
     *      - RUNNING
     *      - SUCCESS
     *      - FAILED
     * ✔ Jobs grouped by TYPE:
     *      - IMMEDIATE
     *      - ONE_TIME
     *      - RECURRING
     *
     * Return Type:
     * ------------
     * Map<String, Object>
     * - Key   → Name of statistic
     * - Value → Count (Long / Integer)
     */
    public Map<String, Object> getJobStats() {

        /*
         * LinkedHashMap is used instead of HashMap
         * because it maintains insertion order.
         *
         * This is useful when displaying data
         * in dashboards or API responses.
         */
        Map<String, Object> stats = new LinkedHashMap<>();

        /*
         * Fetch all job records from the database
         *
         * This executes:
         * SELECT * FROM job;
         */
        List<Job> allJobs = jobRepository.findAll();

        /*
         * Total number of jobs
         */
        stats.put("totalJobs", allJobs.size());

        /*
         * ============================
         * Count jobs by STATUS
         * ============================
         *
         * Java Streams are used here to:
         * - Filter jobs by status
         * - Count matching records
         */

        // Count jobs with status = PENDING
        stats.put("pending",
                allJobs.stream()
                        .filter(j -> "PENDING".equalsIgnoreCase(j.getStatus()))
                        .count());

        // Count jobs with status = RUNNING
        stats.put("running",
                allJobs.stream()
                        .filter(j -> "RUNNING".equalsIgnoreCase(j.getStatus()))
                        .count());

        // Count jobs with status = SUCCESS
        stats.put("success",
                allJobs.stream()
                        .filter(j -> "SUCCESS".equalsIgnoreCase(j.getStatus()))
                        .count());

        // Count jobs with status = FAILED
        stats.put("failed",
                allJobs.stream()
                        .filter(j -> "FAILED".equalsIgnoreCase(j.getStatus()))
                        .count());

        /*
         * ============================
         * Count jobs by JOB TYPE
         * ============================
         *
         * Job types indicate how the job is scheduled.
         */

        // Jobs that run immediately
        stats.put("immediateJobs",
                allJobs.stream()
                        .filter(j -> "IMMEDIATE".equalsIgnoreCase(j.getJobType()))
                        .count());

        // Jobs scheduled to run once at a specific time
        stats.put("oneTimeJobs",
                allJobs.stream()
                        .filter(j -> "ONE_TIME".equalsIgnoreCase(j.getJobType()))
                        .count());

        // Jobs that run repeatedly based on a cron expression
        stats.put("recurringJobs",
                allJobs.stream()
                        .filter(j -> "RECURRING".equalsIgnoreCase(j.getJobType()))
                        .count());

        /*
         * Return the final statistics map
         *
         * This map can be:
         * - Returned directly from a REST controller
         * - Used to build dashboards
         * - Logged or monitored
         */
        return stats;
    }
}
