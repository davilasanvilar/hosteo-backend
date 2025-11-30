package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.forms.AssignmentCreateForm;
import com.viladevcorp.hosteo.model.forms.AssignmentSearchForm;
import com.viladevcorp.hosteo.model.forms.AssignmentUpdateForm;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AssignmentService {

        private AssignmentRepository assignmentRepository;
        private TaskService taskService;
        private WorkerService workerService;

        @Autowired
        public AssignmentService(AssignmentRepository assignmentRepository, TaskService taskService,
                        WorkerService workerService) {
                this.assignmentRepository = assignmentRepository;
                this.taskService = taskService;
                this.workerService = workerService;
        }

        public Assignment createAssignment(AssignmentCreateForm form)
                        throws InstanceNotFoundException, NotAllowedResourceException {
                Task task;
                try {
                        task = taskService.getTaskById(form.getTaskId());
                } catch (NotAllowedResourceException e) {
                        throw new NotAllowedResourceException("Not allowed to create assignment for this task.");
                }

                Worker worker;
                try {
                        worker = workerService.getWorkerById(form.getWorkerId());
                } catch (NotAllowedResourceException e) {
                        throw new NotAllowedResourceException("Not allowed to assign this worker.");
                }

                Assignment assignment = Assignment.builder()
                                .task(task)
                                .startDate(form.getStartDate())
                                .worker(worker)
                                .state(form.getState())
                                .build();

                return assignmentRepository.save(assignment);
        }

        public Assignment updateAssignment(AssignmentUpdateForm form)
                        throws InstanceNotFoundException, NotAllowedResourceException {
                Assignment assignment = getAssignmentById(form.getId());

                Task task;
                try {
                        task = taskService.getTaskById(form.getTaskId());
                } catch (NotAllowedResourceException e) {
                        throw new NotAllowedResourceException("Not allowed to assign this task.");
                }

                Worker worker;
                try {
                        worker = workerService.getWorkerById(form.getWorkerId());
                } catch (NotAllowedResourceException e) {
                        throw new NotAllowedResourceException("Not allowed to assign this worker.");
                }

                assignment.setTask(task);
                assignment.setStartDate(form.getStartDate());
                assignment.setWorker(worker);
                assignment.setState(form.getState());

                return assignmentRepository.save(assignment);
        }

        public Assignment getAssignmentById(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
                Assignment assignment = assignmentRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.error("[AssignmentService.getAssignmentById] - Assignment not found with id: {}",
                                                        id);
                                        return new InstanceNotFoundException(
                                                        "Assignment not found with id: " + id);
                                });
                try {
                        AuthUtils.checkIfCreator(assignment.getTask().getApartment(), "assignment");
                } catch (NotAllowedResourceException e) {
                        throw new NotAllowedResourceException("Not allowed to access this assignment.");
                }
                return assignment;
        }

        public List<Assignment> findAssignments(AssignmentSearchForm form) {
                String taskName = form.getTaskName() == null || form.getTaskName().isEmpty() ? null
                                : "%" + form.getTaskName().toLowerCase() + "%";
                String workerName = form.getWorkerName() == null || form.getWorkerName().isEmpty() ? null
                                : "%" + form.getWorkerName().toLowerCase() + "%";
                PageRequest pageRequest = null;
                if (form.getPageSize() > 0) {
                        int pageNumber = form.getPageNumber() <= 0 ? 0 : form.getPageNumber();
                        pageRequest = PageRequest.of(pageNumber, form.getPageSize());
                }
                return assignmentRepository.advancedSearch(
                                AuthUtils.getUsername(),
                                taskName,
                                workerName,
                                form.getState(),
                                pageRequest);
        }

        public PageMetadata getAssignmentsMetadata(AssignmentSearchForm form) {
                String taskName = form.getTaskName() == null || form.getTaskName().isEmpty() ? null
                                : "%" + form.getTaskName().toLowerCase() + "%";
                String workerName = form.getWorkerName() == null || form.getWorkerName().isEmpty() ? null
                                : "%" + form.getWorkerName().toLowerCase() + "%";
                int totalRows = assignmentRepository.advancedCount(
                                AuthUtils.getUsername(),
                                taskName,
                                workerName,
                                form.getState());
                int totalPages = form.getPageSize() > 0 ? ((Double) Math.ceil((double) totalRows /
                                form.getPageSize())).intValue() : 1;
                return new PageMetadata(totalPages, totalRows);
        }

        public void deleteAssignment(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
                Assignment assignment = getAssignmentById(id);
                assignmentRepository.delete(assignment);
        }
}
