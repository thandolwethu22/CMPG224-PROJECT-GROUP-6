package com.nwu.csvts.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String name;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Admin() {}

    public Admin(String name, User user) {
        this.name = name;
        this.user = user;
    }

    // Getters and Setters
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}