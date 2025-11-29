package com.viladevcorp.hosteo.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.repository.WorkerRepository;
import com.viladevcorp.hosteo.service.AuthService;

import lombok.Getter;
import lombok.Setter;

import static com.viladevcorp.hosteo.common.TestConstants.*;

@Component
@Getter
@Setter
public class TestSetupHelper {

        @Autowired
        AuthService authService;

        @Autowired
        WorkerRepository workerRepository;

        @Autowired
        UserRepository userRepository;

        @Autowired
        ApartmentRepository apartmentRepository;

        @Autowired
        BookingRepository bookingRepository;

        private List<User> testUsers;

        private List<Apartment> testApartments;

        private List<Booking> testBookings;

        private List<Worker> testWorkers;

        public void createTestUsers()
                        throws Exception {
                TestUtils.injectUserSession(null, userRepository);
                User us1 = authService.registerUser(ACTIVE_USER_EMAIL_1, ACTIVE_USER_USERNAME_1,
                                ACTIVE_USER_PASSWORD_1);
                us1.setValidated(true);
                us1 = userRepository.save(us1);
                User us2 = authService.registerUser(ACTIVE_USER_EMAIL_2, ACTIVE_USER_USERNAME_2,
                                ACTIVE_USER_PASSWORD_2);
                us2.setValidated(true);
                us2 = userRepository.save(us2);
                testUsers = List.of(us1, us2);
        }

        public void resetTestUsers() throws Exception {
                bookingRepository.deleteAll();
                apartmentRepository.deleteAll();
                workerRepository.deleteAll();
                userRepository.deleteAll();
                createTestUsers();
        }

        public void createTestApartments() {
                TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

                Apartment apt1 = apartmentRepository.save(Apartment.builder().name(CREATED_APARTMENT_NAME_1)
                                .state(CREATE_APARTMENT_STATE_1).build());

                Apartment apt2 = apartmentRepository.save(Apartment.builder().name(CREATED_APARTMENT_NAME_2)
                                .state(CREATE_APARTMENT_STATE_2).build());

                Apartment apt3 = apartmentRepository.save(Apartment.builder().name(CREATED_APARTMENT_NAME_3)
                                .state(CREATE_APARTMENT_STATE_3).build());

                Apartment apt4 = apartmentRepository.save(Apartment.builder().name(CREATED_APARTMENT_NAME_4)
                                .state(CREATE_APARTMENT_STATE_4).build());

                testApartments = List.of(apt1, apt2, apt3, apt4);
        }

        public void resetTestApartments() {
                apartmentRepository.deleteAll();
                createTestApartments();
        }

        public void createTestWorkers() {
                TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

                Worker wk1 = workerRepository
                                .save(Worker.builder().name(CREATED_WORKER_NAME_1).language(CREATED_WORKER_LANGUAGE_1)
                                                .build());

                Worker wk2 = workerRepository
                                .save(Worker.builder().name(CREATED_WORKER_NAME_2).language(CREATED_WORKER_LANGUAGE_2)
                                                .build());

                Worker wk3 = workerRepository
                                .save(Worker.builder().name(CREATED_WORKER_NAME_3).language(CREATED_WORKER_LANGUAGE_3)
                                                .build());

                Worker wk4 = workerRepository
                                .save(Worker.builder().name(CREATED_WORKER_NAME_4).language(CREATED_WORKER_LANGUAGE_4)
                                                .build());

                testWorkers = List.of(wk1, wk2, wk3, wk4);
        }

        public void resetTestWorkers() {
                workerRepository.deleteAll();
                createTestWorkers();
        }


        public void createTestBookings() throws Exception {

                createTestApartments();

                Booking bk1 = bookingRepository.save(Booking.builder()
                                .apartment(testApartments.get(0))
                                .name(CREATED_BOOKING_NAME_1)
                                .startDate(TestUtils.dateStrToCalendar(CREATED_BOOKING_START_DATE_1))
                                .endDate(TestUtils.dateStrToCalendar(CREATED_BOOKING_END_DATE_1))
                                .price(CREATED_BOOKING_PRICE_1)
                                .paid(false)
                                .state(CREATED_BOOKING_STATE_1)
                                .build());

                Booking bk2 = bookingRepository.save(Booking.builder()
                                .apartment(testApartments.get(1))
                                .name(CREATED_BOOKING_NAME_2)
                                .startDate(TestUtils.dateStrToCalendar(CREATED_BOOKING_START_DATE_2))
                                .endDate(TestUtils.dateStrToCalendar(CREATED_BOOKING_END_DATE_2))
                                .price(CREATED_BOOKING_PRICE_2)
                                .paid(false)
                                .state(CREATED_BOOKING_STATE_2)
                                .build());

                Booking bk3 = bookingRepository.save(Booking.builder()
                                .apartment(testApartments.get(2))
                                .name(CREATED_BOOKING_NAME_3)
                                .startDate(TestUtils.dateStrToCalendar(CREATED_BOOKING_START_DATE_3))
                                .endDate(TestUtils.dateStrToCalendar(CREATED_BOOKING_END_DATE_3))
                                .price(CREATED_BOOKING_PRICE_3)
                                .paid(false)
                                .state(CREATED_BOOKING_STATE_3)
                                .build());

                Booking bk4 = bookingRepository.save(Booking.builder()
                                .apartment(testApartments.get(3))
                                .name(CREATED_BOOKING_NAME_4)
                                .startDate(TestUtils.dateStrToCalendar(CREATED_BOOKING_START_DATE_4))
                                .endDate(TestUtils.dateStrToCalendar(CREATED_BOOKING_END_DATE_4))
                                .price(CREATED_BOOKING_PRICE_4)
                                .paid(false)
                                .state(CREATED_BOOKING_STATE_4)
                                .build());

                testBookings = List.of(bk1, bk2, bk3, bk4);
        }

        public void resetTestBookings() throws Exception {
                bookingRepository.deleteAll();
                apartmentRepository.deleteAll();
                createTestBookings();
        }

}
