package com.example.tinkdrao.model;

public class User {
    String id;
    String username;
    String email;
    String password;
    String phoneno;
    String avatar;
    String role;
    String gioitinh;
    public User() {
    }

    public User(String id, String username, String email, String password, String phoneno, String avatar, String role, String gioitinh) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneno = phoneno;
        this.avatar = avatar;
        this.role = role;
        this.gioitinh =gioitinh;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGioitinh() {
        return gioitinh;
    }

    public void setGioitinh(String gioitinh) {
        this.gioitinh = gioitinh;
    }
}