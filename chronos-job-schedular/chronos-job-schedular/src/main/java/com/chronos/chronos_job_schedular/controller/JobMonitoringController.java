package com.chronos.chronos_job_schedular.controller;

import com.chronos.chronos_job_schedular.service.JobMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * =========================================================
 * JobMonitoringController
 * =========================================================
 *
 * ROLE:
 * -----
 * Acts as a MONITORING API for the job scheduling system.
 *
 * This controller provides aggregated job statistics instead
 * of individual job details.
 *
 * Typical consumers:
 * ------------------
 * ✔ Admin dashboards
 * ✔ Monitoring tools
 * ✔ DevOps / SRE teams
 * ✔ Health & analytics views
 *
 * This follows READ-ONLY design (no create/update/delete).
 */

@RestController
/*
 * @RestController:
 * ----------------
 * Combination of:
 *   @Controller + @ResponseBody
 *
 * - Indicates this class handles HTTP requests
 * - Automatically converts Java objects to JSON responses
 */
@RequestMapping("/api/monitoring/jobs")
/*
 * Base API Path:
 * --------------
 * All monitoring-related job endpoints start with:
 *   /api/monitoring/jobs
 */
public class JobMonitoringController {

    /*
     * =====================================================
     * DEPENDENCY INJECTION
     * =====================================================
     */

    @Autowired
    /*
     * @Autowired:
     * ------------
     * Injects JobMonitoringService bean managed by Spring.
     *
     * Service layer responsibilities:
     * --------------------------------
     * ✔ Fetch job data from database
     * ✔ Aggregate counts
     * ✔ Apply business rules
     */
    private JobMonitoringService jobMonitoringService;

    /*
     * =====================================================
     * GET JOB STATISTICS
     * =====================================================
     */

    @GetMapping
    /*
     * @GetMapping:
     * ------------
     * Handles HTTP GET requests
     *
     * Full endpoint:
     * --------------
     * GET /api/monitoring/jobs
     */
    public ResponseEntity<Map<String, Object>> getJobStats() {

        /*
         * Step 1:
         * -------
         * Delegate the logic to service layer.
         *
         * Service computes:
         * -----------------
         * ✔ Total job count
         * ✔ Status-wise counts (PENDING, RUNNING, SUCCESS, FAILED)
         * ✔ Job type counts (IMMEDIATE, ONE_TIME, RECURRING)
         */
        Map<String, Object> stats = jobMonitoringService.getJobStats();

        /*
         * Step 2:
         * -------
         * Wrap the response inside ResponseEntity
         *
         * Benefits of ResponseEntity:
         * ---------------------------
         * ✔ Control HTTP status code
         * ✔ Add headers if needed
         * ✔ Clean REST response handling
         *
         * Returns:
         * --------
         * HTTP 200 OK
         * JSON response body containing statistics
         */
        return ResponseEntity.ok(stats);
    }
}
