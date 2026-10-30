package com.bidpro.security;

import com.bidpro.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private final BoundedRequestLimiter limiter = new BoundedRequestLimiter(Clock.systemUTC(), 20000);
    private final ObjectMapper json;
    private final int authLimit;
    private final int signupLimit;
    private final int recoveryLimit;
    private final int bidLimit;

    public AuthRateLimitFilter(ObjectMapper json,
            @Value("${app.security.rate-limit.auth-per-minute:60}") int authLimit,
            @Value("${app.security.rate-limit.signup-per-hour:30}") int signupLimit,
            @Value("${app.security.rate-limit.recovery-per-hour:30}") int recoveryLimit,
            @Value("${app.security.rate-limit.bid-per-minute:120}") int bidLimit) {
        if (authLimit < 1 || signupLimit < 1 || recoveryLimit < 1 || bidLimit < 1) {
            throw new IllegalArgumentException("Security rate limits must be positive");
        }
        this.json = json;
        this.authLimit = authLimit;
        this.signupLimit = signupLimit;
        this.recoveryLimit = recoveryLimit;
        this.bidLimit = bidLimit;
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        boolean posting = "POST".equals(request.getMethod());
        boolean updating = "PUT".equals(request.getMethod());
        if (!posting && !updating) { chain.doFilter(request, response); return; }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        Policy policy = posting ? switch (path) {
            case "/api/auth/signin", "/api/admins/login" -> new Policy("signin", authLimit, 60000);
            case "/api/auth/signup" -> new Policy("signup", signupLimit, 3600000);
            case "/api/auth/forgot-password" -> new Policy("recovery", recoveryLimit, 3600000);
            case "/api/auth/reset-password" -> new Policy("reset", recoveryLimit, 3600000);
            case "/api/bids" -> new Policy("bid", bidLimit, 60000);
            default -> null;
        } : path.matches("/api/bids/[^/]+") ? new Policy("bid", bidLimit, 60000) : null;
        if (policy == null) { chain.doFilter(request, response); return; }
        // Forwarded headers are untrusted unless an operator configures a trusted reverse proxy.
        String client = request.getRemoteAddr();
        var principal = SecurityContextHolder.getContext().getAuthentication();
        if ("bid".equals(policy.name) && principal != null && principal.isAuthenticated()) {
            client = "user:" + principal.getName();
        }
        var result = limiter.acquire(policy.name + ":" + client, policy.limit, policy.intervalMillis);
        if (!result.allowed()) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            response.setHeader("Cache-Control", "no-store");
            json.writeValue(response.getOutputStream(), ApiResponse.error(
                "Too many attempts. Please wait before trying again.", "RATE_LIMITED", 429));
            return;
        }
        chain.doFilter(request, response);
    }

    private record Policy(String name, int limit, long intervalMillis) {}
}
