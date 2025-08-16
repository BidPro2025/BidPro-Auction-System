package com.bidpro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Use /** to match all sub-paths under /api
                .allowedOrigins("http://localhost:5173", "http://localhost:3000") // Allowed frontend origins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Include OPTIONS for preflight requests
                .allowedHeaders("Authorization", "Content-Type", "Accept") // Specify headers explicitly
                .allowCredentials(true) // Allow cookies and credentials
                .maxAge(3600); // Cache preflight response for 1 hour
    }
}