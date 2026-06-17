package com.collectohub.reservations.api;

import com.collectohub.TestSecurityConfiguration;
import com.collectohub.auth.security.JwtAuthenticationFilter;
import com.collectohub.auth.security.JwtService;
import com.collectohub.config.SecurityConfig;
import com.collectohub.inventory.application.ShopProductNotFoundException;
import com.collectohub.reservations.application.InvalidReservationTransitionException;
import com.collectohub.reservations.application.ReservationNotFoundException;
import com.collectohub.reservations.application.ReservationService;
import com.collectohub.reservations.application.ReservationUnavailableException;
import com.collectohub.reservations.dto.ReservationResponse;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReservationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class,
        TestSecurityConfiguration.class,
        ReservationControllerSecurityTest.ReservationServiceTestConfiguration.class
})
@TestPropertySource(properties = "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production")
class ReservationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reset(reservationService);
    }

    @Test
    void createsReservationWithToken() throws Exception {
        when(reservationService.createReservation(any(), any())).thenReturn(reservationResponse("PENDING"));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shopProductId": 900,
                                  "quantity": 1,
                                  "userMessage": "Please hold it"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(700))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createReservationWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shopProductId": 900
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void createReservationForMissingProductReturnsNotFound() throws Exception {
        when(reservationService.createReservation(any(), any())).thenThrow(new ShopProductNotFoundException(900L));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shopProductId": 900
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReservationForUnavailableProductReturnsConflict() throws Exception {
        when(reservationService.createReservation(any(), any()))
                .thenThrow(new ReservationUnavailableException("Shop product cannot be reserved"));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shopProductId": 900
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createReservationWithNegativeQuantityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shopProductId": 900,
                                  "quantity": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void userListsOwnReservations() throws Exception {
        when(reservationService.myReservations(any(), eq("PENDING"), eq(500L))).thenReturn(List.of(reservationResponse("PENDING")));

        mockMvc.perform(get("/api/reservations/my")
                        .param("status", "PENDING")
                        .param("shopId", "500")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(700));
    }

    @Test
    void userGetsReservationDetail() throws Exception {
        when(reservationService.getReservation(any(), eq(700L))).thenReturn(reservationResponse("PENDING"));

        mockMvc.perform(get("/api/reservations/700")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(700));
    }

    @Test
    void foreignReservationDetailReturnsForbidden() throws Exception {
        when(reservationService.getReservation(any(), eq(700L)))
                .thenThrow(new AccessDeniedException("User cannot access this reservation"));

        mockMvc.perform(get("/api/reservations/700")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shopOwnerListsShopReservations() throws Exception {
        when(reservationService.shopReservations(any(), eq(500L), eq("PENDING"), eq(42L), eq(900L)))
                .thenReturn(List.of(reservationResponse("PENDING")));

        mockMvc.perform(get("/api/shops/500/reservations")
                        .param("status", "PENDING")
                        .param("userId", "42")
                        .param("shopProductId", "900")
                        .header("Authorization", "Bearer " + ownerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shopId").value(500));
    }

    @Test
    void userOutsideShopCannotListShopReservations() throws Exception {
        when(reservationService.shopReservations(any(), eq(500L), eq(null), eq(null), eq(null)))
                .thenThrow(new AccessDeniedException("User cannot manage this shop reservations"));

        mockMvc.perform(get("/api/shops/500/reservations")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shopUpdatesReservationStatus() throws Exception {
        when(reservationService.updateReservationStatus(any(), eq(500L), eq(700L), any()))
                .thenReturn(reservationResponse("ACCEPTED"));

        mockMvc.perform(put("/api/shops/500/reservations/700/status")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "ACCEPTED",
                                  "shopResponse": "Accepted"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void invalidStatusTransitionReturnsConflict() throws Exception {
        when(reservationService.updateReservationStatus(any(), eq(500L), eq(700L), any()))
                .thenThrow(new InvalidReservationTransitionException("Invalid reservation status transition"));

        mockMvc.perform(put("/api/shops/500/reservations/700/status")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void reservationFromAnotherShopReturnsNotFoundOnStatusUpdate() throws Exception {
        when(reservationService.updateReservationStatus(any(), eq(500L), eq(700L), any()))
                .thenThrow(new ReservationNotFoundException(700L));

        mockMvc.perform(put("/api/shops/500/reservations/700/status")
                        .header("Authorization", "Bearer " + ownerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "ACCEPTED"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void userCancelsReservation() throws Exception {
        when(reservationService.cancelReservation(any(), eq(700L))).thenReturn(reservationResponse("CANCELLED"));

        mockMvc.perform(put("/api/reservations/700/cancel")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private String userToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser("alice@example.com"));
    }

    private String ownerToken() {
        return jwtService.generateAccessToken(TestSecurityConfiguration.testUser(
                "shop-owner@example.com",
                "USER",
                "SHOP_OWNER"
        ));
    }

    private ReservationResponse reservationResponse(String status) {
        return new ReservationResponse(
                700L,
                42L,
                "Alice",
                500L,
                "Collector Cave",
                900L,
                200L,
                "Dragon Ball 1",
                1,
                status,
                "Please hold it",
                "Accepted",
                Instant.parse("2026-06-19T10:00:00Z"),
                "COMPLETED".equals(status) ? Instant.parse("2026-06-18T10:00:00Z") : null,
                Instant.parse("2026-06-17T10:00:00Z")
        );
    }

    @TestConfiguration
    static class ReservationServiceTestConfiguration {

        @Bean
        ReservationService reservationService() {
            return Mockito.mock(ReservationService.class);
        }
    }
}
