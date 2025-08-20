package com.example.kosplus.model;

public class Shops {
    public String id;
    public String address;
    public String phone;
    public String bankCode;
    public String bankName;
    public String bankNumber;
    public boolean status;

    public Shops() {
    }

    public Shops(String id, String address, String phone, String bankCode, String bankName, String bankNumber, boolean status) {
        this.id = id;
        this.address = address;
        this.phone = phone;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.bankNumber = bankNumber;
        this.status = status;
    }
}
