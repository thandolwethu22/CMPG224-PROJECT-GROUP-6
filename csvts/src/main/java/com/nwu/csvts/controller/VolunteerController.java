package com.nwu.csvts.controller;

import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.model.User;
import com.nwu.csvts.service.VolunteerService;
import com.nwu.csvts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/volunteer")
public class VolunteerController {
    
    private final VolunteerService volunteerService;
    private final UserService userService;
    
    @Autowired
    public VolunteerController(VolunteerService volunteerService, UserService userService) {
        this.volunteerService = volunteerService;
        this.userService = userService;
    }
    
    // View volunteer profile
    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
        if (volunteer.isPresent()) {
            model.addAttribute("volunteer", volunteer.get());
            model.addAttribute("user", user);
            model.addAttribute("title", "My Profile");
            model.addAttribute("role", "VOLUNTEER");
            model.addAttribute("username", volunteer.get().getFirstName());
            return "volunteer/profile";
        } else {
            return "redirect:/dashboard?error=Volunteer profile not found";
        }
    }
    
    // Update volunteer profile - FIXED: This handles the form submission from profile.html
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
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to update profile: " + e.getMessage());
            return "redirect:/volunteer/profile";
        }
    }
    
    // Volunteer Dashboard
    @GetMapping("/dashboard")
    public String volunteerDashboard(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
            if (volunteer.isPresent()) {
                model.addAttribute("volunteer", volunteer.get());
                model.addAttribute("username", volunteer.get().getFirstName());
                model.addAttribute("role", "VOLUNTEER");
                model.addAttribute("title", "Dashboard");
                
                // Add placeholder statistics (you can implement these later)
                model.addAttribute("totalAssignments", 0);
                model.addAttribute("activeAssignments", java.util.List.of());
                model.addAttribute("completedAssignments", java.util.List.of());
                model.addAttribute("assignedTasks", java.util.List.of());
                model.addAttribute("completionRate", 0);
                
                return "volunteer/dashboard";
            } else {
                model.addAttribute("error", "Volunteer profile not found");
                return "redirect:/login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    // My Tasks page (placeholder - you can implement this later)
    @GetMapping("/tasks")
    public String myTasks(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
        if (volunteer.isPresent()) {
            model.addAttribute("volunteer", volunteer.get());
            model.addAttribute("title", "My Tasks");
            model.addAttribute("role", "VOLUNTEER");
            model.addAttribute("username", volunteer.get().getFirstName());
            return "volunteer/my-tasks";
        } else {
            return "redirect:/dashboard?error=Volunteer profile not found";
        }
    }
}