package com.collectohub.shops.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.shops.application.ShopService;
import com.collectohub.shops.dto.CreateShopRequest;
import com.collectohub.shops.dto.AddShopMemberRequest;
import com.collectohub.shops.dto.ChangeShopMemberRoleRequest;
import com.collectohub.shops.dto.ShopMemberResponse;
import com.collectohub.shops.dto.ShopResponse;
import com.collectohub.shops.dto.UpdateShopRequest;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@Tag(name = "Shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a shop for the authenticated user")
    public ShopResponse createShop(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateShopRequest request
    ) {
        return shopService.createShop(user, request);
    }

    @GetMapping("/my")
    @Operation(summary = "Get shops associated with the authenticated user")
    public List<ShopResponse> myShops(@AuthenticationPrincipal AuthenticatedUser user) {
        return shopService.myShops(user);
    }

    @GetMapping("/{shopId}")
    @Operation(summary = "Get public shop details")
    public ShopResponse getShop(@PathVariable Long shopId) {
        return shopService.getPublicShop(shopId);
    }

    @GetMapping("/{shopId}/members")
    @Operation(summary = "List active shop members when the authenticated user can manage the shop")
    public List<ShopMemberResponse> listMembers(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId
    ) {
        return shopService.listMembers(user, shopId);
    }

    @PostMapping("/{shopId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an existing user as a shop member when the authenticated user is the owner")
    public ShopMemberResponse addMember(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId,
            @Valid @RequestBody AddShopMemberRequest request
    ) {
        return shopService.addMember(user, shopId, request);
    }

    @PutMapping("/{shopId}/members/{memberId}/role")
    @Operation(summary = "Change an active shop member role when the authenticated user is the owner")
    public ShopMemberResponse changeMemberRole(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId,
            @PathVariable Long memberId,
            @Valid @RequestBody ChangeShopMemberRoleRequest request
    ) {
        return shopService.changeMemberRole(user, shopId, memberId, request);
    }

    @DeleteMapping("/{shopId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate an active shop member when the authenticated user is the owner")
    public void deactivateMember(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId,
            @PathVariable Long memberId
    ) {
        shopService.deactivateMember(user, shopId, memberId);
    }

    @PutMapping("/{shopId}")
    @Operation(summary = "Update a shop when the authenticated user can manage it")
    public ShopResponse updateShop(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long shopId,
            @Valid @RequestBody UpdateShopRequest request
    ) {
        return shopService.updateShop(user, shopId, request);
    }
}
