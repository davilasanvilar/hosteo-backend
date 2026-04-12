package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.forms.TaskCreateForm;
import com.viladevcorp.hosteo.model.forms.TaskSearchForm;
import com.viladevcorp.hosteo.model.forms.TaskUpdateForm;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import java.util.List;
import java.util.UUID;
import javax.management.InstanceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
      throws InstanceNotFoundException {

    Apartment apartment =
        apartmentRepository.findByIdAndCreatedByUsername(
            form.getApartmentId(), AuthUtils.getUsername());
    if (apartment == null) {
      throw new InstanceNotFoundException("Apartment not found with id: " + form.getApartmentId());
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
      throws InstanceNotFoundException {
    Task task = getTaskById(form.getId());
    BeanUtils.copyProperties(form, task, "id");
    return taskRepository.save(task);
  }

  public Task getTaskById(UUID id) throws InstanceNotFoundException {
    Task result = taskRepository.findByIdAndCreatedByUsername(id, AuthUtils.getUsername());
    if (result == null) {
      throw new InstanceNotFoundException("Task not found with id: " + id);
    } else {
      return result;
    }
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

  public void deleteTask(UUID id) throws InstanceNotFoundException {
    Task task = getTaskById(id);
    Apartment apartment = task.getApartment();
    apartment.removeTask(task);
    taskRepository.delete(task);
    workflowService.calculateApartmentState(apartment.getId());
  }
}
