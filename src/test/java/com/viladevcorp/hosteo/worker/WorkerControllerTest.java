package com.viladevcorp.hosteo.worker;

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
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.forms.WorkerCreateForm;
import com.viladevcorp.hosteo.model.forms.WorkerSearchForm;
import com.viladevcorp.hosteo.model.forms.WorkerUpdateForm;
import com.viladevcorp.hosteo.model.types.Language;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.repository.WorkerRepository;
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
class WorkerControllerTest {

	private static final String ACTIVE_USER_EMAIL_1 = "test@gmail.com";
	private static final String ACTIVE_USER_USERNAME_1 = "test";
	private static final String ACTIVE_USER_PASSWORD_1 = "12test34";

	private static final String ACTIVE_USER_EMAIL_2 = "test2@gmail.com";
	private static final String ACTIVE_USER_USERNAME_2 = "test2";
	private static final String ACTIVE_USER_PASSWORD_2 = "12test34";

	private static UUID alreadyCreatedWorkerId;
	private static final String ALREADY_CREATED_WORKER_NAME = "John 1";
	private static final String ALREADY_CREATED_WORKER_NAME_2 = "John 2";
	private static final String ALREADY_CREATED_WORKER_NAME_3 = "Peter 3";
	private static final String ALREADY_CREATED_WORKER_NAME_4 = "Peter 4";
	private static final String NONEXISTENT_WORKER_ID = UUID.randomUUID().toString();

	private static final String UPDATED_NAME = "Updated worker name";
	private static final Language UPDATED_LANGUAGE = Language.FR;
	private static final boolean UPDATED_VISIBLE = false;

	private static final String WORKER_NAME_1 = "Created worker";
	private static final Language WORKER_LANGUAGE_1 = Language.UK;
	private static final boolean WORKER_VISIBLE_1 = true;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private WorkerRepository workerRepository;
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
		Worker worker = Worker.builder().name(ALREADY_CREATED_WORKER_NAME).language(Language.EN)
				.createdBy(user1).build();
		worker = workerRepository.save(worker);
		alreadyCreatedWorkerId = worker.getId();
		worker = Worker.builder().name(ALREADY_CREATED_WORKER_NAME_2).language(Language.EN)
				.createdBy(user1).build();
		workerRepository.save(worker);
		worker = Worker.builder().name(ALREADY_CREATED_WORKER_NAME_3).language(Language.EN)
				.createdBy(user1).build();
		workerRepository.save(worker);
		worker = Worker.builder().name(ALREADY_CREATED_WORKER_NAME_4).language(Language.EN)
				.createdBy(user1).build();
		workerRepository.save(worker);
	}

	@AfterEach
	void clean() {
		workerRepository.deleteAll();
	}

	@AfterAll
	void globalClean() {
		userRepository.deleteAll();
	}

	@Nested
	@DisplayName("Create workers")
	class CreateWorkers {
		@Test
		@WithMockUser("test")
		void When_CreateWorker_Ok() throws Exception {
			WorkerCreateForm form = new WorkerCreateForm();

			form.setName(WORKER_NAME_1);
			form.setLanguage(WORKER_LANGUAGE_1);
			form.setVisible(WORKER_VISIBLE_1);
			ObjectMapper obj = new ObjectMapper();
			String resultString = mockMvc.perform(post("/api/worker")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Worker> result = null;
			TypeReference<ApiResponse<Worker>> typeReference = new TypeReference<ApiResponse<Worker>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Worker returnedWorker = result.getData();
			assertEquals(WORKER_NAME_1, returnedWorker.getName());
			assertEquals(WORKER_LANGUAGE_1, returnedWorker.getLanguage());
			assertEquals(WORKER_VISIBLE_1, returnedWorker.isVisible());
			assertEquals(ACTIVE_USER_USERNAME_1, returnedWorker.getCreatedBy().getUsername());
		}

		@Test
		@WithMockUser("test")
		void When_LeavingBlankName_BadRequest() throws Exception {
			WorkerCreateForm form = new WorkerCreateForm();
			// Name is not set
			form.setVisible(WORKER_VISIBLE_1);
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(post("/api/worker")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("Get worker")
	class GetWorker {
		@Test
		@WithMockUser("test")
		void When_GetWorker_Ok() throws Exception {
			String resultString = mockMvc.perform(get("/api/worker/" + alreadyCreatedWorkerId.toString()))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			ApiResponse<Worker> result = null;
			TypeReference<ApiResponse<Worker>> typeReference = new TypeReference<ApiResponse<Worker>>() {
			};
			ObjectMapper obj = new ObjectMapper();
			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Worker returnedWorker = result.getData();
			assertEquals(ALREADY_CREATED_WORKER_NAME, returnedWorker.getName());
		}

		@Test
		@WithMockUser("test2")
		void When_GetWorkerNotOwned_Forbidden() throws Exception {
			mockMvc.perform(get("/api/worker/" + alreadyCreatedWorkerId.toString()))
					.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser("test")
		void When_GetWorkerNotExist_NotFound() throws Exception {
			mockMvc.perform(get("/api/worker/" + NONEXISTENT_WORKER_ID))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("Update workers")
	class UpdateWorkers {
		@Test
		@WithMockUser("test")
		void When_UpdateWorker_Ok() throws Exception {
			WorkerUpdateForm form = new WorkerUpdateForm();
			Worker workerToUpdate = workerRepository.findById(alreadyCreatedWorkerId).orElse(null);
			BeanUtils.copyProperties(workerToUpdate, form);
			form.setName(UPDATED_NAME);
			form.setLanguage(UPDATED_LANGUAGE);
			form.setVisible(UPDATED_VISIBLE);
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(patch("/api/worker")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isOk());
			Worker workerUpdated = workerRepository.findById(alreadyCreatedWorkerId).orElse(null);
			assertEquals(UPDATED_NAME, workerUpdated.getName());
			assertEquals(UPDATED_LANGUAGE, workerUpdated.getLanguage());
			assertEquals(UPDATED_VISIBLE, workerUpdated.isVisible());
		}

		@Test
		@WithMockUser("test2")
		void When_UpdateWorkerNotOwned_Forbidden() throws Exception {
			WorkerUpdateForm form = new WorkerUpdateForm();
			Worker workerToUpdate = workerRepository.findById(alreadyCreatedWorkerId).orElse(null);
			BeanUtils.copyProperties(workerToUpdate, form);
			form.setName(UPDATED_NAME);
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(patch("/api/worker")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser("test")
		void When_UpdateWorkerNotExist_NotFound() throws Exception {
			WorkerUpdateForm form = new WorkerUpdateForm();
			form.setId(UUID.fromString(NONEXISTENT_WORKER_ID));
			form.setName(UPDATED_NAME);
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(patch("/api/worker")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isNotFound());
		}

		@Test
		@WithMockUser("test")
		void When_UpdateNameIsEmptyInForm_BadRequest() throws Exception {
			WorkerUpdateForm form = new WorkerUpdateForm();
			Worker workerToUpdate = workerRepository.findById(alreadyCreatedWorkerId).orElse(null);
			BeanUtils.copyProperties(workerToUpdate, form);
			form.setName("");
			ObjectMapper obj = new ObjectMapper();
			mockMvc.perform(patch("/api/worker")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("Search workers")
	class SearchWorkers {
		@Test
		@WithMockUser("test")
		void When_SearchAllWorkers_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			WorkerSearchForm searchFormObj = new WorkerSearchForm();
			searchFormObj.setPageNumber(0);
			String resultString = mockMvc.perform(post("/api/workers/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Worker>> result = null;
			TypeReference<ApiResponse<Page<Worker>>> typeReference = new TypeReference<ApiResponse<Page<Worker>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Page<Worker> returnedPage = result.getData();
			List<Worker> workers = returnedPage.getContent();
			assertEquals(4, workers.size());
		}

		@Test
		@WithMockUser("test")
		void When_SearchAllWorkersWithPagination_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			WorkerSearchForm searchFormObj = new WorkerSearchForm();
			searchFormObj.setPageNumber(0);
			searchFormObj.setPageSize(2);
			String resultString = mockMvc.perform(post("/api/workers/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Worker>> result = null;
			TypeReference<ApiResponse<Page<Worker>>> typeReference = new TypeReference<ApiResponse<Page<Worker>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Page<Worker> returnedPage = result.getData();
			List<Worker> workers = returnedPage.getContent();
			assertEquals(2, workers.size());
			assertEquals(2, returnedPage.getTotalPages());
			assertEquals(4, returnedPage.getTotalRows());
		}

		@Test
		@WithMockUser("test2")
		void When_SearchNoWorkers_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			WorkerSearchForm searchFormObj = new WorkerSearchForm();
			searchFormObj.setPageNumber(-1);
			String resultString = mockMvc.perform(post("/api/workers/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Worker>> result = null;
			TypeReference<ApiResponse<Page<Worker>>> typeReference = new TypeReference<ApiResponse<Page<Worker>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Page<Worker> returnedPage = result.getData();
			List<Worker> workers = returnedPage.getContent();
			assertEquals(0, workers.size());
		}

		@Test
		@WithMockUser("test")
		void When_SearchWorkersByName_Ok() throws Exception {
			ObjectMapper obj = new ObjectMapper();
			// Search for workers with name containing "john"
			WorkerSearchForm searchFormObj = new WorkerSearchForm();
			searchFormObj.setName("john");
			searchFormObj.setPageNumber(-1);
			String resultString = mockMvc.perform(post("/api/workers/search")
					.contentType("application/json")
					.content(obj.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<Page<Worker>> result = null;
			TypeReference<ApiResponse<Page<Worker>>> typeReference = new TypeReference<ApiResponse<Page<Worker>>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
			Page<Worker> returnedPage = result.getData();
			List<Worker> workers = returnedPage.getContent();
			assertEquals(2, workers.size());
			for (Worker worker : workers) {
				assertTrue(worker.getName().toLowerCase().contains("john"));
			}
		}
	}

	@Nested
	@DisplayName("Delete workers")
	class DeleteWorkers {
		@Test
		@WithMockUser("test")
		void When_DeleteWorker_Ok() throws Exception {
			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
					.delete("/api/worker/" + alreadyCreatedWorkerId.toString()))
					.andExpect(status().isOk());
			boolean exists = workerRepository.existsById(alreadyCreatedWorkerId);
			assertTrue(!exists, "Worker was not deleted");
		}

		@Test
		@WithMockUser("test2")
		void When_DeleteWorkerNotOwned_Forbidden() throws Exception {
			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
					.delete("/api/worker/" + alreadyCreatedWorkerId.toString()))
					.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser("test")
		void When_DeleteWorkerNotExist_NotFound() throws Exception {
			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
					.delete("/api/worker/" + NONEXISTENT_WORKER_ID))
					.andExpect(status().isNotFound());
		}
	}
}
