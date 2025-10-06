package com.nwu.csvts.controller;

import com.nwu.csvts.model.Assignment;
import com.nwu.csvts.model.Task;
import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.service.AssignmentService;
import com.nwu.csvts.service.TaskService;
import com.nwu.csvts.service.VolunteerService;
import com.nwu.csvts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    private final TaskService taskService;
    private final VolunteerService volunteerService;
    private final UserService userService;
    
    @Autowired
    public AssignmentController(AssignmentService assignmentService,
                              TaskService taskService,
                              VolunteerService volunteerService,
                              UserService userService) {
        this.assignmentService = assignmentService;
        this.taskService = taskService;
        this.volunteerService = volunteerService;
        this.userService = userService;
    }
    
    // Admin: Assign task to volunteer
    @PostMapping("/admin/assign")
    public String assignTaskToVolunteer(@RequestParam Long taskId,
                                      @RequestParam Long volunteerId,
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
            return "redirect:/tasks/admin/" + taskId;
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
            assignmentService.markAssignmentAsInProgress(assignmentId);
            redirectAttributes.addFlashAttribute("success", 
                "Task marked as in progress!");
            return "redirect:/tasks/volunteer";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to update task status: " + e.getMessage());
            return "redirect:/tasks/volunteer";
        }
    }
    
    // Volunteer: Mark assignment as completed
    @PostMapping("/volunteer/{assignmentId}/complete")
    public String completeAssignment(@PathVariable Long assignmentId,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            assignmentService.markAssignmentAsCompleted(assignmentId);
            redirectAttributes.addFlashAttribute("success", 
                "Task marked as completed!");
            return "redirect:/tasks/volunteer";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to complete task: " + e.getMessage());
            return "redirect:/tasks/volunteer";
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
                taskService.removeAssignment(assignmentId);
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
}