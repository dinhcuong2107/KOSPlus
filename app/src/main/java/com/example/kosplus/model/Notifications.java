package com.example.kosplus.model;

public class Notifications {
    public String id;
    public String imageUrl;
    public String title;
    public String content;
    public String userId;
    public String time;
    public boolean status;

    public Notifications() {
    }

    public Notifications(String id, String imageUrl, String title, String content, String userId, String time, boolean status) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.time = time;
        this.status = status;
    }
}
