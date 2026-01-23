package com.chronos.chronos_job_schedular.controller;

import com.chronos.chronos_job_schedular.entity.Job;
import com.chronos.chronos_job_schedular.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/*
 * =========================================================
 * JobController
 * =========================================================
 *
 * PURPOSE:
 * --------
 * Exposes REST APIs to manage Jobs in the Chronos Job Scheduler.
 *
 * Responsibilities:
 * -----------------
 * ✔ Create jobs (Immediate / One-Time / Recurring)
 * ✔ View jobs (All / By ID / By Status / By Type)
 * ✔ Delete jobs (Admin only)
 * ✔ Reschedule jobs with validation
 *
 * This controller interacts ONLY with the service layer.
 */

@RestController
@RequestMapping("/api/jobs")
/*
 * Base URL:
 * ---------
 * All job-related APIs start with:
 *   /api/jobs
 */
public class JobController {

    /*
     * =====================================================
     * DEPENDENCY INJECTION
     * =====================================================
     */

    // Service layer that contains business logic & Quartz interaction
    @Autowired
    private JobService jobService;

    /*
     * =====================================================
     * CREATE JOB API
     * =====================================================
     */

    @PostMapping("/create")
    public ResponseEntity<Job> createJob(@RequestBody Job job) throws Exception {

        /*
         * @RequestBody:
         * --------------
         * Accepts Job details in JSON format from Postman/UI.
         *
         * This may include:
         *  - jobName
         *  - jobType (IMMEDIATE / ONE_TIME / RECURRING)
         *  - startTime
         *  - timeDuration
         *  - recurrencePattern
         */

        // Delegates creation & scheduling logic to service layer
        Job createdJob = jobService.createJob(job);

        // Returns created job with HTTP 200 OK
        return ResponseEntity.ok(createdJob);
    }

    /*
     * =====================================================
     * VIEW ALL JOBS
     * =====================================================
     */

    @GetMapping("/all")
    public ResponseEntity<List<Job>> getAllJobs() {

        /*
         * Fetches all jobs from database
         * including their current status.
         */
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    /*
     * =====================================================
     * VIEW JOB BY ID
     * =====================================================
     */

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJobById(@PathVariable Long jobId) {

        /*
         * @PathVariable:
         * --------------
         * Extracts jobId from URL.
         *
         * Example:
         * GET /api/jobs/10
         */
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    /*
     * =====================================================
     * VIEW JOBS BY STATUS
     * =====================================================
     */

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getJobsByStatus(@PathVariable String status) {

        /*
         * Status Examples:
         * ----------------
         * PENDING
         * RUNNING
         * SUCCESS
         * FAILED
         */
        return ResponseEntity.ok(jobService.getJobsByStatus(status));
    }

    /*
     * =====================================================
     * VIEW JOBS BY TYPE
     * =====================================================
     */

    @GetMapping("/type/{jobType}")
    public ResponseEntity<List<Job>> getJobsByType(@PathVariable String jobType) {

        /*
         * Job Type Examples:
         * ------------------
         * IMMEDIATE
         * ONE_TIME
         * RECURRING
         */
        return ResponseEntity.ok(jobService.getJobsByType(jobType));
    }

    /*
     * =====================================================
     * DELETE JOB (ADMIN ONLY)
     * =====================================================
     */

    @PreAuthorize("hasRole('ADMIN')")
    /*
     * @PreAuthorize:
     * --------------
     * Only users with ADMIN role can delete jobs.
     * Enforced by Spring Security + JWT.
     */
    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<String> deleteJob(@PathVariable Long jobId) throws Exception {

        /*
         * Service layer:
         * --------------
         * ✔ Checks job existence
         * ✔ Prevents deletion of running jobs (if applicable)
         * ✔ Deletes Quartz job + DB record
         */
        jobService.deleteJob(jobId);

        return ResponseEntity.ok("Job deleted successfully");
    }

    /*
     * =====================================================
     * RESCHEDULE JOB API
     * =====================================================
     */

    @PutMapping("/reschedule/{jobId}")
    public ResponseEntity<Job> rescheduleJob(
            @PathVariable Long jobId,
            @RequestBody Map<String, String> payload
    ) throws Exception {

        /*
         * Payload is dynamic:
         * -------------------
         * Client may send:
         *  - startTime
         *  - timeDuration
         *  - recurrencePattern
         *
         * All fields are OPTIONAL.
         */

        // Initialize optional fields as null
        LocalDateTime startTime = null;
        Integer timeDuration = null;
        String recurrencePattern = null;

        /*
         * Parse startTime only if provided
         */
        if (payload.containsKey("startTime") && payload.get("startTime") != null) {
            startTime = LocalDateTime.parse(payload.get("startTime"));
        }

        /*
         * Parse timeDuration only if provided
         */
        if (payload.containsKey("timeDuration") && payload.get("timeDuration") != null) {
            timeDuration = Integer.parseInt(payload.get("timeDuration"));
        }

        /*
         * Parse recurrencePattern only if provided
         */
        if (payload.containsKey("recurrencePattern")) {
            recurrencePattern = payload.get("recurrencePattern");
        }

        /*
         * Delegates full validation & rescheduling logic
         * to service layer:
         *
         * ✔ Immediate jobs cannot be rescheduled
         * ✔ Completed / running jobs validation
         * ✔ Future time validation
         * ✔ Quartz trigger update
         */
        Job updatedJob = jobService.rescheduleJob(
                jobId,
                startTime,
                timeDuration,
                recurrencePattern
        );

        return ResponseEntity.ok(updatedJob);
    }
}
