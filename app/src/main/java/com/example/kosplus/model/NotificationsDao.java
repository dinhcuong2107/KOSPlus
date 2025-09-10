package com.example.kosplus.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
@Dao
public interface NotificationsDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY time DESC LIMIT :limit")
    LiveData<List<Notifications>> getAll(String userId, int limit);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND status = 1")
    LiveData<Integer> getUnreadCount(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Notifications> notifications);

    @Query("DELETE FROM notifications")
    void clearAll();


}
