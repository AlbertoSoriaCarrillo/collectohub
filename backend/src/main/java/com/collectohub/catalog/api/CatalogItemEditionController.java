package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogItemEditionService;
import com.collectohub.catalog.dto.CatalogItemEditionResponse;
import com.collectohub.catalog.dto.CreateCatalogItemEditionRequest;
import com.collectohub.catalog.dto.UpdateCatalogItemEditionRequest;
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
@Tag(name = "Editorial catalog editions", description = "Public reads and ADMIN-only catalog edition management")
public class CatalogItemEditionController {

    private final CatalogItemEditionService editionService;

    public CatalogItemEditionController(CatalogItemEditionService editionService) {
        this.editionService = editionService;
    }

    @GetMapping("/items/{itemId}/editions")
    @Operation(summary = "Search active editions of a catalog item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of catalog item editions"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination"),
            @ApiResponse(responseCode = "403", description = "recordStatus filter used without ADMIN"),
            @ApiResponse(responseCode = "404", description = "Item missing or not publicly visible")
    })
    public PageResponse<CatalogItemEditionResponse> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long itemId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String ean,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publicationYear,asc") String sort
    ) {
        return editionService.search(
                itemId, user, publisherId, isbn, ean, format, language,
                country, publicationYear, recordStatus, page, size, sort
        );
    }

    @GetMapping("/editions/{id}")
    @Operation(summary = "Get an active catalog edition, or any non-deleted edition as ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog edition found"),
            @ApiResponse(responseCode = "404", description = "Edition missing or not publicly visible")
    })
    public CatalogItemEditionResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return editionService.get(id, user);
    }

    @PostMapping("/items/{itemId}/editions")
    @ResponseStatus(HttpStatus.CREATED)
    @EditorialAdminRequired
    @Operation(summary = "Create a catalog item edition", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Catalog edition created"),
            @ApiResponse(responseCode = "400", description = "Validation or dependency state error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Item or publisher not found"),
            @ApiResponse(responseCode = "409", description = "ISBN or EAN already exists")
    })
    public CatalogItemEditionResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long itemId,
            @Valid @RequestBody CreateCatalogItemEditionRequest request
    ) {
        return editionService.create(itemId, user, request);
    }

    @PutMapping("/editions/{id}")
    @EditorialAdminRequired
    @Operation(summary = "Update a catalog item edition", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog edition updated"),
            @ApiResponse(responseCode = "400", description = "Validation or dependency state error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Edition, item or publisher not found"),
            @ApiResponse(responseCode = "409", description = "ISBN or EAN already exists")
    })
    public CatalogItemEditionResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCatalogItemEditionRequest request
    ) {
        return editionService.update(id, user, request);
    }
}
