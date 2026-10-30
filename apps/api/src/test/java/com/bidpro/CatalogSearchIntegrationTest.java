package com.bidpro;

import com.bidpro.dao.*;
import com.bidpro.entities.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogSearchIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProductDao products;
    @Autowired UserDao users;
    @Autowired CategoryDao categories;
    @Autowired BidDao bids;
    String term;
    User seller;
    Category category;
    Product boosted, plain, upcoming, ended, sold;

    @BeforeEach
    void fixture() {
        term = "catalog-" + UUID.randomUUID();
        String id = UUID.randomUUID().toString();
        seller = users.saveAndFlush(new User(id, "Catalog", "Seller", "catalog-" + id.substring(0, 8),
                id + "@example.test", "", "unused", User.Role.BOTH, User.Status.APPROVED));
        category = category();
        boosted = product("first", Product.ProductStatus.APPROVED, category, -5, 20, "100.00", 5);
        plain = product("second", Product.ProductStatus.APPROVED, category, -5, 40, "200.00", 4);
        upcoming = product("upcoming", Product.ProductStatus.APPROVED, category, 20, 60, "300.00", 3);
        ended = product("ended", Product.ProductStatus.APPROVED, category, -60, -10, "400.00", 2);
        sold = product("sold", Product.ProductStatus.SOLD, category(), -60, -20, "500.00", 1);
        product("pending", Product.ProductStatus.PENDING, category, -5, 20, "1.00", 0);
        product("rejected", Product.ProductStatus.REJECTED, category, -5, 20, "2.00", 0);
        Bid high = new Bid();
        high.setBidID(UUID.randomUUID().toString());
        high.setProductID(boosted.getProductID());
        high.setBidderID(seller.getUserID());
        high.setAmount(new BigDecimal("350.00"));
        high.setTimestamp(LocalDateTime.now().minusMinutes(1));
        bids.saveAndFlush(high);
    }

    @Test
    void paginationIsPublicStableAndHasAccurateTotals() throws Exception {
        JsonNode first = search(Map.of("size", "2"));
        JsonNode second = search(Map.of("size", "2", "page", "1"));
        assertEquals(5, first.get("totalItems").asLong());
        assertEquals(3, first.get("totalPages").asInt());
        assertEquals(0, first.get("page").asInt());
        assertEquals(2, first.get("size").asInt());
        assertEquals(2, first.get("items").size());
        assertEquals(boosted.getProductID(), first.at("/items/0/productID").asText());
        assertNotEquals(first.at("/items/0/productID"), second.at("/items/0/productID"));
        assertEquals(first, search(Map.of("size", "2")), "Repeated reads have deterministic tie-breaking");
        JsonNode beyond = search(Map.of("page", "99", "size", "2"));
        assertEquals(0, beyond.get("items").size());
        assertEquals(5, beyond.get("totalItems").asLong());
        assertEquals(12, search(Map.of()).get("size").asInt());
    }

    @Test
    void filtersAndOrderingUsePublishedDatabaseStateAndHighestBid() throws Exception {
        JsonNode live = search(Map.of("status", "Live", "sort", "price-low"));
        assertEquals(2, live.get("totalItems").asLong());
        assertEquals(plain.getProductID(), live.at("/items/0/productID").asText(), "Highest bid, not the starting price, controls ordering");
        assertEquals(boosted.getProductID(), live.at("/items/1/productID").asText());
        assertEquals(350, live.at("/items/1/currentBid").asInt());
        JsonNode descending = search(Map.of("status", "Live", "sort", "price-high"));
        assertEquals(boosted.getProductID(), descending.at("/items/0/productID").asText());
        assertEquals(upcoming.getProductID(), search(Map.of("status", "Upcoming")).at("/items/0/productID").asText());
        assertEquals(ended.getProductID(), search(Map.of("status", "Ended")).at("/items/0/productID").asText());
        assertEquals(sold.getProductID(), search(Map.of("status", "Sold")).at("/items/0/productID").asText());
        assertEquals(sold.getProductID(), search(Map.of("sort", "newest")).at("/items/0/productID").asText());
        assertEquals(4, search(Map.of("category", category.getCategoryID())).get("totalItems").asLong());
        JsonNode literal = search(Map.of("q", term + "%"));
        assertEquals(0, literal.get("totalItems").asLong(), "SQL wildcards in a search term are treated literally");
        assertEquals(0, search(Map.of("q", term + "-PENDING")).get("totalItems").asLong());
        assertEquals(1, search(Map.of("q", term.toUpperCase() + "-FIRST")).get("totalItems").asLong());
    }

    @Test
    void invalidPageSizeSortAndStatusAreRejected() throws Exception {
        for (Map<String,String> input : java.util.List.of(Map.of("page", "-1"), Map.of("size", "0"),
                Map.of("size", "49"), Map.of("size", "48", "page", "2147483647"),
                Map.of("sort", "unknown"), Map.of("status", "PENDING"), Map.of("q", "x".repeat(201)))) {
            mvc.perform(request(input)).andExpect(status().isBadRequest());
        }
    }

    private JsonNode search(Map<String,String> values) throws Exception {
        return json.readTree(mvc.perform(request(values)).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString()).get("data");
    }

    private MockHttpServletRequestBuilder request(Map<String,String> values) {
        var request = get("/api/products/search").param("q", values.getOrDefault("q", term));
        values.forEach((key, value) -> { if (!"q".equals(key)) request.param(key, value); });
        return request;
    }

    private Category category() {
        Category result = new Category();
        result.setCategoryID(UUID.randomUUID().toString());
        result.setName("Catalog " + result.getCategoryID());
        return categories.saveAndFlush(result);
    }

    private Product product(String suffix, Product.ProductStatus status, Category category,
            int startMinutes, int endMinutes, String startingPrice, int ageDays) {
        Product result = new Product();
        result.setProductID(UUID.randomUUID().toString());
        result.setTitle(term + "-" + suffix);
        result.setDescription("Database search fixture");
        result.setSellerID(seller.getUserID());
        result.setCategoryID(category.getCategoryID());
        result.setProductStatus(status);
        result.setAuctionStartDate(LocalDateTime.now().plusMinutes(startMinutes));
        result.setAuctionEndDate(LocalDateTime.now().plusMinutes(endMinutes));
        result.setStartingPrice(new BigDecimal(startingPrice));
        result.setIncrementGap(new BigDecimal("5.00"));
        result.setProductCondition(Product.ProductCondition.USED);
        result.setPromotionStatus(Product.PromotionStatus.NONE);
        result.setCreatedAt(LocalDateTime.now().minusDays(ageDays));
        return products.saveAndFlush(result);
    }
}
