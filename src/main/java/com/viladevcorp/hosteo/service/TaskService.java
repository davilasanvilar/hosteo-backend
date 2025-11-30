package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.forms.TaskCreateForm;
import com.viladevcorp.hosteo.model.forms.TaskSearchForm;
import com.viladevcorp.hosteo.model.forms.TaskUpdateForm;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TaskService {

    private TaskRepository taskRepository;
    private ApartmentService apartmentService;

    @Autowired
    public TaskService(TaskRepository taskRepository, ApartmentService apartmentService) {
        this.taskRepository = taskRepository;
        this.apartmentService = apartmentService;
    }

    public Task createTask(TaskCreateForm form) throws InstanceNotFoundException, NotAllowedResourceException {
        Apartment apartment;
        try {
            apartment = apartmentService.getApartmentById(form.getApartmentId());
        } catch (NotAllowedResourceException e) {
            throw new NotAllowedResourceException("Not allowed to create task for this apartment.");
        }

        Task task = Task.builder()
                .name(form.getName())
                .category(form.getCategory())
                .duration(form.getDuration())
                .prepTask(form.isPrepTask())
                .apartment(apartment)
                .steps(form.getSteps())
                .build();

        return taskRepository.save(task);
    }

    public Task updateTask(TaskUpdateForm form) throws InstanceNotFoundException, NotAllowedResourceException {
        Task task = getTaskById(form.getId());
        Apartment apartment;
        try {
            apartment = apartmentService.getApartmentById(form.getApartmentId());
        } catch (NotAllowedResourceException e) {
            throw new NotAllowedResourceException("Not allowed to create task for this apartment.");
        }

        task.setName(form.getName());
        task.setCategory(form.getCategory());
        task.setDuration(form.getDuration());
        task.setPrepTask(form.isPrepTask());
        task.setApartment(apartment);
        task.setSteps(form.getSteps());

        return taskRepository.save(task);
    }

    public Task getTaskById(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[TaskService.getTaskById] - Task not found with id: {}", id);
                    return new InstanceNotFoundException("Task not found with id: " + id);
                });
        try {
            AuthUtils.checkIfCreator(task.getApartment(), "task");
        } catch (NotAllowedResourceException e) {
            log.error("[TaskService.getTaskById] - Not allowed to access task with id: {}", id);
            throw e;
        }
        return task;
    }

    public List<Task> findTasks(TaskSearchForm form) {
        String name = form.getName() == null || form.getName().isEmpty() ? null
                : "%" + form.getName().toLowerCase() + "%";
        PageRequest pageRequest = null;
        if (form.getPageSize() > 0) {
            int pageNumber = form.getPageNumber() <= 0 ? 0 : form.getPageNumber();
            pageRequest = PageRequest.of(pageNumber, form.getPageSize());
        }
        return taskRepository.advancedSearch(
                AuthUtils.getUsername(),
                name,
                pageRequest);
    }

    public PageMetadata getTasksMetadata(TaskSearchForm form) {
        String name = form.getName() == null || form.getName().isEmpty() ? null
                : "%" + form.getName().toLowerCase() + "%";
        int totalRows = taskRepository.advancedCount(
                AuthUtils.getUsername(),
                name);
        int totalPages = form.getPageSize() > 0 ? ((Double) Math.ceil((double) totalRows /
                form.getPageSize())).intValue() : 1;
        return new PageMetadata(totalPages, totalRows);
    }

    public void deleteTask(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
}
