package com.collectohub.catalog.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.CatalogFranchiseNotFoundException;
import com.collectohub.catalog.application.CatalogFranchiseService;
import com.collectohub.catalog.application.CatalogSeriesService;
import com.collectohub.catalog.application.DuplicateEditorialCatalogException;
import com.collectohub.catalog.application.PublisherNotFoundException;
import com.collectohub.catalog.application.PublisherService;
import com.collectohub.catalog.dto.CatalogFranchiseResponse;
import com.collectohub.catalog.dto.CatalogSeriesResponse;
import com.collectohub.catalog.dto.PublisherResponse;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        PublisherController.class,
        CatalogFranchiseController.class,
        CatalogSeriesController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        EditorialCatalogControllerSecurityTest.ServiceConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class EditorialCatalogControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private CatalogFranchiseService franchiseService;

    @Autowired
    private CatalogSeriesService seriesService;

    @BeforeEach
    void setUp() {
        reset(publisherService, franchiseService, seriesService);
    }

    @Test
    void publicListsActivePublishers() throws Exception {
        when(publisherService.search(isNull(), isNull(), isNull(), eq(0), eq(20), eq("name,asc")))
                .thenReturn(page(publisherResponse()));

        mockMvc.perform(get("/api/catalog/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Planeta"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void publicGetsActivePublisher() throws Exception {
        when(publisherService.get(10L, null)).thenReturn(publisherResponse());

        mockMvc.perform(get("/api/catalog/publishers/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordStatus").value("ACTIVE"));
    }

    @Test
    void publicCannotUseAdministrativeRecordStatusFilter() throws Exception {
        when(publisherService.search(isNull(), isNull(), eq("DRAFT"), eq(0), eq(20), eq("name,asc")))
                .thenThrow(new AccessDeniedException("recordStatus filter requires ADMIN authority"));

        mockMvc.perform(get("/api/catalog/publishers").param("recordStatus", "DRAFT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicCannotGetDraftPublisher() throws Exception {
        when(publisherService.get(10L, null)).thenThrow(new PublisherNotFoundException(10L));

        mockMvc.perform(get("/api/catalog/publishers/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCreatesPublisher() throws Exception {
        when(publisherService.create(any(), any())).thenReturn(publisherResponse());

        mockMvc.perform(post("/api/catalog/publishers")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publisherRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void regularUserCannotCreatePublisher() throws Exception {
        mockMvc.perform(post("/api/catalog/publishers")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publisherRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotCreatePublisher() throws Exception {
        mockMvc.perform(post("/api/catalog/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publisherRequest()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicatePublisherReturnsConflict() throws Exception {
        when(publisherService.create(any(), any()))
                .thenThrow(new DuplicateEditorialCatalogException("publisher", "name already exists"));

        mockMvc.perform(post("/api/catalog/publishers")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publisherRequest()))
                .andExpect(status().isConflict());
    }

    @Test
    void publicListsActiveFranchises() throws Exception {
        when(franchiseService.search(isNull(), isNull(), isNull(), eq(0), eq(20), eq("name,asc")))
                .thenReturn(page(franchiseResponse()));

        mockMvc.perform(get("/api/catalog/franchises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("dragon-ball"));
    }

    @Test
    void adminCreatesFranchise() throws Exception {
        when(franchiseService.create(any(), any())).thenReturn(franchiseResponse());

        mockMvc.perform(post("/api/catalog/franchises")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchiseRequest("dragon-ball")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dragon Ball"));
    }

    @Test
    void invalidFranchiseSlugReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/catalog/franchises")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchiseRequest("Dragon Ball")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateFranchiseSlugReturnsConflict() throws Exception {
        when(franchiseService.create(any(), any()))
                .thenThrow(new DuplicateEditorialCatalogException("catalog franchise", "slug already exists"));

        mockMvc.perform(post("/api/catalog/franchises")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchiseRequest("dragon-ball")))
                .andExpect(status().isConflict());
    }

    @Test
    void regularUserCannotCreateFranchise() throws Exception {
        mockMvc.perform(post("/api/catalog/franchises")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchiseRequest("dragon-ball")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shopOwnerCannotCreateFranchise() throws Exception {
        mockMvc.perform(post("/api/catalog/franchises")
                        .header("Authorization", "Bearer " + shopOwnerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchiseRequest("dragon-ball")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotCreateFranchise() throws Exception {
        mockMvc.perform(post("/api/catalog/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franchiseRequest("dragon-ball")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicListsSeriesWithFilters() throws Exception {
        when(seriesService.search(
                isNull(),
                eq("trigun"),
                eq(20L),
                eq("MANGA"),
                eq("COMPLETED"),
                eq(10L),
                eq("ja"),
                eq("JP"),
                isNull(),
                eq(0),
                eq(20),
                eq("title,asc")
        )).thenReturn(page(seriesResponse(null, null)));

        mockMvc.perform(get("/api/catalog/series")
                        .param("q", "trigun")
                        .param("franchiseId", "20")
                        .param("type", "MANGA")
                        .param("publicationStatus", "COMPLETED")
                        .param("publisherId", "10")
                        .param("language", "ja")
                        .param("country", "JP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Trigun Maximum"));
    }

    @Test
    void adminCreatesSeriesWithoutFranchise() throws Exception {
        when(seriesService.create(any(), any())).thenReturn(seriesResponse(null, null));

        mockMvc.perform(post("/api/catalog/series")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesRequest(null, null, 1997, 2007)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.franchiseId").doesNotExist());
    }

    @Test
    void adminCreatesSeriesWithFranchiseAndPublisher() throws Exception {
        when(seriesService.create(any(), any())).thenReturn(seriesResponse(20L, 10L));

        mockMvc.perform(post("/api/catalog/series")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesRequest(20L, 10L, 1997, 2007)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.franchiseId").value(20))
                .andExpect(jsonPath("$.primaryPublisherId").value(10));
    }

    @Test
    void missingFranchiseReturnsNotFound() throws Exception {
        when(seriesService.create(any(), any())).thenThrow(new CatalogFranchiseNotFoundException(99L));

        mockMvc.perform(post("/api/catalog/series")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesRequest(99L, null, 1997, 2007)))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingPublisherReturnsNotFound() throws Exception {
        when(seriesService.create(any(), any())).thenThrow(new PublisherNotFoundException(99L));

        mockMvc.perform(post("/api/catalog/series")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesRequest(null, 99L, 1997, 2007)))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidYearRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/catalog/series")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesRequest(null, null, 2007, 1997)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularUserCannotCreateSeries() throws Exception {
        mockMvc.perform(post("/api/catalog/series")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesRequest(null, null, 1997, 2007)))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotCreateSeries() throws Exception {
        mockMvc.perform(post("/api/catalog/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesRequest(null, null, 1997, 2007)))
                .andExpect(status().isUnauthorized());
    }

    private String adminToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("admin@example.com", "ADMIN"));
    }

    private String userToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("alice@example.com", "USER"));
    }

    private String shopOwnerToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser(
                "shop-owner@example.com",
                "USER",
                "SHOP_OWNER"
        ));
    }

    private String publisherRequest() {
        return """
                {
                  "name": "Planeta",
                  "country": "ES",
                  "recordStatus": "ACTIVE"
                }
                """;
    }

    private String franchiseRequest(String slug) {
        return """
                {
                  "name": "Dragon Ball",
                  "slug": "%s",
                  "description": "Manga franchise",
                  "recordStatus": "ACTIVE"
                }
                """.formatted(slug);
    }

    private String seriesRequest(Long franchiseId, Long publisherId, int startYear, int endYear) {
        String franchise = franchiseId == null ? "null" : franchiseId.toString();
        String publisher = publisherId == null ? "null" : publisherId.toString();
        return """
                {
                  "franchiseId": %s,
                  "primaryPublisherId": %s,
                  "title": "Trigun Maximum",
                  "type": "MANGA",
                  "publicationStatus": "COMPLETED",
                  "originCountry": "JP",
                  "originalLanguage": "ja",
                  "startYear": %d,
                  "endYear": %d,
                  "recordStatus": "ACTIVE"
                }
                """.formatted(franchise, publisher, startYear, endYear);
    }

    private PublisherResponse publisherResponse() {
        return new PublisherResponse(10L, "Planeta", "ES", "ACTIVE", Instant.now(), null);
    }

    private CatalogFranchiseResponse franchiseResponse() {
        return new CatalogFranchiseResponse(
                20L,
                "Dragon Ball",
                "dragon-ball",
                "Manga franchise",
                "ACTIVE",
                Instant.now(),
                null
        );
    }

    private CatalogSeriesResponse seriesResponse(Long franchiseId, Long publisherId) {
        return new CatalogSeriesResponse(
                30L,
                franchiseId,
                franchiseId == null ? null : "Trigun",
                publisherId,
                publisherId == null ? null : "Dark Horse",
                "Trigun Maximum",
                null,
                "MANGA",
                "COMPLETED",
                "Manga series",
                "JP",
                "ja",
                1997,
                2007,
                "ACTIVE",
                Instant.now(),
                null
        );
    }

    private <T> PageResponse<T> page(T content) {
        return new PageResponse<>(List.of(content), 0, 20, 1, 1, true, true);
    }

    @TestConfiguration
    static class ServiceConfiguration {

        @Bean
        PublisherService publisherService() {
            return Mockito.mock(PublisherService.class);
        }

        @Bean
        CatalogFranchiseService catalogFranchiseService() {
            return Mockito.mock(CatalogFranchiseService.class);
        }

        @Bean
        CatalogSeriesService catalogSeriesService() {
            return Mockito.mock(CatalogSeriesService.class);
        }
    }
}
