package com.bidpro.controller;
import com.bidpro.dto.ApiResponse;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.dao.DataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    public HealthController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @GetMapping({"/api/health", "/api/health/ready"})
    public ResponseEntity<ApiResponse<Map<String,String>>> ready() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(Map.of("status", "UP", "database", "UP"), "BidPro API is ready"));
        } catch (DataAccessException error) {
            return ResponseEntity.status(503).cacheControl(CacheControl.noStore())
                .body(ApiResponse.error("A required service is unavailable", "NOT_READY", 503));
        }
    }
    @GetMapping("/api/health/live")
    public ResponseEntity<ApiResponse<Map<String,String>>> live() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(ApiResponse.success(Map.of("status", "UP"), "BidPro API is alive"));
    }
}
