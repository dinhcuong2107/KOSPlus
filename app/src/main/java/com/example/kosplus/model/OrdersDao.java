package com.example.kosplus.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.kosplus.datalocal.DataLocalManager;

import java.util.List;

@Dao
public interface OrdersDao {
    @Query("SELECT * FROM orders")
    LiveData<List<Orders>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Orders> orders);

    @Query("DELETE FROM orders")
    void clearAll();

    @Query("SELECT * FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix ORDER BY createdTime DESC")
    LiveData<List<Orders>> getAllOrders(String prefix);
    @Query("SELECT COUNT(*) FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix")
    LiveData<Integer> getCountOrders(String prefix);
    @Query("SELECT * FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND confirmedTime = 0 AND canceledTime = 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersCreated(String prefix);
    @Query("SELECT COUNT(*) FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND confirmedTime = 0 AND canceledTime = 0")
    LiveData<Integer> getCountOrdersCreated(String prefix);

    @Query("SELECT * FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND deliveryTime = 0 AND confirmedTime > 0 AND canceledTime = 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersConfirmed(String prefix);
    @Query("SELECT COUNT(*) FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND deliveryTime = 0 AND confirmedTime > 0 AND canceledTime = 0")
    LiveData<Integer> getCountOrdersConfirmed(String prefix);

    @Query("SELECT * FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND completedTime = 0 AND deliveryTime >0 AND canceledTime = 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersDelivery(String prefix);
    @Query("SELECT COUNT(*) FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND completedTime = 0 AND deliveryTime >0  AND canceledTime = 0")
    LiveData<Integer> getCountOrdersDelivery(String prefix);

    @Query("SELECT * FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND completedTime > 0 AND canceledTime = 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersCompleted(String prefix);
    @Query("SELECT COUNT(*) FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND completedTime > 0  AND canceledTime = 0")
    LiveData<Integer> getCountOrdersCompleted(String prefix);

    @Query("SELECT * FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND canceledTime > 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersCanceled(String prefix);
    @Query("SELECT COUNT(*) FROM orders WHERE substr(address, 1, instr(address, '-') - 1) = :prefix AND canceledTime > 0")
    LiveData<Integer> getCountOrdersCanceled(String prefix);

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersByUserId(String userId);
    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId")
    LiveData<Integer> getCountOrdersByUserId(String userId);

    @Query("SELECT * FROM orders WHERE userId = :userId AND confirmedTime = 0  AND canceledTime = 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersCreatedByUserID(String userId);
    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId AND confirmedTime = 0  AND canceledTime = 0")
    LiveData<Integer> getCountOrdersCreatedByUserID(String userId);

    @Query("SELECT * FROM orders WHERE userId = :userId AND confirmedTime > 0 AND deliveryTime = 0 AND canceledTime = 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersConfirmedByUserID(String userId);
    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId AND confirmedTime > 0 AND deliveryTime = 0 AND canceledTime = 0")
    LiveData<Integer> getCountOrdersConfirmedByUserID(String userId);

    @Query("SELECT * FROM orders WHERE userId = :userId AND deliveryTime > 0 AND completedTime = 0 AND canceledTime = 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersDeliveryByUserID(String userId);
    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId AND deliveryTime > 0 AND completedTime = 0  AND canceledTime = 0")
    LiveData<Integer> getCountOrdersDeliveryByUserID(String userId);
    @Query("SELECT * FROM orders WHERE userId = :userId AND completedTime > 0 AND canceledTime = 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersCompletedByUserID(String userId);
    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId AND completedTime > 0 AND canceledTime = 0")
    LiveData<Integer> getCountOrdersCompletedByUserID(String userId);
    @Query("SELECT * FROM orders WHERE userId = :userId AND canceledTime > 0 ORDER BY createdTime DESC")
    LiveData<List<Orders>> getOrdersCanceledByUserID(String userId);
    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId AND canceledTime > 0")
    LiveData<Integer> getCountOrdersCanceledByUserID(String userId);

    // Lọc theo category
    default LiveData<List<Orders>> getOrdersByCategory(String category, String prefix) {
        if (DataLocalManager.getRole().equals("Customer")) {
            String userId = DataLocalManager.getUid();
            switch (category) {
                case "Chưa xác nhận": return getOrdersCreatedByUserID(userId);
                case "Đã xác nhận": return getOrdersConfirmedByUserID(userId);
                case "Đang giao": return getOrdersDeliveryByUserID(userId);
                case "Hoàn thành": return getOrdersCompletedByUserID(userId);
                case "Đã hủy": return getOrdersCanceledByUserID(userId);
                default: return getOrdersByUserId(userId);
            }
        } else {
            switch (category) {
                case "Chưa xác nhận": return getOrdersCreated(prefix);
                case "Đã xác nhận": return getOrdersConfirmed(prefix);
                case "Đang giao": return getOrdersDelivery(prefix);
                case "Hoàn thành": return getOrdersCompleted(prefix);
                case "Đã hủy": return getOrdersCanceled(prefix);
                default: return getAllOrders(prefix);
            }
        }
    }
    @Query("SELECT oi.productId AS productId, " +
            "SUM(oi.quantity) AS totalQuantity, " +
            "SUM(oi.price) AS totalRevenue " + //oi.price là tổng của đơn hàng đó
            "FROM order_items oi " +
            "JOIN orders o ON oi.orderId = o.id " +
            "WHERE o.completedTime BETWEEN :startOfMonth AND :endOfMonth " +
            "GROUP BY oi.productId")
    LiveData<List<ProductSalesTotal>> getProductsAndRevenueInMonth(long startOfMonth, long endOfMonth);

}
