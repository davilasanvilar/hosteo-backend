package com.viladevcorp.hosteo.apartment;

import static com.viladevcorp.hosteo.common.TestConstants.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestSetupHelper;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.forms.ApartmentCreateForm;
import com.viladevcorp.hosteo.model.forms.ApartmentSearchForm;
import com.viladevcorp.hosteo.model.forms.ApartmentUpdateForm;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApartmentControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;
  @Autowired private ApartmentRepository apartmentRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private TestSetupHelper testSetupHelper;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    testSetupHelper.resetTestApartments();
  }

  @Nested
  @DisplayName("Create apartments")
  class CreateApartments {
    @Test
    void When_CreateApartment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      ApartmentCreateForm form = new ApartmentCreateForm();
      form.setName(NEW_APARTMENT_NAME_1);
      form.setAirbnbId(NEW_APARTMENT_AIRBNB_ID_1);
      form.setBookingId(NEW_APARTMENT_BOOKING_ID_1);
      form.setVisible(NEW_APARTMENT_VISIBLE_1);
      form.setAddress(NEW_APARTMENT_ADDRESS_1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/apartment")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Apartment> result = null;
      TypeReference<ApiResponse<Apartment>> typeReference =
          new TypeReference<ApiResponse<Apartment>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Apartment returnedApartment = result.getData();
      assertEquals(NEW_APARTMENT_NAME_1, returnedApartment.getName());
      assertEquals(NEW_APARTMENT_AIRBNB_ID_1, returnedApartment.getAirbnbId());
      assertEquals(NEW_APARTMENT_BOOKING_ID_1, returnedApartment.getBookingId());
      assertEquals(NEW_APARTMENT_ADDRESS_1, returnedApartment.getAddress());
      assertEquals(NEW_APARTMENT_VISIBLE_1, returnedApartment.isVisible());
      assertEquals(ApartmentState.READY, returnedApartment.getState());
      assertEquals(ACTIVE_USER_USERNAME_1, returnedApartment.getCreatedBy().getUsername());
    }

    @Test
    void When_LeavingBlankName_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      ApartmentCreateForm form = new ApartmentCreateForm();
      // Name is not set
      form.setAirbnbId(NEW_APARTMENT_AIRBNB_ID_1);
      form.setBookingId(NEW_APARTMENT_BOOKING_ID_1);
      form.setVisible(NEW_APARTMENT_VISIBLE_1);

      mockMvc
          .perform(
              post("/api/apartment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Get apartment")
  class GetApartment {
    @Test
    void When_GetApartment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      String resultString =
          mockMvc
              .perform(
                  get(
                      "/api/apartment/"
                          + testSetupHelper.getTestApartments().get(0).getId().toString()))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Apartment> result = null;
      TypeReference<ApiResponse<Apartment>> typeReference =
          new TypeReference<ApiResponse<Apartment>>() {};
      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Apartment returnedApartment = result.getData();
      assertEquals(CREATED_APARTMENT_NAME_1, returnedApartment.getName());
    }

    @Test
    void When_GetApartmentNotOwned_Forbidden() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(
              get(
                  "/api/apartment/"
                      + testSetupHelper.getTestApartments().get(0).getId().toString()))
          .andExpect(status().isForbidden());
    }

    @Test
    void When_GetApartmentNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc.perform(get("/api/apartment/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Update apartments")
  class UpdateApartments {
    @Test
    void When_UpdateApartment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      ApartmentUpdateForm form = new ApartmentUpdateForm();
      Apartment apartmentToUpdate =
          apartmentRepository
              .findById(testSetupHelper.getTestApartments().get(0).getId())
              .orElse(null);
      BeanUtils.copyProperties(apartmentToUpdate, form);
      form.setName(UPDATED_APARTMENT_NAME);
      form.setState(UPDATED_APARTMENT_STATE);
      form.setAddress(UPDATED_APARTMENT_ADDRESS);
      form.setAirbnbId(UPDATED_APARTMENT_AIRBNB_ID);
      form.setBookingId(UPDATED_APARTMENT_BOOKING_ID);
      form.setVisible(UPDATED_APARTMENT_VISIBLE);

      mockMvc
          .perform(
              patch("/api/apartment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isOk());
      Apartment apartmentUpdated =
          apartmentRepository
              .findById(testSetupHelper.getTestApartments().get(0).getId())
              .orElse(null);
      assertEquals(UPDATED_APARTMENT_NAME, apartmentUpdated.getName());
      assertEquals(UPDATED_APARTMENT_STATE, apartmentUpdated.getState());
      assertEquals(UPDATED_APARTMENT_ADDRESS, apartmentUpdated.getAddress());
      assertEquals(UPDATED_APARTMENT_AIRBNB_ID, apartmentUpdated.getAirbnbId());
      assertEquals(UPDATED_APARTMENT_BOOKING_ID, apartmentUpdated.getBookingId());
      assertEquals(UPDATED_APARTMENT_VISIBLE, apartmentUpdated.isVisible());
    }

    @Test
    void When_UpdateApartmentNotOwned_Forbidden() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      ApartmentUpdateForm form = new ApartmentUpdateForm();
      Apartment apartmentToUpdate =
          apartmentRepository
              .findById(testSetupHelper.getTestApartments().get(0).getId())
              .orElse(null);
      BeanUtils.copyProperties(apartmentToUpdate, form);
      form.setName(UPDATED_APARTMENT_NAME);

      mockMvc
          .perform(
              patch("/api/apartment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isForbidden());
    }

    @Test
    void When_UpdateApartmentNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      ApartmentUpdateForm form = new ApartmentUpdateForm();
      form.setId(UUID.randomUUID());
      form.setName(UPDATED_APARTMENT_NAME);
      form.setState(ApartmentState.READY);

      mockMvc
          .perform(
              patch("/api/apartment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_NameIsEmptyInForm_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      ApartmentUpdateForm form = new ApartmentUpdateForm();
      Apartment apartmentToUpdate =
          apartmentRepository
              .findById(testSetupHelper.getTestApartments().get(0).getId())
              .orElse(null);
      BeanUtils.copyProperties(apartmentToUpdate, form);
      form.setName("");

      mockMvc
          .perform(
              patch("/api/apartment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Search apartments")
  class SearchApartments {
    @Test
    void When_SearchAllApartments_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/apartments/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<Apartment>> result = null;
      TypeReference<ApiResponse<Page<Apartment>>> typeReference =
          new TypeReference<ApiResponse<Page<Apartment>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<Apartment> returnedPage = result.getData();
      List<Apartment> apartments = returnedPage.getContent();
      assertEquals(4, apartments.size());
    }

    @Test
    void When_SearchAllApartmentsWithPagination_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
      searchFormObj.setPageNumber(0);
      searchFormObj.setPageSize(2);
      String resultString =
          mockMvc
              .perform(
                  post("/api/apartments/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<Apartment>> result = null;
      TypeReference<ApiResponse<Page<Apartment>>> typeReference =
          new TypeReference<ApiResponse<Page<Apartment>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<Apartment> returnedPage = result.getData();
      List<Apartment> apartments = returnedPage.getContent();
      assertEquals(2, apartments.size());
      assertEquals(2, returnedPage.getTotalPages());
      assertEquals(4, returnedPage.getTotalRows());
    }

    @Test
    void When_SearchNoApartments_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

      ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/apartments/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<Apartment>> result = null;
      TypeReference<ApiResponse<Page<Apartment>>> typeReference =
          new TypeReference<ApiResponse<Page<Apartment>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<Apartment> returnedPage = result.getData();
      List<Apartment> apartments = returnedPage.getContent();
      assertEquals(0, apartments.size());
    }

    @Test
    void When_SearchApartmentsByState_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for READY apartments
      ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
      searchFormObj.setState(ApartmentState.READY);
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/apartments/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<Apartment>> result = null;
      TypeReference<ApiResponse<Page<Apartment>>> typeReference =
          new TypeReference<ApiResponse<Page<Apartment>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<Apartment> returnedPage = result.getData();
      List<Apartment> apartments = returnedPage.getContent();
      assertEquals(3, apartments.size());
      for (Apartment apartment : apartments) {
        assertEquals(ApartmentState.READY, apartment.getState());
      }
    }

    @Test
    void When_SearchApartmentsByName_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for apartments with name containing "loft"
      ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
      searchFormObj.setName("loft");
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/apartments/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<Apartment>> result = null;
      TypeReference<ApiResponse<Page<Apartment>>> typeReference =
          new TypeReference<ApiResponse<Page<Apartment>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<Apartment> returnedPage = result.getData();
      List<Apartment> apartments = returnedPage.getContent();
      assertEquals(2, apartments.size());
      for (Apartment apartment : apartments) {
        assertTrue(apartment.getName().toLowerCase().contains("loft"));
      }
    }
  }

  @Nested
  @DisplayName("Delete apartments")
  class DeleteApartments {
    @Test
    void When_DeleteApartment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/apartment/"
                      + testSetupHelper.getTestApartments().get(0).getId().toString()))
          .andExpect(status().isOk());
      boolean exists =
          apartmentRepository.existsById(testSetupHelper.getTestApartments().get(0).getId());
      assertFalse(exists, "Apartment was not deleted");
    }

    @Test
    void When_DeleteApartmentNotOwned_Forbidden() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/apartment/"
                      + testSetupHelper.getTestApartments().get(0).getId().toString()))
          .andExpect(status().isForbidden());
    }

    @Test
    void When_DeleteApartmentNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/apartment/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }
  }
}
