package com.viladevcorp.hosteo.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

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
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.forms.TaskCreateForm;
import com.viladevcorp.hosteo.model.forms.TaskSearchForm;
import com.viladevcorp.hosteo.model.forms.TaskUpdateForm;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;

import static com.viladevcorp.hosteo.common.TestConstants.*;

class TaskControllerTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    TestSetupHelper testSetupHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        testSetupHelper.resetTestTasks();
    }

    @Nested
    @DisplayName("Create tasks")
    class CreateTasks {

        @Test
        void When_CreateTask_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskCreateForm form = new TaskCreateForm();
            form.setName(NEW_TASK_NAME_1);
            form.setCategory(NEW_TASK_CATEGORY_1);
            form.setDuration(NEW_TASK_DURATION_1);
            form.setPrepTask(NEW_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(NEW_TASK_STEPS_1);

            
            String resultString = mockMvc.perform(post("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            TypeReference<ApiResponse<Task>> typeReference = new TypeReference<ApiResponse<Task>>() {
            };
            ApiResponse<Task> result = objectMapper.readValue(resultString, typeReference);
            Task createdTask = taskRepository.findById(result.getData().getId()).orElse(null);
            assertNotNull(createdTask);
            assertEquals(NEW_TASK_NAME_1, createdTask.getName());
            assertEquals(NEW_TASK_CATEGORY_1, createdTask.getCategory());
            assertEquals(NEW_TASK_DURATION_1, createdTask.getDuration());
            assertEquals(NEW_TASK_PREP_TASK_1, createdTask.isPrepTask());
            assertEquals(NEW_TASK_STEPS_1.toString(), createdTask.getSteps().toString());
            assertEquals(testSetupHelper.getTestApartments().get(0).getId(), createdTask.getApartment().getId());
        }

        @Test
        void When_CreateTask_MissingName_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskCreateForm form = new TaskCreateForm();
            form.setCategory(NEW_TASK_CATEGORY_1);
            form.setDuration(NEW_TASK_DURATION_1);
            form.setPrepTask(NEW_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(NEW_TASK_STEPS_1);

            mockMvc.perform(post("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateTask_MissingCategory_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskCreateForm form = new TaskCreateForm();
            form.setName(NEW_TASK_NAME_1);
            form.setDuration(NEW_TASK_DURATION_1);
            form.setPrepTask(NEW_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(NEW_TASK_STEPS_1);

            mockMvc.perform(post("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateTask_NegativeDuration_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskCreateForm form = new TaskCreateForm();
            form.setName(NEW_TASK_NAME_1);
            form.setCategory(NEW_TASK_CATEGORY_1);
            form.setDuration(-10);
            form.setPrepTask(NEW_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(NEW_TASK_STEPS_1);

            mockMvc.perform(post("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateTask_MissingApartmentId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskCreateForm form = new TaskCreateForm();
            form.setName(NEW_TASK_NAME_1);
            form.setCategory(NEW_TASK_CATEGORY_1);
            form.setDuration(NEW_TASK_DURATION_1);
            form.setPrepTask(NEW_TASK_PREP_TASK_1);
            form.setSteps(NEW_TASK_STEPS_1);

            mockMvc.perform(post("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateTask_NonExistentApartment_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskCreateForm form = new TaskCreateForm();
            form.setName(NEW_TASK_NAME_1);
            form.setCategory(NEW_TASK_CATEGORY_1);
            form.setDuration(NEW_TASK_DURATION_1);
            form.setPrepTask(NEW_TASK_PREP_TASK_1);
            form.setApartmentId(UUID.randomUUID());
            form.setSteps(NEW_TASK_STEPS_1);

            mockMvc.perform(post("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Update tasks")
    class UpdateTasks {

        @Test
        void When_UpdateTask_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setId(testSetupHelper.getTestTasks().get(0).getId());
            form.setName(UPDATED_TASK_NAME_1);
            form.setCategory(UPDATED_TASK_CATEGORY_1);
            form.setDuration(UPDATED_TASK_DURATION_1);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(1).getId());
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isOk());

            Task updated = taskRepository.findById(testSetupHelper.getTestTasks().get(0).getId()).get();
            assertEquals(UPDATED_TASK_NAME_1, updated.getName());
            assertEquals(UPDATED_TASK_CATEGORY_1, updated.getCategory());
            assertEquals(UPDATED_TASK_DURATION_1, updated.getDuration());
            assertEquals(UPDATED_TASK_PREP_TASK_1, updated.isPrepTask());
            assertEquals(UPDATED_TASK_STEPS_1.toString(), updated.getSteps().toString());
            assertEquals(testSetupHelper.getTestApartments().get(1).getId(), updated.getApartment().getId());
        }

        @Test
        void When_UpdateTask_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setId(UUID.randomUUID());
            form.setName(UPDATED_TASK_NAME_1);
            form.setCategory(UPDATED_TASK_CATEGORY_1);
            form.setDuration(UPDATED_TASK_DURATION_1);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_UpdateTask_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setId(testSetupHelper.getTestTasks().get(0).getId());
            form.setName(UPDATED_TASK_NAME_1);
            form.setCategory(UPDATED_TASK_CATEGORY_1);
            form.setDuration(UPDATED_TASK_DURATION_1);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void When_UpdateTask_MissingName_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setId(testSetupHelper.getTestTasks().get(0).getId());
            form.setCategory(UPDATED_TASK_CATEGORY_1);
            form.setDuration(UPDATED_TASK_DURATION_1);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateTask_MissingCategory_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setId(testSetupHelper.getTestTasks().get(0).getId());
            form.setName(UPDATED_TASK_NAME_1);
            form.setDuration(UPDATED_TASK_DURATION_1);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateTask_NegativeDuration_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setId(testSetupHelper.getTestTasks().get(0).getId());
            form.setName(UPDATED_TASK_NAME_1);
            form.setCategory(UPDATED_TASK_CATEGORY_1);
            form.setDuration(-50);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateTask_MissingId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setName(UPDATED_TASK_NAME_1);
            form.setCategory(UPDATED_TASK_CATEGORY_1);
            form.setDuration(UPDATED_TASK_DURATION_1);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateTask_MissingApartmentId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setId(testSetupHelper.getTestTasks().get(0).getId());
            form.setName(UPDATED_TASK_NAME_1);
            form.setCategory(UPDATED_TASK_CATEGORY_1);
            form.setDuration(UPDATED_TASK_DURATION_1);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateTask_NonExistentApartment_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TaskUpdateForm form = new TaskUpdateForm();
            form.setId(testSetupHelper.getTestTasks().get(0).getId());
            form.setName(UPDATED_TASK_NAME_1);
            form.setCategory(UPDATED_TASK_CATEGORY_1);
            form.setDuration(UPDATED_TASK_DURATION_1);
            form.setPrepTask(UPDATED_TASK_PREP_TASK_1);
            form.setApartmentId(UUID.randomUUID());
            form.setSteps(UPDATED_TASK_STEPS_1);
            mockMvc.perform(patch("/api/task")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get task")
    class GetTask {

        @Test
        void When_GetTask_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            
            String result = mockMvc.perform(get("/api/task/" + testSetupHelper.getTestTasks().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            TypeReference<ApiResponse<Task>> typeReference = new TypeReference<ApiResponse<Task>>() {
            };
            ApiResponse<Task> apiResponse = objectMapper.readValue(result, typeReference);
            Task fetchedTask = apiResponse.getData();
            assertNotNull(fetchedTask);
            assertEquals(CREATED_TASK_NAME_1, fetchedTask.getName());
        }

        @Test
        void When_GetTask_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(get("/api/task/" + UUID.randomUUID())
                    .contentType("application/json"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_GetTask_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            mockMvc.perform(get("/api/task/" + testSetupHelper.getTestTasks().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isForbidden());
        }
    }

     @Nested
    @DisplayName("Search tasks")
    class SearchTasks {
        @Test
        void When_SearchAllTasks_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            TaskSearchForm searchFormObj = new TaskSearchForm();
            searchFormObj.setPageSize(0);
            String resultString = mockMvc.perform(post("/api/tasks/search")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Task>> result = null;
            TypeReference<ApiResponse<Page<Task>>> typeReference = new TypeReference<ApiResponse<Page<Task>>>() {
            };

            try {
                result = objectMapper.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Task> returnedPage = result.getData();
            List<Task> tasks = returnedPage.getContent();
            assertEquals(5, tasks.size());
        }

        @Test
        void When_SearchAllTasksWithPagination_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            TaskSearchForm searchFormObj = new TaskSearchForm();
            searchFormObj.setPageNumber(0);
            searchFormObj.setPageSize(2);
            String resultString = mockMvc.perform(post("/api/tasks/search")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Task>> result = null;
            TypeReference<ApiResponse<Page<Task>>> typeReference = new TypeReference<ApiResponse<Page<Task>>>() {
            };

            try {
                result = objectMapper.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Task> returnedPage = result.getData();
            List<Task> tasks = returnedPage.getContent();
            assertEquals(2, tasks.size());
            assertEquals(3, returnedPage.getTotalPages());
            assertEquals(5, returnedPage.getTotalRows());
        }

        @Test
        void When_SearchNoTasks_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

            TaskSearchForm searchFormObj = new TaskSearchForm();
            searchFormObj.setPageNumber(-1);
            String resultString = mockMvc.perform(post("/api/tasks/search")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Task>> result = null;
            TypeReference<ApiResponse<Page<Task>>> typeReference = new TypeReference<ApiResponse<Page<Task>>>() {
            };

            try {
                result = objectMapper.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Task> returnedPage = result.getData();
            List<Task> tasks = returnedPage.getContent();
            assertEquals(0, tasks.size());
        }

        @Test
        void When_SearchTasksByName_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            // Search for templates with name containing "maintenance"
            TaskSearchForm searchFormObj = new TaskSearchForm();
            searchFormObj.setName("maintenance");
            searchFormObj.setPageNumber(-1);
            String resultString = mockMvc.perform(post("/api/tasks/search")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(searchFormObj))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            ApiResponse<Page<Task>> result = null;
            TypeReference<ApiResponse<Page<Task>>> typeReference = new TypeReference<ApiResponse<Page<Task>>>() {
            };

            try {
                result = objectMapper.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            Page<Task> returnedPage = result.getData();
            List<Task> tasks = returnedPage.getContent();
            assertEquals(3, tasks.size());
            for (Task task : tasks) {
                assertTrue(task.getName().toLowerCase().contains("maintenance"));
            }
        }
    }

    @Nested
    @DisplayName("Delete task")
    class DeleteTask {

        @Test
        void When_DeleteTask_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(delete("/api/task/" + testSetupHelper.getTestTasks().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isOk());
            boolean exists = taskRepository.existsById(testSetupHelper.getTestTasks().get(0).getId());
            assertEquals(false, exists);
        }

        @Test
        void When_DeleteTask_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(delete("/api/task/" + UUID.randomUUID())
                    .contentType("application/json"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_DeleteTask_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            mockMvc.perform(delete("/api/task/" + testSetupHelper.getTestTasks().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isForbidden());
        }
    }
}
