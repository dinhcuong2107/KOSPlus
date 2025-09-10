package com.example.kosplus.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProductsDao {

    // Lây tất cả sản phẩm
    @Query("SELECT * FROM products")
    LiveData<List<Products>> getAll();

    // Lấy sản phẩm theo loại
    @Query("SELECT * FROM products WHERE type = :type AND status = 1 ORDER BY name ASC")
    LiveData<List<Products>> getProductsByType(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Products> products);

    @Query("DELETE FROM products")
    void clearAll();

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    Products getProductByIdSync(String productId);

    @Query("SELECT * FROM products WHERE id IN (:productIds)")
    List<Products> getProductsByIds(List<String> productIds);

    // Đếm tổng số lượng bán của 1 sản phẩm
    @Query("SELECT SUM(oi.quantity) FROM order_items oi " +
            "JOIN orders o ON oi.orderId = o.id " +
            "WHERE oi.productId = :productId AND o.completedTime > 0")
    LiveData<Integer> getSoldQuantityByProduct(String productId);

    @Query("SELECT oi.productId, SUM(oi.quantity) AS totalSold " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.orderId = o.id " +
            "WHERE o.completedTime > 0 " +
            "GROUP BY oi.productId " +
            "ORDER BY totalSold DESC " +
            "LIMIT 10")
    LiveData<List<ProductSales>> getTop10BestSellingProducts();

    @Query("SELECT oi.productId, SUM(oi.quantity) AS totalSold " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.orderId = o.id " +
            "WHERE o.completedTime BETWEEN :startOfWeek AND :endOfWeek " +
            "GROUP BY oi.productId " +
            "ORDER BY totalSold DESC " +
            "LIMIT 10")
    LiveData<List<ProductSales>> getTop10BestSellingProductsOfTime(long startOfWeek, long endOfWeek);

    @Query("SELECT * FROM products ORDER BY RANDOM() LIMIT 10")
    LiveData<List<Products>> getRandomProducts();

    @Query("SELECT p.* FROM products p " +
            "JOIN promotions promo ON p.promotion = promo.id " +
            "WHERE p.status = 1 AND promo.status = 1 " +
            "AND :now BETWEEN promo.start_date AND promo.end_date " +
            "LIMIT 5")
    LiveData<List<Products>> getActivePromotions(long now);


}
