package com.example.kosplus.model;

import java.util.List;

public class Orders {
    public String id;
    public String userId;
    public List<String> productId;
    public List<Integer> quantity;
    public List<Integer> price;
    public String address;
    public String note;
    public String paymentMethod;
    public String createdTime; // Thời gian tạo đơn hàng
    public String confirmedTime; // Thời gian xác nhận đơn hàng
    public String deliveryTime; // Thời gian giao hàng
    public String completedTime; // Thời gian hoàn thành đơn hàng
    public String canceledTime; // Thời gian hủy đơn hàng
    public String canceledReason; // Lý do hủy đơn hàng
    public int total; // Tổng giá trị đơn hàng
    public boolean delivery; // Trạng thái giao hàng;

    public boolean status;

    public Orders() {
    }

    public Orders(String id, String userId, List<String> productId, List<Integer> quantity, List<Integer> price, String address, String note, String paymentMethod, String createdTime, String confirmedTime, String deliveryTime, String completedTime, String canceledTime, String canceledReason, int total, boolean delivery, boolean status) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
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
