-- ============================================
-- Hosteo Database Creation Script
-- ============================================

-- Drop existing tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS IMP_BOOKING CASCADE;

-- ============================================
-- BOOKINGS TABLE
-- ============================================
CREATE TABLE imp_bookings (
    id UUID PRIMARY KEY,
    apartment_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL,
    source VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    conflict TEXT,
    created_by UUID,
    CONSTRAINT fk_bookings_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Critical: Used in checkApartmentAvailability and date range queries
    CREATE INDEX idx_imp_bookings_created_by ON bookings(created_by);


