package com.bidpro;
import com.bidpro.dao.UserDao;
import com.bidpro.entities.User;
import com.bidpro.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/** Even a forged flag cannot expose simulated payments outside the local profile. */
@SpringBootTest(properties={"payments.enabled=false","payments.local-test=true"})
@AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class ProductionPaymentGuardTest {
    @Autowired MockMvc mvc;@Autowired UserDao users;@Autowired JwtUtil jwt;
    @Test void localSandboxCannotBeEnabledOutsideLocalProfile() throws Exception {
        String id=UUID.randomUUID().toString();User user=users.saveAndFlush(new User(id,"Guard","Buyer",id,id+"@example.test","","unused",User.Role.BIDDER,User.Status.APPROVED));
        String bearer="Bearer "+jwt.generateToken(user.getEmail(),user.getRole().name());
        mvc.perform(get("/api/payments/config").header("Authorization",bearer)).andExpect(status().isOk()).andExpect(jsonPath("$.data.enabled").value(false));
        mvc.perform(post("/api/payments/local-test").header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON)
            .content("{\"invoiceID\":\"forged\",\"outcome\":\"SUCCESS\"}")).andExpect(status().isServiceUnavailable());
    }
}
