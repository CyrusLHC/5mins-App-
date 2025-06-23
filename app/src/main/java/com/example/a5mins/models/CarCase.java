package com.example.a5mins.models;

public class CarCase {
    private String id;
    private String location;
    private String note;
    private String status;
    private String time;
    private long createdAt;

    public CarCase() {
        // 空构造函数用于Firebase
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getStatusColor() {
        switch (status) {
            case "未接單":
                return android.graphics.Color.YELLOW;
            case "進行中":
                return android.graphics.Color.GREEN;
            case "已完成":
                return android.graphics.Color.RED;
            default:
                return android.graphics.Color.GRAY;
        }
    }
} 