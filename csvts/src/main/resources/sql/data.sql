-- Insert default admin user (password: admin123)
INSERT INTO users (username, password_hash, role) VALUES 
('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ADMIN');

INSERT INTO admins (user_id, name) 
SELECT user_id, 'System Administrator' FROM users WHERE username = 'admin';

-- Insert sample volunteers
INSERT INTO users (username, password_hash, role) VALUES 
('john.doe', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'VOLUNTEER'),
('jane.smith', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'VOLUNTEER'),
('mike.wilson', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'VOLUNTEER');

INSERT INTO volunteers (user_id, first_name, last_name, email, phone, skills, availability) VALUES
((SELECT user_id FROM users WHERE username = 'john.doe'), 'John', 'Doe', 'john.doe@email.com', '123-456-7890', 'Teaching,First Aid,Public Speaking', 'Weekends,Evenings'),
((SELECT user_id FROM users WHERE username = 'jane.smith'), 'Jane', 'Smith', 'jane.smith@email.com', '123-456-7891', 'Cooking,Childcare,Cleaning', 'Weekdays'),
((SELECT user_id FROM users WHERE username = 'mike.wilson'), 'Mike', 'Wilson', 'mike.wilson@email.com', '123-456-7892', 'Construction,Driving,Manual Labor', 'Weekends');

-- Insert sample tasks
INSERT INTO tasks (title, description, status, start_date, due_date, created_by) VALUES
('Community Cleanup', 'Help clean up the local park and surrounding areas', 'OPEN', '2025-01-15', '2025-01-20', 1),
('Food Distribution', 'Assist in packing and distributing food to families in need', 'IN_PROGRESS', '2025-01-10', '2025-01-25', 1),
('Tutoring Program', 'Provide tutoring services to underprivileged children', 'OPEN', '2025-02-01', '2025-03-31', 1);

-- Insert sample assignments
INSERT INTO assignments (task_id, volunteer_id, status) VALUES
(1, 1, 'ASSIGNED'),
(1, 2, 'ASSIGNED'),
(2, 3, 'IN_PROGRESS'),
(3, 1, 'ASSIGNED');

-- Insert sample time logs
INSERT INTO timelogs (volunteer_id, task_id, hours_logged, date_logged, status) VALUES
(1, 1, 4.5, '2025-01-15', 'APPROVED'),
(2, 1, 3.0, '2025-01-15', 'PENDING'),
(3, 2, 6.0, '2025-01-12', 'APPROVED');