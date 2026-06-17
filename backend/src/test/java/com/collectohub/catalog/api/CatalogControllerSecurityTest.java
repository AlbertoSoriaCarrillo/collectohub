package com.collectohub.catalog.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.catalog.application.CatalogService;
import com.collectohub.catalog.application.DuplicateMasterProductException;
import com.collectohub.catalog.dto.MasterProductResponse;
import com.collectohub.catalog.dto.ProductCategoryResponse;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductCategoryController.class, MasterProductController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        CatalogControllerSecurityTest.CatalogServiceTestConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class CatalogControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        reset(catalogService);
    }

    @Test
    void publicCategoriesCanBeListedWithoutToken() throws Exception {
        when(catalogService.listCategories()).thenReturn(List.of(
                new ProductCategoryResponse(1L, "MANGA_COMIC", "Manga and comic", null),
                new ProductCategoryResponse(2L, "TRADING_CARD", "Trading card", null),
                new ProductCategoryResponse(3L, "FIGURE", "Figure", null),
                new ProductCategoryResponse(4L, "VIDEOGAME", "Videogame", null),
                new ProductCategoryResponse(5L, "MERCHANDISING", "Merchandising", null),
                new ProductCategoryResponse(6L, "MOVIE_SERIES", "Movie and series", null)
        ));

        mockMvc.perform(get("/api/product-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MANGA_COMIC"))
                .andExpect(jsonPath("$[5].code").value("MOVIE_SERIES"));
    }

    @Test
    void publicMasterProductsCanBeListedWithoutToken() throws Exception {
        when(catalogService.searchMasterProducts(null, null, null, null, null, null))
                .thenReturn(List.of(masterProductResponse()));

        mockMvc.perform(get("/api/master-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].name").value("Dragon Ball 1"));
    }

    @Test
    void publicMasterProductCanBeReadWithoutToken() throws Exception {
        when(catalogService.getMasterProduct(100L)).thenReturn(masterProductResponse());

        mockMvc.perform(get("/api/master-products/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.category.code").value("MANGA_COMIC"));
    }

    @Test
    void shopOwnerCreatesMasterProduct() throws Exception {
        when(catalogService.createMasterProduct(any(), any())).thenReturn(masterProductResponse());

        mockMvc.perform(post("/api/master-products")
                        .header("Authorization", "Bearer " + shopOwnerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.limitedEditionTotalUnits").value(500));
    }

    @Test
    void createMasterProductWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/master-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void regularUserWithoutShopOwnerCannotCreateMasterProduct() throws Exception {
        when(catalogService.createMasterProduct(any(), any()))
                .thenThrow(new AccessDeniedException("User cannot manage master products"));

        mockMvc.perform(post("/api/master-products")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void duplicateMasterProductByIsbnReturnsConflict() throws Exception {
        when(catalogService.createMasterProduct(any(), any()))
                .thenThrow(new DuplicateMasterProductException("isbn already exists"));

        mockMvc.perform(post("/api/master-products")
                        .header("Authorization", "Bearer " + shopOwnerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void duplicateMasterProductByEanReturnsConflict() throws Exception {
        when(catalogService.createMasterProduct(any(), any()))
                .thenThrow(new DuplicateMasterProductException("ean already exists"));

        mockMvc.perform(post("/api/master-products")
                        .header("Authorization", "Bearer " + shopOwnerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void duplicateMasterProductByLogicalCombinationReturnsConflict() throws Exception {
        when(catalogService.createMasterProduct(any(), any()))
                .thenThrow(new DuplicateMasterProductException("logical combination already exists"));

        mockMvc.perform(post("/api/master-products")
                        .header("Authorization", "Bearer " + shopOwnerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createMasterProductValidationErrorsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/master-products")
                        .header("Authorization", "Bearer " + shopOwnerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "9788490000001"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shopOwnerUpdatesMasterProduct() throws Exception {
        when(catalogService.updateMasterProduct(any(), eq(100L), any())).thenReturn(masterProductResponse("Updated"));

        mockMvc.perform(put("/api/master-products/100")
                        .header("Authorization", "Bearer " + shopOwnerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    private String shopOwnerToken() {
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
                  "name": "Dragon Ball 1",
                  "categoryCode": "MANGA_COMIC",
                  "franchise": "Dragon Ball",
                  "collectionName": "Tankobon",
                  "volumeNumber": "1",
                  "publisher": "Planeta",
                  "isbn": "9788490000001",
                  "language": "es",
                  "limitedEdition": true,
                  "limitedEditionTotalUnits": 500,
                  "publicationCountries": ["ES", "FR"],
                  "attributes": {
                    "format": "paperback"
                  }
                }
                """;
    }

    private MasterProductResponse masterProductResponse() {
        return masterProductResponse("Dragon Ball 1");
    }

    private MasterProductResponse masterProductResponse(String name) {
        return new MasterProductResponse(
                100L,
                name,
                "First volume",
                new ProductCategoryResponse(1L, "MANGA_COMIC", "Manga and comic", null),
                "Dragon Ball",
                "Tankobon",
                "1",
                "Planeta",
                "9788490000001",
                null,
                LocalDate.of(1985, 9, 10),
                null,
                null,
                "es",
                true,
                500,
                List.of("ES", "FR"),
                null,
                "ACTIVE",
                Map.of("limitedEditionTotalUnits", 500, "format", "paperback")
        );
    }

    @TestConfiguration
    static class CatalogServiceTestConfiguration {

        @Bean
        CatalogService catalogService() {
            return Mockito.mock(CatalogService.class);
        }
    }
}
