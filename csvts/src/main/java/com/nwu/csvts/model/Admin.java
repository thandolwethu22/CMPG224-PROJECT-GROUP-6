package com.nwu.csvts.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id") // ADD THIS: Map to the correct column name
    private Long adminId;

    @Column(name = "first_name", nullable = false) // ADD THIS: Match your schema
    private String firstName;
    
    @Column(name = "last_name", nullable = false) // ADD THIS: Match your schema
    private String lastName;
    
    @Column(name = "email", nullable = false, unique = true) // ADD THIS: Match your schema
    private String email;
    
    @Column(name = "phone") // ADD THIS: Match your schema
    private String phone;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id") // ADD: referencedColumnName
    private User user;

    public Admin() {}

    public Admin(String firstName, String lastName, String email, User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.user = user;
    }

    // Getters and Setters
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    // Convenience method for full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Convenience method to get name (for backward compatibility)
    public String getName() {
        return getFullName();
    }
    
    public void setName(String name) {
        // Parse name into first and last name if needed
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            this.firstName = parts[0];
            this.lastName = parts[1];
        } else {
            this.firstName = name;
            this.lastName = "";
        }
    }
}