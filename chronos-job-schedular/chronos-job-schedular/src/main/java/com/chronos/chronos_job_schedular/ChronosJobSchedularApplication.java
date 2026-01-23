package com.chronos.chronos_job_schedular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * =========================================================
 * ChronosJobSchedularApplication
 * =========================================================
 *
 * PURPOSE:
 * --------
 * This class is the MAIN ENTRY POINT of the Chronos Job Scheduler
 * Spring Boot application.
 *
 * When this class is executed:
 * ----------------------------
 * ✔ Spring Boot framework is started
 * ✔ Application context is created
 * ✔ All Spring-managed beans are initialized
 * ✔ Embedded web server (Tomcat) is started
 * ✔ REST APIs become available
 *
 * In simple words:
 * ----------------
 * 👉 This is the class that BOOTS your entire application.
 */

@SpringBootApplication
/*
 * =========================================================
 * @SpringBootApplication
 * =========================================================
 *
 * This is a META-ANNOTATION (combination of multiple annotations).
 *
 * It includes:
 *
 * 1️⃣ @Configuration
 * ------------------
 * - Marks this class as a source of Spring bean definitions
 * - Allows defining @Bean methods inside this class
 *
 * 2️⃣ @EnableAutoConfiguration
 * ---------------------------
 * - Automatically configures Spring Boot based on dependencies
 * - Examples:
 *   ✔ DataSource (MySQL / H2)
 *   ✔ JPA / Hibernate
 *   ✔ Spring Security
 *   ✔ Quartz Scheduler
 *   ✔ Spring MVC
 *
 * 3️⃣ @ComponentScan
 * -----------------
 * - Scans this package (com.chronos.chronos_job_schedular)
 *   and all its sub-packages
 * - Detects:
 *   ✔ @Controller / @RestController
 *   ✔ @Service
 *   ✔ @Repository
 *   ✔ @Component
 *
 * ⚠️ Important:
 * -------------
 * All your classes MUST be inside this package or sub-packages
 * to be detected by Spring automatically.
 */
public class ChronosJobSchedularApplication {

    /*
     * =========================================================
     * main() method
     * =========================================================
     *
     * This is the STANDARD Java entry point.
     *
     * JVM execution starts from here.
     */
    public static void main(String[] args) {

        /*
         * =====================================================
         * SpringApplication.run()
         * =====================================================
         *
         * This method does the following internally:
         *
         * ✔ Creates SpringApplication instance
         * ✔ Loads application.properties / application.yml
         * ✔ Sets up ApplicationContext
         * ✔ Performs component scanning
         * ✔ Initializes all beans (@Component, @Service, etc.)
         * ✔ Applies auto-configurations
         * ✔ Starts embedded Tomcat server
         * ✔ Deploys REST endpoints
         *
         * After this call:
         * ----------------
         * 👉 Your application is UP and RUNNING
         * 👉 Quartz Scheduler starts
         * 👉 APIs are ready to receive requests
         */
        SpringApplication.run(ChronosJobSchedularApplication.class, args);
    }

}
