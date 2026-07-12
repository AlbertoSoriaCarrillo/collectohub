package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.MasterProductCatalogBackfillService;
import com.collectohub.catalog.application.MasterProductCatalogLinkService;
import com.collectohub.catalog.dto.BackfillMasterProductCatalogLinksResponse;
import com.collectohub.catalog.dto.CreateMasterProductCatalogLinkRequest;
import com.collectohub.catalog.dto.MasterProductCatalogLinkResponse;
import com.collectohub.catalog.dto.UpdateMasterProductCatalogLinkRequest;
import com.collectohub.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/master-product-links")
@EditorialAdminRequired
@Tag(name = "Editorial catalog bridge", description = "ADMIN-only reconciliation of legacy and editorial catalog identities")
public class MasterProductCatalogLinkController {

    private final MasterProductCatalogLinkService linkService;
    private final MasterProductCatalogBackfillService backfillService;

    public MasterProductCatalogLinkController(
            MasterProductCatalogLinkService linkService,
            MasterProductCatalogBackfillService backfillService
    ) {
        this.linkService = linkService;
        this.backfillService = backfillService;
    }

    @GetMapping
    @Operation(summary = "Search master product catalog links", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of links"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required")
    })
    public PageResponse<MasterProductCatalogLinkResponse> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long masterProductId,
            @RequestParam(required = false) Long catalogItemId,
            @RequestParam(required = false) Long catalogItemEditionId,
            @RequestParam(required = false) String linkStatus,
            @RequestParam(required = false) String linkSource,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return linkService.search(
                user, masterProductId, catalogItemId, catalogItemEditionId,
                linkStatus, linkSource, page, size, sort
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a catalog bridge link", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Link found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Link not found")
    })
    public MasterProductCatalogLinkResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return linkService.get(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a catalog bridge link", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Link created"),
            @ApiResponse(responseCode = "400", description = "Validation or consistency error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Dependency not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate or verified conflict")
    })
    public MasterProductCatalogLinkResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateMasterProductCatalogLinkRequest request
    ) {
        return linkService.create(user, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a catalog bridge link", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Link updated"),
            @ApiResponse(responseCode = "400", description = "Validation or consistency error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Link or dependency not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate or verified conflict")
    })
    public MasterProductCatalogLinkResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateMasterProductCatalogLinkRequest request
    ) {
        return linkService.update(id, user, request);
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Verify a proposed catalog bridge link", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Link verified"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Link not found"),
            @ApiResponse(responseCode = "409", description = "Another verified link exists")
    })
    public MasterProductCatalogLinkResponse verify(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return linkService.verify(id, user);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a catalog bridge link", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Link rejected"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Link not found")
    })
    public MasterProductCatalogLinkResponse reject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return linkService.reject(id, user);
    }

    @PostMapping("/backfill")
    @Operation(summary = "Propose idempotent links from legacy catalog data", description = "Requires ADMIN authority; never verifies automatically")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Backfill proposal summary"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required")
    })
    public BackfillMasterProductCatalogLinksResponse backfill(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return backfillService.run(user);
    }
}
