package com.collectohub.recommendations.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.InvalidCatalogFilterException;
import com.collectohub.config.SecurityConfig;
import com.collectohub.recommendations.application.RecommendationService;
import com.collectohub.recommendations.dto.RecommendationReasonResponse;
import com.collectohub.recommendations.dto.RecommendedShopProductResponse;
import com.collectohub.recommendations.dto.UserRecommendationResponse;
import com.collectohub.recommendations.dto.UserRecommendationSummaryResponse;
import com.collectohub.shared.api.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecommendationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        RecommendationControllerSecurityTest.RecommendationServiceTestConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class RecommendationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        reset(recommendationService);
    }

    @Test
    void recommendationsWithoutTokenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/recommendations/my"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void authenticatedUserGetsRecommendations() throws Exception {
        when(recommendationService.myRecommendations(any(), eq("MANGA_COMIC"), eq("10"), eq("EUR"), eq("NEW"), eq("500")))
                .thenReturn(UserRecommendationResponse.from(List.of(recommendation())));

        mockMvc.perform(get("/api/recommendations/my")
                        .param("categoryCode", "MANGA_COMIC")
                        .param("maxPrice", "10")
                        .param("currency", "EUR")
                        .param("physicalCondition", "NEW")
                        .param("shopId", "500")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecommendations").value(1))
                .andExpect(jsonPath("$.recommendations[0].shopProductId").value(900))
                .andExpect(jsonPath("$.recommendations[0].recommendationReason.code").value("COLLECTION_ITEM_MISSING"))
                .andExpect(jsonPath("$.recommendations[0].matchedCollectionItemStatus").value("MISSING"));
    }

    @Test
    void invalidFilterReturnsBadRequest() throws Exception {
        when(recommendationService.myRecommendations(any(), eq(null), eq("-1"), eq(null), eq(null), eq(null)))
                .thenThrow(new InvalidCatalogFilterException("maxPrice must be greater than or equal to 0"));

        mockMvc.perform(get("/api/recommendations/my")
                        .param("maxPrice", "-1")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void authenticatedUserGetsSummary() throws Exception {
        when(recommendationService.mySummary(any(), eq(null), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(new UserRecommendationSummaryResponse(2, 1, 3, 2, List.of("FIGURE", "MANGA_COMIC")));

        mockMvc.perform(get("/api/recommendations/my/summary")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingCollectionItems").value(2))
                .andExpect(jsonPath("$.wantedCollectionItems").value(1))
                .andExpect(jsonPath("$.recommendedProducts").value(3))
                .andExpect(jsonPath("$.matchedShops").value(2))
                .andExpect(jsonPath("$.matchedCategoryCodes[0]").value("FIGURE"));
    }

    private String userToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("alice@example.com"));
    }

    private RecommendedShopProductResponse recommendation() {
        return new RecommendedShopProductResponse(
                900L,
                500L,
                "Collector Cave",
                200L,
                "Dragon Ball 1",
                "MANGA_COMIC",
                "Dragon Ball",
                "Tankobon",
                "1",
                "https://example.test/cover.jpg",
                new BigDecimal("9.99"),
                "EUR",
                2,
                "NEW",
                "AVAILABLE",
                new RecommendationReasonResponse("COLLECTION_ITEM_MISSING", "Product marked as missing in one of your collections"),
                100L,
                "Wishlist",
                "MISSING"
        );
    }

    @TestConfiguration
    static class RecommendationServiceTestConfiguration {

        @Bean
        RecommendationService recommendationService() {
            return Mockito.mock(RecommendationService.class);
        }
    }
}
