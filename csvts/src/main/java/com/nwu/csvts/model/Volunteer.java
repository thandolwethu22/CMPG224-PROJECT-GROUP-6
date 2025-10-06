package com.nwu.csvts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "volunteers")
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long volunteerId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String skills;

    private String availability;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Volunteer() {}

    public Volunteer(String firstName, String lastName, String email, User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.user = user;
    }

    // Getters and Setters
    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}