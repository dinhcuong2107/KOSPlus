package com.example.kosplus.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "promotions")
public class Promotions {
    @PrimaryKey()
    @NonNull
    public String id;
    public String code;
    public String title;
    public String type; // percent or amount
    public long start_date;
    public long end_date;
    public int discount;
    public boolean status;

    public Promotions() {
    }

    public Promotions(@NonNull String id, String code, String title, String type, long start_date, long end_date, int discount, boolean status) {
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
