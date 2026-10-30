package com.bidpro.config;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Deployment preconditions; no environment value or secret is included in failures. */
@Component
@Profile("production")
public class ProductionSafetyValidator {
    private final Environment environment;
    public ProductionSafetyValidator(Environment environment) { this.environment = environment; }

    @PostConstruct
    public void validate() {
        Set<String> profiles = Set.copyOf(Arrays.asList(environment.getActiveProfiles()));
        require(profiles.contains("mysql") && !profiles.contains("local") && !profiles.contains("test"),
            "Production requires the mysql profile and cannot be mixed with local or test");
        require(!Boolean.parseBoolean(value("app.seed.enabled")), "Demo seeding must be disabled in production");
        require(!Boolean.parseBoolean(value("payments.local-test")), "Local checkout must be disabled in production");
        require("validate".equals(value("spring.jpa.hibernate.ddl-auto")),
            "Production schema changes must be provisioned separately; ddl-auto must be validate");
        String secret = value("jwt.secret");
        require(secret.getBytes(StandardCharsets.UTF_8).length >= 32
            && !secret.toLowerCase().matches(".*(local-development|integration-test|change-before|change-me).*"),
            "Configure a private production JWT signing key of at least 32 bytes");
        URI publicUrl;
        try { publicUrl = URI.create(value("app.public-url")); }
        catch (IllegalArgumentException error) { throw new IllegalStateException("Configure an HTTPS APP_PUBLIC_URL"); }
        require("https".equalsIgnoreCase(publicUrl.getScheme()) && publicUrl.getHost() != null
            && publicUrl.getUserInfo() == null && publicUrl.getQuery() == null && publicUrl.getFragment() == null,
            "Configure an HTTPS APP_PUBLIC_URL without credentials, query or fragment");
        require(!value("app.cors.origins").isBlank(), "Configure explicit HTTPS CORS origins in production");
        for (String origin : value("app.cors.origins").split(",")) {
            URI uri;
            try { uri = URI.create(origin.trim()); }
            catch (IllegalArgumentException error) { throw new IllegalStateException("Production CORS origins must be explicit HTTPS origins"); }
            require("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && !origin.contains("*")
                && uri.getUserInfo() == null && uri.getQuery() == null && uri.getFragment() == null
                && (uri.getPath() == null || uri.getPath().isEmpty()),
                "Production CORS origins must be explicit HTTPS origins");
        }
        require("smtp".equals(value("app.mail.mode")) && !value("spring.mail.host").isBlank(),
            "Configure an SMTP host for production recovery and notifications");
        boolean tls = Boolean.parseBoolean(value("spring.mail.properties.mail.smtp.ssl.enable"))
            || (Boolean.parseBoolean(value("spring.mail.properties.mail.smtp.starttls.enable"))
                && Boolean.parseBoolean(value("spring.mail.properties.mail.smtp.starttls.required")));
        require(tls && Boolean.parseBoolean(value("spring.mail.properties.mail.smtp.ssl.checkserveridentity")),
            "Production SMTP must require TLS with server identity verification");
        require(value("spring.datasource.url").startsWith("jdbc:mysql:"), "Production requires a MySQL datasource");
        require(value("spring.datasource.url").matches("(?i).*[?&]sslMode=VERIFY_IDENTITY(?:&.*)?$"),
            "Production MySQL URL must require sslMode=VERIFY_IDENTITY");
    }
    private String value(String key) { return environment.getProperty(key, "").trim(); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
