package com.collectohub.inventory.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.MasterProductNotFoundException;
import com.collectohub.config.SecurityConfig;
import com.collectohub.inventory.application.InventoryService;
import com.collectohub.inventory.application.ShopProductNotFoundException;
import com.collectohub.inventory.dto.ShopProductResponse;
import com.collectohub.inventory.dto.PublicShopProductResponse;
import com.collectohub.shared.api.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ShopInventoryController.class, PublicShopProductController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        InventoryControllerSecurityTest.InventoryServiceTestConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class InventoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        reset(inventoryService);
    }

    @Test
    void ownerCreatesShopProduct() throws Exception {
        when(inventoryService.createShopProduct(any(), eq(100L), any())).thenReturn(shopProductResponse());

        mockMvc.perform(post("/api/shops/100/products")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(300))
                .andExpect(jsonPath("$.shopId").value(100))
                .andExpect(jsonPath("$.commercialStatus").value("AVAILABLE"));
    }

    @Test
    void createShopProductWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/shops/100/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void userOutsideShopCannotCreateShopProduct() throws Exception {
        when(inventoryService.createShopProduct(any(), eq(100L), any()))
                .thenThrow(new AccessDeniedException("User cannot manage this shop inventory"));

        mockMvc.perform(post("/api/shops/100/products")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void missingMasterProductReturnsNotFound() throws Exception {
        when(inventoryService.createShopProduct(any(), eq(100L), any()))
                .thenThrow(new MasterProductNotFoundException(200L));

        mockMvc.perform(post("/api/shops/100/products")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createShopProductWithNegativePriceReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/shops/100/products")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "masterProductId": 200,
                                  "priceAmount": -1,
                                  "stockQuantity": 2,
                                  "physicalCondition": "NEW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createShopProductWithNegativeStockReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/shops/100/products")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "masterProductId": 200,
                                  "priceAmount": 9.99,
                                  "stockQuantity": -1,
                                  "physicalCondition": "NEW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void memberCanListOwnShopInventory() throws Exception {
        when(inventoryService.myShopProducts(any(), eq(100L))).thenReturn(List.of(shopProductResponse()));

        mockMvc.perform(get("/api/shops/100/products/my")
                        .header("Authorization", "Bearer " + ownerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(300));
    }

    @Test
    void publicShopProductsCanBeListedWithoutToken() throws Exception {
        when(inventoryService.publicShopProducts(100L, null, null, null, null, null, null, null))
                .thenReturn(List.of(PublicShopProductResponse.from(shopProductResponse())));

        mockMvc.perform(get("/api/shops/100/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].visible").value(true))
                .andExpect(jsonPath("$[0].commercialStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].notes").doesNotExist());
    }

    @Test
    void publicEndpointDoesNotReturnHiddenProductDetails() throws Exception {
        when(inventoryService.getPublicShopProduct(300L)).thenThrow(new ShopProductNotFoundException(300L));

        mockMvc.perform(get("/api/shop-products/300"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void ownerUpdatesShopProduct() throws Exception {
        when(inventoryService.updateShopProduct(any(), eq(100L), eq(300L), any()))
                .thenReturn(shopProductResponse("RESERVED"));

        mockMvc.perform(put("/api/shops/100/products/300")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "commercialStatus": "RESERVED",
                                  "stockQuantity": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(300))
                .andExpect(jsonPath("$.commercialStatus").value("RESERVED"));
    }

    @Test
    void userOutsideShopCannotUpdateShopProduct() throws Exception {
        when(inventoryService.updateShopProduct(any(), eq(100L), eq(300L), any()))
                .thenThrow(new AccessDeniedException("User cannot manage this shop inventory"));

        mockMvc.perform(put("/api/shops/100/products/300")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stockQuantity": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void cannotUpdateShopProductThroughAnotherShopId() throws Exception {
        when(inventoryService.updateShopProduct(any(), eq(101L), eq(300L), any()))
                .thenThrow(new ShopProductNotFoundException(300L));

        mockMvc.perform(put("/api/shops/101/products/300")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stockQuantity": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String ownerToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser(
                "shop-owner@example.com",
                "USER",
                "SHOP_OWNER"
        ));
    }

    private String userToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("alice@example.com"));
    }

    private String validCreateRequest() {
        return """
                {
                  "masterProductId": 200,
                  "priceAmount": 9.99,
                  "stockQuantity": 2,
                  "physicalCondition": "NEW"
                }
                """;
    }

    private ShopProductResponse shopProductResponse() {
        return shopProductResponse("AVAILABLE");
    }

    private ShopProductResponse shopProductResponse(String commercialStatus) {
        return new ShopProductResponse(
                300L,
                100L,
                200L,
                "Dragon Ball 1",
                "MANGA_COMIC",
                "Dragon Ball",
                "Tankobon",
                "1",
                new BigDecimal("9.99"),
                "EUR",
                2,
                commercialStatus,
                "NEW",
                true,
                null,
                null,
                null
        );
    }

    @TestConfiguration
    static class InventoryServiceTestConfiguration {

        @Bean
        InventoryService inventoryService() {
            return Mockito.mock(InventoryService.class);
        }
    }
}
