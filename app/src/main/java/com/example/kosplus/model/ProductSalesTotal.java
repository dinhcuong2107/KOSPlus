package com.example.kosplus.model;

public class ProductSalesTotal {
    public String productId;
    public int totalQuantity;
    public int totalRevenue;



    public ProductSalesTotal(String productId, int totalQuantity, int totalRevenue) {
        this.productId = productId;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }
}
