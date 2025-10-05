-- Users table (base table for all users)
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('VOLUNTEER', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Volunteers table (extends users)
CREATE TABLE IF NOT EXISTS volunteers (
    volunteer_id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    skills TEXT,
    availability TEXT
);

-- Admins table (extends users)
CREATE TABLE IF NOT EXISTS admins (
    admin_id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL
);

-- Tasks table
CREATE TABLE IF NOT EXISTS tasks (
    task_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    start_date DATE,
    due_date DATE,
    created_by INTEGER NOT NULL REFERENCES admins(admin_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Assignments table (junction between volunteers and tasks)
CREATE TABLE IF NOT EXISTS assignments (
    assignment_id SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL REFERENCES tasks(task_id) ON DELETE CASCADE,
    volunteer_id INTEGER NOT NULL REFERENCES volunteers(volunteer_id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'ASSIGNED' CHECK (status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED')),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(task_id, volunteer_id)
);

-- Time logs table
CREATE TABLE IF NOT EXISTS timelogs (
    log_id SERIAL PRIMARY KEY,
    volunteer_id INTEGER NOT NULL REFERENCES volunteers(volunteer_id) ON DELETE CASCADE,
    task_id INTEGER NOT NULL REFERENCES tasks(task_id) ON DELETE CASCADE,
    hours_logged DECIMAL(5,2) NOT NULL CHECK (hours_logged > 0),
    date_logged DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    approved_by INTEGER REFERENCES admins(admin_id) ON DELETE SET NULL,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reports table
CREATE TABLE IF NOT EXISTS reports (
    report_id SERIAL PRIMARY KEY,
    report_type VARCHAR(50) NOT NULL,
    generated_by INTEGER NOT NULL REFERENCES admins(admin_id) ON DELETE CASCADE,
    generated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content TEXT
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_volunteers_user_id ON volunteers(user_id);
CREATE INDEX IF NOT EXISTS idx_admins_user_id ON admins(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_created_by ON tasks(created_by);
CREATE INDEX IF NOT EXISTS idx_assignments_task_id ON assignments(task_id);
CREATE INDEX IF NOT EXISTS idx_assignments_volunteer_id ON assignments(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_timelogs_volunteer_id ON timelogs(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_timelogs_task_id ON timelogs(task_id);
CREATE INDEX IF NOT EXISTS idx_timelogs_status ON timelogs(status);