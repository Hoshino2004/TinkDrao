package com.example.tinkdrao.model;

public class Cart {
    private long id;
    private String imageUrl;
    private String name;
    private double price;
    private double discount;  // Giảm giá
    private String drinkType; // Loại đồ uống
    private int quantity; // Số lượng
    private String unit; // Đơn vị
    public Cart(){}
    public Cart(long id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }
    public Cart(long id, String imageUrl, String name, double price, double discount, String drinkType, int quantity, String unit) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.drinkType = drinkType;
        this.quantity = quantity;
        this.unit = unit;
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
}
