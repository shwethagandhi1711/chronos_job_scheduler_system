package com.chronos.chronos_job_schedular.controller;

import com.chronos.chronos_job_schedular.entity.JobLog;
import com.chronos.chronos_job_schedular.service.JobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/*
 * =========================================================
 * JobLogController
 * =========================================================
 *
 * PURPOSE:
 * --------
 * Exposes REST APIs to VIEW execution logs of jobs.
 *
 * Job logs store execution history such as:
 * ----------------------------------------
 * ✔ Job start time
 * ✔ Job end time
 * ✔ Job status (SUCCESS / FAILED)
 * ✔ Next execution time (for recurring jobs)
 * ✔ Log file path
 *
 * IMPORTANT:
 * ----------
 * - Logs are READ-ONLY
 * - No CREATE / UPDATE / DELETE operations
 * - Only VIEW operations are allowed
 */

@RestController
@RequestMapping("/api/job-logs")
/*
 * Base URL:
 * ---------
 * All job log related APIs start with:
 *   /api/job-logs
 */
public class JobLogController {

    /*
     * =====================================================
     * DEPENDENCY INJECTION
     * =====================================================
     */

    // Service layer that contains business logic for fetching logs
    @Autowired
    private JobLogService jobLogService;

    /*
     * =====================================================
     * VIEW ALL JOB LOGS
     * =====================================================
     */

    @GetMapping
    public ResponseEntity<List<JobLog>> getAllLogs() {

        /*
         * Fetches ALL job execution logs from database.
         *
         * Use cases:
         * ----------
         * ✔ Admin dashboard
         * ✔ Audit history
         * ✔ Monitoring job executions
         *
         * If no logs exist:
         * -----------------
         * Service layer throws appropriate exception
         */
        return ResponseEntity.ok(jobLogService.getAllLogs());
    }

    /*
     * =====================================================
     * VIEW LOG BY LOG ID
     * =====================================================
     */

    @GetMapping("/{logId}")
    public ResponseEntity<JobLog> getLogById(
            @PathVariable Long logId
    ) {

        /*
         * @PathVariable:
         * --------------
         * Extracts logId from URL
         *
         * Example:
         * GET /api/job-logs/15
         *
         * Use case:
         * ---------
         * ✔ View detailed execution info of a single run
         */
        return ResponseEntity.ok(jobLogService.getLogById(logId));
    }

    /*
     * =====================================================
     * VIEW LOGS BY JOB ID
     * =====================================================
     */

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobLog>> getLogsByJobId(
            @PathVariable Long jobId
    ) {

        /*
         * Returns ALL execution logs for a given job.
         *
         * Example:
         * GET /api/job-logs/job/10
         *
         * Useful for:
         * ------------
         * ✔ Tracking execution history of a job
         * ✔ Debugging failures
         * ✔ Retry analysis
         */
        return ResponseEntity.ok(jobLogService.getLogsByJobId(jobId));
    }

    /*
     * =====================================================
     * VIEW LOGS BY EXACT START TIME
     * =====================================================
     */

    @GetMapping("/start-time")
    public ResponseEntity<List<JobLog>> getLogsByStartTime(
            @RequestParam String startTime
    ) {

        /*
         * @RequestParam:
         * --------------
         * Accepts startTime as query parameter
         *
         * Example:
         * GET /api/job-logs/start-time?startTime=2026-01-21T10:30:00
         *
         * Converts String → LocalDateTime
         */
        return ResponseEntity.ok(
                jobLogService.getLogsByStartTime(
                        LocalDateTime.parse(startTime)
                )
        );
    }

    /*
     * =====================================================
     * VIEW LOGS BETWEEN A TIME RANGE
     * =====================================================
     */

    @GetMapping("/between")
    public ResponseEntity<List<JobLog>> getLogsBetweenTimes(
            @RequestParam String from,
            @RequestParam String to
    ) {

        /*
         * @RequestParam:
         * --------------
         * Accepts two timestamps:
         *   - from → start boundary
         *   - to   → end boundary
         *
         * Example:
         * GET /api/job-logs/between
         *     ?from=2026-01-20T00:00:00
         *     &to=2026-01-21T23:59:59
         *
         * Use cases:
         * ----------
         * ✔ Daily / weekly reports
         * ✔ Failure analysis
         * ✔ Audit queries
         */
        return ResponseEntity.ok(
                jobLogService.getLogsBetweenTimes(
                        LocalDateTime.parse(from),
                        LocalDateTime.parse(to)
                )
        );
    }
}
