package com.chronos.chronos_job_schedular.service;

import com.chronos.chronos_job_schedular.entity.JobLog;
import com.chronos.chronos_job_schedular.repository.JobLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/*
 * JobLogService
 *
 * Purpose:
 * --------
 * This service class contains business logic related to Job Logs.
 *
 * Responsibilities:
 * -----------------
 * - Fetch job execution logs from database
 * - Validate data before returning to controller
 * - Throw meaningful exceptions when data is missing or invalid
 *
 * This layer acts as:
 * ------------------
 * Controller  →  Service  →  Repository
 */

@Service
// Marks this class as a Spring Service component
public class JobLogService {

    /*
     * JobLogRepository
     *
     * Used to perform database operations on JobLog entity
     */
    @Autowired
    private JobLogRepository jobLogRepository;

    /* =====================================================
     * VIEW ALL JOB LOGS
     * ===================================================== */

    /*
     * Fetches all job execution logs from database
     *
     * @return:
     *  - List of JobLog entries
     *
     * Throws:
     * -------
     * - RuntimeException if no logs exist
     */
    public List<JobLog> getAllLogs() {

        // Fetch all job logs from database
        List<JobLog> logs = jobLogRepository.findAll();

        // If no logs found, throw exception
        if (logs.isEmpty()) {
            throw new RuntimeException("No job logs found");
        }

        // Return logs to controller
        return logs;
    }

    /* =====================================================
     * VIEW JOB LOG BY LOG ID
     * ===================================================== */

    /*
     * Fetch a specific job log using its unique log ID
     *
     * @param logId:
     *  - Primary key of JobLog table
     *
     * @return:
     *  - JobLog object if found
     *
     * Throws:
     * -------
     * - RuntimeException if log ID does not exist
     */
    public JobLog getLogById(Long logId) {

        return jobLogRepository.findById(logId)

                // If log not found, throw custom error message
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job log with ID " + logId + " not found"
                        )
                );
    }

    /* =====================================================
     * VIEW LOGS BY JOB ID
     * ===================================================== */

    /*
     * Fetch all logs related to a specific job
     *
     * @param jobId:
     *  - ID of the job whose logs are required
     *
     * @return:
     *  - List of JobLog entries for the given job
     *
     * Throws:
     * -------
     * - RuntimeException if no logs exist for the job
     */
    public List<JobLog> getLogsByJobId(Long jobId) {

        // Fetch logs for the given job ID
        List<JobLog> logs = jobLogRepository.findAllByJobId(jobId);

        // If no logs found, throw exception
        if (logs.isEmpty()) {
            throw new RuntimeException(
                    "No job logs found for jobId: " + jobId
            );
        }

        return logs;
    }

    /* =====================================================
     * VIEW LOGS BY EXACT START TIME
     * ===================================================== */

    /*
     * Fetch logs that started at a specific time
     *
     * @param startTime:
     *  - Exact job start time
     *
     * @return:
     *  - List of JobLog entries matching start time
     *
     * Throws:
     * -------
     * - RuntimeException if no logs match the given time
     */
    public List<JobLog> getLogsByStartTime(LocalDateTime startTime) {

        // Fetch logs by start time
        List<JobLog> logs = jobLogRepository.findByStartTime(startTime);

        // If no logs found, throw exception
        if (logs.isEmpty()) {
            throw new RuntimeException(
                    "No job logs found for startTime: " + startTime
            );
        }

        return logs;
    }

    /* =====================================================
     * VIEW LOGS BETWEEN TIME RANGE
     * ===================================================== */

    /*
     * Fetch logs whose start time falls between two timestamps
     *
     * @param from:
     *  - Start of time range
     *
     * @param to:
     *  - End of time range
     *
     * @return:
     *  - List of JobLog entries within the time range
     *
     * Validation:
     * -----------
     * - 'from' must be before 'to'
     *
     * Throws:
     * -------
     * - RuntimeException for invalid time range
     * - RuntimeException if no logs found
     */
    public List<JobLog> getLogsBetweenTimes(
            LocalDateTime from,
            LocalDateTime to
    ) {

        // Validate time range
        if (from.isAfter(to)) {
            throw new RuntimeException(
                    "From time must be before To time"
            );
        }

        // Fetch logs between the given time range
        List<JobLog> logs =
                jobLogRepository.findByStartTimeBetween(from, to);

        // If no logs found, throw exception
        if (logs.isEmpty()) {
            throw new RuntimeException(
                    "No job logs found between " + from + " and " + to
            );
        }

        return logs;
    }
}
