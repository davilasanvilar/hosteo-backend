-- V3 Refactor Architecture - Corrected

-- Worker
ALTER TABLE workers DROP COLUMN state;

-- Task
ALTER TABLE tasks ADD COLUMN type VARCHAR(255);
ALTER TABLE tasks DROP COLUMN extra;


-- Template
ALTER TABLE templates ADD COLUMN type VARCHAR(255);

-- Event
CREATE TABLE events (
    id UUID PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    source VARCHAR(255),
    state VARCHAR(255) NOT NULL,
    apartment_id UUID REFERENCES apartments(id),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_event_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Critical: Used in checkApartmentAvailability and date range queries
CREATE INDEX idx_events_apartment_id ON events(apartment_id);
CREATE INDEX idx_events_start_date ON events(start_date);
CREATE INDEX idx_events_end_date ON events(end_date);
CREATE INDEX idx_events_state ON events(state);


-- Assignment
ALTER TABLE assignments ADD COLUMN event_id UUID REFERENCES events(id);

-- Drop old bookings table
DROP TABLE bookings;
