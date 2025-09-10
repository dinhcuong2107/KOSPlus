package com.example.kosplus.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "itemcarts", primaryKeys = {"productId", "userId"})
public class ItemCarts {
    @NonNull
    public String productId;
    @NonNull
    public String userId;
    public int quantity;

    public ItemCarts() {
    }
    public ItemCarts(@NonNull String userId, @NonNull String productId, int quantity) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
    }
}
