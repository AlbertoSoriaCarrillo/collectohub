package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogFranchiseService;
import com.collectohub.catalog.dto.CatalogFranchiseResponse;
import com.collectohub.catalog.dto.CreateCatalogFranchiseRequest;
import com.collectohub.catalog.dto.UpdateCatalogFranchiseRequest;
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
@RequestMapping("/api/catalog/franchises")
@Tag(name = "Editorial franchises", description = "Public reads and ADMIN-only catalog franchise management")
public class CatalogFranchiseController {

    private final CatalogFranchiseService franchiseService;

    public CatalogFranchiseController(CatalogFranchiseService franchiseService) {
        this.franchiseService = franchiseService;
    }

    @GetMapping
    @Operation(summary = "Search active catalog franchises")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of franchises"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination"),
            @ApiResponse(responseCode = "403", description = "recordStatus filter used without ADMIN")
    })
    public PageResponse<CatalogFranchiseResponse> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        return franchiseService.search(user, q, recordStatus, page, size, sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an active franchise, or any non-deleted franchise as ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Franchise found"),
            @ApiResponse(responseCode = "404", description = "Franchise missing or not publicly visible")
    })
    public CatalogFranchiseResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return franchiseService.get(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @EditorialAdminRequired
    @Operation(summary = "Create a catalog franchise", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Franchise created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "409", description = "Franchise name or slug already exists")
    })
    public CatalogFranchiseResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateCatalogFranchiseRequest request
    ) {
        return franchiseService.create(user, request);
    }

    @PutMapping("/{id}")
    @EditorialAdminRequired
    @Operation(summary = "Update a catalog franchise", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Franchise updated"),
            @ApiResponse(responseCode = "400", description = "Validation or lifecycle error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Franchise not found"),
            @ApiResponse(responseCode = "409", description = "Franchise name or slug already exists")
    })
    public CatalogFranchiseResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCatalogFranchiseRequest request
    ) {
        return franchiseService.update(id, user, request);
    }
}
