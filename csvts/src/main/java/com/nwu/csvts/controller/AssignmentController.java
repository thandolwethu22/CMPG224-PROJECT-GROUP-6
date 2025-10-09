package com.nwu.csvts.controller;

import com.nwu.csvts.model.*;
import com.nwu.csvts.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    private final TaskService taskService;
    private final VolunteerService volunteerService;
    private final TimeLogService timeLogService;
    
    @Autowired
    public AssignmentController(AssignmentService assignmentService,
                              TaskService taskService,
                              VolunteerService volunteerService,
                              TimeLogService timeLogService) {
        this.assignmentService = assignmentService;
        this.taskService = taskService;
        this.volunteerService = volunteerService;
        this.timeLogService = timeLogService;
    }

    // Admin: Assign task to volunteer
    @PostMapping("/admin/assign")
    public String assignTaskToVolunteer(@RequestParam Long taskId,
                                      @RequestParam Long volunteerId,
                                      @RequestParam String status,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            Assignment assignment = taskService.assignTaskToVolunteer(taskId, volunteerId);
            redirectAttributes.addFlashAttribute("success", 
                "Task assigned to volunteer successfully!");
            return "redirect:/tasks/admin/" + taskId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to assign task: " + e.getMessage());
            return "redirect:/assignments/admin/" + taskId + "/assign";
        }
    }
    
    // Admin: Show assign task form
    @GetMapping("/admin/{taskId}/assign")
    public String showAssignTaskForm(@PathVariable Long taskId,
                                   Authentication authentication,
                                   Model model) {
        Optional<Task> task = taskService.getTaskById(taskId);
        List<Volunteer> volunteers = volunteerService.getAllVolunteers();
        
        if (task.isPresent()) {
            model.addAttribute("task", task.get());
            model.addAttribute("volunteers", volunteers);
            model.addAttribute("title", "Assign Task");
            model.addAttribute("role", "ADMIN");
            model.addAttribute("username", authentication.getName());
            return "admin/assign-task";
        } else {
            return "redirect:/tasks/admin?error=Task not found";
        }
    }
    
    // Volunteer: Mark assignment as in progress
    @PostMapping("/volunteer/{assignmentId}/start")
    public String startAssignment(@PathVariable Long assignmentId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            assignmentService.markAssignmentAsInProgress(assignmentId);
            
            // Start time tracking
            timeLogService.startTimeTracking(assignmentId, username);
            
            redirectAttributes.addFlashAttribute("success", 
                "Task marked as in progress! Time tracking started.");
            return "redirect:/volunteer/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to update task status: " + e.getMessage());
            return "redirect:/volunteer/tasks";
        }
    }
    
    // Volunteer: Mark assignment as completed with hours
    @PostMapping("/volunteer/{assignmentId}/complete")
    public String completeAssignment(@PathVariable Long assignmentId,
                                   @RequestParam Double hoursWorked,
                                   @RequestParam(required = false) String description,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            assignmentService.markAssignmentAsCompleted(assignmentId);
            
            // Log hours
            timeLogService.logHours(assignmentId, hoursWorked, description, username);
            
            redirectAttributes.addFlashAttribute("success", 
                "Task completed! " + hoursWorked + " hours logged.");
            return "redirect:/volunteer/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to complete task: " + e.getMessage());
            return "redirect:/volunteer/tasks";
        }
    }
    
    // Admin: Remove assignment
    @PostMapping("/admin/{assignmentId}/remove")
    public String removeAssignment(@PathVariable Long assignmentId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Optional<Assignment> assignment = assignmentService.getAssignmentById(assignmentId);
            if (assignment.isPresent()) {
                Long taskId = assignment.get().getTask().getTaskId();
                assignmentService.deleteAssignment(assignmentId);
                redirectAttributes.addFlashAttribute("success", 
                    "Assignment removed successfully!");
                return "redirect:/tasks/admin/" + taskId;
            }
            return "redirect:/tasks/admin?error=Assignment not found";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to remove assignment: " + e.getMessage());
            return "redirect:/tasks/admin";
        }
    }
    
    // Volunteer: View assignment details
    @GetMapping("/volunteer/{assignmentId}")
    public String viewAssignment(@PathVariable Long assignmentId,
                               Authentication authentication,
                               Model model) {
        try {
            String username = authentication.getName();
            Optional<Assignment> assignment = assignmentService.getAssignmentById(assignmentId);
            
            if (assignment.isPresent() && 
                assignment.get().getVolunteer().getUser().getUsername().equals(username)) {
                model.addAttribute("assignment", assignment.get());
                
                // Get time logs for this assignment
                List<TimeLog> timeLogs = timeLogService.getTimeLogsByAssignment(assignmentId);
                model.addAttribute("timeLogs", timeLogs);
                model.addAttribute("title", "Assignment Details");
                model.addAttribute("role", "VOLUNTEER");
                model.addAttribute("username", assignment.get().getVolunteer().getFirstName());
                
                return "volunteer/assignment-details";
            } else {
                return "redirect:/volunteer/dashboard?error=Assignment not found or access denied";
            }
        } catch (Exception e) {
            return "redirect:/volunteer/dashboard?error=" + e.getMessage();
        }
    }
}