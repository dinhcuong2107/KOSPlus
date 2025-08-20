package com.example.kosplus.model;

public class TransactionHistory {
    public String id;
    public String userId;
    public long amount;
    public String time;
    public String type; // deposit, payment, refund
    public String description;
    public String maker;
    public boolean status;

    public TransactionHistory() {
    }

    public TransactionHistory(String id, String userId, long amount, String time, String type, String description, String maker, boolean status) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.time = time;
        this.type = type;
        this.description = description;
        this.maker = maker;
        this.status = status;
    }

}
