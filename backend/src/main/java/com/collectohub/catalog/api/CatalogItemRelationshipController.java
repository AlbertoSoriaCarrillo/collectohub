package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogItemRelationshipService;
import com.collectohub.catalog.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/items/{itemId}/relationships")
public class CatalogItemRelationshipController {
    private final CatalogItemRelationshipService service;

    public CatalogItemRelationshipController(CatalogItemRelationshipService service) { this.service = service; }

    @GetMapping
    public List<CatalogItemRelationshipResponse> list(@PathVariable Long itemId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String recordStatus) {
        return service.listRelationships(itemId, user, recordStatus);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @EditorialAdminRequired
    public CatalogItemRelationshipResponse create(@PathVariable Long itemId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateCatalogItemRelationshipRequest request) {
        return service.create(itemId, user, request);
    }

    @GetMapping("/{relationshipId}")
    public CatalogItemRelationshipResponse get(@PathVariable Long itemId, @PathVariable Long relationshipId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String recordStatus) {
        return service.get(itemId, relationshipId, user, recordStatus);
    }

    @PutMapping("/{relationshipId}") @EditorialAdminRequired
    public CatalogItemRelationshipResponse update(@PathVariable Long itemId, @PathVariable Long relationshipId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateCatalogItemRelationshipRequest request) {
        return service.update(itemId, relationshipId, user, request);
    }

    @DeleteMapping("/{relationshipId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @EditorialAdminRequired
    public void delete(@PathVariable Long itemId, @PathVariable Long relationshipId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        service.delete(itemId, relationshipId, user);
    }
}
