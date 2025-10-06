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
            return "volunteer/profile";
        } else {
            return "redirect:/dashboard?error=Volunteer profile not found";
        }
    }
    
    // Show edit profile form
    @GetMapping("/profile/edit")
    public String showEditProfileForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Optional<Volunteer> volunteer = volunteerService.getVolunteerByUser(user);
        if (volunteer.isPresent()) {
            model.addAttribute("volunteer", volunteer.get());
            return "volunteer/edit-profile";
        } else {
            return "redirect:/dashboard?error=Volunteer profile not found";
        }
    }
    
    // Update volunteer profile
    @PostMapping("/profile/edit")
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
            return "redirect:/volunteer/profile/edit";
        }
    }
}