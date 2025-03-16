package com.example.tinkdrao.model;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String nameUser;
    private String phoneNo;
    private String address;
    private String createdAt;
    private String statusOrder;
    private Long total;
    private String id;
    private String statusPay;
    private List<Drink> items; // Thêm trường này để lưu danh sách sản phẩm
    public Order()  {
    }

    public Order(String nameUser, String phoneNo, String address, String createdAt, String statusOrder, Long total, String id, String statusPay) {
        this.nameUser = nameUser;
        this.phoneNo = phoneNo;
        this.address = address;
        this.createdAt = createdAt;
        this.statusOrder = statusOrder;
        this.total = total;
        this.id = id;
        this.statusPay = statusPay;

    }

    public Order(String createdAt, String address, Long total, String nameUser, String phoneNo, List<Drink> items) {
        this.createdAt = createdAt;
        this.address = address;
        this.phoneNo = phoneNo;
        this.total = total;
        this.items = items;
        this.nameUser = nameUser;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatusOrder() {
        return statusOrder;
    }

    public void setStatusOrder(String statusOrder) {
        this.statusOrder = statusOrder;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatusPay() {
        return statusPay;
    }

    public void setStatusPay(String statusPay) {
        this.statusPay = statusPay;
    }

    public List<Drink> getItems() {
        return items;
    }

    public void setItems(List<Drink> items) {
        this.items = items;
    }
}
