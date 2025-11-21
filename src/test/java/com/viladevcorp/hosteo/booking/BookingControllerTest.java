package com.viladevcorp.hosteo.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.forms.BookingCreateForm;
import com.viladevcorp.hosteo.model.forms.BookingSearchForm;
import com.viladevcorp.hosteo.model.forms.BookingUpdateForm;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.service.AuthService;
import com.viladevcorp.hosteo.utils.ApiResponse;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingControllerTest {

    private static final String ACTIVE_USER_EMAIL_1 = "test@gmail.com";
    private static final String ACTIVE_USER_USERNAME_1 = "test";
    private static final String ACTIVE_USER_PASSWORD_1 = "12test34";

    private static final String ACTIVE_USER_EMAIL_2 = "test2@gmail.com";
    private static final String ACTIVE_USER_USERNAME_2 = "test2";
    private static final String ACTIVE_USER_PASSWORD_2 = "12test34";

    private static final String PRE_CREATED_APARTMENT_NAME_1 = "Test Apartment 1";
    private static final String PRE_CREATED_APARTMENT_NAME_2 = "Test Apartment 2";

    private static final String PRE_CREATED_BOOKING_NAME_1 = "Test Booking 1";
    private static final String PRE_CREATED_BOOKING_START_DATE_1_STR = "2025-11-21T15:30:00";
    private static final String PRE_CREATED_BOOKING_END_DATE_1_STR = "2025-11-25T10:00:00";
    private static final BookingState PRE_CREATED_BOOKING_STATE_1 = BookingState.IN_PROGRESS;

    private static final String PRE_CREATED_BOOKING_NAME_2 = "Test Booking 2";
    private static final String PRE_CREATED_BOOKING_START_DATE_2_STR = "2025-12-01T14:00:00";
    private static final String PRE_CREATED_BOOKING_END_DATE_2_STR = "2025-12-05T11:00:00";
    private static final BookingState PRE_CREATED_BOOKING_STATE_2 = BookingState.PENDING;

    private static final String PRE_CREATED_BOOKING_NAME_3 = "Test Booking 3";
    private static final String PRE_CREATED_BOOKING_START_DATE_3_STR = "2026-01-10T16:00:00";
    private static final String PRE_CREATED_BOOKING_END_DATE_3_STR = "2026-01-15T09:00:00";
    private static final BookingState PRE_CREATED_BOOKING_STATE_3 = BookingState.PENDING;

    private static final String PRE_CREATED_BOOKING_NAME_4 = "Test Booking 4";
    private static final String PRE_CREATED_BOOKING_START_DATE_4_STR = "2026-02-20T13:00:00";
    private static final String PRE_CREATED_BOOKING_END_DATE_4_STR = "2026-02-25T12:00:00";
    private static final BookingState PRE_CREATED_BOOKING_STATE_4 = BookingState.CANCELLED;

    private static final String UPDATED_BOOKING_NAME = "Updated Test Booking";
    private static final double UPDATED_BOOKING_PRICE = 600.0;
    private static final BookingSource UPDATED_BOOKING_SOURCE = BookingSource.BOOKING;
    private static final BookingState UPDATED_BOOKING_STATE = BookingState.FINISHED;
    private static final boolean UPDATED_BOOKING_PAID = true;
    private static final String UPDATED_BOOKING_START_DATE_STR = "2025-11-21T16:30:00";
    private static final String UPDATED_BOOKING_END_DATE_STR = "2025-11-25T15:00:00";

    private static final String NEW_BOOKING_NAME = "New Booking";
    private static final String NEW_BOOKING_START_DATE_STR = "2026-03-01T14:00:00";
    private static final String NEW_BOOKING_END_DATE_STR = "2026-03-05T11:00:00";
    private static final BookingState NEW_BOOKING_STATE = BookingState.PENDING;
    private static final double NEW_BOOKING_PRICE = 700.0;
    private static final BookingSource NEW_BOOKING_SOURCE = BookingSource.NONE;
    private static final boolean NEW_BOOKING_PAID = false;

    private static final UUID NONEXISTENT_BOOKING_ID = UUID.randomUUID();

    private static UUID testApartmentId;
    private static UUID testBookingId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private UUID createTestBooking(String name, String startDateStr, String endDateStr, BookingState state,
            Apartment apartment, User creator) throws Exception {
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(sdf.parse(startDateStr));
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(sdf.parse(endDateStr));

        Booking booking = Booking.builder()
                .apartment(apartment)
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .price(300.0)
                .paid(false)
                .state(state)
                .createdBy(creator)
                .build();
        Booking createdBooking = bookingRepository.save(booking);
        return createdBooking.getId();
    }

    @BeforeAll
    void initialize() throws Exception {
        // Create test users
        User user1 = authService.registerUser(ACTIVE_USER_EMAIL_1, ACTIVE_USER_USERNAME_1, ACTIVE_USER_PASSWORD_1);
        user1.setValidated(true);
        user1 = userRepository.save(user1);

        User user2 = authService.registerUser(ACTIVE_USER_EMAIL_2, ACTIVE_USER_USERNAME_2, ACTIVE_USER_PASSWORD_2);
        user2.setValidated(true);
        user2 = userRepository.save(user2);

        // Create test apartment
        Apartment apartment = Apartment.builder()
                .name(PRE_CREATED_APARTMENT_NAME_1)
                .state(ApartmentState.READY)
                .visible(true)
                .createdBy(user1)
                .build();
        apartment = apartmentRepository.save(apartment);
        testApartmentId = apartment.getId();

        // Create test apartment owned by user2
        Apartment apartment2 = Apartment.builder()
                .name(PRE_CREATED_APARTMENT_NAME_2)
                .state(ApartmentState.READY)
                .visible(true)
                .createdBy(user2)
                .build();
        apartment2 = apartmentRepository.save(apartment2);

        testBookingId = createTestBooking(PRE_CREATED_BOOKING_NAME_1, PRE_CREATED_BOOKING_START_DATE_1_STR,
                PRE_CREATED_BOOKING_END_DATE_1_STR, PRE_CREATED_BOOKING_STATE_1, apartment, user1);
        createTestBooking(PRE_CREATED_BOOKING_NAME_2, PRE_CREATED_BOOKING_START_DATE_2_STR,
                PRE_CREATED_BOOKING_END_DATE_2_STR, PRE_CREATED_BOOKING_STATE_2, apartment, user1);
        createTestBooking(PRE_CREATED_BOOKING_NAME_3, PRE_CREATED_BOOKING_START_DATE_3_STR,
                PRE_CREATED_BOOKING_END_DATE_3_STR, PRE_CREATED_BOOKING_STATE_3, apartment2, user1);
        createTestBooking(PRE_CREATED_BOOKING_NAME_4, PRE_CREATED_BOOKING_START_DATE_4_STR,
                PRE_CREATED_BOOKING_END_DATE_4_STR, PRE_CREATED_BOOKING_STATE_4, apartment2, user1);

    }

    @AfterEach
    void clean() {
        bookingRepository.deleteAll(bookingRepository.findAll().stream()
                .filter(b -> !b.getName().contains("Test Booking"))
                .toList());
    }

    @AfterAll
    void globalClean() {
        bookingRepository.deleteAll();
        apartmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Create bookings")
    class CreateBookings {

        @Test
        @WithMockUser("test")
        void When_CreateBooking_Ok() throws Exception {

            Calendar startDate = Calendar.getInstance();
            startDate.setTime(sdf.parse(NEW_BOOKING_START_DATE_STR));
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(sdf.parse(NEW_BOOKING_END_DATE_STR));

            BookingCreateForm form = new BookingCreateForm();
            form.setApartmentId(testApartmentId);
            form.setName(NEW_BOOKING_NAME);
            form.setStartDate(startDate);
            form.setEndDate(endDate);
            form.setPrice(NEW_BOOKING_PRICE);
            form.setPaid(NEW_BOOKING_PAID);

            ObjectMapper obj = new ObjectMapper();
            String resultString = mockMvc.perform(post("/api/booking")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse().getContentAsString();

            TypeReference<ApiResponse<Booking>> typeReference = new TypeReference<ApiResponse<Booking>>() {
            };
            ApiResponse<Booking> result = obj.readValue(resultString, typeReference);
            Booking returnedBooking = result.getData();

            assertNotNull(returnedBooking);
            assertEquals(NEW_BOOKING_NAME, returnedBooking.getName());
            assertEquals(NEW_BOOKING_PRICE, returnedBooking.getPrice());
            assertEquals(NEW_BOOKING_PAID, returnedBooking.isPaid());
            assertEquals(NEW_BOOKING_STATE, returnedBooking.getState());
            assertEquals(NEW_BOOKING_SOURCE, returnedBooking.getSource());
            assertTrue(returnedBooking.getStartDate().getTime().compareTo(startDate.getTime()) == 0);
            assertTrue(returnedBooking.getEndDate().getTime().compareTo(endDate.getTime()) == 0);
        }

        @Test
        @WithMockUser("test")
        void When_CreateBookingMissingName_BadRequest() throws Exception {
            Calendar startDate = Calendar.getInstance();
            startDate.add(Calendar.DAY_OF_MONTH, 5);
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.DAY_OF_MONTH, 7);

            BookingCreateForm form = new BookingCreateForm();
            form.setApartmentId(testApartmentId);
            form.setStartDate(startDate);
            form.setEndDate(endDate);
            form.setPrice(300.0);
            form.setPaid(false);

            ObjectMapper obj = new ObjectMapper();
            mockMvc.perform(post("/api/booking")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update bookings")
    class UpdateBookings {

        @Test
        @WithMockUser("test")
        void When_UpdateBooking_Ok() throws Exception {
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(sdf.parse(UPDATED_BOOKING_START_DATE_STR));
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(sdf.parse(UPDATED_BOOKING_END_DATE_STR));

            BookingUpdateForm form = new BookingUpdateForm();
            form.setId(testBookingId);
            form.setName(UPDATED_BOOKING_NAME);
            form.setStartDate(startDate);
            form.setEndDate(endDate);
            form.setPrice(UPDATED_BOOKING_PRICE);
            form.setPaid(UPDATED_BOOKING_PAID);
            form.setState(UPDATED_BOOKING_STATE);
            form.setSource(UPDATED_BOOKING_SOURCE);

            ObjectMapper obj = new ObjectMapper();
            String resultString = mockMvc.perform(patch("/api/booking")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse().getContentAsString();

            TypeReference<ApiResponse<Booking>> typeReference = new TypeReference<ApiResponse<Booking>>() {
            };
            ApiResponse<Booking> result = obj.readValue(resultString, typeReference);
            Booking returnedBooking = result.getData();

            assertNotNull(returnedBooking);
            assertEquals(UPDATED_BOOKING_NAME, returnedBooking.getName());
            assertEquals(UPDATED_BOOKING_PRICE, returnedBooking.getPrice());
            assertEquals(UPDATED_BOOKING_PAID, returnedBooking.isPaid());
            assertEquals(UPDATED_BOOKING_STATE, returnedBooking.getState());
            assertEquals(UPDATED_BOOKING_SOURCE, returnedBooking.getSource());
            assertTrue(returnedBooking.getStartDate().getTime().compareTo(startDate.getTime()) == 0);
            assertTrue(returnedBooking.getEndDate().getTime().compareTo(endDate.getTime()) == 0);
        }

        @Test
        @WithMockUser("test2")
        void When_UpdateBookingNotOwned_Forbidden() throws Exception {
            Calendar startDate = Calendar.getInstance();
            startDate.add(Calendar.DAY_OF_MONTH, 10);
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.DAY_OF_MONTH, 12);

            BookingUpdateForm form = new BookingUpdateForm();
            form.setId(testBookingId);
            form.setName(UPDATED_BOOKING_NAME);
            form.setStartDate(startDate);
            form.setEndDate(endDate);
            form.setPrice(UPDATED_BOOKING_PRICE);
            form.setPaid(UPDATED_BOOKING_PAID);
            form.setState(UPDATED_BOOKING_STATE);
            form.setSource(UPDATED_BOOKING_SOURCE);

            ObjectMapper obj = new ObjectMapper();
            mockMvc.perform(patch("/api/booking")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get booking")
    class GetBooking {

        @Test
        @WithMockUser("test")
        void When_GetBooking_Ok() throws Exception {
            String resultString = mockMvc.perform(get("/api/booking/" + testBookingId.toString()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse().getContentAsString();

            ObjectMapper obj = new ObjectMapper();
            TypeReference<ApiResponse<Booking>> typeReference = new TypeReference<ApiResponse<Booking>>() {
            };
            ApiResponse<Booking> result = obj.readValue(resultString, typeReference);
            Booking returnedBooking = result.getData();

            assertNotNull(returnedBooking);
            assertEquals(PRE_CREATED_BOOKING_NAME_1, returnedBooking.getName());
        }

        @Test
        @WithMockUser("test2")
        void When_GetBookingNotOwned_Forbidden() throws Exception {
            mockMvc.perform(get("/api/booking/" + testBookingId.toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser("test")
        void When_GetBookingNonExistent_NotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            mockMvc.perform(get("/api/booking/" + nonExistentId.toString()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Search bookings")
    class SearchBookings {

        @Test
        @WithMockUser("test")
        void When_SearchAllBookings_Ok() throws Exception {
            ObjectMapper obj = new ObjectMapper();
            BookingSearchForm searchFormObj = new BookingSearchForm();
            searchFormObj.setPageSize(0);
            String resultString = mockMvc.perform(post("/api/bookings/search")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Booking>> result = null;
            TypeReference<ApiResponse<Page<Booking>>> typeReference = new TypeReference<ApiResponse<Page<Booking>>>() {
            };

            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Booking> returnedPage = result.getData();
            List<Booking> bookings = returnedPage.getContent();
            assertEquals(4, bookings.size());
        }

        @Test
        @WithMockUser("test")
        void When_SearchAllBookingsWithPagination_Ok() throws Exception {
            ObjectMapper obj = new ObjectMapper();
            BookingSearchForm searchFormObj = new BookingSearchForm();
            searchFormObj.setPageNumber(0);
            searchFormObj.setPageSize(2);
            String resultString = mockMvc.perform(post("/api/bookings/search")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Booking>> result = null;
            TypeReference<ApiResponse<Page<Booking>>> typeReference = new TypeReference<ApiResponse<Page<Booking>>>() {
            };

            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Booking> returnedPage = result.getData();
            List<Booking> bookings = returnedPage.getContent();
            assertEquals(2, bookings.size());
            assertEquals(2, returnedPage.getTotalPages());
            assertEquals(4, returnedPage.getTotalRows());
        }

        @Test
        @WithMockUser("test2")
        void When_SearchNoBookings_Ok() throws Exception {
            ObjectMapper obj = new ObjectMapper();
            BookingSearchForm searchFormObj = new BookingSearchForm();
            searchFormObj.setPageNumber(-1);
            String resultString = mockMvc.perform(post("/api/bookings/search")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Booking>> result = null;
            TypeReference<ApiResponse<Page<Booking>>> typeReference = new TypeReference<ApiResponse<Page<Booking>>>() {
            };

            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Booking> returnedPage = result.getData();
            List<Booking> bookings = returnedPage.getContent();
            assertEquals(0, bookings.size());
        }

        @Test
        @WithMockUser("test")
        void When_SearchBookingsByState_Ok() throws Exception {
            ObjectMapper obj = new ObjectMapper();
            // Search for READY apartments
            BookingSearchForm searchFormObj = new BookingSearchForm();
            searchFormObj.setState(BookingState.PENDING);
            searchFormObj.setPageSize(0);
            String resultString = mockMvc.perform(post("/api/bookings/search")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Booking>> result = null;
            TypeReference<ApiResponse<Page<Booking>>> typeReference = new TypeReference<ApiResponse<Page<Booking>>>() {
            };

            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Booking> returnedPage = result.getData();
            List<Booking> bookings = returnedPage.getContent();
            assertEquals(2, bookings.size());
            for (Booking booking : bookings) {
                assertEquals(BookingState.PENDING, booking.getState());
            }
        }

        @Test
        @WithMockUser("test")
        void When_SearchBookingsByApartmentName_Ok() throws Exception {
            ObjectMapper obj = new ObjectMapper();
            // Search for apartments with name containing "loft"
            BookingSearchForm searchFormObj = new BookingSearchForm();
            searchFormObj.setApartmentName("apartment 1");
            searchFormObj.setPageSize(0);
            String resultString = mockMvc.perform(post("/api/bookings/search")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Booking>> result = null;
            TypeReference<ApiResponse<Page<Booking>>> typeReference = new TypeReference<ApiResponse<Page<Booking>>>() {
            };

            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Booking> returnedPage = result.getData();
            List<Booking> bookings = returnedPage.getContent();
            assertEquals(2, bookings.size());
            for (Booking booking : bookings) {
                assertTrue(booking.getApartment().getName().toLowerCase().contains("apartment 1"));
            }
        }

        @Test
        @WithMockUser("test")
        void When_SearchBookingsByDateRange_Ok() throws Exception {
            ObjectMapper obj = new ObjectMapper();
            // Search for bookings within a date range
            BookingSearchForm searchFormObj = new BookingSearchForm();
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(sdf.parse("2025-11-20T00:00:00"));
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(sdf.parse("2025-12-02T00:00:00"));
            searchFormObj.setStartDate(startDate);
            searchFormObj.setEndDate(endDate);
            searchFormObj.setPageSize(0);
            String resultString = mockMvc.perform(post("/api/bookings/search")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Booking>> result = null;
            TypeReference<ApiResponse<Page<Booking>>> typeReference = new TypeReference<ApiResponse<Page<Booking>>>() {
            };

            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Booking> returnedPage = result.getData();
            List<Booking> bookings = returnedPage.getContent();
            assertEquals(2, bookings.size());
            for (Booking booking : bookings) {
                assertTrue(!booking.getStartDate().getTime().before(startDate.getTime())
                        && !booking.getStartDate().getTime().after(endDate.getTime()));
            }
        }
    }

    @Nested
    @DisplayName("Delete bookings")
    class DeleteBookings {
        private UUID forDeletionBookingId;

        @BeforeEach
        void setup() throws Exception {
            // Create a booking to be deleted
            Calendar startDate = Calendar.getInstance();
            startDate.add(Calendar.DAY_OF_MONTH, 5);
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.DAY_OF_MONTH, 10);

            Booking booking = Booking.builder()
                    .apartment(apartmentRepository.findById(testApartmentId).orElseThrow())
                    .name("Booking To Be Deleted")
                    .startDate(startDate)
                    .endDate(endDate)
                    .price(400.0)
                    .paid(false)
                    .state(BookingState.PENDING)
                    .createdBy(userRepository.findByUsername(ACTIVE_USER_USERNAME_1))
                    .build();
            booking = bookingRepository.save(booking);
            forDeletionBookingId = booking.getId();
        }

        @Test
        @WithMockUser("test")
        void When_DeleteBooking_Ok() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .delete("/api/booking/" + forDeletionBookingId.toString()))
                    .andExpect(status().isOk());
            boolean exists = bookingRepository.existsById(forDeletionBookingId);
            assertTrue(!exists, "Booking was not deleted");
        }

        @Test
        @WithMockUser("test2")
        void When_DeleteBookingNotOwned_Forbidden() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .delete("/api/booking/" + forDeletionBookingId.toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser("test")
        void When_DeleteBookingNotExist_NotFound() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .delete("/api/booking/" + NONEXISTENT_BOOKING_ID))
                    .andExpect(status().isNotFound());
        }
    }

}
