package com.example.kosplus.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;
@Entity(tableName = "orders")
public class Orders {
    @PrimaryKey
    @NonNull
    public String id;
    public String userId;
    public String address;
    public String note;
    public String paymentMethod;
    public Long createdTime; // Thời gian tạo đơn hàng
    public Long confirmedTime; // Thời gian xác nhận đơn hàng
    public Long deliveryTime; // Thời gian giao hàng
    public Long completedTime; // Thời gian hoàn thành đơn hàng
    public Long canceledTime; // Thời gian hủy đơn hàng
    public String canceledReason; // Lý do hủy đơn hàng
    public int total; // Tổng giá trị đơn hàng
    public boolean delivery; // Trạng thái giao hàng;

    public boolean status;

    public Orders() {
    }

    public Orders(@NonNull String id, String userId, String address, String note, String paymentMethod, Long createdTime, Long confirmedTime, Long deliveryTime, Long completedTime, Long canceledTime, String canceledReason, int total, boolean delivery, boolean status) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.createdTime = createdTime;
        this.confirmedTime = confirmedTime;
        this.deliveryTime = deliveryTime;
        this.completedTime = completedTime;
        this.canceledTime = canceledTime;
        this.canceledReason = canceledReason;
        this.total = total;
        this.delivery = delivery;
        this.status = status;
    }
}
