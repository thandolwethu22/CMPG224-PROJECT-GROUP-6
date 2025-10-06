-- Insert default admin user (password: admin123)
INSERT INTO users (username, password_hash, role) VALUES 
('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ADMIN');

INSERT INTO admins (name, user_id) 
SELECT 'System Administrator', user_id FROM users WHERE username = 'admin';