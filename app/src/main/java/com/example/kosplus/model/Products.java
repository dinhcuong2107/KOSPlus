package com.example.kosplus.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Products {
    @PrimaryKey()
    @NonNull
    public String id;
    public String imageUrl;
    public String name;
    public String description;
    public String category;
    public String type;
    public String promotion;
    public int price;

    public boolean status;

    public Products() {
    }

    public Products(@NonNull String id, String imageUrl, String name, String description, String category, String type, String promotion, int price, boolean status) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.description = description;
        this.category = category;
        this.type = type;
        this.promotion = promotion;
        this.price = price;
        this.status = status;
    }

}
