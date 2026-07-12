package com.collectohub.catalog.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.*;
import com.collectohub.catalog.application.*;
import com.collectohub.catalog.dto.*;
import com.collectohub.config.SecurityConfig;
import com.collectohub.shared.api.GlobalExceptionHandler;
import com.collectohub.shared.dto.PageResponse;
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

@WebMvcTest(controllers = {CreatorController.class, CatalogItemCreatorController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, GlobalExceptionHandler.class,
        TestSecurityConfiguration.class, CreatorControllerSecurityTest.Services.class})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class CreatorControllerSecurityTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Autowired CreatorService creatorService;
    @Autowired CatalogItemCreatorService creditService;

    @BeforeEach void resetMocks() { reset(creatorService, creditService); }

    @Test void publicGetsCreatorsAndItemCredits() throws Exception {
        when(creatorService.search(isNull(), isNull(), isNull(), eq(0), eq(20), eq("name,asc")))
                .thenReturn(new PageResponse<>(List.of(creator()), 0, 20, 1, 1, true, true));
        when(creditService.listPublic(20L)).thenReturn(List.of(credit()));
        mockMvc.perform(get("/api/catalog/creators")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("akira-toriyama"));
        mockMvc.perform(get("/api/catalog/items/20/creators")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].creditRole").value("AUTHOR"));
    }

    @Test void adminCanCreateCreatorAndCredit() throws Exception {
        when(creatorService.create(any(), any())).thenReturn(creator());
        when(creditService.create(eq(20L), any(), any())).thenReturn(credit());
        mockMvc.perform(post("/api/catalog/creators").header("Authorization", bearer("admin", "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(creatorJson())).andExpect(status().isCreated());
        mockMvc.perform(post("/api/catalog/items/20/creators").header("Authorization", bearer("admin", "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(creditJson())).andExpect(status().isCreated());
    }

    @Test void editorialAdminCanCreateCreatorAndCredit() throws Exception {
        when(creatorService.create(any(), any())).thenReturn(creator());
        when(creditService.create(eq(20L), any(), any())).thenReturn(credit());
        mockMvc.perform(post("/api/catalog/creators").header("Authorization", bearer("editorial-admin", "EDITORIAL_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(creatorJson())).andExpect(status().isCreated());
        mockMvc.perform(post("/api/catalog/items/20/creators").header("Authorization", bearer("editorial-admin", "EDITORIAL_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(creditJson())).andExpect(status().isCreated());
    }

    @Test void regularUserCannotCreateUpdateOrDelete() throws Exception {
        String token = bearer("user", "USER");
        mockMvc.perform(post("/api/catalog/creators").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(creatorJson())).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/catalog/items/20/creators/40").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"creditRole\":\"WRITER\",\"creditOrder\":2}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/catalog/creators/10").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test void anonymousWritesRequireAuthentication() throws Exception {
        mockMvc.perform(delete("/api/catalog/items/20/creators/40")).andExpect(status().isUnauthorized());
    }

    private String bearer(String prefix, String role) {
        return "Bearer " + jwtService.generateAccessToken(
                TestSecurityConfiguration.testUser(prefix + "@example.com", role));
    }
    private String creatorJson() { return "{\"name\":\"Akira Toriyama\",\"recordStatus\":\"ACTIVE\"}"; }
    private String creditJson() { return "{\"creatorId\":10,\"creditRole\":\"AUTHOR\",\"creditOrder\":1}"; }
    private CreatorResponse creator() { return new CreatorResponse(10L, "Akira Toriyama", "akira-toriyama", null,
            null, "JP", 1955, null, "ACTIVE"); }
    private CatalogItemCreatorResponse credit() { return new CatalogItemCreatorResponse(
            40L, 20L, 10L, "Akira Toriyama", "akira-toriyama", "AUTHOR", 1, null); }

    @TestConfiguration static class Services {
        @Bean CreatorService creatorService() { return Mockito.mock(CreatorService.class); }
        @Bean CatalogItemCreatorService catalogItemCreatorService() { return Mockito.mock(CatalogItemCreatorService.class); }
    }
}
