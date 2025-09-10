package com.example.kosplus.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemCartsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ItemCarts item);

    @Update
    void update(ItemCarts item);

    @Delete
    void delete(ItemCarts item);

    @Query("SELECT * FROM itemcarts WHERE userId = :userId")
    LiveData<List<ItemCarts>> getAllCartItems(String userId);

    @Query("DELETE FROM itemcarts WHERE userId = :userId")
    void clearAll(String userId);

    @Query("DELETE FROM itemcarts WHERE userId = :userId AND productId = :productId")
    void clearItem(String userId, String productId);

    @Query("SELECT * FROM itemcarts WHERE userId = :userId AND productId = :productId LIMIT 1")
    ItemCarts getCartItemByProductId(String userId, String productId);
}
