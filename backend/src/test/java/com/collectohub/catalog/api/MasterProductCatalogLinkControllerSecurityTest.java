package com.collectohub.catalog.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.MasterProductCatalogBackfillService;
import com.collectohub.catalog.application.MasterProductCatalogLinkService;
import com.collectohub.catalog.dto.BackfillMasterProductCatalogLinksResponse;
import com.collectohub.catalog.dto.MasterProductCatalogLinkResponse;
import com.collectohub.config.SecurityConfig;
import com.collectohub.shared.api.GlobalExceptionHandler;
import com.collectohub.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MasterProductCatalogLinkController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        MasterProductCatalogLinkControllerSecurityTest.ServiceConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class MasterProductCatalogLinkControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private MasterProductCatalogLinkService linkService;
    @Autowired private MasterProductCatalogBackfillService backfillService;

    @BeforeEach
    void setUp() {
        reset(linkService, backfillService);
    }

    @Test
    void anonymousAndUserCannotListLinks() throws Exception {
        mockMvc.perform(get("/api/catalog/master-product-links"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/catalog/master-product-links")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListLinks() throws Exception {
        when(linkService.search(
                any(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("createdAt,desc")))
                .thenReturn(new PageResponse<>(List.of(response("PROPOSED")), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/catalog/master-product-links")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].linkStatus").value("PROPOSED"));
    }

    @Test
    void editorialAdminCanListLinks() throws Exception {
        when(linkService.search(
                any(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("createdAt,desc")))
                .thenReturn(new PageResponse<>(List.of(response("PROPOSED")), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/catalog/master-product-links")
                        .header("Authorization", "Bearer " + editorialAdminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousAndUserCannotCreateLink() throws Exception {
        mockMvc.perform(post("/api/catalog/master-product-links")
                        .contentType(MediaType.APPLICATION_JSON).content(request()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/catalog/master-product-links")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON).content(request()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateVerifyRejectAndBackfill() throws Exception {
        when(linkService.create(any(), any())).thenReturn(response("PROPOSED"));
        when(linkService.verify(eq(4L), any())).thenReturn(response("VERIFIED"));
        when(linkService.reject(eq(4L), any())).thenReturn(response("REJECTED"));
        when(backfillService.run(any())).thenReturn(new BackfillMasterProductCatalogLinksResponse(3, 1, 1, 1));

        mockMvc.perform(post("/api/catalog/master-product-links")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content(request()))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/catalog/master-product-links/4/verify")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkStatus").value("VERIFIED"));
        mockMvc.perform(put("/api/catalog/master-product-links/4/reject")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkStatus").value("REJECTED"));
        mockMvc.perform(post("/api/catalog/master-product-links/backfill")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proposed").value(1));
    }

    private String request() {
        return """
                {
                  "masterProductId": 1,
                  "catalogItemId": 2,
                  "catalogItemEditionId": 3,
                  "linkStatus": "PROPOSED",
                  "linkSource": "MANUAL",
                  "confidenceScore": 0.9000,
                  "matchReason": "Manual reconciliation"
                }
                """;
    }

    private MasterProductCatalogLinkResponse response(String status) {
        return new MasterProductCatalogLinkResponse(
                4L, 1L, "Trigun Maximum Vol. 1", 2L, "Trigun Maximum Vol. 1",
                3L, "Spanish edition", status, "MANUAL", new BigDecimal("0.9000"),
                "Manual reconciliation", null, Instant.now(), null
        );
    }

    private String adminToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("admin@example.com", "ADMIN"));
    }

    private String userToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("user@example.com", "USER"));
    }

    private String editorialAdminToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser(
                "editorial-admin@example.com", "EDITORIAL_ADMIN"
        ));
    }

    @TestConfiguration
    static class ServiceConfiguration {
        @Bean
        MasterProductCatalogLinkService masterProductCatalogLinkService() {
            return Mockito.mock(MasterProductCatalogLinkService.class);
        }

        @Bean
        MasterProductCatalogBackfillService masterProductCatalogBackfillService() {
            return Mockito.mock(MasterProductCatalogBackfillService.class);
        }
    }
}
