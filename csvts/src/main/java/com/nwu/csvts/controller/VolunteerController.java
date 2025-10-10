package com.nwu.csvts.controller;

import com.nwu.csvts.model.*;
import com.nwu.csvts.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/volunteer")
public class VolunteerController {
    
    private final VolunteerService volunteerService;
    private final UserService userService;
    private final TaskService taskService;
    private final TimeLogService timeLogService;
    private final AssignmentService assignmentService;
    
    public VolunteerController(VolunteerService volunteerService, 
                             UserService userService, 
                             TaskService taskService,
                             TimeLogService timeLogService,
                             AssignmentService assignmentService) {
        this.volunteerService = volunteerService;
        this.userService = userService;
        this.taskService = taskService;
        this.timeLogService = timeLogService;
        this.assignmentService = assignmentService;
    }
    
    // View volunteer profile
    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                // Get total hours for the volunteer
                Double totalHours = timeLogService.getTotalApprovedHoursByVolunteer(volunteer.get().getVolunteerId());
                
                model.addAttribute("volunteer", volunteer.get());
                model.addAttribute("user", user);
                model.addAttribute("totalHours", totalHours != null ? totalHours : 0.0);
                model.addAttribute("title", "My Profile");
                model.addAttribute("role", "VOLUNTEER");
                model.addAttribute("username", volunteer.get().getFirstName());
                return "volunteer/profile";
            } else {
                model.addAttribute("error", "Volunteer profile not found");
                return "volunteer/profile";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading profile: " + e.getMessage());
            return "volunteer/profile";
        }
    }
    
    // Update volunteer profile
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute Volunteer volunteerDetails,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> existingVolunteer = volunteerService.getVolunteerByUser(user);
            if (existingVolunteer.isPresent()) {
                Volunteer updatedVolunteer = volunteerService.updateVolunteerProfile(
                    existingVolunteer.get().getVolunteerId(), volunteerDetails);
                
                redirectAttributes.addFlashAttribute("success", 
                    "Profile updated successfully!");
                return "redirect:/volunteer/profile";
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Volunteer profile not found");
                return "redirect:/volunteer/profile";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to update profile: " + e.getMessage());
            return "redirect:/volunteer/profile";
        }
    }
    
    // Volunteer Dashboard - FIXED VERSION
    @GetMapping("/dashboard")
    public String volunteerDashboard(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                Volunteer volunteerObj = volunteer.get();
                
                // Get task statistics for dashboard - FIXED: Handle null cases
                List<Task> assignedTasks = taskService.getTasksAssignedToVolunteer(volunteerObj.getVolunteerId());
                List<Task> openTasks = assignedTasks != null ? assignedTasks.stream()
                        .filter(task -> task != null && "OPEN".equals(task.getStatus()))
                        .collect(Collectors.toList()) : new ArrayList<>();
                List<Task> inProgressTasks = assignedTasks != null ? assignedTasks.stream()
                        .filter(task -> task != null && "IN_PROGRESS".equals(task.getStatus()))
                        .collect(Collectors.toList()) : new ArrayList<>();
                List<Task> completedTasks = assignedTasks != null ? assignedTasks.stream()
                        .filter(task -> task != null && "COMPLETED".equals(task.getStatus()))
                        .collect(Collectors.toList()) : new ArrayList<>();
                
                // Get total hours - FIXED: Handle null cases
                Double totalHours = timeLogService.getTotalApprovedHoursByVolunteer(volunteerObj.getVolunteerId());
                Double pendingHours = timeLogService.getTotalPendingHoursByVolunteer(volunteerObj.getVolunteerId());
                
                // Calculate completion rate safely
                int completionRate = 0;
                if (assignedTasks != null && !assignedTasks.isEmpty()) {
                    completionRate = (completedTasks.size() * 100) / assignedTasks.size();
                }
                
                model.addAttribute("volunteer", volunteerObj);
                model.addAttribute("username", volunteerObj.getFirstName());
                model.addAttribute("role", "VOLUNTEER");
                model.addAttribute("title", "Dashboard");
                model.addAttribute("totalAssignments", assignedTasks != null ? assignedTasks.size() : 0);
                model.addAttribute("activeAssignments", inProgressTasks.size());
                model.addAttribute("completedAssignments", completedTasks.size());
                model.addAttribute("assignedTasks", assignedTasks != null ? assignedTasks : new ArrayList<>());
                model.addAttribute("totalHours", totalHours != null ? totalHours : 0.0);
                model.addAttribute("pendingHours", pendingHours != null ? pendingHours : 0.0);
                model.addAttribute("completionRate", completionRate);
                
                return "volunteer/dashboard";
            } else {
                model.addAttribute("error", "Volunteer profile not found. Please contact administrator.");
                return "volunteer/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "volunteer/dashboard";
        }
    }
    
    // My Tasks page
    @GetMapping("/tasks")
    public String myTasks(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                // Get tasks assigned to this volunteer
                List<Task> assignedTasks = taskService.getTasksAssignedToVolunteer(volunteer.get().getVolunteerId());
                
                // Filter tasks by status
                List<Task> openTasks = assignedTasks != null ? assignedTasks.stream()
                        .filter(task -> task != null && "OPEN".equals(task.getStatus()))
                        .collect(Collectors.toList()) : new ArrayList<>();
                        
                List<Task> inProgressTasks = assignedTasks != null ? assignedTasks.stream()
                        .filter(task -> task != null && "IN_PROGRESS".equals(task.getStatus()))
                        .collect(Collectors.toList()) : new ArrayList<>();
                        
                List<Task> completedTasks = assignedTasks != null ? assignedTasks.stream()
                        .filter(task -> task != null && "COMPLETED".equals(task.getStatus()))
                        .collect(Collectors.toList()) : new ArrayList<>();
                
                model.addAttribute("volunteer", volunteer.get());
                model.addAttribute("assignedTasks", assignedTasks != null ? assignedTasks : new ArrayList<>());
                model.addAttribute("openTasks", openTasks);
                model.addAttribute("inProgressTasks", inProgressTasks);
                model.addAttribute("completedTasks", completedTasks);
                model.addAttribute("title", "My Tasks");
                model.addAttribute("role", "VOLUNTEER");
                model.addAttribute("username", volunteer.get().getFirstName());
                
                return "volunteer/my-tasks";
            } else {
                model.addAttribute("error", "Volunteer profile not found");
                return "volunteer/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading tasks: " + e.getMessage());
            return "volunteer/dashboard";
        }
    }
    
    // Start a task
    @PostMapping("/tasks/{taskId}/start")
    public String startTask(@PathVariable Long taskId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                // Verify the task is assigned to this volunteer
                List<Task> volunteerTasks = taskService.getTasksAssignedToVolunteer(volunteer.get().getVolunteerId());
                boolean isAssigned = volunteerTasks != null && volunteerTasks.stream()
                        .anyMatch(task -> task != null && task.getTaskId().equals(taskId));
                
                if (isAssigned) {
                    // Update task status
                    taskService.updateTaskStatus(taskId, "IN_PROGRESS");
                    
                    redirectAttributes.addFlashAttribute("success", "Task started successfully!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Task not found or not assigned to you");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Volunteer profile not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error starting task: " + e.getMessage());
        }
        return "redirect:/volunteer/tasks";
    }
    
    // Show complete task form
    @GetMapping("/tasks/{taskId}/complete-form")
    public String showCompleteTaskForm(@PathVariable Long taskId, Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                Optional<Task> task = taskService.getTaskById(taskId);
                if (task.isPresent()) {
                    // Verify assignment
                    List<Task> volunteerTasks = taskService.getTasksAssignedToVolunteer(volunteer.get().getVolunteerId());
                    boolean isAssigned = volunteerTasks != null && volunteerTasks.stream()
                            .anyMatch(t -> t != null && t.getTaskId().equals(taskId));
                    
                    if (isAssigned && "IN_PROGRESS".equals(task.get().getStatus())) {
                        model.addAttribute("task", task.get());
                        model.addAttribute("volunteer", volunteer.get());
                        model.addAttribute("title", "Complete Task");
                        model.addAttribute("role", "VOLUNTEER");
                        model.addAttribute("username", volunteer.get().getFirstName());
                        return "volunteer/complete-tasks-form";
                    } else {
                        return "redirect:/volunteer/tasks?error=Task not found, not assigned to you, or not in progress";
                    }
                }
            }
            return "redirect:/volunteer/tasks?error=Task not found or access denied";
        } catch (Exception e) {
            return "redirect:/volunteer/tasks?error=" + e.getMessage();
        }
    }
    
    // Complete a task with hours
    @PostMapping("/tasks/{taskId}/complete")
    public String completeTask(@PathVariable Long taskId,
                             @RequestParam Double hoursWorked,
                             @RequestParam String completionNotes,
                             @RequestParam(required = false) String additionalComments,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            // Validate hours worked
            if (hoursWorked <= 0 || hoursWorked > 24) {
                redirectAttributes.addFlashAttribute("error", "Hours worked must be between 0.1 and 24");
                return "redirect:/volunteer/tasks/" + taskId + "/complete-form";
            }
            
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                // Verify the task is assigned to this volunteer and in progress
                List<Task> volunteerTasks = taskService.getTasksAssignedToVolunteer(volunteer.get().getVolunteerId());
                boolean isAssigned = volunteerTasks != null && volunteerTasks.stream()
                        .anyMatch(task -> task != null && task.getTaskId().equals(taskId) && "IN_PROGRESS".equals(task.getStatus()));
                
                if (isAssigned) {
                    // Update task status to completed
                    taskService.updateTaskStatus(taskId, "COMPLETED");
                    
                    // Find the assignment and log hours
                    List<Assignment> assignments = assignmentService.getAssignmentsByVolunteer(volunteer.get());
                    Optional<Assignment> assignment = assignments.stream()
                            .filter(a -> a != null && a.getTask() != null && a.getTask().getTaskId().equals(taskId))
                            .findFirst();
                    
                    if (assignment.isPresent()) {
                        // Combine notes and additional comments
                        String description = completionNotes;
                        if (additionalComments != null && !additionalComments.trim().isEmpty()) {
                            description += "\n\nAdditional Comments: " + additionalComments;
                        }
                        
                        // Create time log with PENDING status
                        TimeLog timeLog = new TimeLog();
                        timeLog.setAssignment(assignment.get());
                        timeLog.setVolunteer(volunteer.get());
                        timeLog.setTask(assignment.get().getTask());
                        timeLog.setHoursWorked(hoursWorked);
                        timeLog.setDescription(description);
                        timeLog.setStatus("PENDING"); // Set to pending for admin approval
                        timeLog.setLoggedBy(username);
                        
                        timeLogService.saveTimeLog(timeLog);
                        
                        redirectAttributes.addFlashAttribute("success", 
                            "Task completed successfully! " + hoursWorked + " hours logged and submitted for approval.");
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Assignment not found for this task");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", 
                        "Task not found, not assigned to you, or not in progress status");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Volunteer profile not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error completing task: " + e.getMessage());
        }
        return "redirect:/volunteer/tasks";
    }
    
    // View my time logs
    @GetMapping("/time-logs")
    public String viewMyTimeLogs(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                List<TimeLog> timeLogs = timeLogService.getTimeLogsByVolunteerId(volunteer.get().getVolunteerId());
                Double totalHours = timeLogService.getTotalApprovedHoursByVolunteer(volunteer.get().getVolunteerId());
                Double pendingHours = timeLogService.getTotalPendingHoursByVolunteer(volunteer.get().getVolunteerId());
                
                model.addAttribute("timeLogs", timeLogs != null ? timeLogs : new ArrayList<>());
                model.addAttribute("totalHours", totalHours != null ? totalHours : 0.0);
                model.addAttribute("pendingHours", pendingHours != null ? pendingHours : 0.0);
                model.addAttribute("volunteer", volunteer.get());
                model.addAttribute("title", "My Time Logs");
                model.addAttribute("role", "VOLUNTEER");
                model.addAttribute("username", volunteer.get().getFirstName());
                return "volunteer/time-logs";
            } else {
                model.addAttribute("error", "Volunteer profile not found");
                return "volunteer/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading time logs: " + e.getMessage());
            return "volunteer/dashboard";
        }
    }
    
    // View assignment details
    @GetMapping("/assignments/{assignmentId}")
    public String viewAssignment(@PathVariable Long assignmentId, Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                // Get assignment details
                Optional<Assignment> assignment = assignmentService.getAssignmentById(assignmentId);
                if (assignment.isPresent() && 
                    assignment.get().getVolunteer().getVolunteerId().equals(volunteer.get().getVolunteerId())) {
                    
                    model.addAttribute("assignment", assignment.get());
                    
                    // Get time logs for this assignment
                    List<TimeLog> timeLogs = timeLogService.getTimeLogsByAssignment(assignmentId);
                    model.addAttribute("timeLogs", timeLogs != null ? timeLogs : new ArrayList<>());
                    
                    model.addAttribute("title", "Assignment Details");
                    model.addAttribute("role", "VOLUNTEER");
                    model.addAttribute("username", volunteer.get().getFirstName());
                    return "volunteer/assignment-details";
                }
            }
            return "redirect:/volunteer/dashboard?error=Assignment not found or access denied";
        } catch (Exception e) {
            return "redirect:/volunteer/dashboard?error=" + e.getMessage();
        }
    }
}