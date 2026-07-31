package com.collectohub.collections.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.collections.application.CollectionService;
import com.collectohub.collections.application.CollectionProgressService;
import com.collectohub.collections.domain.CollectionVisibility;
import com.collectohub.collections.dto.CollectionItemResponse;
import com.collectohub.collections.dto.CollectionResponse;
import com.collectohub.collections.dto.CollectionSeriesProgressResponse;
import com.collectohub.collections.dto.CollectionSeriesProgressSummaryResponse;
import com.collectohub.collections.dto.CreateCollectionItemRequest;
import com.collectohub.collections.dto.CreateCollectionRequest;
import com.collectohub.collections.dto.UpdateCollectionItemRequest;
import com.collectohub.collections.dto.UpdateCollectionRequest;
import com.collectohub.collections.dto.LinkManualCollectionItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@Tag(name = "Collections")
public class CollectionController {

    private final CollectionService collectionService;
    private final CollectionProgressService collectionProgressService;

    public CollectionController(CollectionService collectionService, CollectionProgressService collectionProgressService) {
        this.collectionService = collectionService;
        this.collectionProgressService = collectionProgressService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a user collection")
    public CollectionResponse createCollection(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateCollectionRequest request
    ) {
        return collectionService.createCollection(user, request);
    }

    @GetMapping("/my")
    @Operation(summary = "List collections owned by the authenticated user")
    public List<CollectionResponse> myCollections(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) CollectionVisibility visibility,
            @RequestParam(required = false) String categoryCode
    ) {
        return collectionService.myCollections(user, visibility, categoryCode);
    }

    @GetMapping("/{collectionId}")
    @Operation(summary = "Get a public collection or a collection owned by the authenticated user")
    public CollectionResponse getCollection(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId
    ) {
        return collectionService.getCollection(user, collectionId);
    }

    @GetMapping("/{collectionId}/series/{seriesId}/progress")
    @Operation(summary = "Calculate owner-only collection progress for a public catalog series")
    public CollectionSeriesProgressResponse getSeriesProgress(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId,
            @PathVariable Long seriesId
    ) {
        return collectionProgressService.getSeriesProgress(user, collectionId, seriesId);
    }

    @PutMapping("/{collectionId}")
    @Operation(summary = "Update a collection owned by the authenticated user")
    public CollectionResponse updateCollection(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId,
            @Valid @RequestBody UpdateCollectionRequest request
    ) {
        return collectionService.updateCollection(user, collectionId, request);
    }

    @DeleteMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete a collection owned by the authenticated user")
    public void deleteCollection(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId
    ) {
        collectionService.deleteCollection(user, collectionId);
    }

    @PostMapping("/{collectionId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a manual item, canonical catalog item with optional edition, or legacy master product to a collection")
    public CollectionItemResponse addItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId,
            @Valid @RequestBody CreateCollectionItemRequest request
    ) {
        return collectionService.addItem(user, collectionId, request);
    }

    @GetMapping("/{collectionId}/items")
    @Operation(summary = "List collection items; public reads omit private notes and acquisition dates")
    public List<CollectionItemResponse> listItems(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> referenceKind,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) String sort
    ) {
        return collectionService.listItems(user, collectionId, query, status, referenceKind, seriesId, sort);
    }

    @GetMapping("/{collectionId}/series-progress")
    @Operation(summary = "Summarize owner-only collection progress for all participating public catalog series")
    public List<CollectionSeriesProgressSummaryResponse> getSeriesProgressSummary(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId
    ) {
        return collectionProgressService.getSeriesProgressSummary(user, collectionId);
    }

    @PutMapping("/{collectionId}/items/{itemId}")
    @Operation(summary = "Update an item in a collection owned by the authenticated user")
    public CollectionItemResponse updateItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCollectionItemRequest request
    ) {
        return collectionService.updateItem(user, collectionId, itemId, request);
    }

    @PutMapping("/{collectionId}/items/{itemId}/catalog-reference")
    @Operation(summary = "Link a manual collection item to a public catalog item and optional edition")
    public CollectionItemResponse linkManualItemToCatalog(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId,
            @PathVariable Long itemId,
            @Valid @RequestBody LinkManualCollectionItemRequest request
    ) {
        return collectionService.linkManualItemToCatalog(user, collectionId, itemId, request);
    }

    @DeleteMapping("/{collectionId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete an item from a collection owned by the authenticated user")
    public void deleteItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long collectionId,
            @PathVariable Long itemId
    ) {
        collectionService.deleteItem(user, collectionId, itemId);
    }
}
