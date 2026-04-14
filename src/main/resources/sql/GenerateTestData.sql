-- =================================================================
-- TEST DATA GENERATION SCRIPT FOR HOSTE-O (POSTGRESQL)
-- =================================================================
-- This script uses a DO block to declare and use variables,
-- ensuring the entire script is self-contained and readable.
-- =================================================================

DO $$
DECLARE
    -- ---------------------------------
    -- CONFIGURATION VARIABLES
    -- ---------------------------------
    base_date date := '2026-04-02';
    user_id uuid := '06c7ab9a-f1da-4d5b-8d39-fc81fe3c0f0e';

    -- ---------------------------------
    -- ID DEFINITIONS
    -- ---------------------------------
   -- Apartments
      apt1_id uuid := 'a1a1a1a1-0001-4001-8001-000000000001';
      apt2_id uuid := 'a1a1a1a1-0002-4002-8002-000000000002';
      apt3_id uuid := 'a1a1a1a1-0003-4003-8003-000000000003';
      apt4_id uuid := 'a1a1a1a1-0004-4004-8004-000000000004';
      apt5_id uuid := 'a1a1a1a1-0005-4005-8005-000000000005';

      -- Tasks
      task1_1_id uuid := 'b1b1b1b1-0001-4001-8001-000000000001';
      task1_2_id uuid := 'b1b1b1b1-0001-4001-8001-000000000002';
      task1_3_id uuid := 'b1b1b1b1-0001-4001-8001-000000000003';
      task2_1_id uuid := 'b1b1b1b1-0002-4002-8002-000000000001';
      task2_2_id uuid := 'b1b1b1b1-0002-4002-8002-000000000002';
      task2_3_id uuid := 'b1b1b1b1-0002-4002-8002-000000000003';
      task3_1_id uuid := 'b1b1b1b1-0003-4003-8003-000000000001';
      task3_2_id uuid := 'b1b1b1b1-0003-4003-8003-000000000002';
      task3_3_id uuid := 'b1b1b1b1-0003-4003-8003-000000000003';
      task4_1_id uuid := 'b1b1b1b1-0004-4004-8004-000000000001';
      task4_2_id uuid := 'b1b1b1b1-0004-4004-8004-000000000002';
      task4_3_id uuid := 'b1b1b1b1-0004-4004-8004-000000000003';
      task5_1_id uuid := 'b1b1b1b1-0005-4005-8005-000000000001';
      task5_2_id uuid := 'b1b1b1b1-0005-4005-8005-000000000002';
      task5_3_id uuid := 'b1b1b1b1-0005-4005-8005-000000000003';

      -- Workers
      worker1_id uuid := 'c1c1c1c1-0001-4001-8001-000000000001';
      worker2_id uuid := 'c1c1c1c1-0002-4002-8002-000000000002';
      worker3_id uuid := 'c1c1c1c1-0003-4003-8003-000000000003';
      worker4_id uuid := 'c1c1c1c1-0004-4004-8004-000000000004';
      worker5_id uuid := 'c1c1c1c1-0005-4005-8005-000000000005';
      worker6_id uuid := 'c1c1c1c1-0006-4006-8006-000000000006';
      worker7_id uuid := 'c1c1c1c1-0007-4007-8007-000000000007';
      worker8_id uuid := 'c1c1c1c1-0008-4008-8008-000000000008';


BEGIN
    -- ---------------------------------
    -- DATA DELETION
    -- ---------------------------------
    DELETE FROM assignments;
    DELETE FROM imp_bookings;
    DELETE FROM templates;
    DELETE FROM tasks;
    DELETE FROM bookings;
    DELETE FROM workers;
    DELETE FROM apartments;

    -- ---------------------------------
    -- DATA CREATION
    -- ---------------------------------

    -- Create 5 apartments
INSERT INTO apartments (id, name, booking_id, airbnb_id, address, state, visible, created_by, created_at) VALUES
(apt1_id, 'Sunny Downtown Loft', 'bk-apt-001', 'air-apt-001', '{"street": "123 Main St", "city": "Metropolis", "country": "USA"}', 'READY', true, user_id, NOW()),
(apt2_id, 'Cozy Garden Flat', 'bk-apt-002', 'air-apt-002', '{"street": "456 Oak Ave", "city": "Star City", "country": "USA"}', 'READY', true, user_id, NOW()),
(apt3_id, 'Modern Lakeside Villa', 'bk-apt-003', 'air-apt-003', '{"street": "789 Pine Ln", "city": "Gotham", "country": "USA"}', 'READY', true, user_id, NOW()),
(apt4_id, 'Rustic Mountain Cabin', 'bk-apt-004', 'air-apt-004', '{"street": "101 Mountain Rd", "city": "Central City", "country": "USA"}', 'READY', true, user_id, NOW()),
(apt5_id, 'Chic Urban Studio', 'bk-apt-005', 'air-apt-005', '{"street": "212 River Walk", "city": "Coast City", "country": "USA"}', 'READY', true, user_id, NOW());

    -- Create tasks for each apartment
    INSERT INTO tasks (id, name, category, duration, extra, apartment_id, created_by, created_at) VALUES
    (task1_1_id, 'Standard Cleaning', 'CLEANING', 120, false, apt1_id, user_id, NOW()),
    (task1_2_id, 'Check faucets', 'MAINTENANCE', 45, false, apt1_id, user_id, NOW()),
    (task1_3_id, 'Restock Amenities', 'MAINTENANCE', 30, false, apt1_id, user_id, NOW()),
    (task2_1_id, 'Full Cleaning', 'CLEANING', 150, false, apt2_id, user_id, NOW()),
    (task2_2_id, 'Change Linens', 'CLEANING', 30, false, apt2_id, user_id, NOW()),
    (task2_3_id, 'Check Wi-Fi', 'MAINTENANCE', 20, false, apt2_id, user_id, NOW()),
    (task3_1_id, 'Post-Stay Cleaning', 'CLEANING', 120, false, apt3_id, user_id, NOW()),
    (task3_2_id, 'Garden Tidy-Up', 'MAINTENANCE', 90, false, apt3_id, user_id, NOW()),
    (task3_3_id, 'Guest Welcome Prep', 'MAINTENANCE', 45, false, apt3_id, user_id, NOW()),
    (task4_1_id, 'Cabin Cleaning', 'CLEANING', 180, false, apt4_id, user_id, NOW()),
    (task4_2_id, 'Firewood Restock', 'MAINTENANCE', 60, false, apt4_id, user_id, NOW()),
    (task4_3_id, 'Hot Tub Maintenance', 'MAINTENANCE', 75, false, apt4_id, user_id, NOW()),
    (task5_1_id, 'Studio Cleaning', 'CLEANING', 90, false, apt5_id, user_id, NOW()),
    (task5_2_id, 'Appliance Check', 'MAINTENANCE', 40, false, apt5_id, user_id, NOW()),
    (task5_3_id, 'Patio Cleaning', 'CLEANING', 60, false, apt5_id, user_id, NOW());

    -- Create 8 workers (2 inactive)
    INSERT INTO workers (id, name, state, visible, created_by, created_at) VALUES
    (worker1_id, 'John Doe', 'AVAILABLE', true, user_id, NOW()),
    (worker2_id, 'Jane Smith', 'AVAILABLE', true, user_id, NOW()),
    (worker3_id, 'Peter Jones', 'AVAILABLE', true, user_id, NOW()),
    (worker4_id, 'Mary Williams', 'AVAILABLE', true, user_id, NOW()),
    (worker5_id, 'David Brown', 'AVAILABLE', true, user_id, NOW()),
    (worker6_id, 'Susan Davis', 'AVAILABLE', true, user_id, NOW()),
    (worker7_id, 'Robert Miller', 'INACTIVE', false, user_id, NOW()),
    (worker8_id, 'Linda Wilson', 'INACTIVE', false, user_id, NOW());

    -- Create Bookings
    -- Apt 1 Bookings (State: IN_PROGRESS)
    INSERT INTO bookings (id, apartment_id, start_date, end_date, name, state, source, created_by, created_at) VALUES
    (gen_random_uuid(), apt1_id, base_date - INTERVAL '10 day', base_date - INTERVAL '7 day', 'Guest A', 'FINISHED', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt1_id, base_date - INTERVAL '6 day', base_date - INTERVAL '4 day', 'Guest B', 'FINISHED', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt1_id, base_date - INTERVAL '1 day', base_date + INTERVAL '2 day', 'Guest C (Current)', 'IN_PROGRESS', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt1_id, base_date + INTERVAL '3 day', base_date + INTERVAL '5 day', 'Guest D', 'PENDING', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt1_id, base_date + INTERVAL '6 day', base_date + INTERVAL '8 day', 'Guest E', 'PENDING', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt1_id, base_date + INTERVAL '10 day', base_date + INTERVAL '12 day', 'Guest F', 'PENDING', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt1_id, base_date + INTERVAL '14 day', base_date + INTERVAL '16 day', 'Guest G', 'PENDING', 'NONE', user_id, NOW());

    -- Apt 2 Bookings (State: USED)
    INSERT INTO bookings (id, apartment_id, start_date, end_date, name, state, source, created_by, created_at) VALUES
    (gen_random_uuid(), apt2_id, base_date - INTERVAL '12 day', base_date - INTERVAL '10 day', 'Guest H', 'FINISHED', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt2_id, base_date - INTERVAL '8 day', base_date - INTERVAL '6 day', 'Guest I', 'FINISHED', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt2_id, base_date - INTERVAL '5 day', base_date - INTERVAL '3 day', 'Guest J', 'FINISHED', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt2_id, base_date - INTERVAL '2 day', base_date, 'Guest K (Just Left)', 'FINISHED', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt2_id, base_date + INTERVAL '2 day', base_date + INTERVAL '4 day', 'Guest L', 'PENDING', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt2_id, base_date + INTERVAL '5 day', base_date + INTERVAL '8 day', 'Guest M', 'PENDING', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt2_id, base_date + INTERVAL '9 day', base_date + INTERVAL '11 day', 'Guest N', 'PENDING', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt2_id, base_date + INTERVAL '13 day', base_date + INTERVAL '15 day', 'Guest O', 'PENDING', 'BOOKING', user_id, NOW());

    -- Apt 3 Bookings (State: IN_PROGRESS)
    INSERT INTO bookings (id, apartment_id, start_date, end_date, name, state, source, created_by, created_at) VALUES
    (gen_random_uuid(), apt3_id, base_date - INTERVAL '9 day', base_date - INTERVAL '7 day', 'Guest P', 'FINISHED', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt3_id, base_date - INTERVAL '5 day', base_date - INTERVAL '3 day', 'Guest Q', 'FINISHED', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt3_id, base_date - INTERVAL '2 day', base_date + INTERVAL '3 day', 'Guest R (Current)', 'IN_PROGRESS', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt3_id, base_date + INTERVAL '4 day', base_date + INTERVAL '6 day', 'Guest S', 'PENDING', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt3_id, base_date + INTERVAL '8 day', base_date + INTERVAL '10 day', 'Guest T', 'PENDING', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt3_id, base_date + INTERVAL '12 day', base_date + INTERVAL '14 day', 'Guest U', 'PENDING', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt3_id, base_date + INTERVAL '15 day', base_date + INTERVAL '18 day', 'Guest V', 'PENDING', 'BOOKING', user_id, NOW());

    -- Apt 4 Bookings (State: READY)
    INSERT INTO bookings (id, apartment_id, start_date, end_date, name, state, source, created_by, created_at) VALUES
    (gen_random_uuid(), apt4_id, base_date - INTERVAL '15 day', base_date - INTERVAL '12 day', 'Guest W', 'FINISHED', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt4_id, base_date - INTERVAL '10 day', base_date - INTERVAL '8 day', 'Guest X', 'FINISHED', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt4_id, base_date - INTERVAL '5 day', base_date - INTERVAL '3 day', 'Guest Y', 'FINISHED', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt4_id, base_date + INTERVAL '3 day', base_date + INTERVAL '5 day', 'Guest Z', 'PENDING', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt4_id, base_date + INTERVAL '7 day', base_date + INTERVAL '9 day', 'Guest AA', 'PENDING', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt4_id, base_date + INTERVAL '11 day', base_date + INTERVAL '13 day', 'Guest BB', 'PENDING', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt4_id, base_date + INTERVAL '15 day', base_date + INTERVAL '17 day', 'Guest CC', 'PENDING', 'NONE', user_id, NOW());

    -- Apt 5 Bookings (State: USED)
    INSERT INTO bookings (id, apartment_id, start_date, end_date, name, state, source, created_by, created_at) VALUES
    (gen_random_uuid(), apt5_id, base_date - INTERVAL '11 day', base_date - INTERVAL '9 day', 'Guest DD', 'FINISHED', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt5_id, base_date - INTERVAL '7 day', base_date - INTERVAL '5 day', 'Guest EE', 'FINISHED', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt5_id, base_date - INTERVAL '4 day', base_date - INTERVAL '1 day', 'Guest FF (Just Left)', 'FINISHED', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt5_id, base_date + INTERVAL '4 day', base_date + INTERVAL '6 day', 'Guest GG', 'PENDING', 'BOOKING', user_id, NOW()),
    (gen_random_uuid(), apt5_id, base_date + INTERVAL '8 day', base_date + INTERVAL '10 day', 'Guest HH', 'PENDING', 'NONE', user_id, NOW()),
    (gen_random_uuid(), apt5_id, base_date + INTERVAL '12 day', base_date + INTERVAL '15 day', 'Guest II', 'PENDING', 'AIRBNB', user_id, NOW()),
    (gen_random_uuid(), apt5_id, base_date + INTERVAL '16 day', base_date + INTERVAL '18 day', 'Guest JJ', 'PENDING', 'NONE', user_id, NOW());

    -- Update Apartment States based on the created bookings
    UPDATE apartments SET state = 'OCCUPIED' WHERE id = apt1_id;
    UPDATE apartments SET state = 'USED' WHERE id = apt2_id;
    UPDATE apartments SET state = 'OCCUPIED' WHERE id = apt3_id;
    UPDATE apartments SET state = 'READY' WHERE id = apt4_id;
    UPDATE apartments SET state = 'USED' WHERE id = apt5_id;

    -- Create FINISHED assignments for past bookings
    -- APT 1
    INSERT INTO assignments (id, task_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task1_1_id, base_date - INTERVAL '7 day', base_date - INTERVAL '7 day' + INTERVAL '120 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_2_id, base_date - INTERVAL '7 day' + INTERVAL '120 minute', base_date - INTERVAL '7 day' + INTERVAL '120 minute' + INTERVAL '45 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_3_id, base_date - INTERVAL '7 day' + INTERVAL '120 minute' + INTERVAL '45 minute', base_date - INTERVAL '7 day' + INTERVAL '120 minute' + INTERVAL '45 minute' + INTERVAL '30 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_1_id, base_date - INTERVAL '3 day', base_date - INTERVAL '3 day' + INTERVAL '120 minute', worker4_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_2_id, base_date - INTERVAL '3 day' + INTERVAL '120 minute', base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '45 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_3_id, base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '45 minute', base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '45 minute' + INTERVAL '30 minute', worker4_id, 'FINISHED', user_id, NOW());

    -- APT 2
    INSERT INTO assignments (id, task_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task2_1_id, base_date - INTERVAL '9 day', base_date - INTERVAL '9 day' + INTERVAL '150 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_2_id, base_date - INTERVAL '9 day' + INTERVAL '150 minute', base_date - INTERVAL '9 day' + INTERVAL '150 minute' + INTERVAL '30 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_3_id, base_date - INTERVAL '9 day' + INTERVAL '150 minute' + INTERVAL '30 minute', base_date - INTERVAL '9 day' + INTERVAL '150 minute' + INTERVAL '30 minute' + INTERVAL '20 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_1_id, base_date - INTERVAL '6 day', base_date - INTERVAL '6 day' + INTERVAL '150 minute', worker4_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_2_id, base_date - INTERVAL '6 day' + INTERVAL '150 minute', base_date - INTERVAL '6 day' + INTERVAL '150 minute' + INTERVAL '30 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_3_id, base_date - INTERVAL '6 day' + INTERVAL '150 minute' + INTERVAL '30 minute', base_date - INTERVAL '6 day' + INTERVAL '150 minute' + INTERVAL '30 minute' + INTERVAL '20 minute', worker4_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_1_id, base_date - INTERVAL '3 day', base_date - INTERVAL '3 day' + INTERVAL '150 minute', worker6_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_2_id, base_date - INTERVAL '3 day' + INTERVAL '150 minute', base_date - INTERVAL '3 day' + INTERVAL '150 minute' + INTERVAL '30 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_3_id, base_date - INTERVAL '3 day' + INTERVAL '150 minute' + INTERVAL '30 minute', base_date - INTERVAL '3 day' + INTERVAL '150 minute' + INTERVAL '30 minute' + INTERVAL '20 minute', worker6_id, 'FINISHED', user_id, NOW());

    -- APT 3
    INSERT INTO assignments (id, task_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task3_1_id, base_date - INTERVAL '6 day', base_date - INTERVAL '6 day' + INTERVAL '120 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_2_id, base_date - INTERVAL '6 day' + INTERVAL '120 minute', base_date - INTERVAL '6 day' + INTERVAL '120 minute' + INTERVAL '90 minute', worker3_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_3_id, base_date - INTERVAL '6 day' + INTERVAL '120 minute' + INTERVAL '90 minute', base_date - INTERVAL '6 day' + INTERVAL '120 minute' + INTERVAL '90 minute' + INTERVAL '45 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_1_id, base_date - INTERVAL '3 day', base_date - INTERVAL '3 day' + INTERVAL '120 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_2_id, base_date - INTERVAL '3 day' + INTERVAL '120 minute', base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '90 minute', worker6_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_3_id, base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '90 minute', base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '90 minute' + INTERVAL '45 minute', worker5_id, 'FINISHED', user_id, NOW());

    -- APT 4
    INSERT INTO assignments (id, task_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task4_1_id, base_date - INTERVAL '11 day', base_date - INTERVAL '11 day' + INTERVAL '180 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_2_id, base_date - INTERVAL '11 day' + INTERVAL '180 minute', base_date - INTERVAL '11 day' + INTERVAL '180 minute' + INTERVAL '60 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_3_id, base_date - INTERVAL '11 day' + INTERVAL '180 minute' + INTERVAL '60 minute', base_date - INTERVAL '11 day' + INTERVAL '180 minute' + INTERVAL '60 minute' + INTERVAL '75 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_1_id, base_date - INTERVAL '7 day', base_date - INTERVAL '7 day' + INTERVAL '180 minute', worker3_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_2_id, base_date - INTERVAL '7 day' + INTERVAL '180 minute', base_date - INTERVAL '7 day' + INTERVAL '180 minute' + INTERVAL '60 minute', worker4_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_3_id, base_date - INTERVAL '7 day' + INTERVAL '180 minute' + INTERVAL '60 minute', base_date - INTERVAL '7 day' + INTERVAL '180 minute' + INTERVAL '60 minute' + INTERVAL '75 minute', worker3_id, 'FINISHED', user_id, NOW());

    -- APT 5
    INSERT INTO assignments (id, task_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task5_1_id, base_date - INTERVAL '8 day', base_date - INTERVAL '8 day' + INTERVAL '90 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_2_id, base_date - INTERVAL '8 day' + INTERVAL '90 minute', base_date - INTERVAL '8 day' + INTERVAL '90 minute' + INTERVAL '40 minute', worker6_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_3_id, base_date - INTERVAL '8 day' + INTERVAL '90 minute' + INTERVAL '40 minute', base_date - INTERVAL '8 day' + INTERVAL '90 minute' + INTERVAL '40 minute' + INTERVAL '60 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_1_id, base_date - INTERVAL '5 day', base_date - INTERVAL '5 day' + INTERVAL '90 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_2_id, base_date - INTERVAL '5 day' + INTERVAL '90 minute', base_date - INTERVAL '5 day' + INTERVAL '90 minute' + INTERVAL '40 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_3_id, base_date - INTERVAL '5 day' + INTERVAL '90 minute' + INTERVAL '40 minute', base_date - INTERVAL '5 day' + INTERVAL '90 minute' + INTERVAL '40 minute' + INTERVAL '60 minute', worker1_id, 'FINISHED', user_id, NOW());


    -- Create assignments to justify the CURRENT state of the system
    -- Apt 2 (USED): Create PENDING assignments for all its tasks for today.
    INSERT INTO assignments (id, task_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task2_1_id, base_date, base_date + INTERVAL '150 minute', worker1_id, 'PENDING', user_id, NOW()),
    (gen_random_uuid(), task2_2_id, base_date + INTERVAL '150 minute', base_date + INTERVAL '150 minute' + INTERVAL '30 minute', worker2_id, 'PENDING', user_id, NOW()),
    (gen_random_uuid(), task2_3_id, base_date + INTERVAL '150 minute' + INTERVAL '30 minute', base_date + INTERVAL '150 minute' + INTERVAL '30 minute' + INTERVAL '20 minute', worker1_id, 'PENDING', user_id, NOW());

    -- Apt 4 (READY): Create FINISHED assignments for all tasks for yesterday.
    INSERT INTO assignments (id, task_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task4_1_id, base_date - INTERVAL '1 day', base_date - INTERVAL '1 day' + INTERVAL '180 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_2_id, base_date - INTERVAL '1 day' + INTERVAL '180 minute', base_date - INTERVAL '1 day' + INTERVAL '180 minute' + INTERVAL '60 minute', worker3_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_3_id, base_date - INTERVAL '1 day' + INTERVAL '180 minute' + INTERVAL '60 minute', base_date - INTERVAL '1 day' + INTERVAL '180 minute' + INTERVAL '60 minute' + INTERVAL '75 minute', worker2_id, 'FINISHED', user_id, NOW());

    -- Apt 5 (USED):  PENDING assignments.
    INSERT INTO assignments (id, task_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task5_1_id, base_date, base_date + INTERVAL '90 minute', worker3_id, 'PENDING', user_id, NOW()),
    (gen_random_uuid(), task5_2_id, base_date + INTERVAL '90 minute', base_date + INTERVAL '90 minute' + INTERVAL '40 minute', worker4_id, 'PENDING', user_id, NOW()),
    (gen_random_uuid(), task5_3_id, base_date + INTERVAL '90 minute' + INTERVAL '40 minute', base_date + INTERVAL '90 minute' + INTERVAL '40 minute' + INTERVAL '60 minute', worker4_id, 'PENDING', user_id, NOW());

END $$;
