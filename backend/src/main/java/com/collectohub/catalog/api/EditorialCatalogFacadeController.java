package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.EditorialCatalogFacadeService;
import com.collectohub.catalog.dto.EditorialCatalogEditionDetailResponse;
import com.collectohub.catalog.dto.EditorialCatalogItemDetailResponse;
import com.collectohub.catalog.dto.EditorialCatalogSeriesDetailResponse;
import com.collectohub.catalog.dto.EditorialLegacyBridgeResponse;
import com.collectohub.catalog.dto.EditorialCatalogSearchItemResponse;
import com.collectohub.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/editorial")
@Tag(name = "Editorial catalog facade", description = "Aggregated editorial reads and legacy bridge lookup")
public class EditorialCatalogFacadeController {

    private final EditorialCatalogFacadeService facadeService;

    public EditorialCatalogFacadeController(EditorialCatalogFacadeService facadeService) {
        this.facadeService = facadeService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search the public editorial catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated editorial results"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination"),
            @ApiResponse(responseCode = "403", description = "Internal link results require ADMIN")
    })
    public PageResponse<EditorialCatalogSearchItemResponse> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long franchiseId,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(required = false) String resultType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title,asc") String sort
    ) {
        return facadeService.search(
                user, q, type, franchiseId, seriesId, publisherId, language, country,
                publicationYear, resultType, page, size, sort
        );
    }

    @GetMapping("/series/{seriesId}/detail")
    @Operation(summary = "Get an aggregated public series detail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Series detail"),
            @ApiResponse(responseCode = "404", description = "Series missing or not publicly visible")
    })
    public EditorialCatalogSeriesDetailResponse seriesDetail(@PathVariable Long seriesId) {
        return facadeService.getSeriesDetail(seriesId);
    }

    @GetMapping("/items/{itemId}/detail")
    @Operation(summary = "Get an aggregated public catalog item detail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog item detail"),
            @ApiResponse(responseCode = "404", description = "Item missing or not publicly visible")
    })
    public EditorialCatalogItemDetailResponse itemDetail(@PathVariable Long itemId) {
        return facadeService.getItemDetail(itemId);
    }

    @GetMapping("/editions/{editionId}/detail")
    @Operation(summary = "Get an aggregated public edition detail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Edition detail"),
            @ApiResponse(responseCode = "404", description = "Edition missing or not publicly visible")
    })
    public EditorialCatalogEditionDetailResponse editionDetail(@PathVariable Long editionId) {
        return facadeService.getEditionDetail(editionId);
    }

    @GetMapping("/master-products/{masterProductId}/link")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Resolve the editorial link for a legacy master product", description = "Requires ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verified link, or latest proposal when unverified"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "No visible link exists")
    })
    public EditorialLegacyBridgeResponse masterProductLink(
            @PathVariable Long masterProductId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return facadeService.getLegacyLink(masterProductId, user);
    }
}
