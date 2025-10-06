package com.nwu.csvts.config;

import com.nwu.csvts.model.User;
import com.nwu.csvts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserConfig implements CommandLineRunner {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if it doesn't exist
        if (userService.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userService.save(admin);
            System.out.println("✅ Admin user created: admin / admin123");
        } else {
            System.out.println("ℹ️ Admin user already exists");
        }
    }
}