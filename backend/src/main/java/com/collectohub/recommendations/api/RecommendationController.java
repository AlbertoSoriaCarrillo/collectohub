package com.collectohub.recommendations.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.recommendations.application.RecommendationService;
import com.collectohub.recommendations.dto.UserRecommendationResponse;
import com.collectohub.recommendations.dto.UserRecommendationSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/my")
    @Operation(summary = "List available shop products matching the authenticated user's missing or wanted items")
    public UserRecommendationResponse myRecommendations(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String physicalCondition,
            @RequestParam(required = false) String shopId
    ) {
        return recommendationService.myRecommendations(user, categoryCode, maxPrice, currency, physicalCondition, shopId);
    }

    @GetMapping("/my/summary")
    @Operation(summary = "Summarize available recommendation matches for the authenticated user")
    public UserRecommendationSummaryResponse mySummary(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String physicalCondition,
            @RequestParam(required = false) String shopId
    ) {
        return recommendationService.mySummary(user, categoryCode, maxPrice, currency, physicalCondition, shopId);
    }
}
