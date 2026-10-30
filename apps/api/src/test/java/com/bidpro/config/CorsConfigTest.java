package com.bidpro.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsConfigTest {
    @Test void rejectsWildcardsCredentialsAndPaths() {
        for (String origin : new String[]{"*", "https://user:password@example.test", "https://example.test/path", "https://example.test?query=1"}) {
            assertThatThrownBy(() -> new CorsConfig().corsConfigurationSource(origin)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test void trimsExplicitOriginsAndExposesOnlyUsefulResponseHeaders() {
        var source = new CorsConfig().corsConfigurationSource(" http://localhost:5173 , https://bid.example.test ");
        var cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/categories"));
        assertThat(cors.getAllowedOrigins()).containsExactly("http://localhost:5173", "https://bid.example.test");
        assertThat(cors.getAllowCredentials()).isFalse();
        assertThat(cors.getAllowedHeaders()).contains("X-Request-ID");
        assertThat(cors.getExposedHeaders()).contains("Retry-After", "X-Request-ID");
    }
}
