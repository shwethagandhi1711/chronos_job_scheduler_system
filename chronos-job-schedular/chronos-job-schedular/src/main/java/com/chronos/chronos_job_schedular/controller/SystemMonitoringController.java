package com.chronos.chronos_job_schedular.controller;

import com.chronos.chronos_job_schedular.service.SystemMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * SystemMonitoringController
 *
 * Purpose:
 * --------
 * This controller exposes a REST API endpoint to monitor
 * system-level status, including Quartz scheduler health
 * and overall system status.
 *
 * Use case:
 * ---------
 * - Admin dashboard
 * - Health check endpoints
 * - Integration with monitoring tools
 *
 * Base URL:
 * ----------
 * /api/monitoring/system
 *
 * Method:
 * -------
 * GET → fetch system status in JSON format
 */

@RestController
// Marks this class as a REST controller that can handle HTTP requests
@RequestMapping("/api/monitoring/system")
// Base URL path for all endpoints in this controller
public class SystemMonitoringController {

    /*
     * Service layer dependency
     *
     * SystemMonitoringService handles the business logic
     * to determine if the scheduler is running and system health.
     */
    @Autowired
    private SystemMonitoringService systemMonitoringService;

    /*
     * GET /api/monitoring/system
     *
     * Purpose:
     * --------
     * Returns system status including scheduler state and general system health.
     *
     * ResponseEntity:
     * ----------------
     * - Wraps the response in HTTP 200 OK
     * - JSON format suitable for dashboards or monitoring tools
     *
     * Sample JSON Response:
     * {
     *   "schedulerRunning": true,
     *   "systemStatus": "UP"
     * }
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        // Call service to fetch system monitoring data
        Map<String, Object> status = systemMonitoringService.getSystemStatus();

        // Return response with HTTP 200 OK
        return ResponseEntity.ok(status);
    }
}
