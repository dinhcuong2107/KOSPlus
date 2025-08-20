package com.example.kosplus.model;

public class Banners {
    public String id;
    public String imageUrl;
    public String title;
    public String description;
    public String link;
    public int position;
    public boolean status;

    public Banners() {
    }
    public Banners(String id, String imageUrl, String title, String description, String link, int position, boolean status) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.link = link;
        this.position = position;
        this.status = status;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
