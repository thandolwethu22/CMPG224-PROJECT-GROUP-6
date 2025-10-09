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
    @Column(name = "task_id") // ADD THIS: Map to the correct column name
    private Long taskId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "start_date") // ADD THIS: Map to the correct column name
    private LocalDate startDate;
    
    @Column(name = "due_date") // ADD THIS: Map to the correct column name
    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "user_id", nullable = false) // ADD: referencedColumnName
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments = new ArrayList<>();

    // Constructors
    public Task() {
        this.createdAt = LocalDateTime.now();
        this.status = "OPEN";
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

    public List<Assignment> getAssignments() { return assignments; }
    public void setAssignments(List<Assignment> assignments) { this.assignments = assignments; }

    // Assignment management methods
    public void addAssignment(Assignment assignment) {
        if (assignments == null) {
            assignments = new ArrayList<>();
        }
        assignments.add(assignment);
        assignment.setTask(this);
    }

    public void removeAssignment(Assignment assignment) {
        if (assignments != null) {
            assignments.remove(assignment);
            assignment.setTask(null);
        }
    }

    // Business logic methods
    public boolean isOverdue() {
        return dueDate != null && 
               dueDate.isBefore(LocalDate.now()) && 
               !"COMPLETED".equals(status) && 
               !"CANCELLED".equals(status);
    }

    public boolean canBeAssigned() {
        return "OPEN".equals(status) || "IN_PROGRESS".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    public boolean isOpen() {
        return "OPEN".equals(status);
    }

    // Convenience method to get assigned volunteers count
    public int getAssignedVolunteersCount() {
        return assignments != null ? assignments.size() : 0;
    }

    // Status change methods
    public void markAsInProgress() {
        this.status = "IN_PROGRESS";
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
    }

    public void reopen() {
        this.status = "OPEN";
    }
}