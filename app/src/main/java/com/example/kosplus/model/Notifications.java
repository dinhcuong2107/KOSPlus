package com.example.kosplus.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class Notifications {
    @PrimaryKey()
    @NonNull
    public String id;
    public String imageUrl;
    public String title;
    public String content;
    public String userId;
    public long time;
    public boolean status;

    public Notifications() {
    }

    public Notifications(@NonNull String id, String imageUrl, String title, String content, String userId, long time, boolean status) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.time = time;
        this.status = status;
    }
}
