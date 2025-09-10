package com.example.kosplus.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OrderItemsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OrderItems> items);
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    LiveData<List<OrderItems>> getByOrderId(String orderId);

    @Query("DELETE FROM order_items")
    void clearAll();
}
