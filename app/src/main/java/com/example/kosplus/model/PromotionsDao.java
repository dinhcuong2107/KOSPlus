package com.example.kosplus.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PromotionsDao {
    @Query("SELECT * FROM promotions")
    LiveData<List<Promotions>> getAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Promotions> promotions);
    @Query("DELETE FROM promotions")
    void clearAll();
}
