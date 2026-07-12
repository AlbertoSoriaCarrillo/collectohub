package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.PublisherService;
import com.collectohub.catalog.dto.CreatePublisherRequest;
import com.collectohub.catalog.dto.PublisherResponse;
import com.collectohub.catalog.dto.UpdatePublisherRequest;
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
@RequestMapping("/api/catalog/publishers")
@Tag(name = "Editorial publishers", description = "Public reads and ADMIN-only editorial publisher management")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping
    @Operation(summary = "Search active editorial publishers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of publishers"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination"),
            @ApiResponse(responseCode = "403", description = "recordStatus filter used without ADMIN")
    })
    public PageResponse<PublisherResponse> search(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        return publisherService.search(user, q, recordStatus, page, size, sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an active publisher, or any non-deleted publisher as ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher found"),
            @ApiResponse(responseCode = "404", description = "Publisher missing or not publicly visible")
    })
    public PublisherResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return publisherService.get(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @EditorialAdminRequired
    @Operation(summary = "Create an editorial publisher", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publisher created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "409", description = "Publisher name already exists")
    })
    public PublisherResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreatePublisherRequest request
    ) {
        return publisherService.create(user, request);
    }

    @PutMapping("/{id}")
    @EditorialAdminRequired
    @Operation(summary = "Update an editorial publisher", description = "Requires ADMIN authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher updated"),
            @ApiResponse(responseCode = "400", description = "Validation or lifecycle error"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Publisher not found"),
            @ApiResponse(responseCode = "409", description = "Publisher name already exists")
    })
    public PublisherResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePublisherRequest request
    ) {
        return publisherService.update(id, user, request);
    }
}
