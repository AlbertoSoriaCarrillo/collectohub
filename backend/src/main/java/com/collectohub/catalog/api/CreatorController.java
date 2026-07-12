package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CreatorService;
import com.collectohub.catalog.dto.*;
import com.collectohub.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/creators")
public class CreatorController {
    private final CreatorService service;
    public CreatorController(CreatorService service) { this.service = service; }

    @GetMapping
    public PageResponse<CreatorResponse> search(@AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String q, @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        return service.search(user, q, recordStatus, page, size, sort);
    }
    @GetMapping("/{id}") public CreatorResponse get(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) { return service.get(id, user); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @EditorialAdminRequired
    public CreatorResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                  @Valid @RequestBody CreateCreatorRequest request) { return service.create(user, request); }
    @PutMapping("/{id}") @EditorialAdminRequired
    public CreatorResponse update(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user,
                                  @Valid @RequestBody UpdateCreatorRequest request) { return service.update(id, user, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @EditorialAdminRequired
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) { service.delete(id, user); }
}
