package com.nwu.csvts.controller;

import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.model.TimeLog;
import com.nwu.csvts.model.Task;
import com.nwu.csvts.service.VolunteerService;
import com.nwu.csvts.service.TimeLogService;
import com.nwu.csvts.service.ReportService;
import com.nwu.csvts.service.TaskService;
import com.nwu.csvts.service.DashboardService;
import com.nwu.csvts.service.DashboardService.AdminDashboardData;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final VolunteerService volunteerService;
    private final TimeLogService timeLogService;
    private final ReportService reportService;
    private final TaskService taskService;
    private final DashboardService dashboardService;
    
    public AdminController(VolunteerService volunteerService,
                           TimeLogService timeLogService,
                           ReportService reportService,
                           TaskService taskService,
                           DashboardService dashboardService) {
        this.volunteerService = volunteerService;
        this.timeLogService = timeLogService;
        this.reportService = reportService;
        this.taskService = taskService;
        this.dashboardService = dashboardService;
    }

    // Admin Dashboard - OPTIMIZED VERSION
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        try {
            // Single optimized query for all admin stats
            AdminDashboardData dashboardData = dashboardService.getAdminDashboardData();
            
            model.addAttribute("totalTasks", dashboardData.totalTasks());
            model.addAttribute("completedTasks", dashboardData.completedTasks());
            model.addAttribute("totalVolunteers", dashboardData.totalVolunteers());
            model.addAttribute("activeVolunteers", dashboardData.activeVolunteers());
            model.addAttribute("totalHours", dashboardData.totalHours());
            model.addAttribute("pendingApprovals", dashboardData.pendingApprovals());
            model.addAttribute("title", "Admin Dashboard");
            model.addAttribute("role", "ADMIN");
            model.addAttribute("username", authentication != null ? authentication.getName() : "admin");
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            model.addAttribute("totalTasks", 0);
            model.addAttribute("completedTasks", 0);
            model.addAttribute("totalVolunteers", 0);
            model.addAttribute("activeVolunteers", 0);
            model.addAttribute("totalHours", 0.0);
            model.addAttribute("pendingApprovals", 0);
        }
        return "admin/dashboard";
    }
    
    // Manage Volunteers Page
    @GetMapping("/volunteers")
    public String manageVolunteers(Model model, Authentication authentication,
                                  @RequestParam(required = false) String search,
                                  @RequestParam(required = false) String skill) {
        try {
            List<Volunteer> volunteers;
            
            if (search != null && !search.trim().isEmpty()) {
                volunteers = volunteerService.searchVolunteers(search);
            } else if (skill != null && !skill.trim().isEmpty()) {
                volunteers = volunteerService.findBySkill(skill);
            } else {
                volunteers = volunteerService.getAllVolunteers();
            }
            
            double totalAllHours = 0.0;
            for (Volunteer volunteer : volunteers) {
                Double totalHours = timeLogService.getTotalApprovedHoursByVolunteer(volunteer.getVolunteerId());
                double hours = totalHours != null ? totalHours : 0.0;
                volunteer.setTotalHours(hours);
                totalAllHours += hours;
            }
            
            model.addAttribute("volunteers", volunteers);
            model.addAttribute("search", search);
            model.addAttribute("skill", skill);
            model.addAttribute("totalHours", totalAllHours);
            model.addAttribute("title", "Manage Volunteers");
            model.addAttribute("role", "ADMIN");
            model.addAttribute("username", authentication != null ? authentication.getName() : "admin");
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading volunteers: " + e.getMessage());
            model.addAttribute("volunteers", List.of());
            model.addAttribute("totalHours", 0.0);
        }
        
        return "admin/manage-volunteers";
    }
    
    // View Volunteer Details
    @GetMapping("/volunteers/{id}")
    public String viewVolunteer(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Optional<Volunteer> volunteer = volunteerService.getVolunteerById(id);
            if (volunteer.isPresent()) {
                Double totalHours = timeLogService.getTotalApprovedHoursByVolunteer(id);
                Double pendingHours = timeLogService.getTotalPendingHoursByVolunteer(id);
                List<TimeLog> timeLogs = timeLogService.getTimeLogsByVolunteerId(id);
                List<Task> assignedTasks = taskService.getTasksAssignedToVolunteer(id);
                
                model.addAttribute("volunteer", volunteer.get());
                model.addAttribute("totalHours", totalHours != null ? totalHours : 0.0);
                model.addAttribute("pendingHours", pendingHours != null ? pendingHours : 0.0);
                model.addAttribute("timeLogs", timeLogs != null ? timeLogs : List.of());
                model.addAttribute("assignedTasks", assignedTasks != null ? assignedTasks : List.of());
                model.addAttribute("title", "Volunteer Details - " + volunteer.get().getFirstName() + " " + volunteer.get().getLastName());
                model.addAttribute("role", "ADMIN");
                model.addAttribute("username", authentication != null ? authentication.getName() : "admin");
            } else {
                model.addAttribute("error", "Volunteer not found");
                return "redirect:/admin/volunteers";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading volunteer: " + e.getMessage());
            return "redirect:/admin/volunteers";
        }
        return "admin/volunteer-details";
    }
    
    // Update volunteer status
    @PostMapping("/volunteers/{id}/status")
    public String updateVolunteerStatus(@PathVariable Long id,
                                        @RequestParam String status,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        try {
            boolean success = volunteerService.updateVolunteerStatus(id, status);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Volunteer status updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update volunteer status.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating volunteer status: " + e.getMessage());
        }
        return "redirect:/admin/volunteers";
    }
    
    // Deactivate Volunteer
    @PostMapping("/volunteers/{id}/deactivate")
    public String deactivateVolunteer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = volunteerService.deactivateVolunteer(id);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Volunteer deactivated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Volunteer not found or already deactivated");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deactivating volunteer: " + e.getMessage());
        }
        return "redirect:/admin/volunteers";
    }
    
    // Activate Volunteer
    @PostMapping("/volunteers/{id}/activate")
    public String activateVolunteer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = volunteerService.activateVolunteer(id);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Volunteer activated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Volunteer not found or already active");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error activating volunteer: " + e.getMessage());
        }
        return "redirect:/admin/volunteers";
    }

    // Time Approval - serve pending logs page (matches dashboard link /admin/time-logs/pending)
    @GetMapping("/time-logs/pending")
    public String pendingTimeLogs(Authentication authentication, Model model) {
        try {
            List<TimeLog> pending = timeLogService.getPendingTimeLogs();
            model.addAttribute("pendingLogs", pending != null ? pending : List.of());
            model.addAttribute("title", "Time Approvals");
            model.addAttribute("role", "ADMIN");
            model.addAttribute("username", authentication != null ? authentication.getName() : "admin");
        } catch (Exception e) {
            model.addAttribute("error", "Error loading pending time logs: " + e.getMessage());
            model.addAttribute("pendingLogs", List.of());
        }
        return "admin/pending-time-logs";
    }

    // Approve Time Log
    @PostMapping("/time-logs/{logId}/approve")
    public String approveTimeLog(@PathVariable Long logId, 
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            String username = authentication != null ? authentication.getName() : "admin";
            boolean success = timeLogService.approveTimeLog(logId, username);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Time log approved successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Time log not found or already processed");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving time log: " + e.getMessage());
        }
        // redirect to the pending list route used by the dashboard/template
        return "redirect:/admin/time-logs/pending";
    }

    // Reject Time Log
    @PostMapping("/time-logs/{logId}/reject")
    public String rejectTimeLog(@PathVariable Long logId, 
                                @RequestParam(required = false) String reason,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            if (reason == null || reason.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Rejection reason is required");
                return "redirect:/admin/time-logs/pending";
            }
            
            String username = authentication != null ? authentication.getName() : "admin";
            boolean success = timeLogService.rejectTimeLog(logId, reason.trim(), username);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Time log rejected");
            } else {
                redirectAttributes.addFlashAttribute("error", "Time log not found or already processed");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting time log: " + e.getMessage());
        }
        // redirect to the pending list route used by the dashboard/template
        return "redirect:/admin/time-logs/pending";
    }
}