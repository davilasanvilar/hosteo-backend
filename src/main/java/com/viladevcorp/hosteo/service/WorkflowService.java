package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.*;
import com.viladevcorp.hosteo.model.types.Alert;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import javax.management.InstanceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkflowService {

  private final ApartmentRepository apartmentRepository;
  private final BookingRepository bookingRepository;
  private final AssignmentRepository assignmentRepository;
  private final TaskRepository taskRepository;
  private final Clock clock;

  @Autowired
  public WorkflowService(
      ApartmentRepository apartmentRepository,
      BookingRepository bookingRepository,
      AssignmentRepository assignmentRepository,
      TaskRepository taskRepository,
      Clock clock) {
    this.apartmentRepository = apartmentRepository;
    this.bookingRepository = bookingRepository;
    this.assignmentRepository = assignmentRepository;
    this.taskRepository = taskRepository;
    this.clock = clock;
  }

  public void calculateApartmentState(UUID id) throws InstanceNotFoundException {

    Apartment apartment =
        apartmentRepository.findByIdAndCreatedByUsername(id, AuthUtils.getUsername());
    if (apartment == null) {
      throw new InstanceNotFoundException("Apartment not found with id: " + id);
    }
    ApartmentState resultState = ApartmentState.READY;

    if (bookingRepository.existsBookingByApartmentIdAndStateAndCreatedByUsername(
        id, BookingState.IN_PROGRESS, AuthUtils.getUsername())) {
      apartment.setState(ApartmentState.OCCUPIED);
      apartmentRepository.save(apartment);
      return;
    }

    Map<UUID, Task> regularTasksMap = new HashMap<>();

    apartment
        .getTasks()
        .forEach(
            task -> {
              if (!task.isExtra()) {
                regularTasksMap.put(task.getId(), task);
              }
            });
    // If there are no tasks apartment is READY
    if (regularTasksMap.isEmpty()) {
      apartment.setState(ApartmentState.READY);
      apartmentRepository.save(apartment);
      return;
    }

    Set<Assignment> assignmentsAffectingApartmentState = getAssignmentsThatAffectApartmentState(id);

    boolean hasBookingFinished =
        bookingRepository.existsBookingByApartmentIdAndStateAndCreatedByUsername(
            id, BookingState.FINISHED, AuthUtils.getUsername());

    for (Assignment assignment : assignmentsAffectingApartmentState) {
      if (assignment.getState().isFinished()) {
        regularTasksMap.remove(assignment.getTask().getId());
      }
    }
    if (hasBookingFinished && !regularTasksMap.isEmpty()) {
      resultState = ApartmentState.USED;
    }
    apartment.setState(resultState);
    apartmentRepository.save(apartment);
  }

  public Set<Assignment> getAssignmentsThatAffectApartmentState(UUID apartmentId)
      throws InstanceNotFoundException {
    Set<Assignment> result = Set.of();
    Optional<Booking> lastFinishedBookingOpt =
        bookingRepository
            .findFirstBookingByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateDesc(
                AuthUtils.getUsername(), apartmentId, BookingState.FINISHED);
    if (lastFinishedBookingOpt.isEmpty()) {
      return result;
    }
    return getAssigmentsRelatedToBooking(lastFinishedBookingOpt.get().getId());
  }

  public Set<Assignment> getAssigmentsRelatedToBooking(UUID bookingId)
      throws InstanceNotFoundException {
    Booking booking =
        bookingRepository.findByIdAndCreatedByUsername(bookingId, AuthUtils.getUsername());
    if (booking == null) {
      throw new InstanceNotFoundException("Booking not found with id: " + bookingId);
    }
    Booking nextBooking =
        bookingRepository
            .findFirstBookingAfterDateWithState(
                AuthUtils.getAuthUser().getId(),
                booking.getApartment().getId(),
                booking.getStartDate(),
                null)
            .orElse(null);

    return assignmentRepository.findByApartmentAndStateAndDateRangeAndExtra(
        AuthUtils.getUsername(),
        booking.getApartment().getId(),
        null,
        booking.getEndDate(),
        nextBooking == null ? null : nextBooking.getStartDate(),
        false);
  }

  private static class ApartmentInfo {
    List<TaskDto> tasks;
    Booking nextPendingBooking;
    ApartmentState state;

    public ApartmentInfo(List<TaskDto> tasks, Booking nextPendingBooking, ApartmentState state) {
      this.tasks = tasks;
      this.nextPendingBooking = nextPendingBooking;
      this.state = state;
    }
  }

  private ApartmentInfo processApartment(
      Apartment apartment, Map<UUID, ApartmentInfo> apartmentInfoMap) {
    UUID apartmentId = apartment.getId();
    if (!apartmentInfoMap.containsKey(apartmentId)) {

      List<TaskDto> apartmentTasks =
          taskRepository
              .findNonExtraTasksByApartmentId(AuthUtils.getUsername(), apartmentId)
              .stream()
              .map(TaskDto::new)
              .collect(Collectors.toList());
      Booking nextPendingBooking =
          bookingRepository
              .findFirstBookingByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateAsc(
                  AuthUtils.getUsername(), apartmentId, BookingState.PENDING)
              .orElse(null);
      ApartmentInfo aptInfo =
          new ApartmentInfo(apartmentTasks, nextPendingBooking, apartment.getState());

      apartmentInfoMap.put(apartmentId, aptInfo);
      return aptInfo;
    } else {
      return apartmentInfoMap.get(apartmentId);
    }
  }

  private BookingSchedulerDto processBookingForScheduler(
      Booking booking,
      Map<UUID, ApartmentInfo> apartmentInfoMap,
      Map<UUID, BookingSchedulerDto> bookingMap)
      throws InstanceNotFoundException {
    if (booking == null) {
      return null;
    }
    if (bookingMap.containsKey(booking.getId())) {
      return bookingMap.get(booking.getId());
    }
    Set<Assignment> bookingAssignments = getAssigmentsRelatedToBooking(booking.getId());
    BookingSchedulerDto bookingDto = new BookingSchedulerDto();
    bookingDto.setBooking(new BookingDto(booking));
    ApartmentInfo apartmentInfo = processApartment(booking.getApartment(), apartmentInfoMap);
    List<TaskDto> apartmentTasks = apartmentInfo.tasks;
    Set<UUID> assignedTaskIds =
        bookingAssignments.stream()
            .map(assignment -> assignment.getTask().getId())
            .collect(Collectors.toSet());
    List<TaskDto> tasksToRemove = new ArrayList<>();
    for (TaskDto taskDto : apartmentTasks) {
      if (assignedTaskIds.contains(taskDto.getId())) {
        bookingDto.getAssignedTasks().add(taskDto);
        tasksToRemove.add(taskDto);
      }
    }
    apartmentTasks.removeAll(tasksToRemove);
    bookingDto.getUnassignedTasks().addAll(apartmentTasks);
    bookingDto.setHasUnfinishedTasks(
        bookingAssignments.stream().anyMatch(assignment -> assignment.getState().isPending()));

    if (!bookingMap.containsKey(booking.getId())) {
      bookingMap.put(booking.getId(), bookingDto);
    }
    Booking previousBooking =
        bookingRepository
            .findFirstBookingBeforeDateWithState(
                AuthUtils.getAuthUser().getId(),
                booking.getApartment().getId(),
                booking.getStartDate(),
                null)
            .orElse(null);
    bookingDto.setPrevBooking(new SimpleBookingSchedulerDto(previousBooking));

    return bookingDto;
  }

  private void passAlertsToRangeBooking(
      List<BookingSchedulerDto> rangebookings, BookingSchedulerDto bookingWithAlert) {
    BookingSchedulerDto rangeBookingSched =
        rangebookings.stream()
            .filter(
                booking ->
                    booking.getBooking().getId().equals(bookingWithAlert.getBooking().getId()))
            .findFirst()
            .orElse(null);
    if (rangeBookingSched != null) {
      rangeBookingSched.setAlert(bookingWithAlert.getAlert());
    }
  }

  public SchedulerInfo getSchedulerInfo(Instant startDate, Instant endDate)
      throws InstanceNotFoundException {
    SchedulerInfo schedulerInfo = new SchedulerInfo();
    List<Booking> bookingsOnRange =
        bookingRepository.findBookingsByDateRange(AuthUtils.getUsername(), startDate, endDate);
    List<BookingSchedulerDto> rangebookings = new ArrayList<>();
    List<BookingSchedulerDto> redAlertBookings = new ArrayList<>();
    List<BookingSchedulerDto> yellowAlertBookings = new ArrayList<>();
    Map<UUID, ApartmentInfo> apartmentInfoMap = new HashMap<>();
    Map<UUID, BookingSchedulerDto> bookingMap = new HashMap<>();
    // With this we got all bookings inside the range of the scheduler
    for (Booking booking : bookingsOnRange) {
      rangebookings.add(processBookingForScheduler(booking, apartmentInfoMap, bookingMap));
    }

    // Now we get the pending bookings until 5 days from now to check for alerts
    List<Booking> alertBookings =
        bookingRepository.advancedSearch(
            AuthUtils.getUsername(),
            null,
            List.of(BookingState.PENDING),
            null,
            Instant.now(clock).plusSeconds(5 * 24 * 3600),
            null);

    // Group by apartment in the alertBookingsMap, processing them for the scheduler
    Map<UUID, List<BookingSchedulerDto>> alertBookingsMap = new HashMap<>();

    for (Booking booking : alertBookings) {
      if (!alertBookingsMap.containsKey(booking.getApartment().getId())) {
        alertBookingsMap.put(booking.getApartment().getId(), new ArrayList<>());
      }
      alertBookingsMap
          .get(booking.getApartment().getId())
          .add(processBookingForScheduler(booking, apartmentInfoMap, bookingMap));
    }

    for (UUID apartmentId : alertBookingsMap.keySet()) {
      List<BookingSchedulerDto> aptBookings = alertBookingsMap.get(apartmentId);
      aptBookings.sort(Comparator.comparing(b -> b.getBooking().getStartDate()));
      for (int i = 0; i < aptBookings.size(); i++) {
        BookingSchedulerDto previousBookingSched;
        BookingSchedulerDto currentBookingSched = aptBookings.get(i);
        // If its the next pending booking and the apartment is ready, we dont care about cleaning
        // tasks (override)
        if (currentBookingSched
                .getBooking()
                .getId()
                .equals(apartmentInfoMap.get(apartmentId).nextPendingBooking.getId())
            && apartmentInfoMap.get(apartmentId).state.isReady()) {
          aptBookings.get(i).setApartmentReady(true);
          continue;
        }
        if (i == 0) {
          previousBookingSched =
              processBookingForScheduler(
                  bookingRepository
                      .findFirstBookingBeforeDateWithState(
                          AuthUtils.getAuthUser().getId(),
                          apartmentId,
                          currentBookingSched.getBooking().getStartDate(),
                          null)
                      .orElse(null),
                  apartmentInfoMap,
                  bookingMap);
        } else {
          previousBookingSched = aptBookings.get(i - 1);
        }
        if (previousBookingSched == null) {
          continue;
        }
        Instant redFlagDateLimit = Instant.now(clock).plusSeconds(2 * 24 * 3600);
        if (currentBookingSched.getBooking().getStartDate().isBefore(redFlagDateLimit)) {
          if (!previousBookingSched.getUnassignedTasks().isEmpty()) {
            currentBookingSched.setAlert(Alert.DAYS_LEFT_2_UNASSIGNED);
            passAlertsToRangeBooking(rangebookings, currentBookingSched);
            redAlertBookings.add(currentBookingSched);
            continue;
          }
          if (previousBookingSched.isHasUnfinishedTasks()) {
            currentBookingSched.setAlert(Alert.DAYS_LEFT_2_NOT_COMPLETED);
            passAlertsToRangeBooking(rangebookings, currentBookingSched);
            redAlertBookings.add(currentBookingSched);
            continue;
          }
        }
        Instant yellowFlagDateLimit = Instant.now(clock).plusSeconds(5 * 24 * 3600);
        if (currentBookingSched.getBooking().getStartDate().isBefore(yellowFlagDateLimit)) {
          if (!previousBookingSched.getUnassignedTasks().isEmpty()) {
            currentBookingSched.setAlert(Alert.DAYS_LEFT_5_UNASSIGNED);
            passAlertsToRangeBooking(rangebookings, currentBookingSched);
            yellowAlertBookings.add(currentBookingSched);
          }
        }
      }
    }

    schedulerInfo.setBookings(rangebookings);
    schedulerInfo.setRedAlertBookings(redAlertBookings);
    schedulerInfo.setYellowAlertBookings(yellowAlertBookings);

    // We get the assignments and for each one, we calculate the limit dates (previous booking end
    // and next booking start)
    schedulerInfo.setAssignments(
        assignmentRepository
            .findByApartmentAndStateAndDateRangeAndExtra(
                AuthUtils.getUsername(), null, null, startDate, endDate, null)
            .stream()
            .map(
                assignment -> {
                  SimpleBookingSchedulerDto previousBooking =
                      new SimpleBookingSchedulerDto(
                          bookingRepository
                              .findFirstBookingBeforeDateWithState(
                                  AuthUtils.getAuthUser().getId(),
                                  assignment.getTask().getApartment().getId(),
                                  assignment.getStartDate(),
                                  null)
                              .orElse(null));

                  Booking nextBooking =
                      bookingRepository
                          .findFirstBookingAfterDateWithState(
                              AuthUtils.getAuthUser().getId(),
                              assignment.getTask().getApartment().getId(),
                              assignment.getStartDate(),
                              null)
                          .orElse(null);
                  SimpleBookingSchedulerDto nextBookingDto = null;

                  if (nextBooking != null) {
                    if (bookingMap.get(nextBooking.getId()) != null) {
                      nextBookingDto =
                          new SimpleBookingSchedulerDto(bookingMap.get(nextBooking.getId()));
                    } else {
                      nextBookingDto = new SimpleBookingSchedulerDto(nextBooking);
                    }
                  }

                  return new AssignmentForSchedulerDto(assignment, previousBooking, nextBookingDto);
                })
            .collect(Collectors.toSet()));
    schedulerInfo.setExtraTasks(
        taskRepository.findExtraTasksNotAssigned(AuthUtils.getUsername()).stream()
            .map(TaskDto::new)
            .collect(Collectors.toList()));
    return schedulerInfo;
  }
}
