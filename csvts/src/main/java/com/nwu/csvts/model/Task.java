package com.nwu.csvts.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, COMPLETED, CANCELLED

    private LocalDate startDate;
    
    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ADD THIS MISSING FIELD:
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments = new ArrayList<>();

    // Constructors
    public Task() {
        this.createdAt = LocalDateTime.now();
    }

    public Task(String title, String description, String status, LocalDate dueDate, User createdBy) {
        this();
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ADD GETTER FOR ASSIGNMENTS:
    public List<Assignment> getAssignments() { return assignments; }
    public void setAssignments(List<Assignment> assignments) { this.assignments = assignments; }

    // ADD THESE MISSING METHODS:
    public void addAssignment(Assignment assignment) {
        this.assignments.add(assignment);
        assignment.setTask(this);
    }

    public void removeAssignment(Assignment assignment) {
        this.assignments.remove(assignment);
        assignment.setTask(null);
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !"COMPLETED".equals(status);
    }

    public boolean canBeAssigned() {
        return "OPEN".equals(status) || "IN_PROGRESS".equals(status);
    }
}