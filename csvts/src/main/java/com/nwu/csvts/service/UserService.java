package com.nwu.csvts.service;

import com.nwu.csvts.model.User;
import com.nwu.csvts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public User save(User user) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return userRepository.save(user);
    }
}