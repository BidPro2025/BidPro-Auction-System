package com.bidpro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * General application configuration class.
 * Enables JPA repositories and scheduling for background tasks.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.bidpro.dao")
@EnableScheduling
public class ApplicationConfig {
}

