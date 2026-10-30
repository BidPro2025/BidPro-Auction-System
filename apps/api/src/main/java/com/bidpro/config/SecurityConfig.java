package com.bidpro.config;
import com.bidpro.dto.ApiResponse;
import com.bidpro.security.AuthRateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        var registration = new FilterRegistrationBean<>(filter); registration.setEnabled(false); return registration;
    }
    @Bean FilterRegistrationBean<AuthRateLimitFilter> rateLimitFilterRegistration(AuthRateLimitFilter filter) {
        var registration = new FilterRegistrationBean<>(filter); registration.setEnabled(false); return registration;
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
            AuthRateLimitFilter rateLimit, ObjectMapper json, Environment environment) throws Exception {
        boolean developmentDocs = environment.acceptsProfiles(Profiles.of("local", "test"));
        return http.csrf(csrf -> csrf.disable()).cors(cors -> {})
            .httpBasic(basic -> basic.disable()).formLogin(form -> form.disable()).logout(logout -> logout.disable())
            .requestCache(cache -> cache.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .addHeaderWriter((request, response) -> {
                    response.setHeader("Referrer-Policy", "no-referrer");
                    response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
                    if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                        response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; form-action 'none'");
                    }
                }))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(401); response.setContentType("application/json");
                    json.writeValue(response.getOutputStream(), ApiResponse.error("Please sign in to continue", "UNAUTHENTICATED", 401));
                })
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(403); response.setContentType("application/json");
                    json.writeValue(response.getOutputStream(), ApiResponse.error("You do not have permission for this action", "FORBIDDEN", 403));
                }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // The prototype messaging API has no active UI or recipient access policy.
                // Keep it unavailable until that feature has a complete authorization model.
                .requestMatchers("/api/messages/**", "/api/message-recipients/**", "/api/response/**").denyAll()
                .requestMatchers("/api/health", "/api/health/**", "/error").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui", "/api-docs/**")
                    .access((authentication, context) -> new org.springframework.security.authorization.AuthorizationDecision(
                        developmentDocs || authentication.get().getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))))
                .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/signin", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/admins/login", "/api/payments/webhooks/razorpay").permitAll()
                .requestMatchers("/api/products/mine", "/api/bids/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/auctions/**", "/api/images/**", "/api/bids/product/**").permitAll()
                .requestMatchers("/api/admins/**", "/api/logs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/categories/**", "/api/auctions", "/api/auctions/*/close").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/auctions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/auctions/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(rateLimit, JwtAuthenticationFilter.class).build();
    }
}
