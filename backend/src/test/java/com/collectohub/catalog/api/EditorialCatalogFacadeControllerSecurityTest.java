package com.collectohub.catalog.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.EditorialCatalogFacadeService;
import com.collectohub.catalog.application.InvalidCatalogFilterException;
import com.collectohub.catalog.dto.EditorialCatalogSearchItemResponse;
import com.collectohub.catalog.dto.EditorialLegacyBridgeResponse;
import com.collectohub.shared.dto.PageResponse;
import com.collectohub.config.SecurityConfig;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EditorialCatalogFacadeController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        EditorialCatalogFacadeControllerSecurityTest.ServiceConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class EditorialCatalogFacadeControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private EditorialCatalogFacadeService facadeService;

    @BeforeEach
    void setUp() {
        reset(facadeService);
    }

    @Test
    void anonymousCanSearchPublicEditorialCatalog() throws Exception {
        when(facadeService.search(
                isNull(), eq("trigun"), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), eq(0), eq(20), eq("title,asc")))
                .thenReturn(searchResponse());

        mockMvc.perform(get("/api/catalog/editorial/search").param("q", "trigun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].resultType").value("SERIES"));
    }

    @Test
    void regularUserCanSearchPublicEditorialCatalog() throws Exception {
        when(facadeService.search(
                any(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), eq(0), eq(20), eq("title,asc")))
                .thenReturn(searchResponse());

        mockMvc.perform(get("/api/catalog/editorial/search")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk());
    }

    @Test
    void legacyLinkRequiresAdmin() throws Exception {
        mockMvc.perform(get("/api/catalog/editorial/master-products/8/link"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/catalog/editorial/master-products/8/link")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadVerifiedLegacyLink() throws Exception {
        when(facadeService.getLegacyLink(eq(8L), any())).thenReturn(bridgeResponse());

        mockMvc.perform(get("/api/catalog/editorial/master-products/8/link")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkStatus").value("VERIFIED"));
    }

    @Test
    void invalidPaginationReturnsBadRequest() throws Exception {
        when(facadeService.search(
                nullable(AuthenticatedUser.class), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                eq(-1), anyInt(), anyString()))
                .thenThrow(new InvalidCatalogFilterException("page must be greater than or equal to 0"));

        mockMvc.perform(get("/api/catalog/editorial/search").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidFilterReturnsBadRequest() throws Exception {
        when(facadeService.search(
                nullable(AuthenticatedUser.class), any(), eq("MOVIE"), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString()))
                .thenThrow(new InvalidCatalogFilterException("Unsupported type: MOVIE"));

        mockMvc.perform(get("/api/catalog/editorial/search").param("type", "MOVIE"))
                .andExpect(status().isBadRequest());
    }

    private PageResponse<EditorialCatalogSearchItemResponse> searchResponse() {
        return new PageResponse<>(List.of(new EditorialCatalogSearchItemResponse(
                "SERIES", 1L, "Trigun", null, null, null, null, "Dark Horse",
                "Trigun", "MANGA", "ja", "JP", 1995, null, null, null
        )), 0, 20, 1, 1, true, true);
    }

    private EditorialLegacyBridgeResponse bridgeResponse() {
        return new EditorialLegacyBridgeResponse(
                4L, 8L, "Trigun Volume 1", "VERIFIED", "MANUAL", BigDecimal.ONE,
                "Reviewed", 2L, "Volume 1", null, null);
    }

    private String adminToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("admin@example.com", "ADMIN"));
    }

    private String userToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("user@example.com", "USER"));
    }

    @TestConfiguration
    static class ServiceConfiguration {
        @Bean
        EditorialCatalogFacadeService editorialCatalogFacadeService() {
            return Mockito.mock(EditorialCatalogFacadeService.class);
        }
    }
}
