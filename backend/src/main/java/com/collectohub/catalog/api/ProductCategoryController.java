package com.collectohub.catalog.api;

import com.collectohub.catalog.application.CatalogService;
import com.collectohub.catalog.dto.ProductCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@Tag(name = "Product categories")
public class ProductCategoryController {

    private final CatalogService catalogService;

    public ProductCategoryController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @Operation(summary = "List public product categories")
    public List<ProductCategoryResponse> listCategories() {
        return catalogService.listCategories();
    }
}
