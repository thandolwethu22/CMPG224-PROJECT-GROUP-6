package com.nwu.csvts.controller;

import com.nwu.csvts.model.User;
import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.service.UserService;
import com.nwu.csvts.service.VolunteerService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    private final UserService userService;
    private final VolunteerService volunteerService;
    
    public AuthController(UserService userService, VolunteerService volunteerService) {
        this.userService = userService;
        this.volunteerService = volunteerService;
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
            return "admin/dashboard";
        } else {
            return "volunteer/dashboard";
        }
    }
}