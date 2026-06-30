package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogSeriesService;
import com.collectohub.catalog.dto.CatalogSeriesResponse;
import com.collectohub.catalog.dto.CreateCatalogSeriesRequest;
import com.collectohub.catalog.dto.UpdateCatalogSeriesRequest;
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
@RequestMapping("/api/catalog/series")
@Tag(name = "Editorial series", description = "Public reads and ADMIN-only catalog series management")
public class CatalogSeriesController {

    private final CatalogSeriesService seriesService;

    public CatalogSeriesController(CatalogSeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @GetMapping
    @Operation(summary = "Search active catalog series")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of catalog series"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination"),
            @ApiResponse(responseCode = "403", description = "recordStatus filter used without ADMIN")
    })
    public PageResponse<CatalogSeriesResponse> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long franchiseId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String publicationStatus,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title,asc") String sort
    ) {
        return seriesService.search(
                user,
                q,
                franchiseId,
                type,
                publicationStatus,
                publisherId,
                language,
                country,
                recordStatus,
                page,
                size,
                sort
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an active series, or any non-deleted series as ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog series found"),
            @ApiResponse(responseCode = "404", description = "Series missing or not publicly visible")
    })
    public CatalogSeriesResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return seriesService.get(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a catalog series", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Catalog series created"),
            @ApiResponse(responseCode = "400", description = "Validation or dependency state error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Franchise or publisher not found"),
            @ApiResponse(responseCode = "409", description = "Series already exists")
    })
    public CatalogSeriesResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateCatalogSeriesRequest request
    ) {
        return seriesService.create(user, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update a catalog series", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog series updated"),
            @ApiResponse(responseCode = "400", description = "Validation or dependency state error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Series, franchise or publisher not found"),
            @ApiResponse(responseCode = "409", description = "Series already exists")
    })
    public CatalogSeriesResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCatalogSeriesRequest request
    ) {
        return seriesService.update(id, user, request);
    }
}
