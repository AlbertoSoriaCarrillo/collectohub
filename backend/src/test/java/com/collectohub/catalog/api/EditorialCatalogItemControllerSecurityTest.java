package com.collectohub.catalog.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.CatalogItemEditionNotFoundException;
import com.collectohub.catalog.application.CatalogItemEditionService;
import com.collectohub.catalog.application.CatalogItemNotFoundException;
import com.collectohub.catalog.application.CatalogItemService;
import com.collectohub.catalog.application.CatalogSeriesNotFoundException;
import com.collectohub.catalog.application.DuplicateEditorialCatalogException;
import com.collectohub.catalog.application.PublisherNotFoundException;
import com.collectohub.catalog.dto.CatalogItemEditionResponse;
import com.collectohub.catalog.dto.CatalogItemResponse;
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
import java.time.LocalDate;
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

@WebMvcTest(controllers = {CatalogItemController.class, CatalogItemEditionController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        EditorialCatalogItemControllerSecurityTest.ServiceConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class EditorialCatalogItemControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CatalogItemService itemService;

    @Autowired
    private CatalogItemEditionService editionService;

    @BeforeEach
    void setUp() {
        reset(itemService, editionService);
    }

    @Test
    void publicListsActiveItems() throws Exception {
        when(itemService.search(
                eq(30L), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("sortOrder,asc")))
                .thenReturn(page(itemResponse("ACTIVE")));

        mockMvc.perform(get("/api/catalog/series/30/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Trigun Maximum Vol. 1"));
    }

    @Test
    void adminListsDraftItemsWithFilter() throws Exception {
        when(itemService.search(
                eq(30L), any(), isNull(), isNull(), isNull(), isNull(), eq("DRAFT"),
                eq(0), eq(20), eq("sortOrder,asc")))
                .thenReturn(page(itemResponse("DRAFT")));

        mockMvc.perform(get("/api/catalog/series/30/items")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("recordStatus", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].recordStatus").value("DRAFT"));
    }

    @Test
    void editorialAdminListsDraftItemsAndCreatesItem() throws Exception {
        when(itemService.search(
                eq(30L), any(), isNull(), isNull(), isNull(), isNull(), eq("DRAFT"),
                eq(0), eq(20), eq("sortOrder,asc")))
                .thenReturn(page(itemResponse("DRAFT")));
        when(itemService.create(eq(30L), any(), any())).thenReturn(itemResponse("ACTIVE"));

        mockMvc.perform(get("/api/catalog/series/30/items")
                        .header("Authorization", "Bearer " + editorialAdminToken())
                        .param("recordStatus", "DRAFT"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/catalog/series/30/items")
                        .header("Authorization", "Bearer " + editorialAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequest(1997, "1.000")))
                .andExpect(status().isCreated());
    }

    @Test
    void publicGetsActiveItem() throws Exception {
        when(itemService.get(40L, null)).thenReturn(itemResponse("ACTIVE"));

        mockMvc.perform(get("/api/catalog/items/40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seriesId").value(30));
    }

    @Test
    void publicCannotGetDraftItem() throws Exception {
        when(itemService.get(40L, null)).thenThrow(new CatalogItemNotFoundException(40L));

        mockMvc.perform(get("/api/catalog/items/40"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCreatesItem() throws Exception {
        when(itemService.create(eq(30L), any(), any())).thenReturn(itemResponse("ACTIVE"));

        mockMvc.perform(post("/api/catalog/series/30/items")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequest(1997, "1.000")))
                .andExpect(status().isCreated());
    }

    @Test
    void missingSeriesReturnsNotFound() throws Exception {
        when(itemService.create(eq(99L), any(), any())).thenThrow(new CatalogSeriesNotFoundException(99L));

        mockMvc.perform(post("/api/catalog/series/99/items")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequest(1997, "1.000")))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateItemReturnsConflict() throws Exception {
        when(itemService.create(eq(30L), any(), any()))
                .thenThrow(new DuplicateEditorialCatalogException("catalog item", "already exists"));

        mockMvc.perform(post("/api/catalog/series/30/items")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequest(1997, "1.000")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidItemYearAndSortOrderReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/catalog/series/30/items")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequest(999, "-1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userAndAnonymousCannotCreateItem() throws Exception {
        mockMvc.perform(post("/api/catalog/series/30/items")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequest(1997, "1.000")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/catalog/series/30/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequest(1997, "1.000")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicListsActiveEditions() throws Exception {
        when(editionService.search(
                eq(40L), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("publicationYear,asc")))
                .thenReturn(page(editionResponse("ACTIVE")));

        mockMvc.perform(get("/api/catalog/items/40/editions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].format").value("PAPERBACK"));
    }

    @Test
    void adminListsDraftEditionsWithFilter() throws Exception {
        when(editionService.search(
                eq(40L), any(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq("DRAFT"),
                eq(0), eq(20), eq("publicationYear,asc")))
                .thenReturn(page(editionResponse("DRAFT")));

        mockMvc.perform(get("/api/catalog/items/40/editions")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("recordStatus", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].recordStatus").value("DRAFT"));
    }

    @Test
    void publicGetsActiveEdition() throws Exception {
        when(editionService.get(50L, null)).thenReturn(editionResponse("ACTIVE"));

        mockMvc.perform(get("/api/catalog/editions/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogItemId").value(40));
    }

    @Test
    void publicCannotGetDraftEdition() throws Exception {
        when(editionService.get(50L, null)).thenThrow(new CatalogItemEditionNotFoundException(50L));

        mockMvc.perform(get("/api/catalog/editions/50"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCreatesEdition() throws Exception {
        when(editionService.create(eq(40L), any(), any())).thenReturn(editionResponse("ACTIVE"));

        mockMvc.perform(post("/api/catalog/items/40/editions")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editionRequest(2001, 240)))
                .andExpect(status().isCreated());
    }

    @Test
    void missingItemAndPublisherReturnNotFound() throws Exception {
        when(editionService.create(eq(99L), any(), any())).thenThrow(new CatalogItemNotFoundException(99L));
        mockMvc.perform(post("/api/catalog/items/99/editions")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editionRequest(2001, 240)))
                .andExpect(status().isNotFound());

        when(editionService.create(eq(40L), any(), any())).thenThrow(new PublisherNotFoundException(10L));
        mockMvc.perform(post("/api/catalog/items/40/editions")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editionRequest(2001, 240)))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidEditionYearAndPageCountReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/catalog/items/40/editions")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editionRequest(999, 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateIsbnAndEanReturnConflict() throws Exception {
        when(editionService.create(eq(40L), any(), any()))
                .thenThrow(new DuplicateEditorialCatalogException("catalog item edition", "ISBN already exists"))
                .thenThrow(new DuplicateEditorialCatalogException("catalog item edition", "EAN already exists"));

        mockMvc.perform(post("/api/catalog/items/40/editions")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editionRequest(2001, 240)))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/catalog/items/40/editions")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editionRequest(2001, 240)))
                .andExpect(status().isConflict());
    }

    @Test
    void userAndAnonymousCannotCreateEdition() throws Exception {
        mockMvc.perform(post("/api/catalog/items/40/editions")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editionRequest(2001, 240)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/catalog/items/40/editions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editionRequest(2001, 240)))
                .andExpect(status().isUnauthorized());
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

    private String itemRequest(int year, String sortOrder) {
        return """
                {
                  "title": "Trigun Maximum Vol. 1",
                  "sequenceLabel": "1",
                  "sortOrder": %s,
                  "firstPublicationYear": %d,
                  "originalLanguage": "ja",
                  "originCountry": "JP",
                  "recordStatus": "ACTIVE"
                }
                """.formatted(sortOrder, year);
    }

    private String editionRequest(int year, int pageCount) {
        return """
                {
                  "publisherId": 10,
                  "isbn": "978-84-1234-567-8",
                  "ean": "8437012345678",
                  "format": "PAPERBACK",
                  "editionName": "Spanish edition",
                  "publicationYear": %d,
                  "language": "es",
                  "country": "ES",
                  "pageCount": %d,
                  "recordStatus": "ACTIVE"
                }
                """.formatted(year, pageCount);
    }

    private CatalogItemResponse itemResponse(String status) {
        return new CatalogItemResponse(
                40L, 30L, "Trigun Maximum", "Trigun Maximum Vol. 1", null, "1",
                new BigDecimal("1.000"), null, null, 1997, "ja", "JP", status,
                Instant.now(), null
        );
    }

    private CatalogItemEditionResponse editionResponse(String status) {
        return new CatalogItemEditionResponse(
                50L, 40L, "Trigun Maximum Vol. 1", 10L, "Dark Horse",
                "9788412345678", "8437012345678", "PAPERBACK", "Spanish edition",
                LocalDate.of(2001, 1, 1), 2001, "es", "ES", 240,
                null, status, Instant.now(), null
        );
    }

    private <T> PageResponse<T> page(T content) {
        return new PageResponse<>(List.of(content), 0, 20, 1, 1, true, true);
    }

    @TestConfiguration
    static class ServiceConfiguration {

        @Bean
        CatalogItemService catalogItemService() {
            return Mockito.mock(CatalogItemService.class);
        }

        @Bean
        CatalogItemEditionService catalogItemEditionService() {
            return Mockito.mock(CatalogItemEditionService.class);
        }
    }
}
