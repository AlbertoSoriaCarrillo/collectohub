package com.collectohub.shops.application;

public class ShopOwnerCannotBeDeactivatedException extends RuntimeException {

    public ShopOwnerCannotBeDeactivatedException() {
        super("Shop owner membership cannot be deactivated");
    }
}
