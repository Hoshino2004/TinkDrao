package com.example.tinkdrao;

import java.io.Serializable;
import java.util.List;

public class Promotion implements Serializable {
    private String promotionId;
    private double discount;
    private List<String> drinkIds;

    public Promotion() {
    }

    public Promotion(String promotionId, double discount, List<String> drinkIds) {
        this.promotionId = promotionId;
        this.discount = discount;
        this.drinkIds = drinkIds;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public List<String> getDrinkIds() {
        return drinkIds;
    }

    public void setDrinkIds(List<String> drinkIds) {
        this.drinkIds = drinkIds;
    }
}