package com.viladevcorp.hosteo.booking;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.types.ApartmentState;
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
import com.viladevcorp.hosteo.model.dto.BookingDto;
import com.viladevcorp.hosteo.model.dto.SimpleBookingDto;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.forms.BookingCreateForm;
import com.viladevcorp.hosteo.model.forms.BookingSearchForm;
import com.viladevcorp.hosteo.model.forms.BookingUpdateForm;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;

import javax.management.InstanceNotFoundException;

import static com.viladevcorp.hosteo.common.TestConstants.*;

class BookingControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;

  @Autowired private ApartmentRepository apartmentRepository;

  @Autowired private BookingRepository bookingRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired TestSetupHelper testSetupHelper;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void initialize() throws Exception {
    testSetupHelper.resetTestBookings();
  }

  @Nested
  @DisplayName("Create bookings")
  class CreateBookings {

    @Test
    void When_CreateBooking_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate = TestUtils.dateStrToInstant(NEW_BOOKING_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(NEW_BOOKING_END_DATE);
      BookingCreateForm form = new BookingCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setName(NEW_BOOKING_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(NEW_BOOKING_PRICE);
      form.setPaid(NEW_BOOKING_PAID);

      String resultString =
          mockMvc
              .perform(
                  post("/api/booking")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
          new TypeReference<ApiResponse<SimpleBookingDto>>() {};
      ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);
      SimpleBookingDto returnedBooking = result.getData();

      assertNotNull(returnedBooking);
      assertEquals(NEW_BOOKING_NAME, returnedBooking.getName());
      assertEquals(NEW_BOOKING_PRICE, returnedBooking.getPrice());
      assertEquals(NEW_BOOKING_PAID, returnedBooking.isPaid());
      assertEquals(NEW_BOOKING_STATE, returnedBooking.getState());
      assertEquals(NEW_BOOKING_SOURCE, returnedBooking.getSource());
      assertEquals(0, returnedBooking.getStartDate().compareTo(startDate));
      assertEquals(0, returnedBooking.getEndDate().compareTo(endDate));
    }

    @Test
    void When_CreateBookingMissingName_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(NEW_BOOKING_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(NEW_BOOKING_END_DATE);

      BookingCreateForm form = new BookingCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(NEW_BOOKING_PRICE);
      form.setPaid(NEW_BOOKING_PAID);

      mockMvc
          .perform(
              post("/api/booking")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_CreateBookingNotOwned_Forbidden() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(NEW_BOOKING_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(NEW_BOOKING_END_DATE);

      BookingCreateForm form = new BookingCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setName(NEW_BOOKING_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(NEW_BOOKING_PRICE);
      form.setPaid(NEW_BOOKING_PAID);

      mockMvc
          .perform(
              post("/api/booking")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isForbidden());
    }

    @Test
    void When_CreateBookingApartmentNotFound_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(NEW_BOOKING_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(NEW_BOOKING_END_DATE);

      BookingCreateForm form = new BookingCreateForm();
      form.setApartmentId(UUID.randomUUID());
      form.setName(NEW_BOOKING_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(NEW_BOOKING_PRICE);
      form.setPaid(NEW_BOOKING_PAID);

      mockMvc
          .perform(
              post("/api/booking")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_CreateBookingApartmentNotAvailable_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      // Dates overlapping with an existing booking
      Instant startDate = TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_1);
      Instant endDate = TestUtils.dateStrToInstant(CREATED_BOOKING_END_DATE_1);

      BookingCreateForm form = new BookingCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setName(NEW_BOOKING_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(NEW_BOOKING_PRICE);
      form.setPaid(NEW_BOOKING_PAID);

      mockMvc
          .perform(
              post("/api/booking")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isConflict());

      startDate = startDate.plusSeconds(24 * 60 * 60);
      endDate = endDate.plusSeconds(24 * 60 * 60);

      form.setStartDate(startDate);
      form.setEndDate(endDate);

      String resultString =
          mockMvc
              .perform(
                  post("/api/booking")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
          new TypeReference<ApiResponse<SimpleBookingDto>>() {};
      ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);

      assertEquals(CodeErrors.NOT_AVAILABLE_DATES, result.getErrorCode());
    }
  }

  @Nested
  @DisplayName("Update bookings")
  class UpdateBookings {

    @Test
    void When_UpdateBooking_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(UPDATED_BOOKING_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(UPDATED_BOOKING_END_DATE);
      BookingUpdateForm form = new BookingUpdateForm();
      form.setId(testSetupHelper.getTestBookings().get(UPDATED_BOOKING_APARTMENT_POSITION).getId());
      form.setName(UPDATED_BOOKING_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(UPDATED_BOOKING_PRICE);
      form.setPaid(UPDATED_BOOKING_PAID);
      form.setState(UPDATED_BOOKING_STATE);
      form.setSource(UPDATED_BOOKING_SOURCE);

      mockMvc
          .perform(
              patch("/api/booking")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isOk());

      Booking returnedBooking =
          bookingRepository
              .findById(
                  testSetupHelper.getTestBookings().get(UPDATED_BOOKING_APARTMENT_POSITION).getId())
              .orElse(null);

      assertNotNull(returnedBooking);
      assertEquals(UPDATED_BOOKING_NAME, returnedBooking.getName());
      assertEquals(UPDATED_BOOKING_PRICE, returnedBooking.getPrice());
      assertEquals(UPDATED_BOOKING_PAID, returnedBooking.isPaid());
      assertEquals(UPDATED_BOOKING_STATE, returnedBooking.getState());
      assertEquals(UPDATED_BOOKING_SOURCE, returnedBooking.getSource());
      assertEquals(0, returnedBooking.getStartDate().compareTo(startDate));
      assertEquals(0, returnedBooking.getEndDate().compareTo(endDate));
    }

    @Test
    void When_UpdateBookingNotOwned_Forbidden() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      Instant startDate = Instant.now().plusSeconds(10 * 24 * 60 * 60);
      Instant endDate = Instant.now().plusSeconds(12 * 24 * 60 * 60);

      BookingUpdateForm form = new BookingUpdateForm();
      form.setId(testSetupHelper.getTestBookings().get(UPDATED_BOOKING_APARTMENT_POSITION).getId());
      form.setName(UPDATED_BOOKING_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(UPDATED_BOOKING_PRICE);
      form.setPaid(UPDATED_BOOKING_PAID);
      form.setState(UPDATED_BOOKING_STATE);
      form.setSource(UPDATED_BOOKING_SOURCE);

      mockMvc
          .perform(
              patch("/api/booking")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isForbidden());
    }

    @Test
    void When_UpdateBookingNotAvailable_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate = TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_1);
      Instant endDate = TestUtils.dateStrToInstant(CREATED_BOOKING_END_DATE_1);

      BookingUpdateForm form = new BookingUpdateForm();
      form.setId(testSetupHelper.getTestBookings().get(2).getId());
      form.setName(UPDATED_BOOKING_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(UPDATED_BOOKING_PRICE);
      form.setPaid(UPDATED_BOOKING_PAID);
      form.setState(UPDATED_BOOKING_STATE);
      form.setSource(UPDATED_BOOKING_SOURCE);

      String resultString =
          mockMvc
              .perform(
                  patch("/api/booking")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
          new TypeReference<ApiResponse<SimpleBookingDto>>() {};
      ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(CodeErrors.NOT_AVAILABLE_DATES, result.getErrorCode());
    }

    @Test
    void When_UpdateBookingToProgressAndHasFinishedTasks_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      testSetupHelper.createTestAssignments();

      try {

        BookingUpdateForm form = new BookingUpdateForm();
        form.setId(testSetupHelper.getTestBookings().get(0).getId());
        form.setName(UPDATED_BOOKING_NAME);
        form.setStartDate(Instant.now().plusSeconds(10 * 24 * 60 * 60));
        form.setEndDate(Instant.now().plusSeconds(12 * 24 * 60 * 60));
        form.setPrice(UPDATED_BOOKING_PRICE);
        form.setPaid(UPDATED_BOOKING_PAID);
        form.setState(BookingState.IN_PROGRESS);
        form.setSource(UPDATED_BOOKING_SOURCE);

        String resultString =
            mockMvc
                .perform(
                    patch("/api/booking")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
            new TypeReference<ApiResponse<SimpleBookingDto>>() {};
        ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);
        assertEquals(CodeErrors.ASSIGNMENTS_FINISHED_FOR_BOOKING, result.getErrorCode());
      } finally {
        testSetupHelper.deleteTestAssignments();
      }
    }

    @Test
    void When_UpdateBookingStateToProgressAndApartmentHasAlreadyInProgressBooking_Conflict()
        throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  patch(
                          "/api/booking/"
                              + testSetupHelper.getTestBookings().get(3).getId()
                              + "/state/"
                              + BookingState.IN_PROGRESS)
                      .contentType("application/json"))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
          new TypeReference<ApiResponse<SimpleBookingDto>>() {};
      ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(CodeErrors.EXISTS_BOOKING_ALREADY_IN_PROGRESS, result.getErrorCode());
    }
  }

  @Nested
  @DisplayName("Get booking")
  class GetBooking {

    @Test
    void When_GetBooking_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      String resultString =
          mockMvc
              .perform(
                  get(
                      "/api/booking/"
                          + testSetupHelper.getTestBookings().get(0).getId().toString()))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<BookingDto>> typeReference =
          new TypeReference<ApiResponse<BookingDto>>() {};
      ApiResponse<BookingDto> result = objectMapper.readValue(resultString, typeReference);
      BookingDto returnedBooking = result.getData();

      assertNotNull(returnedBooking);
      assertEquals(CREATED_BOOKING_NAME_1, returnedBooking.getName());
    }

    @Test
    void When_GetBookingNotOwned_Forbidden() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(
              get("/api/booking/" + testSetupHelper.getTestBookings().get(0).getId().toString()))
          .andExpect(status().isForbidden());
    }

    @Test
    void When_GetBookingNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc.perform(get("/api/booking/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Search bookings")
  class SearchBookings {

    @Test
    void When_SearchAllBookings_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      BookingSearchForm searchFormObj = new BookingSearchForm();
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/bookings/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<SimpleBookingDto>> result = null;
      TypeReference<ApiResponse<Page<SimpleBookingDto>>> typeReference =
          new TypeReference<ApiResponse<Page<SimpleBookingDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<SimpleBookingDto> returnedPage = result.getData();
      List<SimpleBookingDto> bookings = returnedPage.getContent();
      assertEquals(4, bookings.size());
    }

    @Test
    void When_SearchAllBookingsWithPagination_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      BookingSearchForm searchFormObj = new BookingSearchForm();
      searchFormObj.setPageNumber(0);
      searchFormObj.setPageSize(2);
      String resultString =
          mockMvc
              .perform(
                  post("/api/bookings/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<SimpleBookingDto>> result = null;
      TypeReference<ApiResponse<Page<SimpleBookingDto>>> typeReference =
          new TypeReference<ApiResponse<Page<SimpleBookingDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<SimpleBookingDto> returnedPage = result.getData();
      List<SimpleBookingDto> bookings = returnedPage.getContent();
      assertEquals(2, bookings.size());
      assertEquals(2, returnedPage.getTotalPages());
      assertEquals(4, returnedPage.getTotalRows());
    }

    @Test
    void When_SearchNoBookings_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

      BookingSearchForm searchFormObj = new BookingSearchForm();
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/bookings/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<SimpleBookingDto>> result = null;
      TypeReference<ApiResponse<Page<SimpleBookingDto>>> typeReference =
          new TypeReference<ApiResponse<Page<SimpleBookingDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<SimpleBookingDto> returnedPage = result.getData();
      List<SimpleBookingDto> bookings = returnedPage.getContent();
      assertEquals(0, bookings.size());
    }

    @Test
    void When_SearchBookingsByState_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for READY apartments
      BookingSearchForm searchFormObj = new BookingSearchForm();
      searchFormObj.setState(BookingState.PENDING);
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/bookings/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<SimpleBookingDto>> result = null;
      TypeReference<ApiResponse<Page<SimpleBookingDto>>> typeReference =
          new TypeReference<ApiResponse<Page<SimpleBookingDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<SimpleBookingDto> returnedPage = result.getData();
      List<SimpleBookingDto> bookings = returnedPage.getContent();
      assertEquals(2, bookings.size());
      for (SimpleBookingDto booking : bookings) {
        assertEquals(BookingState.PENDING, booking.getState());
      }
    }

    @Test
    void When_SearchBookingsByApartment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for apartments with name containing "loft"
      BookingSearchForm searchFormObj = new BookingSearchForm();
      searchFormObj.setApartmentName("loft");
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/bookings/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<SimpleBookingDto>> result = null;
      TypeReference<ApiResponse<Page<SimpleBookingDto>>> typeReference =
          new TypeReference<ApiResponse<Page<SimpleBookingDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<SimpleBookingDto> returnedPage = result.getData();
      List<SimpleBookingDto> bookings = returnedPage.getContent();
      assertEquals(2, bookings.size());
    }

    @Test
    void When_SearchBookingsByDateRange_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for bookings within a date range
      BookingSearchForm searchFormObj = new BookingSearchForm();
      Instant startDate = Instant.parse("2025-11-20T00:00:00Z");
      Instant endDate = Instant.parse("2025-12-02T00:00:00Z");
      searchFormObj.setStartDate(startDate);
      searchFormObj.setEndDate(endDate);
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/bookings/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<SimpleBookingDto>> result = null;
      TypeReference<ApiResponse<Page<SimpleBookingDto>>> typeReference =
          new TypeReference<ApiResponse<Page<SimpleBookingDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<SimpleBookingDto> returnedPage = result.getData();
      List<SimpleBookingDto> bookings = returnedPage.getContent();
      assertEquals(2, bookings.size());
      for (SimpleBookingDto booking : bookings) {
        assertTrue(
            !booking.getStartDate().isBefore(startDate)
                && !booking.getStartDate().isAfter(endDate));
      }
    }
  }

  @Nested
  @DisplayName("Delete bookings")
  class DeleteBookings {
    private UUID forDeletionBookingId;

    @BeforeEach
    void setup() {
      // Create a booking to be deleted
      Instant startDate = Instant.now().plusSeconds(5 * 24 * 60 * 60);
      Instant endDate = Instant.now().plusSeconds(10 * 24 * 60 * 60);

      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Booking booking =
          Booking.builder()
              .apartment(
                  apartmentRepository
                      .findById(testSetupHelper.getTestApartments().get(0).getId())
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
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/booking/" + forDeletionBookingId.toString()))
          .andExpect(status().isOk());
      boolean exists = bookingRepository.existsById(forDeletionBookingId);
      assertFalse(exists, "Booking was not deleted");
    }

    @Test
    void When_DeleteBookingNotOwned_Forbidden() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/booking/" + forDeletionBookingId.toString()))
          .andExpect(status().isForbidden());
    }

    @Test
    void When_DeleteBookingNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/booking/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Workflow bookings")
  class WorkflowBookings {

      @BeforeEach
        void setup() throws Exception {
            testSetupHelper.resetAssignments();
        }
    @Test
    void When_ChangeBookingStateToInProgress_ApartmentIsOccupied() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  patch(
                          "/api/booking/"
                              + testSetupHelper.getTestBookings().get(2).getId()
                              + "/state/"
                              + BookingState.IN_PROGRESS)
                      .contentType("application/json"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
          new TypeReference<ApiResponse<SimpleBookingDto>>() {};
      ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);
      SimpleBookingDto returnedBooking = result.getData();

      Apartment apartment =
          apartmentRepository
              .findById(returnedBooking.getApartment().getId())
              .orElseThrow(InstanceNotFoundException::new);
      assertEquals(ApartmentState.OCCUPIED, apartment.getState());
    }

    @Test
    void When_ChangeBookingStateToCompleted_ApartmentIsUsed() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  patch(
                          "/api/booking/"
                              + testSetupHelper.getTestBookings().get(1).getId()
                              + "/state/"
                              + BookingState.FINISHED)
                      .contentType("application/json"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
          new TypeReference<ApiResponse<SimpleBookingDto>>() {};
      ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);
      SimpleBookingDto returnedBooking = result.getData();

      Apartment apartment =
          apartmentRepository
              .findById(returnedBooking.getApartment().getId())
              .orElseThrow(InstanceNotFoundException::new);
      assertEquals(ApartmentState.USED, apartment.getState());
    }

    @Test
    void When_CreateInProgressBooking_ApartmentIsOccupied() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_3).minusSeconds(24 * 60 * 60);
      Instant endDate =
          TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_3).minusSeconds(12 * 60 * 60);
      BookingCreateForm form = new BookingCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setName(NEW_BOOKING_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(500.0);
      form.setPaid(true);
      form.setState(BookingState.IN_PROGRESS);

      String resultString =
          mockMvc
              .perform(
                  post("/api/booking")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
          new TypeReference<ApiResponse<SimpleBookingDto>>() {};
      ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);
      SimpleBookingDto returnedBooking = result.getData();

      Apartment apartment =
          apartmentRepository
              .findById(returnedBooking.getApartment().getId())
              .orElseThrow(InstanceNotFoundException::new);
      assertEquals(ApartmentState.OCCUPIED, apartment.getState());
    }

    void When_UpdateBookingToFinished_ApartmentIsUsed() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate = TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_2);
      Instant endDate = TestUtils.dateStrToInstant(CREATED_BOOKING_END_DATE_2);
      BookingUpdateForm form = new BookingUpdateForm();
      form.setId(testSetupHelper.getTestBookings().get(2).getId());
      form.setName(CREATED_BOOKING_NAME_2);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setPrice(CREATED_BOOKING_PRICE_2);
      form.setPaid(true);
      form.setState(BookingState.FINISHED);

      mockMvc
          .perform(
              patch("/api/booking")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isOk());

      Apartment apartment =
          apartmentRepository
              .findById(testSetupHelper.getTestBookings().get(2).getApartment().getId())
              .orElseThrow(InstanceNotFoundException::new);
      assertEquals(ApartmentState.USED, apartment.getState());
    }

    void When_UpdateBookingToFinishedOnlyState_ApartmentIsUsed() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  patch(
                          "/api/booking/"
                              + testSetupHelper.getTestBookings().get(2).getId()
                              + "/state/"
                              + BookingState.FINISHED)
                      .contentType("application/json"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SimpleBookingDto>> typeReference =
          new TypeReference<ApiResponse<SimpleBookingDto>>() {};
      ApiResponse<SimpleBookingDto> result = objectMapper.readValue(resultString, typeReference);
      SimpleBookingDto returnedBooking = result.getData();

      Apartment apartment =
          apartmentRepository
              .findById(returnedBooking.getApartment().getId())
              .orElseThrow(InstanceNotFoundException::new);
      assertEquals(ApartmentState.USED, apartment.getState());
    }
  }
}
