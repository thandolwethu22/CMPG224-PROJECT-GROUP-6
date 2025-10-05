package com.nwu.csvts.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String name;

    // Constructors
    public Admin() {
        super();
        this.setRole("ADMIN");
    }

    public Admin(String username, String passwordHash, String name) {
        super(username, passwordHash, "ADMIN");
        this.name = name;
    }

    // Getters and Setters
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}