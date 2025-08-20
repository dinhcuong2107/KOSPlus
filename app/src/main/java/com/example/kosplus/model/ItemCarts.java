package com.example.kosplus.model;

public class ItemCarts {
    public String id;
    public String productId;
    public String userId;
    public boolean status;

    public ItemCarts() {

    }

    public ItemCarts(String id, String productId, String userId, boolean status) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.status = status;
    }
}
