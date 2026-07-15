package com.collectohub.collections.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.MasterProductNotFoundException;
import com.collectohub.collections.application.CollectionNotFoundException;
import com.collectohub.collections.application.CollectionService;
import com.collectohub.collections.application.CollectionProgressService;
import com.collectohub.collections.dto.CollectionItemResponse;
import com.collectohub.collections.dto.CollectionResponse;
import com.collectohub.collections.dto.LinkManualCollectionItemRequest;
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
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
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
    void ownerAddsManualItemThroughExistingEndpoint() throws Exception {
        when(collectionService.addItem(any(), eq(100L), any())).thenReturn(manualItemResponse());

        mockMvc.perform(post("/api/collections/100/items")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "manualTitle": "Edición promocional",
                                  "collectionStatus": "OWNED"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.manualTitle").value("Edición promocional"))
                .andExpect(jsonPath("$.editorialReferenceSource").value("MANUAL"))
                .andExpect(jsonPath("$.referenceKind").value("MANUAL"))
                .andExpect(jsonPath("$.masterProductId").value(nullValue()))
                .andExpect(jsonPath("$.catalogItemId").value(nullValue()));

        verify(collectionService).addItem(any(), eq(100L), any());
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
    void ownerUpdatesManualItemThroughExistingEndpoint() throws Exception {
        when(collectionService.updateItem(any(), eq(100L), eq(300L), any())).thenReturn(manualItemResponse());

        mockMvc.perform(put("/api/collections/100/items/300")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "manualTitle": "Nuevo título",
                                  "manualDescription": "",
                                  "manualType": "Libro"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manualTitle").value("Edición promocional"))
                .andExpect(jsonPath("$.manualDescription").value("Descripción"))
                .andExpect(jsonPath("$.manualType").value("Libro"));

        verify(collectionService).updateItem(any(), eq(100L), eq(300L), any());
    }

    @Test
    void ownerLinksManualItemToCatalogThroughDedicatedEndpoint() throws Exception {
        when(collectionService.linkManualItemToCatalog(any(), eq(100L), eq(300L), any()))
                .thenReturn(publicItemResponse());

        mockMvc.perform(put("/api/collections/100/items/300/catalog-reference")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catalogItemId\":500,\"catalogItemEditionId\":600}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogItemId").value(500))
                .andExpect(jsonPath("$.catalogItemEditionId").value(600))
                .andExpect(jsonPath("$.manualTitle").value(nullValue()))
                .andExpect(jsonPath("$.editorialReferenceSource").value("MANUAL_EDITORIAL"));

        ArgumentCaptor<LinkManualCollectionItemRequest> captor = ArgumentCaptor.forClass(LinkManualCollectionItemRequest.class);
        verify(collectionService).linkManualItemToCatalog(any(), eq(100L), eq(300L), captor.capture());
        assertThat(captor.getValue().catalogItemId()).isEqualTo(500L);
        assertThat(captor.getValue().catalogItemEditionId()).isEqualTo(600L);
    }

    @Test
    void ownerDeletesItem() throws Exception {
        mockMvc.perform(delete("/api/collections/100/items/300")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void publicCollectionResponseSerializesSanitizedItemFieldsAndEditorialReferences() throws Exception {
        when(collectionService.getCollection(eq(null), eq(100L))).thenReturn(collectionResponse(publicItemResponse()));

        mockMvc.perform(get("/api/collections/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].notes").value(nullValue()))
                .andExpect(jsonPath("$.items[0].acquiredAt").value(nullValue()))
                .andExpect(jsonPath("$.items[0].referenceKind").value("DIRECT_CATALOG"))
                .andExpect(jsonPath("$.items[0].catalogItemId").value(500))
                .andExpect(jsonPath("$.items[0].catalogItemEditionId").value(600))
                .andExpect(jsonPath("$.items[0].editorialReferenceSource").value("MANUAL_EDITORIAL"));
    }

    @Test
    void ownerCollectionResponseSerializesPrivateItemFields() throws Exception {
        when(collectionService.getCollection(any(), eq(100L))).thenReturn(collectionResponse(itemResponse()));

        mockMvc.perform(get("/api/collections/100")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].notes").value("First print"))
                .andExpect(jsonPath("$.items[0].acquiredAt").value("2023-06-01"));
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
        return collectionResponse(name, itemResponse());
    }

    private CollectionResponse collectionResponse(CollectionItemResponse item) {
        return collectionResponse("Manga", item);
    }

    private CollectionResponse collectionResponse(String name, CollectionItemResponse item) {
        return new CollectionResponse(
                100L,
                42L,
                name,
                "My manga",
                "PUBLIC",
                "MANGA_COMIC",
                "Manga and comic",
                List.of(item)
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
                "LEGACY_UNRESOLVED",
                null,
                null,
                null,
                status,
                "LIKE_NEW",
                "1",
                50,
                "First print",
                LocalDate.of(2023, 6, 1)
        );
    }

    private CollectionItemResponse publicItemResponse() {
        return new CollectionItemResponse(
                300L, 100L, 200L, "Dragon Ball 1", "MANGA_COMIC", "Dragon Ball", "Tankobon", "1",
                500L, "Dragon Ball 1", "1", 400L, "Dragon Ball", 600L, "Edition", "PAPERBACK",
                null, null, null, null, null, "MANUAL_EDITORIAL", "DIRECT_CATALOG", null, null, null, "OWNED", "LIKE_NEW",
                "1", 50, null, null
        );
    }

    private CollectionItemResponse manualItemResponse() {
        return new CollectionItemResponse(
                300L, 100L, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, "MANUAL", "MANUAL",
                "Edición promocional", "Descripción", "Libro", "OWNED", "LIKE_NEW",
                "1", 50, "First print", LocalDate.of(2023, 6, 1)
        );
    }

    @Test
    void catalogReferenceRequiresAuthenticationAndCatalogItemId() throws Exception {
        mockMvc.perform(put("/api/collections/100/items/300/catalog-reference")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"catalogItemId\":500}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(put("/api/collections/100/items/300/catalog-reference")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void catalogReferenceMapsConflictAndNotFoundErrors() throws Exception {
        when(collectionService.linkManualItemToCatalog(any(), eq(100L), eq(300L), any()))
                .thenThrow(new com.collectohub.collections.application.ConflictingCollectionItemReferenceException("incompatible"));
        mockMvc.perform(put("/api/collections/100/items/300/catalog-reference")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"catalogItemId\":500}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        when(collectionService.linkManualItemToCatalog(any(), eq(100L), eq(300L), any()))
                .thenThrow(new com.collectohub.collections.application.CollectionItemNotFoundException(300L));
        mockMvc.perform(put("/api/collections/100/items/300/catalog-reference")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"catalogItemId\":500}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @TestConfiguration
    static class CollectionServiceTestConfiguration {

        @Bean
        CollectionService collectionService() {
            return Mockito.mock(CollectionService.class);
        }

        @Bean
        CollectionProgressService collectionProgressService() {
            return Mockito.mock(CollectionProgressService.class);
        }
    }
}
