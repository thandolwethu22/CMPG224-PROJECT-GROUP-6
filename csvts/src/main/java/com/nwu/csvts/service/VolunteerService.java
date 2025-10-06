package com.nwu.csvts.service;

import com.nwu.csvts.model.User;
import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.repository.VolunteerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class VolunteerService {
    
    private final VolunteerRepository volunteerRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    public VolunteerService(VolunteerRepository volunteerRepository, 
                          UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.volunteerRepository = volunteerRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    
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