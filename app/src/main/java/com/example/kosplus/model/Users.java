package com.example.kosplus.model;

public class Users {
    public String id;
    public String imageUrl;
    public String fullname;
    public String sex;
    public String dob;
    public String phone;
    public String password;
    public String role; // Customer Admin Manager Staff
    public String time;
    public String token;
    public boolean status;

    public Users() {
    }

    public Users(String id, String imageUrl, String fullname, String sex, String dob, String phone, String password, String role, String time, String token, boolean status) {
        this.imageUrl = imageUrl;
        this.id = id;
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
