package com.example.tinkdrao.model;

public class Comment {
    private String userId;
    private String username;
    private String content;
    private String timestamp;
    private Long drinkId;
    private float rateStar;

    public Comment() {
    }

    public Comment(String userId, String username, String content, String timestamp, Long drinkId, float rateStar) {
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.drinkId = drinkId;
        this.rateStar = rateStar;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Long getDrinkId() { return drinkId; }
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId; }

    public float getRateStar() { return rateStar; }
    public void setRateStar(float rateStar) { this.rateStar = rateStar; }
}