package com.bidpro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * General application configuration class.
 * Enables JPA repositories and scheduling for background tasks.
 */
@Configuration
@EnableScheduling
public class ApplicationConfig {
}

