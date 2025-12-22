package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.model.forms.TaskCreateForm;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.forms.TaskSearchForm;
import com.viladevcorp.hosteo.model.forms.TaskUpdateForm;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TaskService {

  private final TaskRepository taskRepository;
  private final WorkflowService workflowService;
  private final ApartmentRepository apartmentRepository;

  @Autowired
  public TaskService(
      TaskRepository taskRepository,
      WorkflowService workflowService,
      ApartmentRepository apartmentRepository) {
    this.taskRepository = taskRepository;
    this.workflowService = workflowService;
    this.apartmentRepository = apartmentRepository;
  }

  public Task createTask(TaskCreateForm form)
      throws InstanceNotFoundException, NotAllowedResourceException {

    Apartment apartment;
    try {
      apartment =
          ServiceUtils.getEntityById(
              form.getApartmentId(), apartmentRepository, "TaskService.createTask", "Apartment");
    } catch (NotAllowedResourceException e) {
      throw new NotAllowedResourceException("Not allowed to create task for this apartment.");
    }
    Task task =
        Task.builder()
            .name(form.getName())
            .category(form.getCategory())
            .duration(form.getDuration())
            .extra(form.isExtra())
            .apartment(apartment)
            .steps(form.getSteps())
            .build();

    apartment.addTask(task);
    workflowService.calculateApartmentState(apartment.getId());
    return task;
  }

  public Task updateTask(TaskUpdateForm form)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Task task = getTaskById(form.getId());
    BeanUtils.copyProperties(form, task, "id");
    return taskRepository.save(task);
  }

  public Task getTaskById(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
    return ServiceUtils.getEntityById(id, taskRepository, "TaskService.getTaskById", "Task");
  }

  public List<Task> findTasks(TaskSearchForm form) {
    String name =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";

    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return taskRepository.advancedSearch(AuthUtils.getUsername(), name, pageRequest);
  }

  public PageMetadata getTasksMetadata(TaskSearchForm form) {
    String name =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    int totalRows = taskRepository.advancedCount(AuthUtils.getUsername(), name);
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteTask(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
    Task task = getTaskById(id);
    Apartment apartment = task.getApartment();
    apartment.removeTask(task);
    taskRepository.delete(task);
    workflowService.calculateApartmentState(apartment.getId());
  }
}
