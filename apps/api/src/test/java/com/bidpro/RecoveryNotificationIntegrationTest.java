package com.bidpro;

import com.bidpro.dao.*;
import com.bidpro.entities.*;
import com.bidpro.service.NotificationService;
import com.bidpro.service.PasswordResetService;
import com.bidpro.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecoveryNotificationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserDao users;
    @Autowired PasswordResetTokenDao resets;
    @Autowired EmailOutboxDao emails;
    @Autowired NotificationDao notifications;
    @Autowired NotificationService notices;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtUtil jwt;

    @Test void resetIsPrivateSingleUseAndRevokesEveryPreviousSession() throws Exception {
        User user=account(User.Role.BIDDER);
        String oldToken=jwt.generateToken(user.getEmail(),user.getRole().name());
        String response=mvc.perform(post("/api/auth/forgot-password").with(request->{request.setRemoteAddr(user.getUserID());return request;})
            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("email",user.getEmail()))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty()).andReturn().getResponse().getContentAsString();
        String unknown=mvc.perform(post("/api/auth/forgot-password").with(request->{request.setRemoteAddr("unknown-"+user.getUserID());return request;})
            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("email","unknown-"+user.getEmail()))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var knownResponse = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(response);
        var unknownResponse = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(unknown);
        knownResponse.remove("requestId"); unknownResponse.remove("requestId");
        assertThat(knownResponse).isEqualTo(unknownResponse);
        EmailOutbox mail=emails.findAll().stream().filter(item->item.getRecipient().equals(user.getEmail())).findFirst().orElseThrow();
        var matcher=Pattern.compile("token=([A-Za-z0-9_-]{43})").matcher(mail.getBody());
        assertThat(matcher.find()).isTrue();
        String raw=matcher.group(1);
        assertThat(response).doesNotContain(raw);
        assertThat(resets.findById(raw)).isEmpty();
        assertThat(resets.findById(PasswordResetService.hash(raw))).isPresent();
        mvc.perform(get("/api/admins/email-outbox").header("Authorization","Bearer "+oldToken)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("token",raw,"password","NewStrong123!"))))
            .andExpect(status().isOk());
        mvc.perform(get("/api/auth/validate").header("Authorization","Bearer "+oldToken)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("token",raw,"password","AnotherStrong123!"))))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("email",user.getEmail(),"password","OldStrong123!"))))
            .andExpect(status().isUnauthorized());
        var signedIn=mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("email",user.getEmail(),"password","NewStrong123!"))))
            .andExpect(status().isOk()).andReturn();
        String newToken=json.readTree(signedIn.getResponse().getContentAsString()).at("/data/token").asText();
        assertThat(jwt.validateToken(newToken)).isTrue();
    }

    @Test void expiredLinksAndPasswordsExceedingBcryptByteLimitAreRejected() throws Exception {
        User user=account(User.Role.BIDDER);
        String raw=java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[32]);
        PasswordResetToken reset=new PasswordResetToken();reset.setTokenHash(PasswordResetService.hash(raw));
        reset.setUserID(user.getUserID());reset.setExpiresAt(LocalDateTime.now().minusMinutes(1));resets.saveAndFlush(reset);
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("token",raw,"password","NewStrong123!")))).andExpect(status().isBadRequest());
        reset.setExpiresAt(LocalDateTime.now().plusMinutes(1));resets.saveAndFlush(reset);
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("token",raw,"password","界".repeat(30))))).andExpect(status().isBadRequest());
        assertThat(passwords.matches("OldStrong123!",users.findById(user.getUserID()).orElseThrow().getPasswordHash())).isTrue();
    }

    @Test void notificationReadStateAndEmailPreviewRespectOwnership() throws Exception {
        User owner=account(User.Role.BIDDER), outsider=account(User.Role.BIDDER), admin=account(User.Role.ADMIN);
        var created=notices.notifyUser(owner.getUserID(),"TEST","A private update","/account");
        mvc.perform(get("/api/notifications").header("Authorization",bearer(owner))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].isRead").value(false)).andExpect(jsonPath("$.data[0].link").value("/account"));
        mvc.perform(put("/api/notifications/"+created.getNotificationID()+"/read").header("Authorization",bearer(outsider)))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/notifications/"+created.getNotificationID()+"/read").header("Authorization",bearer(owner)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.isRead").value(true));
        notices.notifyUser(owner.getUserID(),"TEST","Another update","/account");
        mvc.perform(put("/api/notifications/read-all").header("Authorization",bearer(owner))).andExpect(status().isOk());
        assertThat(notifications.findByUserUserID(owner.getUserID())).allMatch(Notification::isRead);
        mvc.perform(get("/api/admins/email-outbox")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admins/email-outbox").header("Authorization",bearer(owner))).andExpect(status().isForbidden());
        var preview=mvc.perform(get("/api/admins/email-outbox").header("Authorization",bearer(admin)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(preview).contains("A private update").contains("LOCAL");
    }

    private String bearer(User value) { return "Bearer "+jwt.generateToken(value.getEmail(),value.getRole().name()); }
    private User account(User.Role role) {
        String id=UUID.randomUUID().toString();
        return users.saveAndFlush(new User(id,"Test",role.name(),id,id+"@example.test","",passwords.encode("OldStrong123!"),role,User.Status.APPROVED));
    }
}
