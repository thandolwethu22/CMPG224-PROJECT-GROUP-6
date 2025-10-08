package com.nwu.csvts.service;

import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.model.User;
import com.nwu.csvts.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VolunteerService {
    
    private final VolunteerRepository volunteerRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public VolunteerService(VolunteerRepository volunteerRepository, 
                          UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.volunteerRepository = volunteerRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    
    // Register new volunteer - FIXED: Uses passwordHash instead of password
    public void registerNewVolunteer(Volunteer volunteer, String username, String password) {
        // Create user account with encoded password
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password)); // FIXED: Use setPasswordHash
        user.setRole("VOLUNTEER");
        User savedUser = userService.save(user);
        
        // Create volunteer profile and link to user
        volunteer.setUser(savedUser);
        volunteerRepository.save(volunteer);
        
        // Set bidirectional relationship
        savedUser.setVolunteer(volunteer);
        userService.save(savedUser); // Save the updated user with volunteer reference
    }
    
    // Existing methods
    public Volunteer saveVolunteer(Volunteer volunteer) {
        return volunteerRepository.save(volunteer);
    }
    
    public Optional<Volunteer> getVolunteerById(Long volunteerId) {
        return volunteerRepository.findById(volunteerId);
    }
    
    public Optional<Volunteer> getVolunteerByUser(User user) {
        return volunteerRepository.findByUser(user);
    }
    
    public Optional<Volunteer> getVolunteerByEmail(String email) {
        return volunteerRepository.findByEmail(email);
    }
    
    public List<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }
    
    public boolean emailExists(String email) {
        return volunteerRepository.existsByEmail(email);
    }
    
    // New methods for task management integration
    public List<Volunteer> searchVolunteersByName(String name) {
        return volunteerRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Volunteer> searchVolunteersBySkill(String skill) {
        return volunteerRepository.findBySkillsContainingIgnoreCase(skill);
    }
    
    public List<Volunteer> searchVolunteersByAvailability(String availability) {
        return volunteerRepository.findByAvailabilityContainingIgnoreCase(availability);
    }
    
    public Volunteer updateVolunteerProfile(Long volunteerId, Volunteer volunteerDetails) {
        return volunteerRepository.findById(volunteerId)
                .map(existingVolunteer -> {
                    // Update only the fields that are provided and allowed to be updated
                    if (volunteerDetails.getFirstName() != null) {
                        existingVolunteer.setFirstName(volunteerDetails.getFirstName());
                    }
                    if (volunteerDetails.getLastName() != null) {
                        existingVolunteer.setLastName(volunteerDetails.getLastName());
                    }
                    if (volunteerDetails.getPhone() != null) {
                        existingVolunteer.setPhone(volunteerDetails.getPhone());
                    }
                    if (volunteerDetails.getEmail() != null) {
                        existingVolunteer.setEmail(volunteerDetails.getEmail());
                    }
                    if (volunteerDetails.getSkills() != null) {
                        existingVolunteer.setSkills(volunteerDetails.getSkills());
                    }
                    if (volunteerDetails.getAvailability() != null) {
                        existingVolunteer.setAvailability(volunteerDetails.getAvailability());
                    }
                    
                    // Handle optional fields safely
                    updateOptionalField(existingVolunteer, volunteerDetails, "address");
                    updateOptionalField(existingVolunteer, volunteerDetails, "interests");
                    updateOptionalField(existingVolunteer, volunteerDetails, "emergencyContact");
                    
                    return volunteerRepository.save(existingVolunteer);
                })
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + volunteerId));
    }
    
    // Helper method to safely update optional fields
    private void updateOptionalField(Volunteer existing, Volunteer updated, String fieldName) {
        try {
            java.lang.reflect.Method getter = updated.getClass().getMethod("get" + capitalize(fieldName));
            java.lang.reflect.Method setter = existing.getClass().getMethod("set" + capitalize(fieldName), String.class);
            
            Object value = getter.invoke(updated);
            if (value != null) {
                setter.invoke(existing, value);
            }
        } catch (Exception e) {
            // Field doesn't exist or can't be accessed, ignore
        }
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    public void deleteVolunteer(Long volunteerId) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + volunteerId));
        volunteerRepository.delete(volunteer);
    }
    
    // Statistics
    public long getTotalVolunteerCount() {
        return volunteerRepository.count();
    }
    
    // Additional helper methods for dashboard
    public int getTotalAssignments(Long volunteerId) {
        // Implement based on your Assignment entity
        return 0; // Placeholder
    }
    
    public List<Object> getActiveAssignments(Long volunteerId) {
        // Implement based on your Assignment entity
        return List.of(); // Placeholder
    }
    
    public List<Object> getCompletedAssignments(Long volunteerId) {
        // Implement based on your Assignment entity
        return List.of(); // Placeholder
    }
    
    public List<Object> getAssignedTasks(Long volunteerId) {
        // Implement based on your Task and Assignment entities
        return List.of(); // Placeholder
    }
}