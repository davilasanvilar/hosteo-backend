package com.viladevcorp.hosteo.booking;

import static com.viladevcorp.hosteo.common.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestSetupHelper;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.dto.ImpBookingDto;
import com.viladevcorp.hosteo.model.types.ConflictType;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

class ImportBookingControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;

  @Autowired private BookingRepository bookingRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired TestSetupHelper testSetupHelper;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setup() throws Exception {
    testSetupHelper.resetImportApartments();
  }

  @Nested
  @DisplayName("Import airbnb bookings")
  class ImportAirbnbBookings {

    @Test
    void When_ImportAirbnbBooking_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      File importFile = new File("src/test/resources/import_airbnb.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file", "import_airbnb.csv", "text/csv", new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/airbnb").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      List<ImpBookingDto> importedBookings = result.getData();
      assertEquals(BOOKING_COUNT_AIRBNB_AFTER_IMPORT, importedBookings.size());
      assertEquals(
          BOOKING_COUNT_AIRBNB_APARTMENT_1,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_1))
              .count());
      assertEquals(
          BOOKING_COUNT_AIRBNB_APARTMENT_2,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_2))
              .count());
      assertEquals(
          BOOKING_COUNT_AIRBNB_APARTMENT_3,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_3))
              .count());
      importedBookings.forEach(impBookingDto -> assertNull(impBookingDto.getConflict()));
    }

    @Test
    void When_ImportAirbnbBookingSomeConflicts_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      testSetupHelper.resetImportConflicts(true);

      File importFile = new File("src/test/resources/import_airbnb.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file", "import_airbnb.csv", "text/csv", new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/airbnb").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      List<ImpBookingDto> importedBookings = result.getData();
      assertEquals(BOOKING_COUNT_AIRBNB_AFTER_IMPORT, importedBookings.size());
      assertEquals(
          BOOKING_COUNT_AIRBNB_APARTMENT_1,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_1))
              .count());
      assertEquals(
          BOOKING_COUNT_AIRBNB_APARTMENT_2,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_2))
              .count());
      assertEquals(
          BOOKING_COUNT_AIRBNB_APARTMENT_3,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_3))
              .count());
      importedBookings.forEach(
          impBookingDto -> {
            if (impBookingDto.getName().equals(CONFLICTED_BOOKING_AIRBNB_NAME)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(ConflictType.BOOKING_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  testSetupHelper.getConflictBooking().getId(),
                  impBookingDto.getConflict().getConflictEntity().getId());
            } else if (impBookingDto.getName().equals(CONFLICTED_BOOKING_AIRBNB_NAME_2)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(ConflictType.ASSIGNMENT_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  testSetupHelper.getConflictAssignment().getId(),
                  impBookingDto.getConflict().getConflictEntity().getId());
            } else {
              assertNull(impBookingDto.getConflict());
            }
          });
    }
  }

  @Nested
  @DisplayName("Import Booking bookings")
  class ImportBookingBookings {

    @Test
    void When_ImportBookingBooking_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      File importFile = new File("src/test/resources/import_booking.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file", "import_booking.csv", "text/csv", new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/booking").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      List<ImpBookingDto> importedBookings = result.getData();
      assertEquals(BOOKING_COUNT_BOOKING_AFTER_IMPORT, importedBookings.size());
      assertEquals(
          BOOKING_COUNT_BOOKING_APARTMENT_1,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_1))
              .count());
      assertEquals(
          BOOKING_COUNT_BOOKING_APARTMENT_2,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_2))
              .count());
      assertEquals(
          BOOKING_COUNT_BOOKING_APARTMENT_3,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_3))
              .count());
      importedBookings.forEach(impBookingDto -> assertNull(impBookingDto.getConflict()));
    }

    @Test
    void When_ImportBookingBookingSomeConflicts_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      testSetupHelper.resetImportConflicts(false);

      File importFile = new File("src/test/resources/import_booking.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file", "import_booking.csv", "text/csv", new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/booking").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      List<ImpBookingDto> importedBookings = result.getData();
      assertEquals(BOOKING_COUNT_BOOKING_AFTER_IMPORT, importedBookings.size());
      assertEquals(
          BOOKING_COUNT_BOOKING_APARTMENT_1,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_1))
              .count());
      assertEquals(
          BOOKING_COUNT_BOOKING_APARTMENT_2,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_2))
              .count());
      assertEquals(
          BOOKING_COUNT_BOOKING_APARTMENT_3,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_3))
              .count());
      importedBookings.forEach(
          impBookingDto -> {
            if (impBookingDto.getName().equals(CONFLICTED_BOOKING_BOOKING_NAME)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(ConflictType.BOOKING_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  testSetupHelper.getConflictBooking().getId(),
                  impBookingDto.getConflict().getConflictEntity().getId());
            } else if (impBookingDto.getName().equals(CONFLICTED_BOOKING_BOOKING_NAME_2)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(ConflictType.ASSIGNMENT_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  testSetupHelper.getConflictAssignment().getId(),
                  impBookingDto.getConflict().getConflictEntity().getId());
            } else {
              assertNull(impBookingDto.getConflict());
            }
          });
    }
  }
}
