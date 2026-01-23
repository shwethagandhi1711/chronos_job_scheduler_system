package com.chronos.chronos_job_schedular.service;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * ============================
 * SystemMonitoringService
 * ============================
 *
 * Role:
 * -----
 * This service is responsible for providing
 * system-level health and monitoring information.
 *
 * Current Responsibilities:
 * --------------------------
 * ✔ Check whether Quartz Scheduler is running
 * ✔ Return overall system status
 *
 * Typical Usage:
 * --------------
 * - Admin dashboard
 * - System health APIs
 * - Monitoring endpoints
 *
 * Future Enhancements:
 * --------------------
 * - Database connectivity check
 * - Disk space usage
 * - Memory / CPU usage
 * - External service health
 */
@Service   // Marks this class as a Spring-managed service component
public class SystemMonitoringService {

    /*
     * Quartz Scheduler Bean
     *
     * Purpose:
     * --------
     * - Quartz Scheduler is the core engine that executes jobs
     * - Injected here to check its runtime status
     *
     * Spring automatically injects the Scheduler bean
     * configured in QuartzConfig
     */
    @Autowired
    private Scheduler scheduler;

    /*
     * ============================
     * getSystemStatus()
     * ============================
     *
     * Purpose:
     * --------
     * Collects and returns system-level status information.
     *
     * Information Provided:
     * ---------------------
     * ✔ Whether the Quartz Scheduler is running
     * ✔ Overall system status (UP / DOWN)
     *
     * Return Type:
     * ------------
     * Map<String, Object>
     *
     * Example Response:
     * -----------------
     * {
     *   "schedulerRunning": true,
     *   "systemStatus": "UP"
     * }
     */
    public Map<String, Object> getSystemStatus() {

        /*
         * LinkedHashMap is used to:
         * - Maintain insertion order
         * - Produce cleaner, predictable JSON output
         */
        Map<String, Object> status = new LinkedHashMap<>();

        /*
         * Variable to store scheduler running state
         * Default is false for safety
         */
        boolean schedulerRunning = false;

        try {
            /*
             * Check if Quartz Scheduler has started
             *
             * isStarted():
             * - Returns true if scheduler has been started
             * - Does NOT mean jobs are currently executing
             */
            schedulerRunning = scheduler.isStarted();

        } catch (Exception e) {
            /*
             * If any exception occurs:
             * - Scheduler may not be initialized
             * - Scheduler may be shut down
             *
             * In such cases, treat scheduler as NOT running
             */
            schedulerRunning = false;
        }

        /*
         * Add scheduler status to response map
         */
        status.put("schedulerRunning", schedulerRunning);

        /*
         * Add overall system status
         *
         * Currently hardcoded as "UP"
         * This can be enhanced later based on:
         * - Scheduler status
         * - Database health
         * - Other infrastructure checks
         */
        status.put("systemStatus", "UP");

        /*
         * Return final system status map
         */
        return status;
    }
}
