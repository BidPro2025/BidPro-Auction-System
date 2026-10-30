package com.bidpro.config;
import java.util.Arrays;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
@Configuration
public class CorsConfig {
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins:http://localhost:5173,http://127.0.0.1:5173}") String origins) {
        var allowed = Arrays.stream(origins.split(",")).map(String::trim).filter(origin -> !origin.isBlank()).toList();
        if (allowed.isEmpty()) throw new IllegalArgumentException("CORS_ORIGINS must specify at least one explicit HTTP(S) origin");
        for (String origin : allowed) {
            URI uri = URI.create(origin);
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null || uri.getRawUserInfo() != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty()) || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("CORS_ORIGINS must contain explicit HTTP(S) origins without paths or wildcards");
            }
        }
        var cors = new CorsConfiguration(); cors.setAllowedOrigins(allowed); cors.setAllowCredentials(false);
        cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Request-ID"));
        cors.setExposedHeaders(Arrays.asList("Content-Disposition", "X-Request-ID", "Retry-After")); cors.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", cors); return source;
    }
}
