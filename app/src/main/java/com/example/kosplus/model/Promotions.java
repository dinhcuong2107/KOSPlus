package com.example.kosplus.model;

public class Promotions {
    public String id;
    public String code;
    public String title;
    public String type; // percent or amount
    public String start_date;
    public String end_date;
    public int discount;
    public boolean status;

    public Promotions() {
    }

    public Promotions(String id, String code, String title, String type, String start_date, String end_date, int discount, boolean status) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.type = type;
        this.start_date = start_date;
        this.end_date = end_date;
        this.discount = discount;
        this.status = status;
    }
}
