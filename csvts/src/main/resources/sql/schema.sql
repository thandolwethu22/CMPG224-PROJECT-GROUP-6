-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('VOLUNTEER', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Volunteers table
CREATE TABLE IF NOT EXISTS volunteers (
    volunteer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    skills TEXT,
    availability TEXT,
    user_id INTEGER UNIQUE REFERENCES users(user_id) ON DELETE CASCADE
);

-- Admins table
CREATE TABLE IF NOT EXISTS admins (
    admin_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    user_id INTEGER UNIQUE REFERENCES users(user_id) ON DELETE CASCADE
);