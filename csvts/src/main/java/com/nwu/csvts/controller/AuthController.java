package com.nwu.csvts.controller;

import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.service.UserService;
import com.nwu.csvts.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VolunteerService volunteerService;
    
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("volunteer", new Volunteer());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerVolunteer(Volunteer volunteer, Model model) {
        try {
            // Check if username already exists
            if (userService.usernameExists(volunteer.getUsername())) {
                model.addAttribute("error", "Username already exists!");
                return "auth/register";
            }
            
            // Check if email already exists
            if (volunteerService.emailExists(volunteer.getEmail())) {
                model.addAttribute("error", "Email already registered!");
                return "auth/register";
            }
            
            // Save volunteer
            volunteerService.saveVolunteer(volunteer);
            model.addAttribute("success", "Registration successful! Please login.");
            return "auth/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}