package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogItemCreatorService;
import com.collectohub.catalog.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/items/{itemId}/creators")
public class CatalogItemCreatorController {
    private final CatalogItemCreatorService service;
    public CatalogItemCreatorController(CatalogItemCreatorService service) { this.service = service; }

    @GetMapping public List<CatalogItemCreatorResponse> list(@PathVariable Long itemId) {
        return service.listPublic(itemId);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @EditorialAdminRequired
    public CatalogItemCreatorResponse create(@PathVariable Long itemId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateCatalogItemCreatorRequest request) { return service.create(itemId, user, request); }
    @PutMapping("/{creditId}") @EditorialAdminRequired
    public CatalogItemCreatorResponse update(@PathVariable Long itemId, @PathVariable Long creditId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateCatalogItemCreatorRequest request) {
        return service.update(itemId, creditId, user, request);
    }
    @DeleteMapping("/{creditId}") @ResponseStatus(HttpStatus.NO_CONTENT) @EditorialAdminRequired
    public void delete(@PathVariable Long itemId, @PathVariable Long creditId,
            @AuthenticationPrincipal AuthenticatedUser user) { service.delete(itemId, creditId, user); }
}
