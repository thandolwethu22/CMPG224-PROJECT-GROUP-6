package com.nwu.csvts.service;

import com.nwu.csvts.model.*;
import com.nwu.csvts.repository.TimeLogRepository;
import com.nwu.csvts.repository.AssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimeLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeLogService.class);

    private final TimeLogRepository timeLogRepository;
    private final AssignmentRepository assignmentRepository;
    
    public TimeLogService(TimeLogRepository timeLogRepository,
                         AssignmentRepository assignmentRepository) {
        this.timeLogRepository = timeLogRepository;
        this.assignmentRepository = assignmentRepository;
    }
    
    // Return total approved hours across all volunteers (never null)
    public double getTotalApprovedHours() {
        Double sum = timeLogRepository.sumAllApprovedHours();
        double result = sum != null ? sum : 0.0;
        logger.debug("getTotalApprovedHours -> {}", result);
        return result;
    }

    // Compatibility method used by ReportService
    // Returns boxed Double to match existing callers that expect a nullable Double
    public Double getTotalVolunteerHours() {
        return Double.valueOf(getTotalApprovedHours());
    }

    // Total approved hours for a specific volunteer
    public double getTotalApprovedHours(Long volunteerId) {
        Double total = timeLogRepository.sumApprovedHoursByVolunteerId(volunteerId);
        double result = total != null ? total : 0.0;
        logger.debug("getTotalApprovedHours(volunteerId={}) -> {}", volunteerId, result);
        return result;
    }

    // Alias for clarity
    public double getTotalApprovedHoursByVolunteer(Long volunteerId) {
        return getTotalApprovedHours(volunteerId);
    }
    
    // Get total pending hours for a volunteer
    public double getTotalPendingHoursByVolunteer(Long volunteerId) {
        Double total = timeLogRepository.sumPendingHoursByVolunteerId(volunteerId);
        double result = total != null ? total : 0.0;
        logger.debug("getTotalPendingHoursByVolunteer({}) -> {}", volunteerId, result);
        return result;
    }
    
    // Start time tracking for an assignment
    public void startTimeTracking(Long assignmentId, String username) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if (assignment.isPresent()) {
            Volunteer volunteer = assignment.get().getVolunteer();
            if (volunteer.getUser().getUsername().equals(username)) {
                TimeLog timeLog = new TimeLog();
                timeLog.setAssignment(assignment.get());
                timeLog.setVolunteer(volunteer);
                timeLog.setTask(assignment.get().getTask());
                timeLog.setHoursWorked(0.0);
                timeLog.setDescription("Time tracking started");
                timeLog.setStatus("TRACKING");
                timeLog.setLoggedBy(username);
                timeLog.setCreatedAt(LocalDateTime.now());
                timeLogRepository.save(timeLog);
                logger.info("Started time tracking for assignment {} by {}", assignmentId, username);
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
                TimeLog saved = timeLogRepository.save(timeLog);
                logger.info("Logged hours {} for assignment {} by {}", hoursWorked, assignmentId, username);
                return saved;
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
        TimeLog saved = timeLogRepository.save(timeLog);
        // use standard id accessor to avoid relying on non-standard method names
        logger.debug("Saved TimeLog id={} status={}", saved.getId(), saved.getStatus());
        return saved;
    }
    
    // Get pending time logs for admin approval
    public List<TimeLog> getPendingTimeLogs() {
        List<TimeLog> list = timeLogRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        logger.debug("getPendingTimeLogs -> size={}", list != null ? list.size() : 0);
        return list;
    }
    
    // Get approved time logs
    public List<TimeLog> getApprovedTimeLogs() {
        List<TimeLog> list = timeLogRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
        logger.debug("getApprovedTimeLogs -> size={}", list != null ? list.size() : 0);
        return list;
    }
    
    // Get rejected time logs
    public List<TimeLog> getRejectedTimeLogs() {
        List<TimeLog> list = timeLogRepository.findByStatusOrderByCreatedAtDesc("REJECTED");
        logger.debug("getRejectedTimeLogs -> size={}", list != null ? list.size() : 0);
        return list;
    }
    
    // Get time logs by volunteer
    public List<TimeLog> getTimeLogsByVolunteer(Long volunteerId) {
        List<TimeLog> list = timeLogRepository.findByVolunteerVolunteerIdOrderByCreatedAtDesc(volunteerId);
        logger.debug("getTimeLogsByVolunteer({}) -> size={}", volunteerId, list != null ? list.size() : 0);
        return list;
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
            logger.info("Approved TimeLog {} by {}", timeLogId, approvedBy);
            return true;
        } else {
            logger.warn("approveTimeLog: not found id={}", timeLogId);
            return false;
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
            logger.info("Rejected TimeLog {} by {} reason={}", timeLogId, rejectedBy, reason);
            return true;
        } else {
            logger.warn("rejectTimeLog: not found id={}", timeLogId);
            return false;
        }
    }
    
    // Get count of pending time logs
    public Long getPendingTimeLogsCount() {
        Long count = timeLogRepository.countByStatus("PENDING");
        logger.debug("getPendingTimeLogsCount -> {}", count);
        return count;
    }
    
    // Get time log by ID
    public Optional<TimeLog> getTimeLogById(Long timeLogId) {
        return timeLogRepository.findById(timeLogId);
    }
}