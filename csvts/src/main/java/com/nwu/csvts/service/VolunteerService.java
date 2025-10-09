package com.nwu.csvts.service;

import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.model.User;
import com.nwu.csvts.repository.VolunteerRepository;
import com.nwu.csvts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VolunteerService {
    
    @Autowired
    private VolunteerRepository volunteerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Get all volunteers
    public List<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }
    
    // Get volunteer by ID
    public Optional<Volunteer> getVolunteerById(Long id) {
        return volunteerRepository.findById(id);
    }
    
    // Get volunteer by user
    public Optional<Volunteer> getVolunteerByUser(User user) {
        return volunteerRepository.findByUser(user);
    }
    
    // Get volunteer by username
    public Optional<Volunteer> getVolunteerByUsername(String username) {
        return volunteerRepository.findByUserUsername(username);
    }
    
    // Search volunteers by name or email
    public List<Volunteer> searchVolunteers(String searchTerm) {
        return volunteerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            searchTerm, searchTerm, searchTerm);
    }
    
    // Find volunteers by skill
    public List<Volunteer> findBySkill(String skill) {
        return volunteerRepository.findBySkillsContainingIgnoreCase(skill);
    }
    
    // Update volunteer profile
    public Volunteer updateVolunteerProfile(Long volunteerId, Volunteer volunteerDetails) {
        Optional<Volunteer> existingVolunteer = volunteerRepository.findById(volunteerId);
        if (existingVolunteer.isPresent()) {
            Volunteer volunteer = existingVolunteer.get();
            
            // Update fields
            if (volunteerDetails.getFirstName() != null) {
                volunteer.setFirstName(volunteerDetails.getFirstName());
            }
            if (volunteerDetails.getLastName() != null) {
                volunteer.setLastName(volunteerDetails.getLastName());
            }
            if (volunteerDetails.getPhone() != null) {
                volunteer.setPhone(volunteerDetails.getPhone());
            }
            if (volunteerDetails.getSkills() != null) {
                volunteer.setSkills(volunteerDetails.getSkills());
            }
            if (volunteerDetails.getAvailability() != null) {
                volunteer.setAvailability(volunteerDetails.getAvailability());
            }
            
            return volunteerRepository.save(volunteer);
        }
        throw new RuntimeException("Volunteer not found with id: " + volunteerId);
    }
    
    // Update volunteer status
    public boolean updateVolunteerStatus(Long volunteerId, String status) {
        Optional<Volunteer> volunteer = volunteerRepository.findById(volunteerId);
        if (volunteer.isPresent()) {
            Volunteer vol = volunteer.get();
            // You might want to add status field to Volunteer entity
            // For now, we'll use user active status
            if ("ACTIVE".equals(status)) {
                vol.getUser().setActive(true);
            } else if ("INACTIVE".equals(status)) {
                vol.getUser().setActive(false);
            }
            volunteerRepository.save(vol);
            return true;
        } else {
            throw new RuntimeException("Volunteer not found with id: " + volunteerId);
        }
    }
    
    // Deactivate volunteer
    public boolean deactivateVolunteer(Long volunteerId) {
        Optional<Volunteer> volunteer = volunteerRepository.findById(volunteerId);
        if (volunteer.isPresent()) {
            Volunteer vol = volunteer.get();
            vol.getUser().setActive(false);
            volunteerRepository.save(vol);
            return true;
        } else {
            throw new RuntimeException("Volunteer not found with id: " + volunteerId);
        }
    }
    
    // Activate volunteer
    public boolean activateVolunteer(Long volunteerId) {
        Optional<Volunteer> volunteer = volunteerRepository.findById(volunteerId);
        if (volunteer.isPresent()) {
            Volunteer vol = volunteer.get();
            vol.getUser().setActive(true);
            volunteerRepository.save(vol);
            return true;
        } else {
            throw new RuntimeException("Volunteer not found with id: " + volunteerId);
        }
    }
    
    // Get total volunteers count
    public Long getTotalVolunteersCount() {
        return volunteerRepository.count();
    }
    
    // Get active volunteers count
    public Long getActiveVolunteersCount() {
        return volunteerRepository.countByUserActiveTrue();
    }
    
    // Create or save volunteer
    public Volunteer saveVolunteer(Volunteer volunteer) {
        return volunteerRepository.save(volunteer);
    }
    
    // Delete volunteer
    public void deleteVolunteer(Long volunteerId) {
        volunteerRepository.deleteById(volunteerId);
    }
    
    // Check if email exists
    public boolean emailExists(String email) {
        return volunteerRepository.existsByEmail(email);
    }
    
    // Register new volunteer with user account
    @Transactional
    public Volunteer registerNewVolunteer(Volunteer volunteer, String username, String password) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (volunteerRepository.existsByEmail(volunteer.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create user account
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("VOLUNTEER");
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        
        // Create volunteer profile
        volunteer.setUser(savedUser);
        return volunteerRepository.save(volunteer);
    }
    
    // Alias method for AuthController compatibility
    public Long getTotalVolunteerCount() {
        return getTotalVolunteersCount();
    }
}