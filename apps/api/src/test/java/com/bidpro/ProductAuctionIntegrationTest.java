package com.bidpro;

import com.bidpro.dao.*;
import com.bidpro.entities.*;
import com.bidpro.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductAuctionIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserDao users;
    @Autowired CategoryDao categories;
    @Autowired ProductDao products;
    @Autowired AuctionDao auctions;
    @Autowired JwtUtil jwt;
    User seller, otherSeller, bidder, admin;
    Category category;

    @BeforeEach void fixture() {
        seller = account(User.Role.SELLER);
        otherSeller = account(User.Role.SELLER);
        bidder = account(User.Role.BIDDER);
        admin = account(User.Role.ADMIN);
        category = new Category();
        category.setCategoryID(UUID.randomUUID().toString());
        category.setName("Product fixture");
        categories.saveAndFlush(category);
    }

    @Test void ownershipStatusAndModerationAreEnforcedByTheServer() throws Exception {
        JsonNode product = createListing();
        String id = product.get("productID").asText();
        assertThat(product.get("sellerID").asText()).isEqualTo(seller.getUserID());
        assertThat(product.get("productStatus").asText()).isEqualTo("PENDING");
        mvc.perform(get("/api/products/" + id)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/products/" + id).header("Authorization", token(otherSeller))).andExpect(status().isForbidden());
        mvc.perform(post("/api/products").header("Authorization", token(bidder)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(input()))).andExpect(status().isForbidden());
        mvc.perform(put("/api/admins/products/" + id + "/status").header("Authorization", token(seller))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\"}")).andExpect(status().isForbidden());
        var approved = mvc.perform(put("/api/admins/products/" + id + "/status").header("Authorization", token(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.productStatus").value("APPROVED")).andReturn();
        String auctionID = json.readTree(approved.getResponse().getContentAsString()).at("/data/auctionID").asText();
        assertThat(auctions.findById(auctionID)).isPresent();
        mvc.perform(get("/api/products/" + id)).andExpect(status().isOk());
        mvc.perform(put("/api/products/" + id).header("Authorization", token(seller)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(input()))).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/products/" + id).header("Authorization", token(seller))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auctions/" + auctionID + "/register").header("Authorization", token(bidder))).andExpect(status().isOk());
        mvc.perform(post("/api/auctions/" + auctionID + "/close").header("Authorization", token(bidder))).andExpect(status().isForbidden());
        mvc.perform(post("/api/auctions/" + auctionID + "/close").header("Authorization", token(admin)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PAST"));
        assertThat(products.findById(id).orElseThrow().getAuctionEndDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test void invalidDatesAndImagesDoNotBecomeListingsOrUploads() throws Exception {
        Map<String,Object> invalid = new java.util.HashMap<>(input());
        invalid.put("auctionEndDate", LocalDateTime.now().minusMinutes(10).toString());
        mvc.perform(post("/api/products").header("Authorization", token(seller)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(invalid))).andExpect(status().isBadRequest());
        String id = createListing().get("productID").asText();
        MockMultipartFile disguised = new MockMultipartFile("file", "image.png", "image/png", "not an image".getBytes());
        mvc.perform(multipart("/api/file-upload/product-image").file(disguised).param("productID", id)
            .header("Authorization", token(seller))).andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/file-upload/product-image").file(disguised).param("productID", id)
            .header("Authorization", token(otherSeller))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/products/" + id).header("Authorization", token(seller))).andExpect(status().isOk());
        assertThat(products.findById(id)).isEmpty();
    }

    private JsonNode createListing() throws Exception {
        var result = mvc.perform(post("/api/products").header("Authorization", token(seller))
            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(input())))
            .andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("data");
    }
    private Map<String,Object> input() {
        return Map.of("title", "Camera", "description", "Test listing", "categoryID", category.getCategoryID(),
            "auctionStartDate", LocalDateTime.now().minusMinutes(1).toString(),
            "auctionEndDate", LocalDateTime.now().plusHours(1).toString(), "startingPrice", 100,
            "incrementGap", 10, "productCondition", "USED", "sellerID", "forged-id", "productStatus", "APPROVED");
    }
    private User account(User.Role role) {
        String id = UUID.randomUUID().toString();
        return users.saveAndFlush(new User(id,"Test",role.name(),id,id+"@example.test","","unused-hash",role,User.Status.APPROVED));
    }
    private String token(User account) { return "Bearer " + jwt.generateToken(account.getEmail(), account.getRole().name()); }
}
