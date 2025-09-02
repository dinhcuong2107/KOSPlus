package com.example.kosplus.model;

public class RewardHistory {
    public String id;
    public String reward;
    public String userId;
    public String time;
    public String usageTime;

    public RewardHistory() {}

    public RewardHistory(String id, String reward, String userId, String time, String usageTime) {
        this.id = id;
        this.reward = reward;
        this.userId = userId;
        this.time = time;
        this.usageTime = usageTime;
    }

}
