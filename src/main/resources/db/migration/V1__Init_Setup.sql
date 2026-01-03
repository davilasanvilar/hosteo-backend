-- ============================================
-- Hosteo Database Creation Script
-- ============================================

-- Drop existing tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS assignments CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS apartments CASCADE;
DROP TABLE IF EXISTS workers CASCADE;
DROP TABLE IF EXISTS templates CASCADE;
DROP TABLE IF EXISTS user_sessions CASCADE;
DROP TABLE IF EXISTS validation_codes CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    validated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Critical: Used for login/authentication
CREATE INDEX idx_users_username ON users(username);

-- ============================================
-- VALIDATION_CODES TABLE
-- ============================================
CREATE TABLE validation_codes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    code VARCHAR(6) NOT NULL,
    type VARCHAR(50) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_validation_codes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_validation_codes_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Critical: Foreign key lookup
CREATE INDEX idx_validation_codes_user_id ON validation_codes(user_id);

-- ============================================
-- USER_SESSIONS TABLE
-- ============================================
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_sessions_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Critical: Foreign key lookup
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);

-- ============================================
-- WORKERS TABLE
-- ============================================
CREATE TABLE workers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    language VARCHAR(50),
    state VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    salary DOUBLE PRECISION NOT NULL DEFAULT 0,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_workers_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Used for worker search/filtering
CREATE INDEX idx_workers_visible ON workers(visible);

-- ============================================
-- APARTMENTS TABLE
-- ============================================
CREATE TABLE apartments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    airbnb_id VARCHAR(255) UNIQUE,
    booking_id VARCHAR(255) UNIQUE,
    address TEXT,
    state VARCHAR(50) NOT NULL DEFAULT 'READY',
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_apartments_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Used for apartment filtering
CREATE INDEX idx_apartments_state ON apartments(state);
CREATE INDEX idx_apartments_visible ON apartments(visible);

-- ============================================
-- BOOKINGS TABLE
-- ============================================
CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    apartment_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    source VARCHAR(50) NOT NULL DEFAULT 'NONE',
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_bookings_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Critical: Used in checkApartmentAvailability and date range queries
CREATE INDEX idx_bookings_apartment_id ON bookings(apartment_id);
CREATE INDEX idx_bookings_start_date ON bookings(start_date);
CREATE INDEX idx_bookings_end_date ON bookings(end_date);
CREATE INDEX idx_bookings_state ON bookings(state);

-- ============================================
-- TASKS TABLE
-- ============================================
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    duration INTEGER NOT NULL DEFAULT 0,
    extra BOOLEAN NOT NULL DEFAULT FALSE,
    apartment_id UUID,
    steps TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_tasks_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Used for task filtering and apartment lookup
CREATE INDEX idx_tasks_apartment_id ON tasks(apartment_id);

-- ============================================
-- ASSIGNMENTS TABLE
-- ============================================
CREATE TABLE assignments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    worker_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    state VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_assignments_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignments_worker FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignments_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Critical: Used in FK lookups, date range queries, and state filtering
CREATE INDEX idx_assignments_task_id ON assignments(task_id);
CREATE INDEX idx_assignments_worker_id ON assignments(worker_id);
CREATE INDEX idx_assignments_start_date ON assignments(start_date);
CREATE INDEX idx_assignments_end_date ON assignments(end_date);
CREATE INDEX idx_assignments_state ON assignments(state);

-- ============================================
-- TEMPLATES TABLE
-- ============================================
CREATE TABLE templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    duration INTEGER NOT NULL DEFAULT 0,
    steps TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_templates_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);



