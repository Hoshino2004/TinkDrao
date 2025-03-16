package com.example.tinkdrao.model;

import java.io.Serializable;

public class Drink implements Serializable {
    private long id;
    private String imageUrl;

    private String name;
    private double price;
    private double discount;  // Giảm giá

    private String drinkType; // Loại đồ uống

    private int purchaseCount; // Số lượng đã bán
    private int quantity; // Số lượng trong kho
    private String unit; // Đơn vị
    private String createdAt; // Thời gian thêm sản phẩm
    private long totalSold; // Tổng số lượng đã bán (thêm trường này)


    public Drink() {
    }

    public Drink(long id, String imageUrl, String name, double price, double discount, String drinkType, int purchaseCount, int quantity, String unit, String createdAt) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.drinkType = drinkType;
        this.purchaseCount = purchaseCount;
        this.quantity = quantity;
        this.unit = unit;
        this.createdAt = createdAt;
    }

    public Drink(String name, int price, int quantity, String unit, String imageUrl) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
        this.imageUrl = imageUrl;
    }

    public Drink(Long id, String name, int price, int discount, int quantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.totalSold = 0; // Mặc định là 0
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getDrinkType() {
        return drinkType;
    }

    public void setDrinkType(String drinkType) {
        this.drinkType = drinkType;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public long getTotalSold() { return totalSold; }
    public void setTotalSold(long totalSold) { this.totalSold = totalSold; }
}
