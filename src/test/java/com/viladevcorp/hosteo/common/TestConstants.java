package com.viladevcorp.hosteo.common;

import com.viladevcorp.hosteo.model.Address;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.model.types.Language;

public class TestConstants {

        // Users
        public static final String ACTIVE_USER_EMAIL_1 = "test@gmail.com";
        public static final String ACTIVE_USER_USERNAME_1 = "test";
        public static final String ACTIVE_USER_PASSWORD_1 = "12test34";

        public static final String ACTIVE_USER_EMAIL_2 = "test2@gmail.com";
        public static final String ACTIVE_USER_USERNAME_2 = "test2";
        public static final String ACTIVE_USER_PASSWORD_2 = "12test34";

        public static final String NEW_USER_EMAIL = "test3@gmail.com";
        public static final String NEW_USER_USERNAME = "test3";
        public static final String NEW_USER_PASSWORD = "1234test";

        // Apartments
        public static final String CREATED_APARTMENT_NAME_1 = "Created apartment";
        public static final ApartmentState CREATE_APARTMENT_STATE_1 = ApartmentState.READY;

        public static final String CREATED_APARTMENT_NAME_2 = "Created loft 2";
        public static final ApartmentState CREATE_APARTMENT_STATE_2 = ApartmentState.READY;

        public static final String CREATED_APARTMENT_NAME_3 = "Created loft 3";
        public static final ApartmentState CREATE_APARTMENT_STATE_3 = ApartmentState.READY;

        public static final String CREATED_APARTMENT_NAME_4 = "Created apartment 4";
        public static final ApartmentState CREATE_APARTMENT_STATE_4 = ApartmentState.OCCUPIED;

        public static final String NEW_APARTMENT_NAME_1 = "My Apartment 1";
        public static final String NEW_APARTMENT_AIRBNB_ID_1 = "airbnb-1";
        public static final String NEW_APARTMENT_BOOKING_ID_1 = "booking-1";
        public static final boolean NEW_APARTMENT_VISIBLE_1 = true;
        public static final Address NEW_APARTMENT_ADDRESS_1 = Address.builder()
                        .street("123 Main St")
                        .number("25")
                        .apartmentNumber("Apt 5")
                        .city("Sample City")
                        .country("Sample Country")
                        .zipCode("12345")
                        .build();

        public static final String UPDATED_APARTMENT_NAME = "Updated apartment name";
        public static final String UPDATED_APARTMENT_AIRBNB_ID = "updated-airbnb-id";
        public static final String UPDATED_APARTMENT_BOOKING_ID = "updated-booking-id";
        public static final boolean UPDATED_APARTMENT_VISIBLE = false;
        public static final ApartmentState UPDATED_APARTMENT_STATE = ApartmentState.OCCUPIED;
        public static final Address UPDATED_APARTMENT_ADDRESS = Address.builder()
                        .street("Updated Street")
                        .number("99B")
                        .apartmentNumber("Apt 10")
                        .city("Updated City")
                        .country("Updated Country")
                        .zipCode("99999")
                        .build();

        // Workers
        public static final String CREATED_WORKER_NAME_1 = "John 1";
        public static final Language CREATED_WORKER_LANGUAGE_1 = Language.EN;

        public static final String CREATED_WORKER_NAME_2 = "John 2";
        public static final Language CREATED_WORKER_LANGUAGE_2 = Language.UK;

        public static final String CREATED_WORKER_NAME_3 = "Peter 3";
        public static final Language CREATED_WORKER_LANGUAGE_3 = Language.FR;

        public static final String CREATED_WORKER_NAME_4 = "Peter 4";
        public static final Language CREATED_WORKER_LANGUAGE_4 = Language.IT;

        public static final String NEW_WORKER_NAME_1 = "Created worker";
        public static final Language NEW_WORKER_LANGUAGE_1 = Language.UK;
        public static final boolean NEW_WORKER_VISIBLE_1 = true;

        public static final String UPDATED_WORKER_NAME = "Updated worker name";
        public static final Language UPDATED_WORKER_LANGUAGE = Language.FR;
        public static final boolean UPDATED_WORKER_VISIBLE = false;

        // Bookings
        public static final String CREATED_BOOKING_NAME_1 = "Test Booking 1";
        public static final String CREATED_BOOKING_START_DATE_1 = "2025-11-21 15:30:00";
        public static final String CREATED_BOOKING_END_DATE_1 = "2025-11-25 10:00:00";
        public static final BookingState CREATED_BOOKING_STATE_1 = BookingState.IN_PROGRESS;
        public static final double CREATED_BOOKING_PRICE_1 = 300.0;

        public static final String CREATED_BOOKING_NAME_2 = "Test Booking 2";
        public static final String CREATED_BOOKING_START_DATE_2 = "2025-12-01 14:00:00";
        public static final String CREATED_BOOKING_END_DATE_2 = "2025-12-05 11:00:00";
        public static final BookingState CREATED_BOOKING_STATE_2 = BookingState.PENDING;
        public static final double CREATED_BOOKING_PRICE_2 = 400.0;

        public static final String CREATED_BOOKING_NAME_3 = "Test Booking 3";
        public static final String CREATED_BOOKING_START_DATE_3 = "2026-01-10 16:00:00";
        public static final String CREATED_BOOKING_END_DATE_3 = "2026-01-15 09:00:00";
        public static final BookingState CREATED_BOOKING_STATE_3 = BookingState.PENDING;
        public static final double CREATED_BOOKING_PRICE_3 = 500.0;

        public static final String CREATED_BOOKING_NAME_4 = "Test Booking 4";
        public static final String CREATED_BOOKING_START_DATE_4 = "2026-02-20 13:00:00";
        public static final String CREATED_BOOKING_END_DATE_4 = "2026-02-25 12:00:00";
        public static final BookingState CREATED_BOOKING_STATE_4 = BookingState.CANCELLED;
        public static final double CREATED_BOOKING_PRICE_4 = 600.0;

        public static final String NEW_BOOKING_NAME = "New Booking";
        public static final String NEW_BOOKING_START_DATE = "2026-03-01 14:00:00";
        public static final String NEW_BOOKING_END_DATE = "2026-03-05 11:00:00";
        public static final BookingState NEW_BOOKING_STATE = BookingState.PENDING;
        public static final double NEW_BOOKING_PRICE = 700.0;
        public static final BookingSource NEW_BOOKING_SOURCE = BookingSource.NONE;
        public static final boolean NEW_BOOKING_PAID = false;

        public static final String UPDATED_BOOKING_NAME = "Updated Test Booking";
        public static final double UPDATED_BOOKING_PRICE = 600.0;
        public static final BookingSource UPDATED_BOOKING_SOURCE = BookingSource.BOOKING;
        public static final BookingState UPDATED_BOOKING_STATE = BookingState.FINISHED;
        public static final boolean UPDATED_BOOKING_PAID = true;
        public static final String UPDATED_BOOKING_START_DATE = "2025-11-21 16:30:00";
        public static final String UPDATED_BOOKING_END_DATE = "2025-11-25 15:00:00";

}
