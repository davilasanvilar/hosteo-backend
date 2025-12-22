package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.model.dto.WorkerDto;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.forms.WorkerCreateForm;
import com.viladevcorp.hosteo.model.forms.WorkerSearchForm;
import com.viladevcorp.hosteo.model.forms.WorkerUpdateForm;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.repository.WorkerRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkerService {

  private final WorkerRepository workerRepository;

  @Autowired
  public WorkerService(WorkerRepository workerRepository) {
    this.workerRepository = workerRepository;
  }

  public Worker createWorker(WorkerCreateForm form) {
    Worker worker =
        Worker.builder()
            .name(form.getName())
            .language(form.getLanguage())
            .visible(form.isVisible())
            .build();
    return workerRepository.save(worker);
  }

  public Worker updateWorker(WorkerUpdateForm form)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Worker worker = getWorkerById(form.getId());
    BeanUtils.copyProperties(form, worker, "id");
    return workerRepository.save(worker);
  }

  public Worker getWorkerById(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    return ServiceUtils.getEntityById(
        id, workerRepository, "WorkerService.getWorkerById", "Worker");
  }

  public List<Worker> findWorkers(WorkerSearchForm form) {
    String workerName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";

    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return workerRepository.advancedSearch(AuthUtils.getUsername(), workerName, null, pageRequest);
  }

  public PageMetadata getWorkersMetadata(WorkerSearchForm form) {
    String workerName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    int totalRows = workerRepository.advancedCount(AuthUtils.getUsername(), workerName, null);
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteWorker(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
    Worker worker = getWorkerById(id);
    workerRepository.delete(worker);
  }
}
