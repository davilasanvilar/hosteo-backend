package com.viladevcorp.hosteo.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.viladevcorp.hosteo.model.Template;
import com.viladevcorp.hosteo.model.forms.TemplateCreateForm;
import com.viladevcorp.hosteo.model.forms.TemplateUpdateForm;
import com.viladevcorp.hosteo.repository.TemplateRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;

import static com.viladevcorp.hosteo.common.TestConstants.*;

class TemplateControllerTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestSetupHelper testSetupHelper;

    @BeforeEach
    void setup() {
        testSetupHelper.resetTestTemplates();
    }

    @Nested
    @DisplayName("Create templates")
    class CreateTemplates {

        @Test
        void When_CreateTemplate_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateCreateForm form = new TemplateCreateForm();
            form.setName(NEW_TEMPLATE_NAME_1);
            form.setCategory(NEW_TEMPLATE_CATEGORY_1);
            form.setDuration(NEW_TEMPLATE_DURATION_1);
            form.setPrepTask(NEW_TEMPLATE_PREP_TASK_1);
            form.setSteps(NEW_TEMPLATE_STEPS_1);

            String resultString = mockMvc.perform(post("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            TypeReference<ApiResponse<Template>> typeReference = new TypeReference<ApiResponse<Template>>() {
            };
            ApiResponse<Template> result = objectMapper.readValue(resultString, typeReference);
            Template createdTemplate = templateRepository.findById(result.getData().getId()).orElse(null);
            assertNotNull(createdTemplate);
            assertEquals(NEW_TEMPLATE_NAME_1, createdTemplate.getName());
            assertEquals(NEW_TEMPLATE_CATEGORY_1, createdTemplate.getCategory());
            assertEquals(NEW_TEMPLATE_DURATION_1, createdTemplate.getDuration());
            assertEquals(NEW_TEMPLATE_PREP_TASK_1, createdTemplate.isPrepTask());
            assertEquals(NEW_TEMPLATE_STEPS_1.toString(), createdTemplate.getSteps().toString());
        }

        @Test
        void When_CreateTemplate_MissingName_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateCreateForm form = new TemplateCreateForm();
            form.setCategory(NEW_TEMPLATE_CATEGORY_1);
            form.setDuration(NEW_TEMPLATE_DURATION_1);
            form.setPrepTask(NEW_TEMPLATE_PREP_TASK_1);
            form.setSteps(NEW_TEMPLATE_STEPS_1);

            mockMvc.perform(post("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateTemplate_MissingCategory_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateCreateForm form = new TemplateCreateForm();
            form.setName(NEW_TEMPLATE_NAME_1);
            form.setDuration(NEW_TEMPLATE_DURATION_1);
            form.setPrepTask(NEW_TEMPLATE_PREP_TASK_1);
            form.setSteps(NEW_TEMPLATE_STEPS_1);

            mockMvc.perform(post("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_CreateTemplate_NegativeDuration_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateCreateForm form = new TemplateCreateForm();
            form.setName(NEW_TEMPLATE_NAME_1);
            form.setCategory(NEW_TEMPLATE_CATEGORY_1);
            form.setDuration(-10);
            form.setPrepTask(NEW_TEMPLATE_PREP_TASK_1);
            form.setSteps(NEW_TEMPLATE_STEPS_1);

            mockMvc.perform(post("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update templates")
    class UpdateTemplates {

        @Test
        void When_UpdateTemplate_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateUpdateForm form = new TemplateUpdateForm();
            form.setId(testSetupHelper.getTestTemplates().get(0).getId());
            form.setName(UPDATED_TEMPLATE_NAME_1);
            form.setCategory(UPDATED_TEMPLATE_CATEGORY_1);
            form.setDuration(UPDATED_TEMPLATE_DURATION_1);
            form.setPrepTask(UPDATED_TEMPLATE_PREP_TASK_1);
            form.setSteps(UPDATED_TEMPLATE_STEPS_1);
            mockMvc.perform(patch("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isOk());

            Template updated = templateRepository.findById(testSetupHelper.getTestTemplates().get(0).getId()).get();
            assertEquals(UPDATED_TEMPLATE_NAME_1, updated.getName());
            assertEquals(UPDATED_TEMPLATE_CATEGORY_1, updated.getCategory());
            assertEquals(UPDATED_TEMPLATE_DURATION_1, updated.getDuration());
            assertEquals(UPDATED_TEMPLATE_PREP_TASK_1, updated.isPrepTask());
            assertEquals(UPDATED_TEMPLATE_STEPS_1.toString(), updated.getSteps().toString());
        }

        @Test
        void When_UpdateTemplate_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateUpdateForm form = new TemplateUpdateForm();
            form.setId(UUID.randomUUID());
            form.setName(UPDATED_TEMPLATE_NAME_1);
            form.setCategory(UPDATED_TEMPLATE_CATEGORY_1);
            form.setDuration(UPDATED_TEMPLATE_DURATION_1);
            form.setPrepTask(UPDATED_TEMPLATE_PREP_TASK_1);
            form.setSteps(UPDATED_TEMPLATE_STEPS_1);
            mockMvc.perform(patch("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_UpdateTemplate_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            TemplateUpdateForm form = new TemplateUpdateForm();
            form.setId(testSetupHelper.getTestTemplates().get(0).getId());
            form.setName(UPDATED_TEMPLATE_NAME_1);
            form.setCategory(UPDATED_TEMPLATE_CATEGORY_1);
            form.setDuration(UPDATED_TEMPLATE_DURATION_1);
            form.setPrepTask(UPDATED_TEMPLATE_PREP_TASK_1);
            form.setSteps(UPDATED_TEMPLATE_STEPS_1);
            mockMvc.perform(patch("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void When_UpdateTemplate_MissingName_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateUpdateForm form = new TemplateUpdateForm();
            form.setId(testSetupHelper.getTestTemplates().get(0).getId());
            form.setCategory(UPDATED_TEMPLATE_CATEGORY_1);
            form.setDuration(UPDATED_TEMPLATE_DURATION_1);
            form.setPrepTask(UPDATED_TEMPLATE_PREP_TASK_1);
            form.setSteps(UPDATED_TEMPLATE_STEPS_1);
            mockMvc.perform(patch("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateTemplate_MissingCategory_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateUpdateForm form = new TemplateUpdateForm();
            form.setId(testSetupHelper.getTestTemplates().get(0).getId());
            form.setName(UPDATED_TEMPLATE_NAME_1);
            form.setDuration(UPDATED_TEMPLATE_DURATION_1);
            form.setPrepTask(UPDATED_TEMPLATE_PREP_TASK_1);
            form.setSteps(UPDATED_TEMPLATE_STEPS_1);
            mockMvc.perform(patch("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateTemplate_NegativeDuration_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateUpdateForm form = new TemplateUpdateForm();
            form.setId(testSetupHelper.getTestTemplates().get(0).getId());
            form.setName(UPDATED_TEMPLATE_NAME_1);
            form.setCategory(UPDATED_TEMPLATE_CATEGORY_1);
            form.setDuration(-50);
            form.setPrepTask(UPDATED_TEMPLATE_PREP_TASK_1);
            form.setSteps(UPDATED_TEMPLATE_STEPS_1);
            mockMvc.perform(patch("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void When_UpdateTemplate_MissingId_BadRequest() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            TemplateUpdateForm form = new TemplateUpdateForm();
            form.setName(UPDATED_TEMPLATE_NAME_1);
            form.setCategory(UPDATED_TEMPLATE_CATEGORY_1);
            form.setDuration(UPDATED_TEMPLATE_DURATION_1);
            form.setPrepTask(UPDATED_TEMPLATE_PREP_TASK_1);
            form.setSteps(UPDATED_TEMPLATE_STEPS_1);
            mockMvc.perform(patch("/api/template")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get template")
    class GetTemplate {

        @Test
        void When_GetTemplate_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

            String result = mockMvc.perform(get("/api/template/" + testSetupHelper.getTestTemplates().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            TypeReference<ApiResponse<Template>> typeReference = new TypeReference<ApiResponse<Template>>() {
            };
            ApiResponse<Template> apiResponse = objectMapper.readValue(result, typeReference);
            Template fetchedTemplate = apiResponse.getData();
            assertNotNull(fetchedTemplate);
            assertEquals(CREATED_TEMPLATE_NAME_1, fetchedTemplate.getName());
        }

        @Test
        void When_GetTemplate_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(get("/api/template/" + UUID.randomUUID())
                    .contentType("application/json"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_GetTemplate_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            mockMvc.perform(get("/api/template/" + testSetupHelper.getTestTemplates().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Delete template")
    class DeleteTemplate {

        @Test
        void When_DeleteTemplate_Ok() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(delete("/api/template/" + testSetupHelper.getTestTemplates().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isOk());
            boolean exists = templateRepository.existsById(testSetupHelper.getTestTemplates().get(0).getId());
            assertEquals(false, exists);
        }

        @Test
        void When_DeleteTemplate_NonExistentId_NotFound() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
            mockMvc.perform(delete("/api/template/" + UUID.randomUUID())
                    .contentType("application/json"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void When_DeleteTemplate_AnotherUser_Forbidden() throws Exception {
            TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
            mockMvc.perform(delete("/api/template/" + testSetupHelper.getTestTemplates().get(0).getId())
                    .contentType("application/json"))
                    .andExpect(status().isForbidden());
        }

    }
}
