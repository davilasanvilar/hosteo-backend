package com.viladevcorp.hosteo.apartment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.model.Address;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.forms.ApartmentCreateForm;
import com.viladevcorp.hosteo.model.forms.ApartmentSearchForm;
import com.viladevcorp.hosteo.model.forms.ApartmentUpdateForm;
import com.viladevcorp.hosteo.model.types.ApartmentStateEnum;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.service.AuthService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApartmentControllerTest {

	private static final String ACTIVE_USER_EMAIL_1 = "test@gmail.com";
	private static final String ACTIVE_USER_USERNAME_1 = "test";
	private static final String ACTIVE_USER_PASSWORD_1 = "12test34";

	private static final String ACTIVE_USER_EMAIL_2 = "test2@gmail.com";
	private static final String ACTIVE_USER_USERNAME_2 = "test2";
	private static final String ACTIVE_USER_PASSWORD_2 = "12test34";

	private static UUID alreadyCreatedApartmentId;
	private static final String ALREADY_CREATED_APARTMENT_NAME = "Created apartment";
	private static final String ALREADY_CREATED_APARTMENT_NAME_2 = "Created loft 2";
	private static final String ALREADY_CREATED_APARTMENT_NAME_3 = "Created loft 3";
	private static final String ALREADY_CREATED_APARTMENT_NAME_4 = "Created apartment 4";
	private static final String NONEXISTENT_APARTMENT_ID = UUID.randomUUID().toString();

	private static final String UPDATED_NAME = "Updated apartment name";

	private static final String APARTMENT_NAME_1 = "My Apartment 1";
	private static final String APARTMENT_AIRBNB_ID_1 = "airbnb-1";
	private static final String APARTMENT_BOOKING_ID_1 = "booking-1";
	private static final boolean APARTMENT_VISIBLE_1 = true;
	private static final String APARTMENT_STREET_1 = "123 Main St";
	private static final String APARTMENT_NUMBER_1 = "25";
	private static final String APARTMENT_APARTMENT_NUMBER_1 = "Apt 5";
	private static final String APARTMENT_CITY_1 = "Sample City";
	private static final String APARTMENT_COUNTRY_1 = "Sample Country";
	private static final String APARTMENT_ZIP_CODE_1 = "12345";

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ApartmentRepository apartmentRepository;
	@Autowired
	private AuthService authService;
	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	void initialize() throws Exception {
		User user1 = authService.registerUser(ACTIVE_USER_EMAIL_1, ACTIVE_USER_USERNAME_1, ACTIVE_USER_PASSWORD_1);
		user1.setValidated(true);
		user1 = userRepository.save(user1);
		User user2 = authService.registerUser(ACTIVE_USER_EMAIL_2, ACTIVE_USER_USERNAME_2, ACTIVE_USER_PASSWORD_2);
		user2.setValidated(true);
		user2 = userRepository.save(user2);
	}

	@BeforeEach
	void setup() {
		User user1 = userRepository.findByUsername(ACTIVE_USER_USERNAME_1);
		Apartment apartment = Apartment.builder().name(ALREADY_CREATED_APARTMENT_NAME).state(ApartmentStateEnum.READY)
				.createdBy(user1).build();
		apartment = apartmentRepository.save(apartment);
		alreadyCreatedApartmentId = apartment.getId();
		apartment = Apartment.builder().name(ALREADY_CREATED_APARTMENT_NAME_2).state(ApartmentStateEnum.READY)
				.createdBy(user1).build();
		apartmentRepository.save(apartment);
		apartment = Apartment.builder().name(ALREADY_CREATED_APARTMENT_NAME_3).state(ApartmentStateEnum.READY)
				.createdBy(user1).build();
		apartmentRepository.save(apartment);
		apartment = Apartment.builder().name(ALREADY_CREATED_APARTMENT_NAME_4).state(ApartmentStateEnum.OCCUPIED)
				.createdBy(user1).build();
		apartmentRepository.save(apartment);
	}

	@AfterEach
	void clean() {
		apartmentRepository.deleteAll();
	}

	@AfterAll
	void globalClean() {
		userRepository.deleteAll();
	}

	@Nested
	@DisplayName("Create apartments")
	class CreateApartments {
		@Test
		@WithMockUser("test")
		void When_CreateApartment_Ok() throws Exception {
			ApartmentCreateForm form = new ApartmentCreateForm();
			Address address = new Address();
			address.setStreet(APARTMENT_STREET_1);
			address.setNumber(APARTMENT_NUMBER_1);
			address.setApartmentNumber(APARTMENT_APARTMENT_NUMBER_1);
			address.setCity(APARTMENT_CITY_1);
			address.setCountry(APARTMENT_COUNTRY_1);
			address.setZipCode(APARTMENT_ZIP_CODE_1);

			form.setName(APARTMENT_NAME_1);
			form.setAirbnbId(APARTMENT_AIRBNB_ID_1);
			form.setBookingId(APARTMENT_BOOKING_ID_1);
			form.setVisible(APARTMENT_VISIBLE_1);
			form.setAddress(address);
			ObjectMapper obj = new ObjectMapper();
			String resultString = mockMvc.perform(post("/api/apartment")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Apartment> result = null;
			TypeReference<ApiResponse<Apartment>> typeReference = new TypeReference<ApiResponse<Apartment>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Apartment returnedApartment = result.getData();
			assertEquals(APARTMENT_NAME_1, returnedApartment.getName());
			assertEquals(APARTMENT_AIRBNB_ID_1, returnedApartment.getAirbnbId());
			assertEquals(APARTMENT_BOOKING_ID_1, returnedApartment.getBookingId());
			assertEquals(address, returnedApartment.getAddress());
			assertEquals(APARTMENT_VISIBLE_1, returnedApartment.isVisible());
			assertEquals(ApartmentStateEnum.READY, returnedApartment.getState());
			assertEquals(ACTIVE_USER_USERNAME_1, returnedApartment.getCreatedBy().getUsername());
		}

		@Test
		@WithMockUser("test")
		void When_LeavingBlankName_BadRequest() throws Exception {
			ApartmentCreateForm form = new ApartmentCreateForm();
			// Name is not set
			form.setAirbnbId(APARTMENT_AIRBNB_ID_1);
			form.setBookingId(APARTMENT_BOOKING_ID_1);
			form.setVisible(APARTMENT_VISIBLE_1);
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(post("/api/apartment")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("Get apartment")
	class GetApartment {
		@Test
		@WithMockUser("test")
		void When_GetApartment_Ok() throws Exception {
			String resultString = mockMvc.perform(get("/api/apartment/" + alreadyCreatedApartmentId.toString()))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			ApiResponse<Apartment> result = null;
			TypeReference<ApiResponse<Apartment>> typeReference = new TypeReference<ApiResponse<Apartment>>() {
			};
			ObjectMapper obj = new ObjectMapper();
			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Apartment returnedApartment = result.getData();
			assertEquals(ALREADY_CREATED_APARTMENT_NAME, returnedApartment.getName());
		}

		@Test
		@WithMockUser("test2")
		void When_GetApartmentNotOwned_Forbidden() throws Exception {
			mockMvc.perform(get("/api/apartment/" + alreadyCreatedApartmentId.toString()))
					.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser("test")
		void When_GetApartmentNotExist_NotFound() throws Exception {
			mockMvc.perform(get("/api/apartment/" + NONEXISTENT_APARTMENT_ID))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("Update apartments")
	class UpdateApartments {
		@Test
		@WithMockUser("test")
		void When_UpdateApartment_Ok() throws Exception {
			ApartmentUpdateForm form = new ApartmentUpdateForm();
			Apartment apartmentToUpdate = apartmentRepository.findById(alreadyCreatedApartmentId).orElse(null);
			BeanUtils.copyProperties(apartmentToUpdate, form);
			form.setName(UPDATED_NAME);
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(patch("/api/apartment")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isOk());
			Apartment apartmentUpdated = apartmentRepository.findById(alreadyCreatedApartmentId).orElse(null);
			assertEquals(UPDATED_NAME, apartmentUpdated.getName());
		}

		@Test
		@WithMockUser("test2")
		void When_UpdateApartmentNotOwned_Forbidden() throws Exception {
			ApartmentUpdateForm form = new ApartmentUpdateForm();
			Apartment apartmentToUpdate = apartmentRepository.findById(alreadyCreatedApartmentId).orElse(null);
			BeanUtils.copyProperties(apartmentToUpdate, form);
			form.setName(UPDATED_NAME);
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(patch("/api/apartment")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser("test")
		void When_UpdateApartmentNotExist_NotFound() throws Exception {
			ApartmentUpdateForm form = new ApartmentUpdateForm();
			form.setId(UUID.randomUUID());
			form.setName(UPDATED_NAME);
			form.setState(ApartmentStateEnum.READY);
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(patch("/api/apartment")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isNotFound());
		}

		@Test
		@WithMockUser("test")
		void When_NameIsEmptyInForm_BadRequest() throws Exception {
			ApartmentUpdateForm form = new ApartmentUpdateForm();
			Apartment apartmentToUpdate = apartmentRepository.findById(alreadyCreatedApartmentId).orElse(null);
			BeanUtils.copyProperties(apartmentToUpdate, form);
			form.setName("");
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(patch("/api/apartment")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("Search apartments")
	class SearchApartments {
		@Test
		@WithMockUser("test")
		void When_SearchAllApartments_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
			searchFormObj.setPageNumber(-1);
			String resultString = mockMvc.perform(post("/api/apartments/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Apartment>> result = null;
			TypeReference<ApiResponse<Page<Apartment>>> typeReference = new TypeReference<ApiResponse<Page<Apartment>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Page<Apartment> returnedPage = result.getData();
			List<Apartment> apartments = returnedPage.getContent();
			assertEquals(4, apartments.size());
		}

		@Test
		@WithMockUser("test")
		void When_SearchAllApartmentsWithPagination_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
			searchFormObj.setPageNumber(0);
			searchFormObj.setPageSize(2);
			String resultString = mockMvc.perform(post("/api/apartments/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Apartment>> result = null;
			TypeReference<ApiResponse<Page<Apartment>>> typeReference = new TypeReference<ApiResponse<Page<Apartment>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Page<Apartment> returnedPage = result.getData();
			List<Apartment> apartments = returnedPage.getContent();
			assertEquals(2, apartments.size());
			assertEquals(2, returnedPage.getTotalPages());
			assertEquals(4, returnedPage.getTotalRows());
		}

		@Test
		@WithMockUser("test2")
		void When_SearchNoApartments_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
			searchFormObj.setPageNumber(-1);
			String resultString = mockMvc.perform(post("/api/apartments/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Apartment>> result = null;
			TypeReference<ApiResponse<Page<Apartment>>> typeReference = new TypeReference<ApiResponse<Page<Apartment>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Page<Apartment> returnedPage = result.getData();
			List<Apartment> apartments = returnedPage.getContent();
			assertEquals(0, apartments.size());
		}

		@Test
		@WithMockUser("test")
		void When_SearchApartmentsByState_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			// Search for READY apartments
			ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
			searchFormObj.setState(ApartmentStateEnum.READY);
			searchFormObj.setPageNumber(-1);
			String resultString = mockMvc.perform(post("/api/apartments/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Apartment>> result = null;
			TypeReference<ApiResponse<Page<Apartment>>> typeReference = new TypeReference<ApiResponse<Page<Apartment>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Page<Apartment> returnedPage = result.getData();
			List<Apartment> apartments = returnedPage.getContent();
			assertEquals(3, apartments.size());
			for (Apartment apartment : apartments) {
				assertEquals(ApartmentStateEnum.READY, apartment.getState());
			}
		}

		@Test
		@WithMockUser("test")
		void When_SearchApartmentsByName_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			// Search for apartments with name containing "loft"
			ApartmentSearchForm searchFormObj = new ApartmentSearchForm();
			searchFormObj.setName("loft");
			searchFormObj.setPageNumber(-1);
			String resultString = mockMvc.perform(post("/api/apartments/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Apartment>> result = null;
			TypeReference<ApiResponse<Page<Apartment>>> typeReference = new TypeReference<ApiResponse<Page<Apartment>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
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
		@WithMockUser("test")
		void When_DeleteApartment_Ok() throws Exception {
			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
					.delete("/api/apartment/" + alreadyCreatedApartmentId.toString()))
					.andExpect(status().isOk());
			boolean exists = apartmentRepository.existsById(alreadyCreatedApartmentId);
			assertTrue(!exists, "Apartment was not deleted");
		}

		@Test
		@WithMockUser("test2")
		void When_DeleteApartmentNotOwned_Forbidden() throws Exception {
			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
					.delete("/api/apartment/" + alreadyCreatedApartmentId.toString()))
					.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser("test")
		void When_DeleteApartmentNotExist_NotFound() throws Exception {
			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
					.delete("/api/apartment/" + NONEXISTENT_APARTMENT_ID))
					.andExpect(status().isNotFound());
		}
	}
}
