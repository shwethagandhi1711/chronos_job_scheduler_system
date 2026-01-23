package com.chronos.chronos_job_schedular.config;

// Quartz Scheduler main interface
import org.quartz.Scheduler;

// Spring annotations
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Marks this class as a Spring configuration class
@Configuration
public class QuartzConfig {

    /**
     * This method creates and exposes a Quartz Scheduler bean.
     *
     * Spring Boot auto-configures SchedulerFactoryBean internally
     * using properties (quartz settings, datasource, thread pool, etc).
     *
     * We inject that factory here and obtain the Scheduler instance.
     */
    @Bean
    public Scheduler scheduler(
            org.springframework.scheduling.quartz.SchedulerFactoryBean factory
    ) throws Exception {

        // Get the Quartz Scheduler instance from the factory
        Scheduler scheduler = factory.getScheduler();

        /*
         * Start the scheduler explicitly.
         *
         * Without calling start():
         *  - Jobs will be registered
         *  - Triggers will exist
         *  - BUT jobs will NOT execute
         *
         * Calling start() tells Quartz to begin executing jobs
         * based on their triggers (cron / one-time / recurring).
         */
        scheduler.start();

        // Return the Scheduler so Spring can manage it as a bean
        return scheduler;
    }
}
