package com.nwu.csvts.controller;

import com.nwu.csvts.model.User;
import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.service.UserService;
import com.nwu.csvts.service.VolunteerService;
import com.nwu.csvts.service.TaskService;
import com.nwu.csvts.service.AssignmentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {
    
    private final UserService userService;
    private final VolunteerService volunteerService;
    private final TaskService taskService;
    private final AssignmentService assignmentService;
    
    public AuthController(UserService userService, 
                         VolunteerService volunteerService,
                         TaskService taskService,
                         AssignmentService assignmentService) {
        this.userService = userService;
        this.volunteerService = volunteerService;
        this.taskService = taskService;
        this.assignmentService = assignmentService;
    }
    
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "registered", required = false) String registered,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("success", "Registration successful! Please login.");
        }
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("volunteer", new Volunteer());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerVolunteer(@RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String firstName,
                                   @RequestParam String lastName,
                                   @RequestParam String email,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) String skills,
                                   @RequestParam(required = false) String availability,
                                   Model model) {
        try {
            if (userService.usernameExists(username)) {
                model.addAttribute("error", "Username already exists!");
                Volunteer volunteer = new Volunteer();
                volunteer.setFirstName(firstName);
                volunteer.setLastName(lastName);
                volunteer.setEmail(email);
                volunteer.setPhone(phone);
                volunteer.setSkills(skills);
                volunteer.setAvailability(availability);
                model.addAttribute("volunteer", volunteer);
                return "auth/register";
            }
            
            if (volunteerService.emailExists(email)) {
                model.addAttribute("error", "Email already registered!");
                Volunteer volunteer = new Volunteer();
                volunteer.setFirstName(firstName);
                volunteer.setLastName(lastName);
                volunteer.setEmail(email);
                volunteer.setPhone(phone);
                volunteer.setSkills(skills);
                volunteer.setAvailability(availability);
                model.addAttribute("volunteer", volunteer);
                return "auth/register";
            }
            
            Volunteer volunteer = new Volunteer();
            volunteer.setFirstName(firstName);
            volunteer.setLastName(lastName);
            volunteer.setEmail(email);
            volunteer.setPhone(phone);
            volunteer.setSkills(skills);
            volunteer.setAvailability(availability);
            
            volunteerService.registerNewVolunteer(volunteer, username, password);
            
            return "redirect:/login?registered=true";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            Volunteer volunteer = new Volunteer();
            volunteer.setFirstName(firstName);
            volunteer.setLastName(lastName);
            volunteer.setEmail(email);
            volunteer.setPhone(phone);
            volunteer.setSkills(skills);
            volunteer.setAvailability(availability);
            model.addAttribute("volunteer", volunteer);
            return "auth/register";
        }
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("username", username);
        model.addAttribute("role", user.getRole());
        
        if ("ADMIN".equals(user.getRole())) {
            // Admin dashboard with statistics
            model.addAttribute("totalTasks", taskService.getTotalTaskCount());
            model.addAttribute("openTasks", taskService.getTaskCountByStatus("OPEN"));
            model.addAttribute("completedTasks", taskService.getTaskCountByStatus("COMPLETED"));
            model.addAttribute("totalVolunteers", volunteerService.getTotalVolunteerCount());
            model.addAttribute("overdueTasks", taskService.getOverdueTasks().size());
            
            // Recent tasks for admin overview
            List<com.nwu.csvts.model.Task> recentTasks = taskService.getAllTasks();
            if (recentTasks.size() > 5) {
                recentTasks = recentTasks.subList(0, 5); // Show only 5 most recent
            }
            model.addAttribute("recentTasks", recentTasks);
            
            return "admin/dashboard";
        } else {
            // Volunteer dashboard with assigned tasks
            // FIXED: Use VolunteerService instead of user.getVolunteer()
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                List<com.nwu.csvts.model.Task> assignedTasks = taskService.getTasksAssignedToVolunteer(volunteer.get().getVolunteerId());
                List<com.nwu.csvts.model.Assignment> activeAssignments = assignmentService.getActiveAssignmentsByVolunteer(volunteer.get());
                List<com.nwu.csvts.model.Assignment> completedAssignments = assignmentService.getCompletedAssignmentsByVolunteer(volunteer.get());
                
                model.addAttribute("volunteer", volunteer.get());
                model.addAttribute("assignedTasks", assignedTasks);
                model.addAttribute("activeAssignments", activeAssignments);
                model.addAttribute("completedAssignments", completedAssignments);
                model.addAttribute("totalAssignments", activeAssignments.size() + completedAssignments.size());
                
                // Calculate completion rate
                double completionRate = 0.0;
                if (!assignedTasks.isEmpty()) {
                    completionRate = (double) completedAssignments.size() / assignedTasks.size() * 100;
                }
                model.addAttribute("completionRate", Math.round(completionRate));
            } else {
                model.addAttribute("error", "Volunteer profile not found. Please contact administrator.");
            }
            return "volunteer/dashboard";
        }
    }
}