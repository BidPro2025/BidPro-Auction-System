package com.bidpro.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Correlates API failures without recording credentials, query strings or request bodies. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestCorrelationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String proposed = request.getHeader("X-Request-ID");
        String requestId = proposed != null && proposed.matches("[A-Za-z0-9._-]{1,64}")
            ? proposed : UUID.randomUUID().toString();
        String previous = MDC.get("requestId");
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);
        long started = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            if (request.getRequestURI().startsWith("/api/")) {
                LOG.info("http_request method={} path={} status={} duration_ms={}",
                    request.getMethod(), request.getRequestURI().replaceAll("[\\r\\n]", ""),
                    response.getStatus(), (System.nanoTime() - started) / 1_000_000);
            }
            if (previous == null) MDC.remove("requestId"); else MDC.put("requestId", previous);
        }
    }
}
