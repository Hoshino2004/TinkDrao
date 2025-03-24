package com.example.tinkdrao;

public class Message {
    private String senderId;
    private String receiverId;
    private String messageText;
    private String status;
    private String imageUrl;
    private String videoUrl;
    private long timestamp;

    public Message() {} // Cho Firebase

    private Message(Builder builder) {
        this.senderId = builder.senderId;
        this.receiverId = builder.receiverId;
        this.messageText = builder.messageText;
        this.status = builder.status;
        this.imageUrl = builder.imageUrl;
        this.videoUrl = builder.videoUrl;
        this.timestamp = builder.timestamp;
    }

    // Getters
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getMessageText() { return messageText; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoUrl() { return videoUrl; }
    public long getTimestamp() { return timestamp; }

    public static class Builder {
        private String senderId;
        private String receiverId;
        private String messageText;
        private String status;
        private String imageUrl;
        private String videoUrl;
        private long timestamp;

        public Builder(String senderId, String receiverId) {
            this.senderId = senderId;
            this.receiverId = receiverId;
        }

        public Builder messageText(String messageText) {
            this.messageText = messageText;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}