package com.collectohub.collections.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.MasterProductNotFoundException;
import com.collectohub.collections.application.CollectionNotFoundException;
import com.collectohub.collections.application.CollectionService;
import com.collectohub.collections.dto.CollectionItemResponse;
import com.collectohub.collections.dto.CollectionResponse;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CollectionController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        CollectionControllerSecurityTest.CollectionServiceTestConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class CollectionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CollectionService collectionService;

    @BeforeEach
    void setUp() {
        reset(collectionService);
    }

    @Test
    void authenticatedUserCreatesCollection() throws Exception {
        when(collectionService.createCollection(any(), any())).thenReturn(collectionResponse());

        mockMvc.perform(post("/api/collections")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Manga",
                                  "description": "My manga",
                                  "visibility": "PUBLIC",
                                  "categoryCode": "MANGA_COMIC"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void createCollectionWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Manga"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void createCollectionWithBlankNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/collections")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void userCanListOwnCollections() throws Exception {
        when(collectionService.myCollections(any(), eq(null), eq(null))).thenReturn(List.of(collectionResponse()));

        mockMvc.perform(get("/api/collections/my")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void userCanFilterOwnCollectionsByVisibility() throws Exception {
        when(collectionService.myCollections(any(), any(), eq("MANGA_COMIC"))).thenReturn(List.of(collectionResponse()));

        mockMvc.perform(get("/api/collections/my")
                        .param("visibility", "PUBLIC")
                        .param("categoryCode", "MANGA_COMIC")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].visibility").value("PUBLIC"));
    }

    @Test
    void publicCollectionCanBeReadWithoutToken() throws Exception {
        when(collectionService.getCollection(null, 100L)).thenReturn(collectionResponse());

        mockMvc.perform(get("/api/collections/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.items[0].id").value(300));
    }

    @Test
    void privateForeignCollectionIsNotExposed() throws Exception {
        when(collectionService.getCollection(null, 100L)).thenThrow(new CollectionNotFoundException(100L));

        mockMvc.perform(get("/api/collections/100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void ownerUpdatesCollection() throws Exception {
        when(collectionService.updateCollection(any(), eq(100L), any())).thenReturn(collectionResponse("Updated"));

        mockMvc.perform(put("/api/collections/100")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void otherUserCannotUpdateCollection() throws Exception {
        when(collectionService.updateCollection(any(), eq(100L), any()))
                .thenThrow(new AccessDeniedException("User cannot manage this collection"));

        mockMvc.perform(put("/api/collections/100")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void ownerDeletesCollection() throws Exception {
        mockMvc.perform(delete("/api/collections/100")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void ownerAddsItem() throws Exception {
        when(collectionService.addItem(any(), eq(100L), any())).thenReturn(itemResponse());

        mockMvc.perform(post("/api/collections/100/items")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateItemRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(300))
                .andExpect(jsonPath("$.collectionStatus").value("OWNED"));
    }

    @Test
    void addItemWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/collections/100/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateItemRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void addItemToForeignCollectionReturnsForbidden() throws Exception {
        when(collectionService.addItem(any(), eq(100L), any()))
                .thenThrow(new AccessDeniedException("User cannot manage this collection"));

        mockMvc.perform(post("/api/collections/100/items")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateItemRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void addItemWithMissingMasterProductReturnsNotFound() throws Exception {
        when(collectionService.addItem(any(), eq(100L), any())).thenThrow(new MasterProductNotFoundException(200L));

        mockMvc.perform(post("/api/collections/100/items")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateItemRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void addItemWithNegativeLimitedUnitsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/collections/100/items")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "masterProductId": 200,
                                  "collectionStatus": "OWNED",
                                  "totalLimitedUnits": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void ownerListsCollectionItems() throws Exception {
        when(collectionService.listItems(any(), eq(100L))).thenReturn(List.of(itemResponse()));

        mockMvc.perform(get("/api/collections/100/items")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(300));
    }

    @Test
    void publicCollectionItemsCanBeListedWithoutToken() throws Exception {
        when(collectionService.listItems(null, 100L)).thenReturn(List.of(itemResponse()));

        mockMvc.perform(get("/api/collections/100/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(300));
    }

    @Test
    void privateForeignCollectionItemsAreNotExposed() throws Exception {
        when(collectionService.listItems(null, 100L)).thenThrow(new CollectionNotFoundException(100L));

        mockMvc.perform(get("/api/collections/100/items"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void ownerUpdatesItem() throws Exception {
        when(collectionService.updateItem(any(), eq(100L), eq(300L), any())).thenReturn(itemResponse("SELLABLE"));

        mockMvc.perform(put("/api/collections/100/items/300")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "collectionStatus": "SELLABLE",
                                  "physicalCondition": "GOOD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collectionStatus").value("SELLABLE"));
    }

    @Test
    void otherUserCannotUpdateItem() throws Exception {
        when(collectionService.updateItem(any(), eq(100L), eq(300L), any()))
                .thenThrow(new AccessDeniedException("User cannot manage this collection"));

        mockMvc.perform(put("/api/collections/100/items/300")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "collectionStatus": "SELLABLE"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void ownerDeletesItem() throws Exception {
        mockMvc.perform(delete("/api/collections/100/items/300")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());
    }

    private String userToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("alice@example.com"));
    }

    private String validCreateItemRequest() {
        return """
                {
                  "masterProductId": 200,
                  "collectionStatus": "OWNED",
                  "physicalCondition": "LIKE_NEW",
                  "unitNumber": "1",
                  "totalLimitedUnits": 50,
                  "notes": "First print",
                  "acquiredAt": "2023-06-01"
                }
                """;
    }

    private CollectionResponse collectionResponse() {
        return collectionResponse("Manga");
    }

    private CollectionResponse collectionResponse(String name) {
        return new CollectionResponse(
                100L,
                42L,
                name,
                "My manga",
                "PUBLIC",
                "MANGA_COMIC",
                "Manga and comic",
                List.of(itemResponse())
        );
    }

    private CollectionItemResponse itemResponse() {
        return itemResponse("OWNED");
    }

    private CollectionItemResponse itemResponse(String status) {
        return new CollectionItemResponse(
                300L,
                100L,
                200L,
                "Dragon Ball 1",
                "MANGA_COMIC",
                "Dragon Ball",
                "Tankobon",
                "1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "LEGACY",
                status,
                "LIKE_NEW",
                "1",
                50,
                "First print",
                LocalDate.of(2023, 6, 1)
        );
    }

    @TestConfiguration
    static class CollectionServiceTestConfiguration {

        @Bean
        CollectionService collectionService() {
            return Mockito.mock(CollectionService.class);
        }
    }
}
