package com.collectohub.inventory.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.inventory.application.InventoryService;
import com.collectohub.inventory.dto.CreateShopProductRequest;
import com.collectohub.inventory.dto.PublicShopProductResponse;
import com.collectohub.inventory.dto.ShopProductResponse;
import com.collectohub.inventory.dto.UpdateShopProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/shops/{shopId}/products")
@Tag(name = "Shop inventory")
public class ShopInventoryController {

    private final InventoryService inventoryService;

    public ShopInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a master product to a shop inventory")
    public ShopProductResponse createShopProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId,
            @Valid @RequestBody CreateShopProductRequest request
    ) {
        return inventoryService.createShopProduct(user, shopId, request);
    }

    @PutMapping("/{shopProductId}")
    @Operation(summary = "Update a shop inventory product")
    public ShopProductResponse updateShopProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId,
            @PathVariable Long shopProductId,
            @Valid @RequestBody UpdateShopProductRequest request
    ) {
        return inventoryService.updateShopProduct(user, shopId, shopProductId, request);
    }

    @GetMapping("/my")
    @Operation(summary = "List shop inventory for a shop member")
    public List<ShopProductResponse> myShopProducts(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId
    ) {
        return inventoryService.myShopProducts(user, shopId);
    }

    @GetMapping
    @Operation(summary = "List public visible shop products")
    public List<PublicShopProductResponse> publicShopProducts(
            @PathVariable Long shopId,
            @RequestParam(required = false) Long masterProductId,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String franchise,
            @RequestParam(required = false) String collectionName,
            @RequestParam(required = false) String physicalCondition,
            @RequestParam(required = false) String commercialStatus
    ) {
        return inventoryService.publicShopProducts(
                shopId,
                masterProductId,
                categoryCode,
                name,
                franchise,
                collectionName,
                physicalCondition,
                commercialStatus
        );
    }
}
