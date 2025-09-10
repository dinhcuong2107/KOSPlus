package com.example.kosplus.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class Users {

    @PrimaryKey
    @NonNull
    public String id = ""; // đảm bảo không null mặc định

    public String imageUrl;
    public String fullname;
    public String sex;
    public String dob;
    public String phone;
    public String password;
    public String role; // Customer Admin Manager Staff
    public long time;
    public String token;
    public boolean status;

    public Users() {
    }

    public Users(@NonNull String id, String imageUrl, String fullname, String sex, String dob,
                 String phone, String password, String role, long time,
                 String token, boolean status) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.fullname = fullname;
        this.sex = sex;
        this.dob = dob;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.time = time;
        this.token = token;
        this.status = status;
    }
}