package com.nwu.csvts.service;

import com.nwu.csvts.model.User;
import com.nwu.csvts.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public User save(User user) {
        // Improved password encoding logic
        if (user.getPasswordHash() != null && !isAlreadyEncoded(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return userRepository.save(user);
    }
    
    // Helper method to check if password is already encoded
    private boolean isAlreadyEncoded(String password) {
        // BCrypt encoded passwords start with $2a$, $2b$, or $2y$
        return password != null && 
               (password.startsWith("$2a$") || 
                password.startsWith("$2b$") || 
                password.startsWith("$2y$"));
    }
    
    // Additional useful methods
    public User createUser(String username, String plainPassword, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setRole(role);
        return userRepository.save(user);
    }
    
    public long getTotalUserCount() {
        return userRepository.count();
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}