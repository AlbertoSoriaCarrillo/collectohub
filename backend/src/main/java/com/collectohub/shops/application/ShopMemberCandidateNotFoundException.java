package com.collectohub.shops.application;

public class ShopMemberCandidateNotFoundException extends RuntimeException {

    public ShopMemberCandidateNotFoundException() {
        super("Eligible user not found");
    }
}
