package com.viladevcorp.hosteo.workflow;

import static com.viladevcorp.hosteo.common.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.BookingSchedulerDto;
import com.viladevcorp.hosteo.model.types.Alert;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.*;
import com.viladevcorp.hosteo.utils.ApiResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

class WorkflowControllerTest extends BaseControllerTest {

  @TestConfiguration
  static class FixedClockTestConfig {

    @Bean
    @Primary
    Clock testClock() {
      return Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC);
    }
  }

  @BeforeEach
  void setup() throws Exception {
    testSetupHelper.resetAssignments();
  }

  @Autowired private UserRepository userRepository;

  @Autowired private BookingRepository bookingRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private static final String START_OF_WEEK = "01-12-2025";
  private static final String NOW = "2025-12-04T10:00:00Z";

  @Nested
  @DisplayName("Get scheduler info")
  class Scheduler {

    @Test
    void When_GetSchedulerInfo_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant now = Instant.parse(NOW);

      Apartment apartment = testSetupHelper.getTestApartments().get(1);

      String bookingIn1DaysName = "Test Booking 1 Days";
      String bookingIn3DaysName = "Test Booking 3 Days";

      // We add one booking in 1 and 3 days to test alerts
      Booking bookingIn1Days =
          Booking.builder()
              .name(bookingIn1DaysName)
              .apartment(apartment)
              .startDate(now.plusSeconds(24 * 3600))
              .endDate(now.plusSeconds(2 * 24 * 3600))
              .state(BookingState.PENDING)
              .build();

      bookingRepository.save(bookingIn1Days);

      Booking bookingIn3Days =
          Booking.builder()
              .name(bookingIn3DaysName)
              .apartment(apartment)
              .startDate(now.plusSeconds(3 * 24 * 3600))
              .endDate(now.plusSeconds(4 * 24 * 3600))
              .state(BookingState.PENDING)
              .build();

      bookingRepository.save(bookingIn3Days);

      String resultString =
          mockMvc
              .perform(post("/api/scheduler/" + START_OF_WEEK))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SchedulerInfo>> typeReference = new TypeReference<>() {};
      ApiResponse<SchedulerInfo> result = objectMapper.readValue(resultString, typeReference);

      SchedulerInfo info = result.getData();
      assertEquals(3, info.getBookings().size());
      assertEquals(1, info.getRedAlertBookings().size());
      assertEquals(1, info.getYellowAlertBookings().size());

      BookingSchedulerDto redAlertBooking = info.getRedAlertBookings().get(0);
      assertEquals(bookingIn1DaysName, redAlertBooking.getBooking().getName());
      assertEquals(Alert.DAYS_LEFT_2_UNASSIGNED, redAlertBooking.getAlert());

      BookingSchedulerDto yellowAlertBooking = info.getYellowAlertBookings().get(0);
      assertEquals(bookingIn3DaysName, yellowAlertBooking.getBooking().getName());
      assertEquals(Alert.DAYS_LEFT_5_UNASSIGNED, yellowAlertBooking.getAlert());
    }
  }
}
