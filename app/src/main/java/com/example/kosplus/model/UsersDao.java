package com.example.kosplus.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsersDao {
    @Query("SELECT * FROM users ORDER BY fullname ASC")
    LiveData<List<Users>> getAllUsers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Users> users);

    @Query("DELETE FROM users")
    void clearAll();

    // Lọc theo status
    @Query("SELECT * FROM users WHERE status = 1 ORDER BY fullname ASC")
    LiveData<List<Users>> getActiveUsers();

    @Query("SELECT * FROM users WHERE status = 0 ORDER BY fullname ASC")
    LiveData<List<Users>> getLockedUsers();

    // Lọc theo role
    @Query("SELECT * FROM users WHERE LOWER(role) = LOWER(:role) ORDER BY fullname ASC")
    LiveData<List<Users>> getUsersByRole(String role);

    @Query("SELECT COUNT(*) FROM users")
    LiveData<Integer> getCountAll();

    @Query("SELECT COUNT(*) FROM users WHERE status = 1")
    LiveData<Integer> getCountActive();

    @Query("SELECT COUNT(*) FROM users WHERE status = 0")
    LiveData<Integer> getCountBlocked();

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    LiveData<Integer> getCountByRole(String role);

    // Lọc theo category
    default LiveData<List<Users>> getUsersByCategory(String category) {
        switch (category) {
            case "Hoạt động": return getActiveUsers();
            case "Khóa": return getLockedUsers();
            case "Admin": return getUsersByRole("admin");
            case "Manager": return getUsersByRole("manager");
            case "Staff": return getUsersByRole("staff");
            case "Customer": return getUsersByRole("customer");
            default: return getAllUsers();
        }
    }
}