package com.nwu.csvts.service;

import com.nwu.csvts.model.User;
import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class VolunteerService {
    
    @Autowired
    private VolunteerRepository volunteerRepository;
    
    @Autowired
    private UserService userService;
    
    public Volunteer saveVolunteer(Volunteer volunteer) {
        return volunteerRepository.save(volunteer);
    }
    
    public Volunteer registerNewVolunteer(Volunteer volunteer, String username, String password) {
        User user = new User(username, password, "VOLUNTEER");
        User savedUser = userService.save(user);
        volunteer.setUser(savedUser);
        return volunteerRepository.save(volunteer);
    }
    
    public boolean emailExists(String email) {
        return volunteerRepository.existsByEmail(email);
    }
    
    public Optional<Volunteer> getVolunteerByUserId(Long userId) {
        return volunteerRepository.findByUserId(userId);
    }
}