package com.bidpro;

import com.bidpro.config.ProductionSafetyValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.*;

class ProductionSafetyValidatorTest {
    private MockEnvironment configured() {
        var env = new MockEnvironment();
        env.setActiveProfiles("production", "mysql");
        return env.withProperty("app.seed.enabled", "false").withProperty("payments.local-test", "false")
            .withProperty("spring.jpa.hibernate.ddl-auto", "validate")
            .withProperty("jwt.secret", "private-random-fixture-key-which-is-long-enough-2026")
            .withProperty("app.public-url", "https://auctions.example.com")
            .withProperty("app.cors.origins", "https://auctions.example.com")
            .withProperty("app.mail.mode", "smtp").withProperty("spring.mail.host", "smtp.example.com")
            .withProperty("spring.mail.properties.mail.smtp.starttls.enable", "true")
            .withProperty("spring.mail.properties.mail.smtp.starttls.required", "true")
            .withProperty("spring.mail.properties.mail.smtp.ssl.checkserveridentity", "true")
            .withProperty("spring.datasource.url", "jdbc:mysql://db.example.com/bidpro?sslMode=VERIFY_IDENTITY");
    }
    @Test void configuredProductionProfilePassesWithoutPrintingSettings() {
        assertThatCode(() -> new ProductionSafetyValidator(configured()).validate()).doesNotThrowAnyException();
    }
    @ParameterizedTest
    @CsvSource({
        "app.seed.enabled,true", "payments.local-test,true", "spring.jpa.hibernate.ddl-auto,update",
        "jwt.secret,short", "app.public-url,http://auctions.example.com", "app.mail.mode,outbox",
        "app.cors.origins,http://localhost:5173",
        "spring.mail.properties.mail.smtp.starttls.required,false",
        "spring.mail.properties.mail.smtp.ssl.checkserveridentity,false",
        "spring.datasource.url,jdbc:mysql://db.example.com/bidpro?useSSL=false"
    })
    void unsafeProductionSettingsFailClosed(String property, String value) {
        var env = configured().withProperty(property, value);
        assertThatThrownBy(() -> new ProductionSafetyValidator(env).validate()).isInstanceOf(IllegalStateException.class);
    }
    @Test void productionCannotBeCombinedWithLocalOrTestProfiles() {
        var env = configured(); env.setActiveProfiles("production", "mysql", "local");
        assertThatThrownBy(() -> new ProductionSafetyValidator(env).validate()).isInstanceOf(IllegalStateException.class);
    }
}
