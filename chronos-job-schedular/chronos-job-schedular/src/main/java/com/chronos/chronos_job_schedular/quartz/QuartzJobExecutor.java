package com.chronos.chronos_job_schedular.quartz;

import com.chronos.chronos_job_schedular.entity.Job;
import com.chronos.chronos_job_schedular.entity.JobLog;
import com.chronos.chronos_job_schedular.repository.JobLogRepository;
import com.chronos.chronos_job_schedular.repository.JobRepository;
import com.chronos.chronos_job_schedular.service.JobService;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;

/*
 * =========================================================
 * QuartzJobExecutor
 * =========================================================
 *
 * Purpose:
 * --------
 * This class represents the ACTUAL JOB EXECUTION logic
 * triggered by the Quartz Scheduler.
 *
 * Quartz calls this class automatically when:
 *  - A trigger fires
 *  - A job reaches its scheduled execution time
 *
 * Responsibilities:
 * -----------------
 * ✔ Fetch job details from database
 * ✔ Update job status (RUNNING → SUCCESS / FAILED)
 * ✔ Simulate job execution for configured duration
 * ✔ Write execution logs into a file
 * ✔ Persist execution history into JobLog table
 * ✔ Handle failures and trigger retry mechanism
 *
 * This class implements:
 * ----------------------
 * org.quartz.Job → required by Quartz framework
 */

@Component
// Registers this class as a Spring-managed bean
public class QuartzJobExecutor implements org.quartz.Job {

    /*
     * Directory where job execution log files are stored
     *
     * Example:
     * joblogfile/job_1_2026-01-20T10-00-00.log
     */
    private static final String LOG_DIR = "joblogfile";

    /*
     * Repository used to fetch and update Job entity
     */
    @Autowired
    private JobRepository jobRepository;

    /*
     * Repository used to store execution history (JobLog)
     */
    @Autowired
    private JobLogRepository jobLogRepository;

    /*
     * Service responsible for retry logic and rescheduling
     */
    @Autowired
    private JobService jobService;

    /*
     * =====================================================
     * Quartz execute() method
     * =====================================================
     *
     * This method is called by Quartz automatically
     * whenever a job is triggered.
     */
    @Override
    public void execute(JobExecutionContext context) {

        /*
         * Read job-related data passed from Quartz Scheduler
         *
         * These values are stored in JobDataMap while scheduling
         */
        Long jobId = context.getJobDetail()
                .getJobDataMap()
                .getLong("jobId");

        String logFilePath = context.getJobDetail()
                .getJobDataMap()
                .getString("logFilePath");

        /*
         * Fetch Job entity from database
         *
         * If job does not exist → throw runtime exception
         */
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        /*
         * Update job status to RUNNING before execution starts
         */
        job.setStatus("RUNNING");
        jobRepository.saveAndFlush(job);

        /*
         * Capture execution start time
         */
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = null;

        FileWriter writer = null;
        File logFile = null;

        try {
            /*
             * Create log directory if it does not exist
             */
            new File(LOG_DIR).mkdirs();

            /*
             * Create new log file for first execution
             * Reuse same log file during retries
             */
            if (logFilePath == null || logFilePath.isEmpty()) {

                logFile = new File(
                        LOG_DIR + "/job_" + jobId + "_" +
                                startTime.toString().replace(":", "-") + ".log"
                );

                writer = new FileWriter(logFile, true);
                logFilePath = logFile.getAbsolutePath();

            } else {
                logFile = new File(logFilePath);
                writer = new FileWriter(logFile, true);
            }

            /*
             * Write job metadata only during first execution
             */
            if (job.getRetryCount() == 0) {

                writer.write("Job ID: " + job.getJobId() + "\n");
                writer.write("Job Name: " + job.getJobName() + "\n");
                writer.write("Job Type: " + job.getJobType() + "\n");
                writer.write("Time Duration: " + job.getTimeDuration() + " sec\n");

                if (job.getJobType().equalsIgnoreCase("RECURRING")) {
                    writer.write("Recurring Pattern: " + job.getRecurrencePattern() + "\n");
                }

                writer.write("▶ Job STARTED at " + startTime + "\n");

            } else {
                /*
                 * Retry execution logging
                 */
                writer.write("\n🔁 Retry Attempt " +
                        (job.getRetryCount() + 1) +
                        " at " + startTime + "\n");
            }

            writer.flush();

            /*
             * Simulate job execution
             *
             * Logs progress every 10 seconds
             */
            int duration = job.getTimeDuration(); // seconds
            int elapsed = 0;
            int interval = 10;

            while (elapsed < duration) {
                int sleepTime = Math.min(interval, duration - elapsed);
                Thread.sleep(sleepTime * 1000L);
                elapsed += sleepTime;

                writer.write("Running... elapsed " + elapsed + " sec\n");
                writer.flush();
            }
            //To stimulate failure
            // throw new RuntimeException("Simulated Failure");
            /*
             * If execution completes successfully
             */
            job.setStatus("SUCCESS");
            jobRepository.save(job);

            endTime = LocalDateTime.now();
            writer.write("✅ Job SUCCESS at " + endTime + "\n");
            writer.flush();

            /*
             * Save execution history in JobLog table
             * A NEW record is created for each execution
             */
            JobLog jobLog = new JobLog();
            jobLog.setJobId(job.getJobId());
            jobLog.setJobName(job.getJobName());
            jobLog.setJobType(job.getJobType());
            jobLog.setStartTime(startTime);
            jobLog.setEndTime(endTime);

            /*
             * Store next execution time only for recurring jobs
             */
            if ("RECURRING".equalsIgnoreCase(job.getJobType())
                    && context.getTrigger().getNextFireTime() != null) {

                jobLog.setNextExecutionTime(
                        context.getTrigger()
                                .getNextFireTime()
                                .toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime()
                );
            } else {
                jobLog.setNextExecutionTime(null);
            }

            jobLog.setJobStatus("SUCCESS");
            jobLog.setLogFilePath(logFilePath);

            jobLogRepository.save(jobLog);

        } catch (Exception e) {

            /*
             * Handle execution failure
             */
            endTime = LocalDateTime.now();

            try {
                if (writer != null) {
                    writer.write("❌ Job FAILED at " + endTime + "\n");
                    writer.write("Reason: " + e.getMessage() + "\n");
                    writer.flush();
                }
            } catch (Exception ignored) {}

            /*
             * Save failure execution log
             */
            JobLog jobLog = jobLogRepository.findByJobId(jobId)
                    .orElse(new JobLog());

            jobLog.setJobId(job.getJobId());
            jobLog.setJobName(job.getJobName());
            jobLog.setJobType(job.getJobType());
            jobLog.setStartTime(startTime);
            jobLog.setEndTime(endTime);

            if ("RECURRING".equalsIgnoreCase(job.getJobType())
                    && context.getTrigger().getNextFireTime() != null) {

                jobLog.setNextExecutionTime(
                        context.getTrigger()
                                .getNextFireTime()
                                .toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime()
                );
            } else {
                jobLog.setNextExecutionTime(null);
            }

            jobLog.setJobStatus("FAILED");
            jobLog.setLogFilePath(logFilePath);
            jobLogRepository.save(jobLog);

            /*
             * Trigger retry mechanism
             */
            try {
                jobService.handleRetry(job, logFilePath);
            } catch (Exception ignored) {}
        } finally {

            /*
             * Always close file resources
             */
            try {
                if (writer != null) writer.close();
            } catch (Exception ignored) {}
        }
    }
}
