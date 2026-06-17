package com.collectohub.inventory.api;

import com.collectohub.inventory.application.InventoryService;
import com.collectohub.inventory.dto.ShopProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop-products")
@Tag(name = "Public shop products")
public class PublicShopProductController {

    private final InventoryService inventoryService;

    public PublicShopProductController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{shopProductId}")
    @Operation(summary = "Get a public visible shop product")
    public ShopProductResponse getPublicShopProduct(@PathVariable Long shopProductId) {
        return inventoryService.getPublicShopProduct(shopProductId);
    }
}
