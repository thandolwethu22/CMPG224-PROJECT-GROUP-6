package com.nwu.csvts.service;

import com.nwu.csvts.model.*;
import com.nwu.csvts.repository.VolunteerRepository;
import com.nwu.csvts.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    @Autowired
    private VolunteerRepository volunteerRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TimeLogService timeLogService;
    
    // Add this method to ReportService class
    public Double getTotalVolunteerHours() {
        return timeLogService.getTotalVolunteerHours();
    }
    
    // Generate volunteer hours report
    public VolunteerHoursReport generateVolunteerHoursReport() {
        List<Volunteer> volunteers = volunteerRepository.findAll();
        List<VolunteerHours> volunteerHours = new ArrayList<>();
        
        for (Volunteer volunteer : volunteers) {
            Double totalHours = timeLogService.getTotalApprovedHours(volunteer.getVolunteerId());
            if (totalHours == null) totalHours = 0.0;
            
            volunteerHours.add(new VolunteerHours(volunteer, totalHours));
        }
        
        // Sort by hours descending
        volunteerHours.sort(Comparator.comparing(VolunteerHours::getTotalHours).reversed());
        
        VolunteerHoursReport report = new VolunteerHoursReport();
        report.setVolunteerHours(volunteerHours);
        report.setGeneratedAt(LocalDateTime.now());
        report.setTotalVolunteers((long) volunteers.size());
        report.setTotalHours(volunteerHours.stream().mapToDouble(VolunteerHours::getTotalHours).sum());
        
        return report;
    }
    
    // Generate task completion report
    public TaskCompletionReport generateTaskCompletionReport() {
        List<Task> tasks = taskRepository.findAll();
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
            .filter(task -> "COMPLETED".equals(task.getStatus()))
            .count();
        
        TaskCompletionReport report = new TaskCompletionReport();
        report.setTotalTasks(totalTasks);
        report.setCompletedTasks(completedTasks);
        report.setCompletionRate(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
        report.setTasks(tasks);
        report.setGeneratedAt(LocalDateTime.now());
        
        return report;
    }
    
    // Export volunteer hours to CSV
    public void exportVolunteerHoursToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", 
            "attachment; filename=volunteer-hours-report-" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");
        
        VolunteerHoursReport report = generateVolunteerHoursReport();
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Volunteer Hours Report");
            writer.println("Generated on: " + report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            writer.println("Total Volunteers: " + report.getTotalVolunteers());
            writer.println("Total Hours: " + String.format("%.2f", report.getTotalHours()));
            writer.println();
            writer.println("Name,Email,Total Hours,Status");
            
            for (VolunteerHours vh : report.getVolunteerHours()) {
                writer.println(String.format("\"%s\",\"%s\",%.2f,%s",
                    vh.getVolunteer().getFirstName() + " " + vh.getVolunteer().getLastName(),
                    vh.getVolunteer().getEmail(),
                    vh.getTotalHours(),
                    vh.getVolunteer().getUser().getActive() ? "Active" : "Inactive"
                ));
            }
        }
    }
    
    // Export task completion to CSV
    public void exportTaskCompletionToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", 
            "attachment; filename=task-completion-report-" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");
        
        TaskCompletionReport report = generateTaskCompletionReport();
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Task Completion Report");
            writer.println("Generated on: " + report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            writer.println("Total Tasks: " + report.getTotalTasks());
            writer.println("Completed Tasks: " + report.getCompletedTasks());
            writer.println("Completion Rate: " + String.format("%.1f%%", report.getCompletionRate()));
            writer.println();
            writer.println("Title,Description,Status,Due Date,Assigned Volunteers");
            
            for (Task task : report.getTasks()) {
                writer.println(String.format("\"%s\",\"%s\",%s,%s,%d",
                    task.getTitle(),
                    task.getDescription() != null ? task.getDescription().replace("\"", "\"\"") : "",
                    task.getStatus(),
                    task.getDueDate() != null ? task.getDueDate().toString() : "Not set",
                    task.getAssignments() != null ? task.getAssignments().size() : 0
                ));
            }
        }
    }
    
    // Report DTO classes
    public static class VolunteerHoursReport {
        private List<VolunteerHours> volunteerHours;
        private LocalDateTime generatedAt;
        private Long totalVolunteers;
        private Double totalHours;
        
        // Getters and setters
        public List<VolunteerHours> getVolunteerHours() { return volunteerHours; }
        public void setVolunteerHours(List<VolunteerHours> volunteerHours) { this.volunteerHours = volunteerHours; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public Long getTotalVolunteers() { return totalVolunteers; }
        public void setTotalVolunteers(Long totalVolunteers) { this.totalVolunteers = totalVolunteers; }
        public Double getTotalHours() { return totalHours; }
        public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }
    }
    
    public static class TaskCompletionReport {
        private Long totalTasks;
        private Long completedTasks;
        private Double completionRate;
        private List<Task> tasks;
        private LocalDateTime generatedAt;
        
        // Getters and setters
        public Long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(Long totalTasks) { this.totalTasks = totalTasks; }
        public Long getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(Long completedTasks) { this.completedTasks = completedTasks; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public List<Task> getTasks() { return tasks; }
        public void setTasks(List<Task> tasks) { this.tasks = tasks; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }
    
    public static class VolunteerHours {
        private Volunteer volunteer;
        private Double totalHours;
        
        public VolunteerHours(Volunteer volunteer, Double totalHours) {
            this.volunteer = volunteer;
            this.totalHours = totalHours;
        }
        
        // Getters
        public Volunteer getVolunteer() { return volunteer; }
        public Double getTotalHours() { return totalHours; }
    }
}