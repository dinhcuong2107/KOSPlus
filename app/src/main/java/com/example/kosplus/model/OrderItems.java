package com.example.kosplus.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_items",
        primaryKeys = {"orderId", "productId"})
public class OrderItems {
        @NonNull
        public String orderId;
        @NonNull
        public String productId;
        public int quantity;
        public int price;

        public OrderItems() {
        }

        public OrderItems(@NonNull String orderId, @NonNull String productId, int quantity, int price) {
            this.orderId = orderId;
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
}
