package com.bidpro;

import com.bidpro.dao.LogDao;
import com.bidpro.dao.UserDao;
import com.bidpro.entities.User;
import com.bidpro.security.AuthRevokedTokenRepository;
import com.bidpro.security.AuthSessionService;
import com.bidpro.dto.SigninDTO;
import com.bidpro.service.UserService;
import com.bidpro.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityHardeningIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserDao users;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtUtil jwt;
    @Autowired AuthRevokedTokenRepository revoked;
    @Autowired LogDao logs;
    @Autowired UserService userService;
    @Autowired AuthSessionService sessions;
    @Value("${jwt.secret}") String signingKey;

    @Test void usernameLoginWorksAndBcryptByteLimitsNeverBecomeServerErrors() throws Exception {
        User user = account(User.Role.BIDDER);
        mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("email", user.getUsername(), "password", "Strong123!"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.user.userID").value(user.getUserID()))
            .andExpect(jsonPath("$.data.user.sessionVersion").doesNotExist());
        mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("email", user.getEmail(), "password", "\u754c".repeat(30)))))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid email or password"));
        String id = UUID.randomUUID().toString();
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("firstName", "New", "lastName", "Member", "username", id,
                "email", id + "@example.test", "password", "\u754c".repeat(30), "role", "BIDDER"))))
            .andExpect(status().isBadRequest());
        assertThat(users.findByEmail(id + "@example.test")).isEmpty();
    }

    @Test void logoutRevokesOnlyThePresentedTokenAndStoresOnlyItsDigest() throws Exception {
        User user = account(User.Role.BIDDER);
        String token = jwt.generateToken(user.getEmail(), user.getRole().name());
        String second = jwt.generateToken(user.getEmail(), user.getRole().name());
        assertThat(second).isNotEqualTo(token);
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/validate").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/validate").header("Authorization", "Bearer " + second)).andExpect(status().isOk());
        assertThat(revoked.findById(JwtUtil.tokenHash(token))).isPresent();
        assertThat(revoked.findById(token)).isEmpty();
        assertThat(logs.findByEntityTypeAndEntityID("USER", user.getUserID()))
            .anySatisfy(log -> { assertThat(log.getOperation()).isEqualTo("SIGNED_OUT"); assertThat(log.getDetails()).doesNotContain(token); });
    }

    @Test void logoutAllRevokesExistingSessionsButAllowsNewAuthentication() throws Exception {
        User user = account(User.Role.BIDDER);
        String first = jwt.generateToken(user.getEmail(), user.getRole().name());
        String second = jwt.generateToken(user.getEmail(), user.getRole().name());
        mvc.perform(post("/api/auth/logout-all").header("Authorization", "Bearer " + first)).andExpect(status().isOk());
        assertThat(jwt.validateToken(first)).isFalse();
        assertThat(jwt.validateToken(second)).isFalse();
        mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("email", user.getEmail(), "password", "Strong123!"))))
            .andExpect(status().isOk());
    }

    @Test void invalidationBetweenCredentialVerificationAndTokenIssueCannotCreateANewValidSession() {
        User user = account(User.Role.BIDDER);
        SigninDTO input = new SigninDTO(); input.setEmail(user.getEmail()); input.setPassword("Strong123!");
        var verified = userService.signin(input);
        sessions.logoutAll(user.getUserID());
        String raced = jwt.generateToken(verified.getEmail(), verified.getRole().name(), verified.getSessionVersion());
        assertThat(jwt.validateToken(raced)).isFalse();
    }

    @Test void rejectedThenApprovedAccountsCannotResurrectAnOldSession() throws Exception {
        User admin = account(User.Role.ADMIN), member = account(User.Role.BIDDER);
        String adminToken = jwt.generateToken(admin.getEmail(), "ADMIN");
        String old = jwt.generateToken(member.getEmail(), "BIDDER");
        for (String status : new String[]{"REJECTED", "APPROVED"}) {
            mvc.perform(put("/api/admins/users/" + member.getUserID() + "/status")
                .header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("status", status)))).andExpect(status().isOk());
        }
        mvc.perform(get("/api/auth/validate").header("Authorization", "Bearer " + old)).andExpect(status().isUnauthorized());
        assertThat(logs.findByEntityTypeAndEntityID("USER", member.getUserID()))
            .filteredOn(log -> "ACCOUNT_STATUS_CHANGED".equals(log.getOperation())).hasSize(2);
    }

    @Test void profileAndSignupUseTheSameSafeUsernameFormat() throws Exception {
        User user = account(User.Role.BIDDER);
        mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + jwt.generateToken(user.getEmail(), "BIDDER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("firstName", "Safe", "lastName", "Member", "username", "bad@username"))))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test void apiHeadersAndCorsAllowExplicitOriginsAndCorrelationHeaders() throws Exception {
        mvc.perform(get("/api/categories").header("Origin", "http://localhost:5173"))
            .andExpect(status().isOk()).andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")))
            .andExpect(header().string("Access-Control-Expose-Headers", containsString("X-Request-ID")))
            .andExpect(header().string("Access-Control-Expose-Headers", containsString("Retry-After")));
        mvc.perform(options("/api/auth/signin").header("Origin", "http://localhost:5173")
            .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "Content-Type,X-Request-ID"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(get("/api/categories").header("Origin", "https://untrusted.example"))
            .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test void aValidSignatureWithTheWrongIssuerCannotAuthenticate() throws Exception {
        User user = account(User.Role.BIDDER);
        String token = Jwts.builder().setSubject(user.getEmail()).setId(UUID.randomUUID().toString())
            .setIssuer("different-application").setAudience("bidpro-web").claim("passwordVersion", 0)
            .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 60000))
            .signWith(Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8))).compact();
        mvc.perform(get("/api/auth/validate").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        assertThat(jwt.validateToken("x".repeat(8193))).isFalse();
    }

    private User account(User.Role role) {
        String id = UUID.randomUUID().toString();
        return users.saveAndFlush(new User(id, "Test", "Member", id, id + "@example.test", "",
            passwords.encode("Strong123!"), role, User.Status.APPROVED));
    }
}
