package com.example.tinkdrao.model;

public class Drink {
    private String name;
    private double originalPrice;  // Giá gốc
    private double discountedPrice;  // Giá đã giảm
    private String imageUrl;

    public Drink() {
        // Constructor mặc định cần cho Firebase
    }

    public Drink(String name, double originalPrice, double discountedPrice, String imageUrl) {
        this.name = name;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public double getDiscountedPrice() {
        return discountedPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
