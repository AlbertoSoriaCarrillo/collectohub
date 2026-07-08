package com.collectohub.catalog.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.*;
import com.collectohub.catalog.application.CatalogItemRelationshipService;
import com.collectohub.catalog.dto.CatalogItemRelationshipResponse;
import com.collectohub.config.SecurityConfig;
import com.collectohub.shared.api.GlobalExceptionHandler;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogItemRelationshipController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, GlobalExceptionHandler.class,
        TestSecurityConfiguration.class, CatalogItemRelationshipControllerSecurityTest.Services.class})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class CatalogItemRelationshipControllerSecurityTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Autowired CatalogItemRelationshipService service;

    @BeforeEach void resetMock() { reset(service); }

    @Test void getRelationshipsAndRelationshipArePublic() throws Exception {
        when(service.listRelationships(10L, null, null)).thenReturn(List.of(response()));
        when(service.get(10L, 30L, null, null)).thenReturn(response());
        mockMvc.perform(get("/api/catalog/items/10/relationships"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].direction").value("OUTGOING"));
        mockMvc.perform(get("/api/catalog/items/10/relationships/30"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.relationshipType").value("SEQUEL"));
    }

    @Test void anonymousWritesRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/catalog/items/10/relationships")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson())).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/catalog/items/10/relationships/30")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson())).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/catalog/items/10/relationships/30")).andExpect(status().isUnauthorized());
    }

    @Test void regularUserCannotWriteRelationships() throws Exception {
        String authorization = bearer("USER");
        mockMvc.perform(post("/api/catalog/items/10/relationships").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(requestJson())).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/catalog/items/10/relationships/30").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(requestJson())).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/catalog/items/10/relationships/30").header("Authorization", authorization))
                .andExpect(status().isForbidden());
    }

    @Test void adminCanCreateUpdateAndDeleteRelationships() throws Exception {
        String authorization = bearer("ADMIN");
        when(service.create(eq(10L), any(), any())).thenReturn(response());
        when(service.update(eq(10L), eq(30L), any(), any())).thenReturn(response());
        mockMvc.perform(post("/api/catalog/items/10/relationships").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(requestJson())).andExpect(status().isCreated());
        mockMvc.perform(put("/api/catalog/items/10/relationships/30").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content(requestJson())).andExpect(status().isOk());
        mockMvc.perform(delete("/api/catalog/items/10/relationships/30").header("Authorization", authorization))
                .andExpect(status().isNoContent());
    }

    private String bearer(String role) {
        return "Bearer " + jwtService.generateAccessToken(
                TestSecurityConfiguration.testUser(role.toLowerCase() + "@example.com", role));
    }
    private String requestJson() {
        return "{\"targetCatalogItemId\":20,\"relationshipType\":\"SEQUEL\",\"relationshipOrder\":1,\"recordStatus\":\"ACTIVE\"}";
    }
    private CatalogItemRelationshipResponse response() {
        return new CatalogItemRelationshipResponse(30L, 10L, "Source", 110L, "Source Series",
                20L, "Target", 120L, "Target Series", "SEQUEL", 1, null, "ACTIVE", "OUTGOING");
    }

    @TestConfiguration static class Services {
        @Bean CatalogItemRelationshipService catalogItemRelationshipService() {
            return Mockito.mock(CatalogItemRelationshipService.class);
        }
    }
}
