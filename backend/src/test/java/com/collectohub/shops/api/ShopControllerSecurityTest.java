package com.collectohub.shops.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.config.SecurityConfig;
import com.collectohub.shared.api.GlobalExceptionHandler;
import com.collectohub.shops.application.ShopService;
import com.collectohub.shops.application.ShopMembershipAlreadyExistsException;
import com.collectohub.shops.dto.AddShopMemberRequest;
import com.collectohub.shops.dto.ShopMemberResponse;
import com.collectohub.shops.dto.ShopResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShopController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        ShopControllerSecurityTest.ShopServiceTestConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class ShopControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ShopService shopService;

    @BeforeEach
    void setUp() {
        reset(shopService);
    }

    @Test
    void authenticatedUserCreatesShop() throws Exception {
        when(shopService.createShop(any(), any())).thenReturn(privateShopResponse());

        mockMvc.perform(post("/api/shops")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Collector Cave",
                                  "contactEmail": "shop@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Collector Cave"))
                .andExpect(jsonPath("$.currentUserMembership.role").value("OWNER"));
    }

    @Test
    void createShopWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/shops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Collector Cave"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void createShopValidationErrorsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/shops")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contactEmail": "not-an-email",
                                  "defaultReservationExpirationHours": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void userCanListOwnShops() throws Exception {
        when(shopService.myShops(any())).thenReturn(List.of(privateShopResponse()));

        mockMvc.perform(get("/api/shops/my")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].currentUserMembership.role").value("OWNER"));
    }

    @Test
    void publicShopCanBeReadWithoutToken() throws Exception {
        when(shopService.getPublicShop(100L)).thenReturn(publicShopResponse());

        mockMvc.perform(get("/api/shops/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Collector Cave"))
                .andExpect(jsonPath("$.currentUserMembership").doesNotExist());
    }

    @Test
    void userCannotModifyAnotherUsersShop() throws Exception {
        when(shopService.updateShop(any(), eq(100L), any()))
                .thenThrow(new AccessDeniedException("User cannot manage this shop"));

        mockMvc.perform(put("/api/shops/100")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Shop"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void ownerCanModifyShop() throws Exception {
        when(shopService.updateShop(any(), eq(100L), any())).thenReturn(privateShopResponse("Updated Shop"));

        mockMvc.perform(put("/api/shops/100")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Shop"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Updated Shop"))
                .andExpect(jsonPath("$.currentUserMembership.role").value("OWNER"));
    }

    @Test
    void managerCanListMembersWithoutExposingPersonalFields() throws Exception {
        when(shopService.listMembers(any(), eq(100L))).thenReturn(List.of(
                new ShopMemberResponse(200L, 42L, "MANAGER", "ACTIVE"),
                new ShopMemberResponse(201L, 43L, "EMPLOYEE", "ACTIVE")
        ));

        mockMvc.perform(get("/api/shops/100/members")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(200))
                .andExpect(jsonPath("$[0].role").value("MANAGER"))
                .andExpect(jsonPath("$[1].userId").value(43))
                .andExpect(jsonPath("$[1].email").doesNotExist());
    }

    @Test
    void listMembersWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/shops/100/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void employeeCannotListMembers() throws Exception {
        when(shopService.listMembers(any(), eq(100L)))
                .thenThrow(new AccessDeniedException("User cannot list this shop's members"));

        mockMvc.perform(get("/api/shops/100/members")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void ownerCanAddMemberWithoutPersonalFieldsInResponse() throws Exception {
        when(shopService.addMember(any(), eq(100L), any()))
                .thenReturn(new ShopMemberResponse(201L, 43L, "EMPLOYEE", "ACTIVE"));

        mockMvc.perform(post("/api/shops/100/members")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "employee@example.com",
                                  "role": "EMPLOYEE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(201))
                .andExpect(jsonPath("$.userId").value(43))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.name").doesNotExist());
    }

    @Test
    void addMemberNormalizesEmailBeforeValidation() throws Exception {
        when(shopService.addMember(any(), eq(100L), any()))
                .thenReturn(new ShopMemberResponse(201L, 43L, "EMPLOYEE", "ACTIVE"));

        mockMvc.perform(post("/api/shops/100/members")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": " EMPLOYEE@EXAMPLE.COM ",
                                  "role": "EMPLOYEE"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(shopService).addMember(any(), eq(100L),
                org.mockito.ArgumentMatchers.argThat((AddShopMemberRequest request) ->
                        request.email().equals("EMPLOYEE@EXAMPLE.COM")));
    }

    @Test
    void addMemberRequiresAuthenticationAndValidEmail() throws Exception {
        String body = """
                {
                  "email": "not-an-email",
                  "role": "EMPLOYEE"
                }
                """;

        mockMvc.perform(post("/api/shops/100/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/shops/100/members")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMemberRejectsOwnerRoleAndMapsDuplicateToConflict() throws Exception {
        mockMvc.perform(post("/api/shops/100/members")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "employee@example.com",
                                  "role": "OWNER"
                                }
                                """))
                .andExpect(status().isBadRequest());

        when(shopService.addMember(any(), eq(100L), any()))
                .thenThrow(new ShopMembershipAlreadyExistsException());
        mockMvc.perform(post("/api/shops/100/members")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "employee@example.com",
                                  "role": "EMPLOYEE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    private String token() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("alice@example.com"));
    }

    private ShopResponse publicShopResponse() {
        return new ShopResponse(
                100L,
                "Collector Cave",
                "Rare items",
                "shop@example.com",
                null,
                null,
                "EUR",
                48,
                null,
                "ACTIVE",
                null
        );
    }

    private ShopResponse privateShopResponse() {
        return privateShopResponse("Collector Cave");
    }

    private ShopResponse privateShopResponse(String name) {
        return new ShopResponse(
                100L,
                name,
                "Rare items",
                "shop@example.com",
                null,
                null,
                "EUR",
                48,
                null,
                "ACTIVE",
                new com.collectohub.shops.dto.ShopMemberResponse(200L, 42L, "OWNER", "ACTIVE")
        );
    }

    @TestConfiguration
    static class ShopServiceTestConfiguration {

        @Bean
        ShopService shopService() {
            return Mockito.mock(ShopService.class);
        }
    }
}
