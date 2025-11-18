package com.viladevcorp.hosteo.apartment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.core.annotation.MergedAnnotations.Search;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.auth.AuthResultDto;
import com.viladevcorp.hosteo.auth.JwtUtils;
import com.viladevcorp.hosteo.forms.CreateApartmentForm;
import com.viladevcorp.hosteo.forms.LoginForm;
import com.viladevcorp.hosteo.forms.RegisterForm;
import com.viladevcorp.hosteo.forms.SearchApartmentForm;
import com.viladevcorp.hosteo.model.Address;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.UserSession;
import com.viladevcorp.hosteo.model.ValidationCode;
import com.viladevcorp.hosteo.model.dto.UserDto;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.repository.UserSessionRepository;
import com.viladevcorp.hosteo.repository.ValidationCodeRepository;
import com.viladevcorp.hosteo.service.AuthService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationCodeTypeEnum;

import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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

	private static final String APARTMENT_NAME_1 = "My Apartment 1";
	private static final String APARTMENT_AIRBNB_ID_1 = "airbnb-1";
	private static final String APARTMENT_BOOKING_ID_1 = "booking-1";
	private static final double APARTMENT_PRICE_1 = 100.0;
	private static final boolean APARTMENT_VISIBLE_1 = true;
	private static final String APARTMENT_STREET_1 = "123 Main St";
	private static final String APARTMENT_NUMBER_1 = "25";
	private static final String APARTMENT_APARTMENT_NUMBER_1 = "Apt 5";
	private static final String APARTMENT_CITY_1 = "Sample City";
	private static final String APARTMENT_COUNTRY_1 = "Sample Country";
	private static final String APARTMENT_ZIP_CODE_1 = "12345";

	private static final String APARTMENT_NAME_2 = "My Apartment 2";
	private static final String APARTMENT_AIRBNB_ID_2 = "airbnb-2";
	private static final String APARTMENT_BOOKING_ID_2 = "booking-2";
	private static final double APARTMENT_PRICE_2 = 200.0;
	private static final boolean APARTMENT_VISIBLE_2 = false;

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
		Apartment apartment = new Apartment();
		apartment.setName(ALREADY_CREATED_APARTMENT_NAME);
		apartment.setState(ApartmentState.READY);
		apartment.setCreatedBy(user1);
		apartment = apartmentRepository.save(apartment);
		alreadyCreatedApartmentId = apartment.getId();
		apartment = new Apartment();
		apartment.setName(ALREADY_CREATED_APARTMENT_NAME_2);
		apartment.setState(ApartmentState.READY);
		apartment.setCreatedBy(user1);
		apartment = apartmentRepository.save(apartment);
		apartment = new Apartment();
		apartment.setName(ALREADY_CREATED_APARTMENT_NAME_3);
		apartment.setState(ApartmentState.READY);
		apartment.setCreatedBy(user1);
		apartment = apartmentRepository.save(apartment);
		apartment = new Apartment();
		apartment.setName(ALREADY_CREATED_APARTMENT_NAME_4);
		apartment.setState(ApartmentState.OCCUPIED);
		apartment.setCreatedBy(user1);
		apartment = apartmentRepository.save(apartment);
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
			CreateApartmentForm form = new CreateApartmentForm();
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
			form.setPrice(APARTMENT_PRICE_1);
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
			assertEquals(APARTMENT_PRICE_1, returnedApartment.getPrice());
			assertEquals(APARTMENT_VISIBLE_1, returnedApartment.isVisible());
			assertEquals(ApartmentState.READY, returnedApartment.getState());
			assertEquals(ACTIVE_USER_USERNAME_1, returnedApartment.getCreatedBy().getUsername());
		}

		@Test
		@WithMockUser("test")
		void When_LeavingBlankName_BadRequest() throws Exception {
			CreateApartmentForm form = new CreateApartmentForm();
			// Name is not set
			form.setAirbnbId(APARTMENT_AIRBNB_ID_1);
			form.setBookingId(APARTMENT_BOOKING_ID_1);
			form.setPrice(APARTMENT_PRICE_1);
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
	@DisplayName("Search apartments")
	class SearchApartments {
		@Test
		@WithMockUser("test")
		void When_SearchAllApartments_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			SearchApartmentForm searchFormObj = new SearchApartmentForm();
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
			SearchApartmentForm searchFormObj = new SearchApartmentForm();
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
			SearchApartmentForm searchFormObj = new SearchApartmentForm();
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
			SearchApartmentForm searchFormObj = new SearchApartmentForm();
			searchFormObj.setState(ApartmentState.READY);
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
				assertEquals(ApartmentState.READY, apartment.getState());
			}
		}

		@Test
		@WithMockUser("test")
		void When_SearchApartmentsByName_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			// Search for apartments with name containing "loft"
			SearchApartmentForm searchFormObj = new SearchApartmentForm();
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

	// @Test
	// void When_RegisterAlreadyRegisterMail_Conflict() throws Exception {
	// RegisterForm form = new RegisterForm(ACTIVE_USER_EMAIL, OTHER_USER_USERNAME,
	// OTHER_USER_PASSWORD);
	// ObjectMapper obj = new ObjectMapper();

	// String resultString = mockMvc.perform(post("/api/public/register")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form))).andExpect(status().isConflict()).andReturn()
	// .getResponse().getContentAsString();

	// ApiResponse<User> result = null;
	// TypeReference<ApiResponse<User>> typeReference = new
	// TypeReference<ApiResponse<User>>() {
	// };

	// try {
	// result = obj.readValue(resultString, typeReference);
	// } catch (Exception e) {
	// assertTrue(false, "Error parsing response");
	// }

	// // We remove the quotes from the UUID (extra quotes being added)
	// String errorCode = result.getErrorCode();
	// assertEquals(CodeErrors.EMAIL_ALREADY_IN_USE, errorCode);

	// }

	// @Test
	// void When_RegisterAlreadyRegisterUsername_Conflict() throws Exception {
	// RegisterForm form = new RegisterForm(OTHER_USER_EMAIL, ACTIVE_USER_USERNAME,
	// OTHER_USER_PASSWORD);
	// ObjectMapper obj = new ObjectMapper();

	// String resultString = mockMvc.perform(post("/api/public/register")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form))).andExpect(status().isConflict()).andReturn()
	// .getResponse().getContentAsString();

	// ApiResponse<User> result = null;
	// TypeReference<ApiResponse<User>> typeReference = new
	// TypeReference<ApiResponse<User>>() {
	// };

	// try {
	// result = obj.readValue(resultString, typeReference);
	// } catch (Exception e) {
	// assertTrue(false, "Error parsing response");
	// }

	// // We remove the quotes from the UUID (extra quotes being added)
	// String errorCode = result.getErrorCode();
	// assertEquals(CodeErrors.USERNAME_ALREADY_IN_USE, errorCode);

	// }

	// @Test
	// void When_RegisterEmptyMandatoryFields_BadRequest() throws Exception {
	// RegisterForm form1 = new RegisterForm(null, OTHER_USER_USERNAME,
	// OTHER_USER_PASSWORD);
	// RegisterForm form2 = new RegisterForm(OTHER_USER_EMAIL, null,
	// OTHER_USER_PASSWORD);
	// RegisterForm form3 = new RegisterForm(OTHER_USER_EMAIL, OTHER_USER_USERNAME,
	// null);
	// ObjectMapper obj = new ObjectMapper();

	// mockMvc.perform(post("/api/public/register")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form1))).andExpect(status().isBadRequest());
	// mockMvc.perform(post("/api/public/register")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form2))).andExpect(status().isBadRequest());

	// mockMvc.perform(post("/api/public/register")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form3))).andExpect(status().isBadRequest());
	// }
	// }

	// @Nested
	// @DisplayName("User login")
	// class UserLogin {

	// @Test
	// void When_LoginEmptyFields_BadRequest() throws Exception {
	// LoginForm form1 = new LoginForm(null, ACTIVE_USER_PASSWORD, false);
	// LoginForm form2 = new LoginForm(ACTIVE_USER_USERNAME, null, false);
	// ObjectMapper obj = new ObjectMapper();

	// mockMvc.perform(post("/api/public/login")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form1))).andExpect(status().isBadRequest());
	// mockMvc.perform(post("/api/public/login")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form2))).andExpect(status().isBadRequest());
	// }

	// @Test
	// void When_LoginInvalidCredentials_Unauthorized() throws Exception {
	// LoginForm form = new LoginForm(ACTIVE_USER_USERNAME, OTHER_USER_PASSWORD,
	// false);
	// ObjectMapper obj = new ObjectMapper();

	// mockMvc.perform(post("/api/public/login")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form))).andExpect(status().isUnauthorized());

	// }

	// @Test
	// void When_LoginNotActivatedAccount_Forbidden() throws Exception {
	// LoginForm form = new LoginForm(INACTIVE_USER_USERNAME,
	// INACTIVE_USER_PASSWORD, false);
	// ObjectMapper obj = new ObjectMapper();

	// mockMvc.perform(post("/api/public/login")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form))).andExpect(status().isForbidden());
	// }

	// @Test
	// void When_LoginSuccesful_Ok() throws Exception {
	// LoginForm form = new LoginForm(ACTIVE_USER_USERNAME, ACTIVE_USER_PASSWORD,
	// false);
	// ObjectMapper obj = new ObjectMapper();

	// String resultString = mockMvc.perform(post("/api/public/login")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form))).andExpect(status().isOk())
	// .andExpect(cookie().exists("REFRESH_TOKEN")).andReturn().getResponse().getContentAsString();

	// TypeReference<ApiResponse<AuthResultDto>> typeReference = new
	// TypeReference<ApiResponse<AuthResultDto>>() {
	// };
	// ApiResponse<AuthResultDto> result = null;
	// try {
	// result = obj.readValue(resultString, typeReference);
	// UUID sessionId = result.getData().getSessionId();
	// String authToken = result.getData().getAuthToken();
	// Authentication auth = jwtUtils.validateToken(authToken);
	// assertEquals(ACTIVE_USER_USERNAME, auth.getName());
	// assertEquals(sessionId.toString(),
	// jwtUtils.extractClaims(authToken).get("sessionId", String.class));
	// } catch (Exception e) {
	// assertTrue(false, "Error parsing response");
	// }
	// }
	// }

	// @Test
	// void When_RefreshSuccesful_Ok() throws Exception {
	// LoginForm form = new LoginForm(ACTIVE_USER_USERNAME, ACTIVE_USER_PASSWORD,
	// false);
	// ObjectMapper obj = new ObjectMapper();

	// MockHttpServletResponse response = mockMvc.perform(post("/api/public/login")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(form))).andReturn().getResponse();
	// String resultLoginString = response.getContentAsString();
	// Cookie refreshTokenCookie = response.getCookie("REFRESH_TOKEN");

	// TypeReference<ApiResponse<AuthResultDto>> typeReference = new
	// TypeReference<ApiResponse<AuthResultDto>>() {
	// };
	// ApiResponse<AuthResultDto> resultLogin = null;
	// resultLogin = obj.readValue(resultLoginString, typeReference);
	// UUID sessionId = resultLogin.getData().getSessionId();

	// String resultString =
	// mockMvc.perform(post("/api/public/refresh-token").cookie(refreshTokenCookie))
	// .andExpect(status().isOk())
	// .andReturn().getResponse().getContentAsString();

	// UserSession oldSession =
	// userSessionRepository.findById(sessionId).orElse(null);
	// assertNotNull(oldSession);
	// assertNotNull(oldSession.getDeletedAt());
	// ApiResponse<AuthResultDto> result = null;
	// try {
	// result = obj.readValue(resultString, typeReference);
	// UUID sessionId2 = result.getData().getSessionId();
	// String authToken = result.getData().getAuthToken();
	// Authentication auth = jwtUtils.validateToken(authToken);
	// assertEquals(ACTIVE_USER_USERNAME, auth.getName());
	// assertEquals(sessionId2.toString(),
	// jwtUtils.extractClaims(authToken).get("sessionId", String.class));
	// } catch (Exception e) {
	// assertTrue(false, "Error parsing response");
	// }
	// }

	// @Nested
	// @DisplayName("Account validation")
	// class AccountValidation {

	// @Test
	// void When_AccountValidationWrongCode_Unauthorized() throws Exception {
	// ValidationCode validationCode = validationCodeRepository
	// .findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
	// ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
	// .get(0);
	// mockMvc.perform(
	// post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" +
	// validationCode.getCode() + "1"))
	// .andExpect(status().isUnauthorized());
	// }

	// @Test
	// void When_AccountValidationExpiredCode_Gone() throws Exception {
	// List<ValidationCode> validationCodeList = validationCodeRepository
	// .findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
	// ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType());
	// // We take the first validation code (more recent) and we delete the rest to
	// // avoid conflicts with other tests
	// ValidationCode validationCode = validationCodeList.get(0);
	// List<ValidationCode> validationCodeToDelete = validationCodeList.subList(1,
	// validationCodeList.size());
	// validationCodeToDelete.forEach(vc -> validationCodeRepository.delete(vc));
	// Calendar oldCreationDate = (Calendar) validationCode.getCreatedAt().clone();
	// Calendar expiredDate = (Calendar) oldCreationDate.clone();
	// expiredDate.add(Calendar.MINUTE, -ValidationCode.EXPIRATION_MINUTES - 1);
	// validationCode.setCreatedAt(expiredDate);
	// validationCodeRepository.save(validationCode);
	// mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" +
	// validationCode.getCode()))
	// .andExpect(status().isGone());
	// validationCode.setCreatedAt(oldCreationDate);
	// validationCodeRepository.save(validationCode);

	// }

	// @Test
	// void When_AccountValidationAlreadyUsedCode_Conflict() throws Exception {
	// ValidationCode validationCode = validationCodeRepository
	// .findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
	// ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
	// .get(0);
	// validationCode.setUsed(true);
	// validationCodeRepository.save(validationCode);
	// mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" +
	// validationCode.getCode()))
	// .andExpect(status().isConflict());
	// validationCode.setUsed(false);
	// validationCodeRepository.save(validationCode);
	// }

	// @Test
	// void When_AccountValidationSuccesful_Ok() throws Exception {
	// ValidationCode validationCode = validationCodeRepository
	// .findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
	// ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
	// .get(0);
	// mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" +
	// validationCode.getCode()))
	// .andExpect(status().isOk());
	// User user = userRepository.findByUsername(INACTIVE_USER_USERNAME);
	// assertTrue(user.isValidated());
	// validationCode = validationCodeRepository
	// .findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
	// ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
	// .get(0);
	// assertTrue(validationCode.isUsed());
	// user.setValidated(false);
	// userRepository.save(user);
	// validationCode.setUsed(false);
	// validationCodeRepository.save(validationCode);
	// }

	// @Test
	// void When_AccountValidationResendCode_Ok() throws Exception {
	// ValidationCode oldValidationCode = validationCodeRepository
	// .findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
	// ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
	// .get(0);
	// mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME +
	// "/resend"))
	// .andExpect(status().isOk());
	// ValidationCode newValidationCode = validationCodeRepository
	// .findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
	// ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
	// .get(0);
	// assertNotEquals(oldValidationCode.getCode(), newValidationCode.getCode());
	// }

	// }

	// @Test
	// void When_ResetPassword_Ok() throws Exception {
	// mockMvc.perform(post("/api/public/forgotten-password/" +
	// ACTIVE_USER_USERNAME))
	// .andExpect(status().isOk());

	// ValidationCode validationCode =
	// validationCodeRepository.findByUserUsernameAndTypeOrderByCreatedAtDesc(
	// ACTIVE_USER_USERNAME,
	// ValidationCodeTypeEnum.RESET_PASSWORD.getType()).get(0);

	// mockMvc.perform(post("/api/public/reset-password/" + ACTIVE_USER_USERNAME +
	// "/" + validationCode.getCode())
	// .content(OTHER_USER_PASSWORD)).andExpect(status().isOk());

	// LoginForm formOldPass = new LoginForm(ACTIVE_USER_USERNAME,
	// ACTIVE_USER_PASSWORD, false);
	// LoginForm formNewPass = new LoginForm(ACTIVE_USER_USERNAME,
	// OTHER_USER_PASSWORD, false);
	// ObjectMapper obj = new ObjectMapper();

	// mockMvc.perform(post("/api/public/login")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(formOldPass))).andExpect(status().isUnauthorized());
	// mockMvc.perform(post("/api/public/login")
	// .contentType("application/json")
	// .content(obj.writeValueAsString(formNewPass))).andExpect(status().isOk());

	// User user = userRepository.findByUsername(ACTIVE_USER_USERNAME);
	// user.setPassword(new BCryptPasswordEncoder().encode(ACTIVE_USER_PASSWORD));
	// userRepository.save(user);
	// }

	// @WithMockUser("test")
	// @Test
	// void When_Self_Ok() throws Exception {

	// String resultString =
	// mockMvc.perform(get("/api/self")).andExpect(status().isOk())
	// .andReturn()
	// .getResponse().getContentAsString();

	// ObjectMapper obj = new ObjectMapper();
	// ApiResponse<UserDto> result = null;
	// TypeReference<ApiResponse<UserDto>> typeReference = new
	// TypeReference<ApiResponse<UserDto>>() {
	// };

	// try {
	// result = obj.readValue(resultString, typeReference);
	// } catch (Exception e) {
	// assertTrue(false, "Error parsing response");
	// }
	// UserDto user = result.getData();
	// assertEquals(ACTIVE_USER_EMAIL, user.getEmail());
	// }

}
