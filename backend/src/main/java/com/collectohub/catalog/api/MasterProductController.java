package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogService;
import com.collectohub.catalog.dto.CreateMasterProductRequest;
import com.collectohub.catalog.dto.MasterProductResponse;
import com.collectohub.catalog.dto.UpdateMasterProductRequest;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@RestController
@RequestMapping("/api/master-products")
@Tag(name = "Master products")
public class MasterProductController {

    private final CatalogService catalogService;

    public MasterProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @Operation(summary = "Search public master products")
    public List<MasterProductResponse> searchMasterProducts(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String franchise,
            @RequestParam(required = false) String collectionName,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String status
    ) {
        return catalogService.searchMasterProducts(categoryCode, name, franchise, collectionName, language, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get public master product details")
    public MasterProductResponse getMasterProduct(@PathVariable Long id) {
        return catalogService.getMasterProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a master product")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHOP_OWNER')")
    public MasterProductResponse createMasterProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateMasterProductRequest request
    ) {
        return catalogService.createMasterProduct(user, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a master product")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHOP_OWNER')")
    public MasterProductResponse updateMasterProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateMasterProductRequest request
    ) {
        return catalogService.updateMasterProduct(user, id, request);
    }
}
