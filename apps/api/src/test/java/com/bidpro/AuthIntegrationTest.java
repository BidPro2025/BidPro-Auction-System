package com.bidpro;
import com.bidpro.dao.UserDao;
import com.bidpro.entities.User;
import com.bidpro.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
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
class AuthIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired UserDao users; @Autowired PasswordEncoder passwords; @Autowired JwtUtil jwt;
    @Test void healthAndCategoriesArePublicButPrivateRoutesRequireAuthentication() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.data.database").value("UP"));
        mvc.perform(get("/api/categories")).andExpect(status().isOk());
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/auth/validate").header("Authorization","Bearer broken")).andExpect(status().isUnauthorized());
    }
    @Test void signupReturnsValidSessionWithoutExposingCredentialsAndPreventsAdminEscalation() throws Exception {
        Map<String,Object> input=Map.of("firstName","New","lastName","Bidder","username","new-bidder","email","NEW@example.com","password","Strong123!","role","BIDDER");
        var result=mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(input)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.user.status").value("PENDING")).andExpect(jsonPath("$.data.user.passwordHash").doesNotExist()).andReturn();
        JsonNode body=json.readTree(result.getResponse().getContentAsString()); String token=body.at("/data/token").asText();
        assertThat(jwt.validateToken(token)).isTrue(); assertThat(users.findByEmail("new@example.com")).isPresent();
        assertThat(passwords.matches("Strong123!",users.findByEmail("new@example.com").orElseThrow().getPasswordHash())).isTrue();
        mvc.perform(get("/api/auth/validate").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.data.email").value("new@example.com"));
        mvc.perform(get("/api/admins/users").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("firstName","Bad","lastName","Admin","username","bad-admin","email","bad@example.com","password","Strong123!","role","ADMIN"))))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(input))).andExpect(status().isBadRequest());
    }
    @Test void signinAndProfileRespectIdentityAndRole() throws Exception {
        User user=users.save(new User(UUID.randomUUID().toString(),"Test","Seller","test-seller","seller-test@example.com","",passwords.encode("Strong123!"),User.Role.SELLER,User.Status.APPROVED));
        mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("email",user.getEmail(),"password","wrong"))))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid email or password"));
        var result=mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("email",user.getEmail(),"password","Strong123!"))))
            .andExpect(status().isOk()).andReturn();
        String token=json.readTree(result.getResponse().getContentAsString()).at("/data/token").asText();
        mvc.perform(get("/api/messages/1").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/message-recipients/message/1").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        mvc.perform(post("/api/message-recipients").header("Authorization","Bearer "+token)
            .contentType(MediaType.APPLICATION_JSON).content("{\"messageID\":1,\"userID\":\"someone-else\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/users/me").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("firstName","Updated","lastName","Seller","username","updated-seller","phone","1234567890","role","ADMIN","status","APPROVED","email","hacker@example.com"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.firstName").value("Updated")).andExpect(jsonPath("$.data.role").value("SELLER"))
            .andExpect(jsonPath("$.data.email").value("seller-test@example.com"));
        user.setStatus(User.Status.REJECTED); users.saveAndFlush(user);
        mvc.perform(get("/api/users/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
    @Test void malformedRequestsHaveConsistentValidationErrors() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content("{bad"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }
}
