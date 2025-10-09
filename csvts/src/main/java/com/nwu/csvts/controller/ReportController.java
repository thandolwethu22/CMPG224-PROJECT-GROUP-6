package com.nwu.csvts.controller;

import com.nwu.csvts.service.ReportService;
import com.nwu.csvts.service.VolunteerService;
import com.nwu.csvts.service.TimeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private VolunteerService volunteerService;
    
    @Autowired
    private TimeLogService timeLogService;
    
    // Reports Dashboard
    @GetMapping
    public String reportsDashboard(Model model) {
        try {
            // Basic statistics
            Long totalVolunteers = volunteerService.getTotalVolunteersCount();
            Long activeVolunteers = volunteerService.getActiveVolunteersCount();
            Double totalHours = timeLogService.getTotalVolunteerHours();
            Long pendingApprovals = timeLogService.getPendingTimeLogsCount();
            
            model.addAttribute("totalVolunteers", totalVolunteers);
            model.addAttribute("activeVolunteers", activeVolunteers);
            model.addAttribute("totalHours", totalHours != null ? totalHours : 0.0);
            model.addAttribute("pendingApprovals", pendingApprovals);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading reports: " + e.getMessage());
        }
        return "admin/reports-dashboard";
    }
    
    // Volunteer Hours Report
    @GetMapping("/volunteer-hours")
    public String volunteerHoursReport(Model model) {
        try {
            var report = reportService.generateVolunteerHoursReport();
            model.addAttribute("report", report);
        } catch (Exception e) {
            model.addAttribute("error", "Error generating report: " + e.getMessage());
        }
        return "admin/reports/volunteer-hours";
    }
    
    // Task Completion Report
    @GetMapping("/task-completion")
    public String taskCompletionReport(Model model) {
        try {
            var report = reportService.generateTaskCompletionReport();
            model.addAttribute("report", report);
        } catch (Exception e) {
            model.addAttribute("error", "Error generating report: " + e.getMessage());
        }
        return "admin/reports/task-completion";
    }
    
    // Export Volunteer Hours to CSV
    @GetMapping("/export/volunteer-hours")
    public void exportVolunteerHours(HttpServletResponse response) throws IOException {
        reportService.exportVolunteerHoursToCsv(response);
    }
    
    // Export Task Completion to CSV
    @GetMapping("/export/task-completion")
    public void exportTaskCompletion(HttpServletResponse response) throws IOException {
        reportService.exportTaskCompletionToCsv(response);
    }
}