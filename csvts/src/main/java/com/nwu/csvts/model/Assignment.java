// Assignment.java
package com.nwu.csvts.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @Column(nullable = false)
    private String status = "ASSIGNED"; // ASSIGNED, IN_PROGRESS, COMPLETED

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    // Constructors
    public Assignment() {
        this.assignedAt = LocalDateTime.now();
    }

    public Assignment(Task task, Volunteer volunteer, String status) {
        this();
        this.task = task;
        this.volunteer = volunteer;
        this.status = status;
    }

    // Getters and Setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Volunteer getVolunteer() { return volunteer; }
    public void setVolunteer(Volunteer volunteer) { this.volunteer = volunteer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}