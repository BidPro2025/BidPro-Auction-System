package com.bidpro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {
    @Test void signInAliasesShareALimitAndSpoofedForwardedHeadersCannotBypassIt() throws Exception {
        var filter = new AuthRateLimitFilter(new ObjectMapper(), 2, 2, 2, 2);
        var calls = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            var request = new MockHttpServletRequest("POST", i == 1 ? "/api/admins/login" : "/api/auth/signin");
            request.setRemoteAddr("192.0.2.4"); request.addHeader("X-Forwarded-For", "192.0.2." + (100 + i));
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> calls.incrementAndGet());
            if (i == 2) {
                assertThat(response.getStatus()).isEqualTo(429);
                assertThat(Long.parseLong(response.getHeader("Retry-After"))).isBetween(1L, 60L);
                assertThat(response.getContentAsString()).contains("RATE_LIMITED").doesNotContain("192.0.2.");
            }
        }
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test void separateClientsAndUnrelatedPublicReadsRemainIndependent() throws Exception {
        var filter = new AuthRateLimitFilter(new ObjectMapper(), 1, 1, 1, 1);
        var calls = new AtomicInteger();
        for (String address : new String[]{"192.0.2.1", "192.0.2.2"}) {
            var request = new MockHttpServletRequest("POST", "/api/auth/signin"); request.setRemoteAddr(address);
            filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> calls.incrementAndGet());
        }
        filter.doFilter(new MockHttpServletRequest("GET", "/api/categories"), new MockHttpServletResponse(), (req, res) -> calls.incrementAndGet());
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test void bidCreationAndIncreaseShareTheSameAbuseGate() throws Exception {
        var filter = new AuthRateLimitFilter(new ObjectMapper(), 5, 5, 5, 1);
        var first = new MockHttpServletRequest("POST", "/api/bids"); first.setRemoteAddr("192.0.2.1");
        filter.doFilter(first, new MockHttpServletResponse(), (req, res) -> {});
        var second = new MockHttpServletRequest("PUT", "/api/bids/bid-id"); second.setRemoteAddr("192.0.2.1");
        var response = new MockHttpServletResponse();
        filter.doFilter(second, response, (req, res) -> { throw new AssertionError("Exhausted bidder must not execute a mutation"); });
        assertThat(response.getStatus()).isEqualTo(429);
    }
}
