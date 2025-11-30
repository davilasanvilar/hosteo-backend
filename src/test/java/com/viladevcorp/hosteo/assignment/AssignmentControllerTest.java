package com.viladevcorp.hosteo.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.time.Instant;
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
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.forms.AssignmentCreateForm;
import com.viladevcorp.hosteo.model.forms.AssignmentUpdateForm;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.CategoryEnum;
import com.viladevcorp.hosteo.model.types.Language;
import com.viladevcorp.hosteo.model.types.TaskState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.repository.WorkerRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;

import static com.viladevcorp.hosteo.common.TestConstants.*;

class AssignmentControllerTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        testSetupHelper.resetAssignments();
    }

    @Nested
    @DisplayName("Create assignments")
    class CreateAssignments {

        @Test
        void When_CreateAssignment_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentCreateForm form = new AssignmentCreateForm();
            form.setTaskId(testSetupHelper.getTestTasks().get(0).getId());
            form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(0).getId());
            form.setState(NEW_ASSIGNMENT_STATE);

            String resultString = mockMvc.perform(post("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            TypeReference<ApiResponse<Assignment>> typeReference = new TypeReference<ApiResponse<Assignment>>() {
            };
            ApiResponse<Assignment> result = objectMapper.readValue(resultString, typeReference);
            Assignment createdAssignment = assignmentRepository.findById(result.getData().getId()).orElse(null);
            assertNotNull(createdAssignment);
            assertEquals(testSetupHelper.getTestTasks().get(0).getId(), createdAssignment.getTask().getId());
            assertEquals(testSetupHelper.getTestWorkers().get(0).getId(), createdAssignment.getWorker().getId());
            assertEquals(NEW_ASSIGNMENT_STATE, createdAssignment.getState());
        }

        @Test
        void When_CreateAssignment_MissingTaskId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentCreateForm form = new AssignmentCreateForm();
            form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(0).getId());
            form.setState(NEW_ASSIGNMENT_STATE);

            mockMvc.perform(post("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateAssignment_MissingStartDate_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            AssignmentCreateForm form = new AssignmentCreateForm();
            form.setTaskId(testSetupHelper.getTestTasks().get(0).getId());
            form.setWorkerId(testSetupHelper.getTestWorkers().get(0).getId());
            form.setState(NEW_ASSIGNMENT_STATE);

            mockMvc.perform(post("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateAssignment_MissingWorkerId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentCreateForm form = new AssignmentCreateForm();
            form.setTaskId(testSetupHelper.getTestTasks().get(0).getId());
            form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
            form.setState(NEW_ASSIGNMENT_STATE);

            mockMvc.perform(post("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateAssignment_MissingState_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentCreateForm form = new AssignmentCreateForm();
            form.setTaskId(testSetupHelper.getTestTasks().get(0).getId());
            form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(0).getId());

            mockMvc.perform(post("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateAssignment_NonExistentTask_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentCreateForm form = new AssignmentCreateForm();
            form.setTaskId(UUID.randomUUID());
            form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(0).getId());
            form.setState(NEW_ASSIGNMENT_STATE);

            mockMvc.perform(post("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_CreateAssignment_NonExistentWorker_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentCreateForm form = new AssignmentCreateForm();
            form.setTaskId(testSetupHelper.getTestTasks().get(0).getId());
            form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
            form.setWorkerId(UUID.randomUUID());
            form.setState(NEW_ASSIGNMENT_STATE);

            mockMvc.perform(post("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Update assignments")
    class UpdateAssignments {

        @Test
        void When_UpdateAssignment_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(testSetupHelper.getTestAssignments().get(0).getId());
            form.setTaskId(testSetupHelper.getTestTasks().get(1).getId());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isOk());

            Assignment updated = assignmentRepository.findById(testSetupHelper.getTestAssignments().get(0).getId())
                    .get();
            assertEquals(UPDATED_ASSIGNMENT_STATE, updated.getState());
            assertEquals(testSetupHelper.getTestTasks().get(1).getId(), updated.getTask().getId());
            assertEquals(testSetupHelper.getTestWorkers().get(1).getId(), updated.getWorker().getId());
            assertEquals(0,
                    TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE).compareTo(updated.getStartDate()));
        }

        @Test
        void When_UpdateAssignment_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(UUID.randomUUID());
            form.setTaskId(testSetupHelper.getTestTasks().get(1).getId());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_UpdateAssignment_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(testSetupHelper.getTestAssignments().get(0).getId());
            form.setTaskId(testSetupHelper.getTestTasks().get(1).getId());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void When_UpdateAssignment_MissingId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setTaskId(testSetupHelper.getTestTasks().get(1).getId());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateAssignment_MissingTaskId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(testSetupHelper.getTestAssignments().get(0).getId());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateAssignment_MissingStartDate_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(testSetupHelper.getTestAssignments().get(0).getId());
            form.setTaskId(testSetupHelper.getTestTasks().get(1).getId());
            form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateAssignment_MissingWorkerId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(testSetupHelper.getTestAssignments().get(0).getId());
            form.setTaskId(testSetupHelper.getTestTasks().get(1).getId());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateAssignment_MissingState_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(testSetupHelper.getTestAssignments().get(0).getId());
            form.setTaskId(testSetupHelper.getTestTasks().get(1).getId());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateAssignment_NonExistentTask_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(testSetupHelper.getTestAssignments().get(0).getId());
            form.setTaskId(UUID.randomUUID());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_UpdateAssignment_NonExistentWorker_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            AssignmentUpdateForm form = new AssignmentUpdateForm();
            form.setId(testSetupHelper.getTestAssignments().get(0).getId());
            form.setTaskId(testSetupHelper.getTestTasks().get(1).getId());
            form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
            form.setWorkerId(UUID.randomUUID());
            form.setState(UPDATED_ASSIGNMENT_STATE);

            mockMvc.perform(patch("/api/assignment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get assignment")
    class GetAssignment {

        @Test
        void When_GetAssignment_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            String result = mockMvc
                    .perform(get("/api/assignment/" + testSetupHelper.getTestAssignments().get(0).getId())
                            .contentType("application/json"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            TypeReference<ApiResponse<Assignment>> typeReference = new TypeReference<ApiResponse<Assignment>>() {
            };
            ApiResponse<Assignment> apiResponse = objectMapper.readValue(result, typeReference);
            Assignment fetchedAssignment = apiResponse.getData();
            assertNotNull(fetchedAssignment);

            assertEquals(CREATED_ASSIGNMENT_STATE_1, fetchedAssignment.getState());
        }

        @Test
        void When_GetAssignment_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(get("/api/assignment/" + UUID.randomUUID())
                    .contentType("application/json"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_GetAssignment_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            mockMvc.perform(get("/api/assignment/" + testSetupHelper.getTestAssignments().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Delete assignment")
    class DeleteAssignment {

        @Test
        void When_DeleteAssignment_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(delete("/api/assignment/" + testSetupHelper.getTestAssignments().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isOk());
            boolean exists = assignmentRepository.existsById(testSetupHelper.getTestAssignments().get(0).getId());
            assertEquals(false, exists);
        }

        @Test
        void When_DeleteAssignment_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(delete("/api/assignment/" + UUID.randomUUID())
                    .contentType("application/json"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_DeleteAssignment_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            mockMvc.perform(delete("/api/assignment/" + testSetupHelper.getTestAssignments().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isForbidden());
        }
    }
}
