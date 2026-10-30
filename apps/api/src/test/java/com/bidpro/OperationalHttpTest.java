package com.bidpro;

import com.bidpro.config.RequestCorrelationFilter;
import com.bidpro.controller.HealthController;
import com.bidpro.dto.ApiResponse;
import com.bidpro.util.ExceptionUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OperationalHttpTest {
    private JdbcTemplate jdbc;
    private MockMvc mvc;
    @BeforeEach void setup() {
        jdbc = mock(JdbcTemplate.class);
        mvc = MockMvcBuilders.standaloneSetup(new HealthController(jdbc), new ProbeController())
            .setControllerAdvice(new ExceptionUtil()).addFilters(new RequestCorrelationFilter()).build();
    }
    @Test void livenessDoesNotRequireTheDatabaseAndTraceIsReturnedAndCleared() throws Exception {
        mvc.perform(get("/api/health/live").header("X-Request-ID", "support-check-123"))
            .andExpect(status().isOk()).andExpect(header().string("X-Request-ID", "support-check-123"))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(jsonPath("$.requestId").value("support-check-123"));
        verifyNoInteractions(jdbc);
        assertThat(MDC.get("requestId")).isNull();
    }
    @Test void readinessReturns503WithoutLeakingDatabaseConnectionDetails() throws Exception {
        when(jdbc.queryForObject("SELECT 1", Integer.class))
            .thenThrow(new CannotGetJdbcConnectionException("jdbc:mysql://private-db password=do-not-disclose"));
        var result = mvc.perform(get("/api/health/ready").header("X-Request-ID", "bad trace\r\nvalue"))
            .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.errorCode").value("NOT_READY"))
            .andExpect(jsonPath("$.requestId").isNotEmpty()).andReturn();
        String trace = result.getResponse().getHeader("X-Request-ID");
        assertThat(trace).matches("[A-Za-z0-9._-]{1,64}");
        assertThat(result.getResponse().getContentAsString()).doesNotContain("private-db", "password", "do-not-disclose");
    }
    @Test void httpContractErrorsDoNotBecomeInternalServerErrors() throws Exception {
        mvc.perform(get("/api/probe").param("page", "not-a-number"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        mvc.perform(delete("/api/probe"))
            .andExpect(status().isMethodNotAllowed()).andExpect(header().exists("Allow"));
        mvc.perform(post("/api/probe").contentType("text/plain").content("hello"))
            .andExpect(status().isUnsupportedMediaType());
    }
    @Test void validationProvidesFieldMessagesButNeverRejectedValues() throws Exception {
        mvc.perform(post("/api/probe").contentType("application/json").content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").value("must not be blank"))
            .andExpect(jsonPath("$.fieldErrors.rejectedValue").doesNotExist());
    }
    @RestController
    static class ProbeController {
        @GetMapping("/api/probe") ApiResponse<Map<String, Integer>> get(@RequestParam int page) {
            return ApiResponse.success(Map.of("page", page), "ok");
        }
        @PostMapping(value = "/api/probe", consumes = "application/json")
        ApiResponse<String> post(@Valid @RequestBody Input input) { return ApiResponse.success(input.name(), "ok"); }
    }
    record Input(@NotBlank String name) {}
}
