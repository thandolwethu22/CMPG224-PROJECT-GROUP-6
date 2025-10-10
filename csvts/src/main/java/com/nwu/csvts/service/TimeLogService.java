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

    public double getTotalApprovedHours() {
        Double sum = timeLogRepository.sumAllApprovedHours();
        double result = sum != null ? sum : 0.0;
        logger.debug("getTotalApprovedHours -> {}", result);
        return result;
    }

    public Double getTotalVolunteerHours() {
        return Double.valueOf(getTotalApprovedHours());
    }

    // ...existing code...
    // Alias for compatibility with controllers that call this name
    public double getTotalApprovedHoursByVolunteer(Long volunteerId) {
        return getTotalApprovedHours(volunteerId);
    }
    // ...existing code...

    public double getTotalApprovedHours(Long volunteerId) {
        Double total = timeLogRepository.sumApprovedHoursByVolunteerId(volunteerId);
        double result = total != null ? total : 0.0;
        logger.debug("getTotalApprovedHours(volunteerId={}) -> {}", volunteerId, result);
        return result;
    }

    public double getTotalPendingHoursByVolunteer(Long volunteerId) {
        Double total = timeLogRepository.sumPendingHoursByVolunteerId(volunteerId);
        double result = total != null ? total : 0.0;
        logger.debug("getTotalPendingHoursByVolunteer({}) -> {}", volunteerId, result);
        return result;
    }

    public List<TimeLog> getPendingTimeLogs() {
        List<TimeLog> list = timeLogRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        logger.debug("getPendingTimeLogs -> size={}", list != null ? list.size() : 0);
        return list;
    }

    public List<TimeLog> getApprovedTimeLogs() {
        List<TimeLog> list = timeLogRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
        logger.debug("getApprovedTimeLogs -> size={}", list != null ? list.size() : 0);
        return list;
    }

    public List<TimeLog> getRejectedTimeLogs() {
        List<TimeLog> list = timeLogRepository.findByStatusOrderByCreatedAtDesc("REJECTED");
        logger.debug("getRejectedTimeLogs -> size={}", list != null ? list.size() : 0);
        return list;
    }

    public List<TimeLog> getTimeLogsByVolunteer(Long volunteerId) {
        List<TimeLog> list = timeLogRepository.findByVolunteerVolunteerIdOrderByCreatedAtDesc(volunteerId);
        logger.debug("getTimeLogsByVolunteer({}) -> size={}", volunteerId, list != null ? list.size() : 0);
        return list;
    }

    public List<TimeLog> getTimeLogsByVolunteerId(Long volunteerId) {
        return getTimeLogsByVolunteer(volunteerId);
    }

    public List<TimeLog> getTimeLogsByAssignment(Long assignmentId) {
        return timeLogRepository.findByAssignmentAssignmentIdOrderByCreatedAtDesc(assignmentId);
    }

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
                timeLog.setStatus("PENDING");
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

    public TimeLog saveTimeLog(TimeLog timeLog) {
        if (timeLog.getCreatedAt() == null) {
            timeLog.setCreatedAt(LocalDateTime.now());
        }
        TimeLog saved = timeLogRepository.save(timeLog);
        logger.debug("Saved TimeLog id={} status={}", saved.getId(), saved.getStatus());
        return saved;
    }

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

    public Long getPendingTimeLogsCount() {
        Long count = timeLogRepository.countByStatus("PENDING");
        logger.debug("getPendingTimeLogsCount -> {}", count);
        return count;
    }

    public Optional<TimeLog> getTimeLogById(Long timeLogId) {
        return timeLogRepository.findById(timeLogId);
    }
}