package com.chronos.chronos_job_schedular.service;

import com.chronos.chronos_job_schedular.entity.Job;
import com.chronos.chronos_job_schedular.quartz.QuartzJobExecutor;
import com.chronos.chronos_job_schedular.repository.JobRepository;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/*
 * ============================
 * JobService
 * ============================
 *
 * This is the CORE business service for job scheduling.
 *
 * Responsibilities:
 * -----------------
 * ✔ Validate job input
 * ✔ Persist job details in DB
 * ✔ Schedule jobs using Quartz
 * ✔ Reschedule jobs
 * ✔ Delete jobs
 * ✔ Handle retries on failure
 * ✔ Calculate next execution times
 *
 * This service acts as the bridge between:
 * - Controller layer
 * - Quartz Scheduler
 * - Database layer
 */
@Service   // Marks this class as a Spring Service component
public class JobService {

    /*
     * JobRepository
     *
     * Used to perform CRUD operations on Job entity.
     */
    @Autowired
    private JobRepository jobRepository;

    /*
     * Quartz Scheduler
     *
     * Core Quartz component used to:
     * - Schedule jobs
     * - Reschedule triggers
     * - Delete jobs
     */
    @Autowired
    private Scheduler scheduler;

    /* =====================================================
     * CREATE JOB WITH VALIDATION
     * =====================================================
     *
     * This method:
     * --------------
     * ✔ Validates job input
     * ✔ Sets default values
     * ✔ Saves job in database
     * ✔ Schedules job in Quartz
     */
    public Job createJob(Job job) throws SchedulerException {

        /* ================= COMMON VALIDATION ================= */

        // Validate job name
        if (job.getJobName() == null || job.getJobName().isBlank()) {
            throw new RuntimeException("Job name is required");
        }

        // Validate job type
        if (job.getJobType() == null || job.getJobType().isBlank()) {
            throw new RuntimeException("Job type is required");
        }

        // Validate execution duration
        if (job.getTimeDuration() <= 0) {
            throw new RuntimeException("Time duration must be greater than 0");
        }

        /* ================= JOB TYPE SPECIFIC VALIDATION ================= */

        /*
         * 🔹 IMMEDIATE JOB
         * ----------------
         * - Starts immediately
         * - No recurrence
         */
        if ("IMMEDIATE".equalsIgnoreCase(job.getJobType())) {

            job.setStartTime(LocalDateTime.now());
            job.setRecurrencePattern(null);
            job.setNextExecutionTime(null);
        }

        /*
         * 🔹 ONE-TIME JOB
         * ---------------
         * - Runs once at a specific future time
         */
        else if ("ONE_TIME".equalsIgnoreCase(job.getJobType())) {

            if (job.getStartTime() == null) {
                throw new RuntimeException("Start time is required for one-time job");
            }

            // Ensure start time is in the future
            validateFutureTime(job.getStartTime());

            job.setRecurrencePattern(null);
            job.setNextExecutionTime(null);
        }

        /*
         * 🔹 RECURRING JOB
         * ----------------
         * - Runs repeatedly based on a recurrence pattern
         */
        else if ("RECURRING".equalsIgnoreCase(job.getJobType())) {

            if (job.getStartTime() == null) {
                throw new RuntimeException("Start time is required for recurring job");
            }

            if (job.getRecurrencePattern() == null || job.getRecurrencePattern().isBlank()) {
                throw new RuntimeException("Recurrence pattern is required for recurring job");
            }

            validateFutureTime(job.getStartTime());
        }

        /*
         * 🔹 INVALID JOB TYPE
         */
        else {
            throw new RuntimeException("Invalid job type");
        }

        /* ================= DEFAULT VALUES ================= */

        // Initial job status
        job.setStatus("PENDING");

        // Retry configuration
        job.setRetryCount(0);
        job.setMaxRetries(3);

        /*
         * Save job in database
         */
        Job savedJob = jobRepository.save(job);

        /*
         * Schedule job in Quartz
         */
        scheduleJob(savedJob);

        return savedJob;
    }

    /* =====================================================
     * INITIAL JOB SCHEDULING
     * =====================================================
     */
    private void scheduleJob(Job job) throws SchedulerException {

        /*
         * Create Quartz JobDetail
         *
         * QuartzJobExecutor contains the actual execution logic
         */
        JobDetail jobDetail = JobBuilder.newJob(QuartzJobExecutor.class)
                .withIdentity("job_" + job.getJobId())
                .usingJobData("jobId", job.getJobId())
                .storeDurably()
                .build();

        // Register job with Quartz
        scheduler.addJob(jobDetail, true);

        /*
         * Build trigger based on job type
         */
        Trigger trigger = buildTrigger(job);

        // Schedule job with trigger
        scheduler.scheduleJob(trigger);

        // Update next execution time in DB
        updateNextExecution(job, trigger);
    }

    /* =====================================================
     * BUILD QUARTZ TRIGGER
     * =====================================================
     */
    private Trigger buildTrigger(Job job) {

        /*
         * Common trigger builder
         */
        TriggerBuilder<Trigger> builder =
                TriggerBuilder.newTrigger()
                        .withIdentity("trigger_" + job.getJobId())
                        .forJob("job_" + job.getJobId())
                        .startAt(Timestamp.valueOf(job.getStartTime()));

        switch (job.getJobType().toUpperCase()) {

            /*
             * IMMEDIATE & ONE-TIME
             * --------------------
             * Uses SimpleTrigger (runs once)
             */
            case "IMMEDIATE":
            case "ONE_TIME":
                return builder
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                        .build();

            /*
             * RECURRING
             * ----------
             * Uses CronTrigger
             */
            case "RECURRING":
                return builder
                        .withSchedule(
                                CronScheduleBuilder
                                        .cronSchedule(
                                                getCronExpression(
                                                        job.getRecurrencePattern(),
                                                        job.getStartTime()
                                                )
                                        )
                                        .inTimeZone(TimeZone.getDefault())
                                        .withMisfireHandlingInstructionFireAndProceed()
                        )
                        .build();

            default:
                throw new RuntimeException("Invalid job type");
        }
    }

    /* =====================================================
     * RESCHEDULE JOB
     * =====================================================
     */
    public Job rescheduleJob(
            Long jobId,
            LocalDateTime newStartTime,
            Integer newTimeDuration,
            String newRecurrencePattern
    ) throws SchedulerException {

        /*
         * Fetch job from DB
         */
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job with ID " + jobId + " not found"));

        /*
         * IMMEDIATE jobs cannot be rescheduled
         */
        if ("IMMEDIATE".equalsIgnoreCase(job.getJobType())) {
            throw new RuntimeException("Immediate jobs cannot be rescheduled");
        }

        /*
         * Ensure new start time is in the future
         */
        if (newStartTime != null && newStartTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reschedule start time must be in the future");
        }

        /*
         * ONE-TIME job rescheduling
         */
        if ("ONE_TIME".equalsIgnoreCase(job.getJobType())) {

            if (!"PENDING".equalsIgnoreCase(job.getStatus())) {
                throw new RuntimeException(
                        "One-time jobs can be rescheduled only when status is PENDING");
            }

            if (newStartTime != null) {
                job.setStartTime(newStartTime);
            }

            if (newTimeDuration != null) {
                job.setTimeDuration(newTimeDuration);
            }

            job.setNextExecutionTime(null);
        }

        /*
         * RECURRING job rescheduling
         */
        if ("RECURRING".equalsIgnoreCase(job.getJobType())) {

            if ("RUNNING".equalsIgnoreCase(job.getStatus())
                    || "FAILED".equalsIgnoreCase(job.getStatus())) {
                throw new RuntimeException(
                        "Recurring jobs cannot be rescheduled when RUNNING or FAILED");
            }

            if (newStartTime != null) {
                job.setStartTime(newStartTime);
            }

            if (newTimeDuration != null) {
                job.setTimeDuration(newTimeDuration);
            }

            if (newRecurrencePattern != null) {
                job.setRecurrencePattern(newRecurrencePattern);
            }
        }

        /*
         * Quartz rescheduling
         */
        TriggerKey triggerKey = TriggerKey.triggerKey("trigger_" + jobId);
        Trigger newTrigger = buildTrigger(job);

        scheduler.rescheduleJob(triggerKey, newTrigger);

        job.setStatus("PENDING");
        updateNextExecution(job, newTrigger);

        return jobRepository.save(job);
    }

    /* =====================================================
     * DELETE JOB
     * =====================================================
     */
    public void deleteJob(Long jobId) throws SchedulerException {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job with ID " + jobId + " not found"));

        // Remove job from Quartz
        scheduler.deleteJob(JobKey.jobKey("job_" + jobId));

        // Remove job from DB
        jobRepository.delete(job);
    }

    /* =====================================================
     * VIEW JOBS
     * =====================================================
     */

    public List<Job> getAllJobs() {

        List<Job> jobs = jobRepository.findAll();

        if (jobs.isEmpty()) {
            throw new RuntimeException("No jobs found");
        }

        return jobs;
    }

    public Job getJobById(Long jobId) {

        return jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job with ID " + jobId + " not found"));
    }

    public List<Job> getJobsByStatus(String status) {

        List<Job> jobs = jobRepository.findByStatusIgnoreCase(status);

        if (jobs.isEmpty()) {
            throw new RuntimeException("No jobs present with status: " + status);
        }

        return jobs;
    }

    public List<Job> getJobsByType(String jobType) {

        List<Job> jobs = jobRepository.findByJobTypeIgnoreCase(jobType);

        if (jobs.isEmpty()) {
            throw new RuntimeException("No jobs present with job type: " + jobType);
        }

        return jobs;
    }

    /* =====================================================
     * UPDATE NEXT EXECUTION TIME
     * =====================================================
     */
    private void updateNextExecution(Job job, Trigger trigger) {

        // IMMEDIATE & ONE-TIME jobs do not have next execution
        if ("IMMEDIATE".equalsIgnoreCase(job.getJobType())
                || "ONE_TIME".equalsIgnoreCase(job.getJobType())) {

            job.setNextExecutionTime(null);
            job.setRecurrencePattern(null);
            jobRepository.save(job);
            return;
        }

        // RECURRING jobs
        if (trigger instanceof CronTrigger cronTrigger) {

            Date firstFireTime = cronTrigger.getNextFireTime();

            if (firstFireTime != null) {

                Date nextFireTime =
                        cronTrigger.getFireTimeAfter(firstFireTime);

                if (nextFireTime != null) {
                    job.setNextExecutionTime(
                            nextFireTime.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                    );
                }
            }
        }

        jobRepository.save(job);
    }

    /* =====================================================
     * FAILURE HANDLING & RETRY MECHANISM
     * =====================================================
     */
    public void handleRetry(Job job, String logFilePath) throws SchedulerException {

        int nextRetry = job.getRetryCount() + 1;

        /*
         * Retry allowed
         */
        if (nextRetry <= job.getMaxRetries()) {

            job.setRetryCount(nextRetry);
            job.setStatus("RUNNING");
            jobRepository.save(job);

            System.out.println("🔁 Retrying Job: " + job.getJobName() +
                    " | Attempt: " + nextRetry + "/" + job.getMaxRetries());

            JobDetail jobDetail = JobBuilder.newJob(QuartzJobExecutor.class)
                    .withIdentity("retry_job_" + job.getJobId() + "_" + nextRetry)
                    .usingJobData("jobId", job.getJobId())
                    .usingJobData("logFilePath", logFilePath != null ? logFilePath : "")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .startAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(1)))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

        }
        /*
         * Max retries exceeded
         */
        else {
            job.setStatus("FAILED");
            jobRepository.save(job);

            System.out.println("⛔ Job FAILED permanently after max retries: " + job.getJobName());

            if (logFilePath != null && !logFilePath.isEmpty()) {
                try (FileWriter writer = new FileWriter(logFilePath, true)) {
                    writer.write("⛔ Job permanently FAILED at " + LocalDateTime.now() + "\n");
                } catch (Exception ignored) {}
            }
        }
    }

    /* =====================================================
     * VALIDATION UTIL
     * =====================================================
     */
    private void validateFutureTime(LocalDateTime time) {
        if (time.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Start time must be in the future");
        }
    }

    /* =====================================================
     * DYNAMIC CRON EXPRESSION GENERATOR
     * =====================================================
     */
    private String getCronExpression(String pattern, LocalDateTime startTime) {

        int minute = startTime.getMinute();
        int hour = startTime.getHour();

        return switch (pattern.toUpperCase()) {
            case "HOURLY" -> String.format("0 %d * * * ?", minute);
            case "DAILY" -> String.format("0 %d %d * * ?", minute, hour);
            case "WEEKLY" -> String.format("0 %d %d ? * MON", minute, hour);
            case "MONTHLY" -> String.format("0 %d %d 1 * ?", minute, hour);
            default -> throw new RuntimeException("Invalid recurrence pattern");
        };
    }
}
