package com.bidpro;

import com.bidpro.dao.UserDao;
import com.bidpro.entities.User;
import com.bidpro.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConversationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserDao users;
    @Autowired JwtUtil jwt;
    User owner, outsider, admin;

    @BeforeEach void fixture() {
        owner=account(User.Role.BIDDER);outsider=account(User.Role.SELLER);admin=account(User.Role.ADMIN);
    }
    @Test void conversationsArePrivateToTheirOwnerAndSupportAdministrators() throws Exception {
        mvc.perform(get("/api/conversations")).andExpect(status().isUnauthorized());
        JsonNode conversation=create(owner,Map.of("subject","Order question","message","Please help with this auction."));
        String id=conversation.get("threadID").asText();
        assertThat(conversation.get("ownerID").asText()).isEqualTo(owner.getUserID());
        assertThat(conversation.get("messages").get(0).get("senderID").asText()).isEqualTo(owner.getUserID());
        mvc.perform(get("/api/conversations/"+id).header("Authorization",token(outsider))).andExpect(status().isForbidden());
        mvc.perform(post("/api/conversations/"+id+"/messages").header("Authorization",token(outsider))
            .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Intrusion\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/conversations/"+id+"/status").header("Authorization",token(outsider))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/conversations/"+id).header("Authorization",token(admin))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ownerEmail").value(owner.getEmail()));
        mvc.perform(get("/api/conversations").header("Authorization",token(outsider))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
        mvc.perform(get("/api/conversations/"+id).header("Authorization",token(owner))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ownerEmail").isEmpty());
    }
    @Test void onlyAdministratorsCanChooseRecipientsAndUnapprovedAccountsCannotSend() throws Exception {
        mvc.perform(post("/api/conversations").header("Authorization",token(owner)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("subject","Forbidden recipient","message","Hello","recipientUserID",outsider.getUserID()))))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/conversations/recipients").header("Authorization",token(owner))).andExpect(status().isForbidden());
        mvc.perform(get("/api/conversations/recipients").header("Authorization",token(admin))).andExpect(status().isOk());
        var conversation=create(admin,Map.of("subject","Support update","message","Your listing was reviewed.","recipientUserID",owner.getUserID()));
        mvc.perform(get("/api/conversations/"+conversation.get("threadID").asText()).header("Authorization",token(owner)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.messages[0].fromAdmin").value(true));
        owner.setStatus(User.Status.PENDING);users.saveAndFlush(owner);
        mvc.perform(post("/api/conversations").header("Authorization",token(owner)).contentType(MediaType.APPLICATION_JSON)
            .content("{\"subject\":\"Question\",\"message\":\"Hello\"}")).andExpect(status().isForbidden());
    }
    @Test void repliesPersistAndClosedConversationsMustBeReopened() throws Exception {
        String id=create(owner,Map.of("subject","Delivery question","message","Can you check the delivery?"))
            .get("threadID").asText();
        mvc.perform(post("/api/conversations/"+id+"/messages").header("Authorization",token(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"We have contacted the seller.\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.messages.length()").value(2));
        mvc.perform(get("/api/conversations").header("Authorization",token(owner)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].unreadCount").value(1));
        mvc.perform(get("/api/conversations/"+id).header("Authorization",token(owner)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.unreadCount").value(0));
        mvc.perform(put("/api/conversations/"+id+"/status").header("Authorization",token(owner))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("CLOSED"));
        mvc.perform(post("/api/conversations/"+id+"/messages").header("Authorization",token(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"This should fail\"}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/conversations/"+id+"/status").header("Authorization",token(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"OPEN\"}"))
            .andExpect(status().isOk());
        mvc.perform(post("/api/conversations/"+id+"/messages").header("Authorization",token(owner))
            .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Thank you!\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.messages.length()").value(3));
    }
    private JsonNode create(User sender,Map<String,Object> input) throws Exception {
        var result=mvc.perform(post("/api/conversations").header("Authorization",token(sender)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(input))).andExpect(status().isOk()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("data");
    }
    private User account(User.Role role) {
        String id=UUID.randomUUID().toString();
        return users.saveAndFlush(new User(id,"Test",role.name(),id,id+"@example.test","","unused-hash",role,User.Status.APPROVED));
    }
    private String token(User account){return "Bearer "+jwt.generateToken(account.getEmail(),account.getRole().name());}
}
