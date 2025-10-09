package com.nwu.csvts.service;

import com.nwu.csvts.model.*;
import com.nwu.csvts.repository.TimeLogRepository;
import com.nwu.csvts.repository.AssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimeLogService {
    
    private final TimeLogRepository timeLogRepository;
    private final AssignmentRepository assignmentRepository;
    
    public TimeLogService(TimeLogRepository timeLogRepository,
                         AssignmentRepository assignmentRepository) {
        this.timeLogRepository = timeLogRepository;
        this.assignmentRepository = assignmentRepository;
    }
    
    // Start time tracking for an assignment
    public void startTimeTracking(Long assignmentId, String username) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if (assignment.isPresent()) {
            // Check if volunteer owns this assignment
            Volunteer volunteer = assignment.get().getVolunteer();
            if (volunteer.getUser().getUsername().equals(username)) {
                // Create a new time log entry
                TimeLog timeLog = new TimeLog();
                timeLog.setAssignment(assignment.get());
                timeLog.setVolunteer(volunteer);
                timeLog.setTask(assignment.get().getTask());
                timeLog.setHoursWorked(0.0); // Start with 0 hours
                timeLog.setDescription("Time tracking started");
                timeLog.setStatus("TRACKING");
                timeLog.setLoggedBy(username);
                timeLog.setCreatedAt(LocalDateTime.now());
                timeLogRepository.save(timeLog);
            } else {
                throw new RuntimeException("You don't have permission to track time for this assignment");
            }
        } else {
            throw new RuntimeException("Assignment not found");
        }
    }
    
    // Log hours for completed assignment
    public TimeLog logHours(Long assignmentId, Double hoursWorked, String description, String username) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if (assignment.isPresent()) {
            Volunteer volunteer = assignment.get().getVolunteer();
            if (volunteer.getUser().getUsername().equals(username)) {
                TimeLog timeLog = new TimeLog();
                timeLog.setAssignment(assignment.get());
                timeLog.setVolunteer(volunteer);
                timeLog.setTask(assignment.get().getTask());
                timeLog.setHoursWorked(hoursWorked);
                timeLog.setDescription(description != null ? description : "Task completed");
                timeLog.setStatus("PENDING"); // Needs admin approval
                timeLog.setLoggedBy(username);
                timeLog.setCreatedAt(LocalDateTime.now());
                return timeLogRepository.save(timeLog);
            } else {
                throw new RuntimeException("Permission denied");
            }
        } else {
            throw new RuntimeException("Assignment not found");
        }
    }
    
    // Save time log
    public TimeLog saveTimeLog(TimeLog timeLog) {
        if (timeLog.getCreatedAt() == null) {
            timeLog.setCreatedAt(LocalDateTime.now());
        }
        return timeLogRepository.save(timeLog);
    }
    
    // Get pending time logs for admin approval
    public List<TimeLog> getPendingTimeLogs() {
        return timeLogRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }
    
    // Get approved time logs
    public List<TimeLog> getApprovedTimeLogs() {
        return timeLogRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
    }
    
    // Get rejected time logs
    public List<TimeLog> getRejectedTimeLogs() {
        return timeLogRepository.findByStatusOrderByCreatedAtDesc("REJECTED");
    }
    
    // Get time logs by volunteer
    public List<TimeLog> getTimeLogsByVolunteer(Long volunteerId) {
        return timeLogRepository.findByVolunteerVolunteerIdOrderByCreatedAtDesc(volunteerId);
    }
    
    // Get time logs by volunteer ID - alias method
    public List<TimeLog> getTimeLogsByVolunteerId(Long volunteerId) {
        return getTimeLogsByVolunteer(volunteerId);
    }
    
    // Get time logs by assignment
    public List<TimeLog> getTimeLogsByAssignment(Long assignmentId) {
        return timeLogRepository.findByAssignmentAssignmentIdOrderByCreatedAtDesc(assignmentId);
    }
    
    // Approve time log
    public boolean approveTimeLog(Long timeLogId, String approvedBy) {
        Optional<TimeLog> timeLog = timeLogRepository.findById(timeLogId);
        if (timeLog.isPresent()) {
            TimeLog log = timeLog.get();
            log.setStatus("APPROVED");
            log.setApprovedAt(LocalDateTime.now());
            log.setApprovedBy(approvedBy);
            timeLogRepository.save(log);
            return true;
        } else {
            throw new RuntimeException("Time log not found");
        }
    }
    
    // Reject time log
    public boolean rejectTimeLog(Long timeLogId, String reason, String rejectedBy) {
        Optional<TimeLog> timeLog = timeLogRepository.findById(timeLogId);
        if (timeLog.isPresent()) {
            TimeLog log = timeLog.get();
            log.setStatus("REJECTED");
            log.setRejectionReason(reason);
            log.setRejectedBy(rejectedBy);
            log.setRejectedAt(LocalDateTime.now());
            timeLogRepository.save(log);
            return true;
        } else {
            throw new RuntimeException("Time log not found");
        }
    }
    
    // Get total approved hours for a volunteer
    public Double getTotalApprovedHours(Long volunteerId) {
        Double total = timeLogRepository.sumApprovedHoursByVolunteerId(volunteerId);
        return total != null ? total : 0.0;
    }
    
    // Alias method for consistency
    public Double getTotalApprovedHoursByVolunteer(Long volunteerId) {
        return getTotalApprovedHours(volunteerId);
    }
    
    // Get total pending hours for a volunteer
    public Double getTotalPendingHoursByVolunteer(Long volunteerId) {
        Double total = timeLogRepository.sumPendingHoursByVolunteerId(volunteerId);
        return total != null ? total : 0.0;
    }
    
    // Get total approved hours across all volunteers
    public Double getTotalVolunteerHours() {
        Double total = timeLogRepository.sumAllApprovedHours();
        return total != null ? total : 0.0;
    }
    
    // Alias method for consistency
    public Double getTotalApprovedHours() {
        return getTotalVolunteerHours();
    }
    
    // Get count of pending time logs
    public Long getPendingTimeLogsCount() {
        return timeLogRepository.countByStatus("PENDING");
    }
    
    // Get time log by ID
    public Optional<TimeLog> getTimeLogById(Long timeLogId) {
        return timeLogRepository.findById(timeLogId);
    }
}