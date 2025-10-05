package com.nwu.csvts.service;

import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VolunteerService {
    
    @Autowired
    private VolunteerRepository volunteerRepository;
    
    public List<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }
    
    public Optional<Volunteer> getVolunteerById(Long id) {
        return volunteerRepository.findById(id);
    }
    
    public Volunteer saveVolunteer(Volunteer volunteer) {
        return volunteerRepository.save(volunteer);
    }
    
    public void deleteVolunteer(Long id) {
        volunteerRepository.deleteById(id);
    }
    
    public List<Volunteer> searchVolunteersByName(String name) {
        return volunteerRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Volunteer> searchVolunteersBySkill(String skill) {
        return volunteerRepository.findBySkillsContainingIgnoreCase(skill);
    }
    
    public boolean emailExists(String email) {
        return volunteerRepository.existsByEmail(email);
    }
}
