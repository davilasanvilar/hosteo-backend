package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.EventUpdateError;
import com.viladevcorp.hosteo.model.dto.EventWithAssignmentsDto;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.forms.EventSearchForm;
import com.viladevcorp.hosteo.model.forms.EventUpdateForm;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import java.time.Instant;
import java.util.*;
import javax.management.InstanceNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class EventService {

  private final EventRepository eventRepository;
  private final AssignmentRepository assignmentRepository;
  private final WorkflowService workflowService;
  private final ApartmentRepository apartmentRepository;

  @Autowired
  public EventService(
      EventRepository eventRepository,
      WorkflowService workflowService,
      AssignmentRepository assignmentRepository,
      ApartmentRepository apartmentRepository) {
    this.eventRepository = eventRepository;
    this.workflowService = workflowService;
    this.assignmentRepository = assignmentRepository;
    this.apartmentRepository = apartmentRepository;
  }

  private void validateEventState(UUID apartmentId, EventState state, Instant startDate)
      throws NextOfPendingCannotBeInprogressOrFinished,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfInProgressCannotBeFinishedOrInProgress {
    // If the state is pending we cannot have IN PROGRESS or FINISHED events after (if the next ones
    // have finished this one should have too)
    if (state.isPending()) {
      Optional<Event> nextEventOpt =
          eventRepository.findFirstEventAfterDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Event nextEvent = nextEventOpt.orElse(null);
      if (nextEvent != null
          && (nextEvent.getState().isInProgress() || nextEvent.getState().isFinished())) {
        log.error(
            "[EventService.validateEventState] - Event cannot be set to PENDING because there is a next event IN_PROGRESS or FINISHED for apartment id: {}",
            apartmentId);
        throw new NextOfPendingCannotBeInprogressOrFinished();
      }
    }
    if (state.isInProgress()) {
      Optional<Event> previousEventOpt =
          eventRepository.findFirstEventBeforeDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Event previousEvent = previousEventOpt.orElse(null);
      if (previousEvent != null
          && (previousEvent.getState().isPending() || previousEvent.getState().isInProgress())) {
        log.error(
            "[EventService.validateEventState] - Event cannot be set to IN_PROGRESS because there is a previous event PENDING or IN_PROGRESS for apartment id: {}",
            apartmentId);
        throw new PrevOfInProgressCannotBePendingOrInProgress();
      }
      Optional<Event> nextEventOpt =
          eventRepository.findFirstEventAfterDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Event nextEvent = nextEventOpt.orElse(null);
      if (nextEvent != null
          && (nextEvent.getState().isInProgress() || nextEvent.getState().isFinished())) {
        log.error(
            "[EventService.validateEventState] - Event cannot be set to IN_PROGRESS because there is a next event IN_PROGRESS or FINISHED for apartment id: {}",
            apartmentId);
        throw new NextOfInProgressCannotBeFinishedOrInProgress();
      }
    }
    if (state.isFinished()) {
      Optional<Event> previousEventOpt =
          eventRepository.findFirstEventBeforeDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Event previousEvent = previousEventOpt.orElse(null);
      if (previousEvent != null
          && (previousEvent.getState().isPending() || previousEvent.getState().isInProgress())) {
        log.error(
            "[EventService.validateEventState] - Event cannot be set to FINISHED because there is a previous event PENDING or IN_PROGRESS for apartment id: {}",
            apartmentId);
        throw new PrevOfFinishedCannotBeNotPendingOrInProgress();
      }
    }
  }

  private Event executeCreateEventLogic(EventCreateForm form)
      throws InstanceNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Optional<Apartment> apartmentOpt =
        apartmentRepository.findById(form.getApartmentId(), AuthUtils.getUsername());
    if (apartmentOpt.isEmpty()) {
      throw new InstanceNotFoundException("Apartment not found with id: " + form.getApartmentId());
    }
    Apartment apartment = apartmentOpt.get();
    Pair<Event, Assignment> conflicts =
        ServiceUtils.getScheduleConflicts(
            eventRepository,
            assignmentRepository,
            form.getApartmentId(),
            form.getStartDate(),
            form.getEndDate(),
            null,
            null);

    if (conflicts.a != null || conflicts.b != null) {
      log.error(
          "[{}] - Apartment with id: {} is not available between {} and {}",
          "EventService.createEvent",
          form.getApartmentId(),
          form.getStartDate(),
          form.getEndDate());
      throw new NotAvailableDatesException("Apartment is not available in the selected dates.");
    }

    Event event =
        Event.builder()
            .type(form.getType())
            .apartment(apartment)
            .startDate(form.getStartDate())
            .endDate(form.getEndDate())
            .name(form.getName())
            .state(form.getState())
            .source(form.getSource())
            .build();

    Event result = eventRepository.save(event);
    workflowService.calculateApartmentState(form.getApartmentId());
    validateEventState(form.getApartmentId(), form.getState(), form.getStartDate());
    return result;
  }

  public Event createEvent(EventCreateForm form)
      throws InstanceNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    return executeCreateEventLogic(form);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public Event createEventInNewTransaction(EventCreateForm form)
      throws InstanceNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    return executeCreateEventLogic(form);
  }

  public Event updateEvent(EventUpdateForm form)
      throws InstanceNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Event event = getEventById(form.getId());
    UUID apartmentId = event.getApartment().getId();

    Pair<Event, Assignment> conflicts =
        ServiceUtils.getScheduleConflicts(
            eventRepository,
            assignmentRepository,
            apartmentId,
            form.getStartDate(),
            form.getEndDate(),
            form.getId(),
            null);

    if (conflicts.a != null || conflicts.b != null) {
      log.error(
          "[{}] - Apartment with id: {} is not available between {} and {}",
          "EventService.updateEvent",
          apartmentId,
          form.getStartDate(),
          form.getEndDate());
      throw new NotAvailableDatesException("Apartment is not available in the selected dates.");
    }

    BeanUtils.copyProperties(form, event, "id");

    Event result = eventRepository.save(event);
    workflowService.calculateApartmentState(result.getApartment().getId());
    validateEventState(apartmentId, form.getState(), form.getStartDate());

    return result;
  }

  private Event executeUpdateStateLogic(UUID eventId, EventState state)
      throws InstanceNotFoundException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Event event = getEventById(eventId);
    UUID apartmentId = event.getApartment().getId();
    event.setState(state);
    Event result = eventRepository.save(event);
    try {
      workflowService.calculateApartmentState(result.getApartment().getId());
      validateEventState(apartmentId, state, event.getStartDate());
    } catch (Exception e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      throw e;
    }
    return result;
  }

  public Event updateEventState(UUID eventId, EventState state)
      throws InstanceNotFoundException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    return executeUpdateStateLogic(eventId, state);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public Event updateEventStateInNewTransaction(UUID eventId, EventState state)
      throws InstanceNotFoundException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    return executeUpdateStateLogic(eventId, state);
  }

  public List<EventUpdateError> updateBulkEventState(Set<UUID> eventIds, EventState state) {

    List<EventUpdateError> errors = new ArrayList<>();

    // Retrieve the events from DB
    List<Event> events =
        eventRepository.findInIdsAndCreatedByUsername(eventIds, AuthUtils.getUsername());

    // Now process the events
    for (Event event : events) {
      try {
        // Call the method that starts a new transaction for each event.
        updateEventStateInNewTransaction(event.getId(), state);
      } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
        errors.add(
            new EventUpdateError(
                event, CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS));
      } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
        errors.add(
            new EventUpdateError(
                event, CodeErrors.PREV_OF_FINISHED_CANNOT_BE_NOT_PENDING_OR_INPROGRESS));
      } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
        errors.add(
            new EventUpdateError(
                event, CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS));
      } catch (NextOfPendingCannotBeInprogressOrFinished e) {
        errors.add(
            new EventUpdateError(
                event, CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED));
      } catch (InstanceNotFoundException e) {
        errors.add(new EventUpdateError(event, e.getMessage()));
      } catch (Exception e) {
        // Catch any other exception to prevent the main loop from stopping.
        errors.add(new EventUpdateError(event, e.getMessage()));
      }
    }
    return errors;
  }

  public Event getEventById(UUID id) throws InstanceNotFoundException {
    Optional<Event> resultOpt = eventRepository.findById(id, AuthUtils.getUsername());
    if (resultOpt.isEmpty()) {
      throw new InstanceNotFoundException("Event not found with id: " + id);
    } else {
      return resultOpt.get();
    }
  }

  public EventWithAssignmentsDto getEventByIdWithAssigments(UUID id)
      throws InstanceNotFoundException {
    Optional<Event> resultOpt =
        eventRepository.findEventByIdWithAssignments(id, AuthUtils.getUsername());
    if (resultOpt.isEmpty()) {
      throw new InstanceNotFoundException("Event not found with id: " + id);
    }
    return new EventWithAssignmentsDto(resultOpt.get());
  }

  public List<Event> findEvents(EventSearchForm form) {
    String apartmentName =
        form.getApartmentName() == null || form.getApartmentName().isEmpty()
            ? null
            : "%" + form.getApartmentName().toLowerCase() + "%";
    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return eventRepository.advancedSearch(
        AuthUtils.getUsername(),
        apartmentName,
        form.getStates(),
        form.getTypes(),
        form.getStartDate(),
        form.getEndDate(),
        pageRequest);
  }

  public PageMetadata getEventsMetadata(EventSearchForm form) {
    String apartmentName =
        form.getApartmentName() == null || form.getApartmentName().isEmpty()
            ? null
            : "%" + form.getApartmentName().toLowerCase() + "%";
    int totalRows =
        eventRepository.advancedCount(
            AuthUtils.getUsername(),
            apartmentName,
            form.getStates(),
            form.getTypes(),
            form.getStartDate(),
            form.getEndDate());
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteEvent(UUID id) throws InstanceNotFoundException {
    Event event = getEventById(id);
    eventRepository.delete(event);
    workflowService.calculateApartmentState(event.getApartment().getId());
  }
}
