package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogItemService;
import com.collectohub.catalog.dto.CatalogItemResponse;
import com.collectohub.catalog.dto.CreateCatalogItemRequest;
import com.collectohub.catalog.dto.UpdateCatalogItemRequest;
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
@RequestMapping("/api/catalog")
@Tag(name = "Editorial catalog items", description = "Public reads and ADMIN-only catalog item management")
public class CatalogItemController {

    private final CatalogItemService itemService;

    public CatalogItemController(CatalogItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/series/{seriesId}/items")
    @Operation(summary = "Search active catalog items in a series")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of catalog items"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination"),
            @ApiResponse(responseCode = "403", description = "recordStatus filter used without ADMIN"),
            @ApiResponse(responseCode = "404", description = "Series missing or not publicly visible")
    })
    public PageResponse<CatalogItemResponse> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long seriesId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder,asc") String sort
    ) {
        return itemService.search(
                seriesId, user, q, publicationYear, language, country,
                recordStatus, page, size, sort
        );
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get an active catalog item, or any non-deleted item as ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog item found"),
            @ApiResponse(responseCode = "404", description = "Item missing or not publicly visible")
    })
    public CatalogItemResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return itemService.get(id, user);
    }

    @PostMapping("/series/{seriesId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a catalog item", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Catalog item created"),
            @ApiResponse(responseCode = "400", description = "Validation or dependency state error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Series not found"),
            @ApiResponse(responseCode = "409", description = "Catalog item already exists")
    })
    public CatalogItemResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long seriesId,
            @Valid @RequestBody CreateCatalogItemRequest request
    ) {
        return itemService.create(seriesId, user, request);
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update a catalog item", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog item updated"),
            @ApiResponse(responseCode = "400", description = "Validation or dependency state error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Item or series not found"),
            @ApiResponse(responseCode = "409", description = "Catalog item already exists")
    })
    public CatalogItemResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCatalogItemRequest request
    ) {
        return itemService.update(id, user, request);
    }
}
