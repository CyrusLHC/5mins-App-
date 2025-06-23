package com.example.a5mins.models;

public class Case {
    private String id;
    private String location;
    private String note;
    private String time;
    private String status;
    private long createdAt;
    private String driver;
    private String spot;
    private String priorityUser;

    // 空构造函数，Firebase 需要
    public Case() {
    }

    public Case(String id, String location, String note, String time, String spot) {
        this.id = id;
        this.location = location;
        this.note = note;
        this.time = time;
        this.status = "未接單";
        this.createdAt = System.currentTimeMillis();
        this.driver = ""; // 默認值為空字符串
        this.spot = spot;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getSpot() {
        return spot;
    }

    public void setSpot(String spot) {
        this.spot = spot;
    }

    public String getPriorityUser() {
        return priorityUser;
    }

    public void setPriorityUser(String priorityUser) {
        this.priorityUser = priorityUser;
    }
} 