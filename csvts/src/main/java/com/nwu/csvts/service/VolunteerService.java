package com.nwu.csvts.service;

import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.model.User;
import com.nwu.csvts.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VolunteerService {
    
    private final VolunteerRepository volunteerRepository;
    
    @Autowired
    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
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
                    existingVolunteer.setFirstName(volunteerDetails.getFirstName());
                    existingVolunteer.setLastName(volunteerDetails.getLastName());
                    existingVolunteer.setPhone(volunteerDetails.getPhone());
                    existingVolunteer.setEmail(volunteerDetails.getEmail());
                    existingVolunteer.setSkills(volunteerDetails.getSkills());
                    existingVolunteer.setAvailability(volunteerDetails.getAvailability());
                    return volunteerRepository.save(existingVolunteer);
                })
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + volunteerId));
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
}