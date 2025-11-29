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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestSetupHelper;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.forms.BookingCreateForm;
import com.viladevcorp.hosteo.model.forms.BookingSearchForm;
import com.viladevcorp.hosteo.model.forms.BookingUpdateForm;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;
import static com.viladevcorp.hosteo.common.TestConstants.*;

class BookingControllerTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    TestSetupHelper testSetupHelper;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeAll
    void initialize() throws Exception {
        testSetupHelper.resetTestBookings();
    }

    @AfterEach
    void clean() {
        bookingRepository.deleteAll(bookingRepository.findAll().stream()
                .filter(b -> !b.getName().contains("Test Booking"))
                .toList());
    }

    @AfterAll
    void cleanTestData() {
        bookingRepository.deleteAll();
        apartmentRepository.deleteAll();
    }

    @Nested
    @DisplayName("Create bookings")
    class CreateBookings {

        @Test
        void When_CreateBooking_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            Calendar startDate = TestUtils.dateStrToCalendar(NEW_BOOKING_START_DATE);
            Calendar endDate = TestUtils.dateStrToCalendar(NEW_BOOKING_END_DATE);
            BookingCreateForm form = new BookingCreateForm();
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
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
        void When_CreateBookingMissingName_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            Calendar startDate = Calendar.getInstance();
            startDate.add(Calendar.DAY_OF_MONTH, 5);
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.DAY_OF_MONTH, 7);

            BookingCreateForm form = new BookingCreateForm();
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
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
        void When_UpdateBooking_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            Calendar startDate = TestUtils.dateStrToCalendar(UPDATED_BOOKING_START_DATE);
            Calendar endDate = TestUtils.dateStrToCalendar(UPDATED_BOOKING_END_DATE);
            BookingUpdateForm form = new BookingUpdateForm();
            form.setId(testSetupHelper.getTestBookings().get(0).getId());
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
        void When_UpdateBookingNotOwned_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            Calendar startDate = Calendar.getInstance();
            startDate.add(Calendar.DAY_OF_MONTH, 10);
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.DAY_OF_MONTH, 12);

            BookingUpdateForm form = new BookingUpdateForm();
            form.setId(testSetupHelper.getTestBookings().get(0).getId());
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
        void When_GetBooking_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            String resultString = mockMvc
                    .perform(get("/api/booking/" + testSetupHelper.getTestBookings().get(0).getId().toString()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse().getContentAsString();

            ObjectMapper obj = new ObjectMapper();
            TypeReference<ApiResponse<Booking>> typeReference = new TypeReference<ApiResponse<Booking>>() {
            };
            ApiResponse<Booking> result = obj.readValue(resultString, typeReference);
            Booking returnedBooking = result.getData();

            assertNotNull(returnedBooking);
            assertEquals(CREATED_BOOKING_NAME_1, returnedBooking.getName());
        }

        @Test
        void When_GetBookingNotOwned_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            mockMvc.perform(get("/api/booking/" + testSetupHelper.getTestBookings().get(0).getId().toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void When_GetBookingNotExist_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            UUID nonExistentId = UUID.randomUUID();
            mockMvc.perform(get("/api/booking/" + nonExistentId.toString()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Search bookings")
    class SearchBookings {

        @Test
        void When_SearchAllBookings_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
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
        void When_SearchAllBookingsWithPagination_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
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
        void When_SearchNoBookings_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
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
        void When_SearchBookingsByState_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
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
        void When_SearchBookingsByApartment_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            ObjectMapper obj = new ObjectMapper();
            // Search for apartments with name containing "loft"
            BookingSearchForm searchFormObj = new BookingSearchForm();
            searchFormObj.setApartmentName("loft");
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
                assertTrue(booking.getApartment().getName().toLowerCase().contains("loft"));
            }
        }

        @Test
        void When_SearchBookingsByDateRange_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
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

            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            Booking booking = Booking.builder()
                    .apartment(apartmentRepository.findById(testSetupHelper.getTestApartments().get(0).getId())
                            .orElseThrow())
                    .name("Booking To Be Deleted")
                    .startDate(startDate)
                    .endDate(endDate)
                    .price(400.0)
                    .paid(false)
                    .state(BookingState.PENDING)
                    .build();
            booking = bookingRepository.save(booking);
            forDeletionBookingId = booking.getId();
        }

        @Test
        void When_DeleteBooking_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .delete("/api/booking/" + forDeletionBookingId.toString()))
                    .andExpect(status().isOk());
            boolean exists = bookingRepository.existsById(forDeletionBookingId);
            assertTrue(!exists, "Booking was not deleted");
        }

        @Test
        void When_DeleteBookingNotOwned_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .delete("/api/booking/" + forDeletionBookingId.toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void When_DeleteBookingNotExist_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .delete("/api/booking/" + UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound());
        }
    }

}
