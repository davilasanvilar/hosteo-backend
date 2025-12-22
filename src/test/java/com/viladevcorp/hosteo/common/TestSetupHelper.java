package com.viladevcorp.hosteo.common;

import java.util.ArrayList;
import java.util.List;

import com.viladevcorp.hosteo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.Template;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.service.AuthService;

import lombok.Getter;
import lombok.Setter;

import static com.viladevcorp.hosteo.common.TestConstants.*;

@Component
@Getter
@Setter
public class TestSetupHelper {

  @Autowired AuthService authService;

  @Autowired ValidationCodeRepository validationCodeRepository;

  @Autowired WorkerRepository workerRepository;

  @Autowired UserRepository userRepository;

  @Autowired ApartmentRepository apartmentRepository;

  @Autowired BookingRepository bookingRepository;

  @Autowired TemplateRepository templateRepository;

  @Autowired TaskRepository taskRepository;

  @Autowired AssignmentRepository assignmentRepository;

  private List<User> testUsers;

  private List<Apartment> testApartments;

  private List<Booking> testBookings;

  private List<Worker> testWorkers;

  private List<Template> testTemplates;

  private List<Task> testTasks;

  private List<Assignment> testAssignments;

  public void createTestUsers() throws Exception {
    TestUtils.injectUserSession(null, userRepository);
    User us1 =
        authService.registerUser(
            ACTIVE_USER_EMAIL_1, ACTIVE_USER_USERNAME_1, ACTIVE_USER_PASSWORD_1);
    us1.setValidated(true);
    us1 = userRepository.save(us1);
    User us2 =
        authService.registerUser(
            ACTIVE_USER_EMAIL_2, ACTIVE_USER_USERNAME_2, ACTIVE_USER_PASSWORD_2);
    us2.setValidated(true);
    us2 = userRepository.save(us2);
    testUsers = List.of(us1, us2);
  }

  public void deleteAll() {
    assignmentRepository.deleteAll();
    taskRepository.deleteAll();
    templateRepository.deleteAll();
    bookingRepository.deleteAll();
    apartmentRepository.deleteAll();
    workerRepository.deleteAll();
    validationCodeRepository.deleteAll();
    userRepository.deleteAll();
  }

  public void resetTestBase() throws Exception {
    deleteAll();
    createTestUsers();
  }

  public void createTestApartments() {
    if (apartmentRepository.count() > 0) {
      return;
    }
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

    Apartment apt1 =
        apartmentRepository.save(
            Apartment.builder()
                .name(CREATED_APARTMENT_NAME_1)
                .state(CREATE_APARTMENT_STATE_1)
                .build());

    Apartment apt2 =
        apartmentRepository.save(
            Apartment.builder()
                .name(CREATED_APARTMENT_NAME_2)
                .state(CREATE_APARTMENT_STATE_2)
                .build());

    Apartment apt3 =
        apartmentRepository.save(
            Apartment.builder()
                .name(CREATED_APARTMENT_NAME_3)
                .state(CREATE_APARTMENT_STATE_3)
                .build());

    Apartment apt4 =
        apartmentRepository.save(
            Apartment.builder()
                .name(CREATED_APARTMENT_NAME_4)
                .state(CREATE_APARTMENT_STATE_4)
                .build());

    testApartments = List.of(apt1, apt2, apt3, apt4);
  }

  public void resetTestApartments() {
    apartmentRepository.deleteAll();
    createTestApartments();
  }

  public void createTestWorkers() {
    if (workerRepository.count() > 0) {
      return;
    }
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

    Worker wk1 =
        workerRepository.save(
            Worker.builder()
                .name(CREATED_WORKER_NAME_1)
                .language(CREATED_WORKER_LANGUAGE_1)
                .build());

    Worker wk2 =
        workerRepository.save(
            Worker.builder()
                .name(CREATED_WORKER_NAME_2)
                .language(CREATED_WORKER_LANGUAGE_2)
                .build());

    Worker wk3 =
        workerRepository.save(
            Worker.builder()
                .name(CREATED_WORKER_NAME_3)
                .language(CREATED_WORKER_LANGUAGE_3)
                .build());

    Worker wk4 =
        workerRepository.save(
            Worker.builder()
                .name(CREATED_WORKER_NAME_4)
                .language(CREATED_WORKER_LANGUAGE_4)
                .build());

    testWorkers = List.of(wk1, wk2, wk3, wk4);
  }

  public void resetTestWorkers() {
    workerRepository.deleteAll();
    createTestWorkers();
  }

  public void createTestBookings() throws Exception {

    if (bookingRepository.count() > 0) {
      return;
    }

    createTestApartments();

    Booking bk1 =
        bookingRepository.save(
            Booking.builder()
                .apartment(testApartments.get(CREATED_BOOKING_APARTMENT_POSITION_1))
                .name(CREATED_BOOKING_NAME_1)
                .startDate(TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_1))
                .endDate(TestUtils.dateStrToInstant(CREATED_BOOKING_END_DATE_1))
                .price(CREATED_BOOKING_PRICE_1)
                .paid(false)
                .state(CREATED_BOOKING_STATE_1)
                .build());

    Booking bk2 =
        bookingRepository.save(
            Booking.builder()
                .apartment(testApartments.get(CREATED_BOOKING_APARTMENT_POSITION_2))
                .name(CREATED_BOOKING_NAME_2)
                .startDate(TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_2))
                .endDate(TestUtils.dateStrToInstant(CREATED_BOOKING_END_DATE_2))
                .price(CREATED_BOOKING_PRICE_2)
                .paid(false)
                .state(CREATED_BOOKING_STATE_2)
                .build());

    Booking bk3 =
        bookingRepository.save(
            Booking.builder()
                .apartment(testApartments.get(CREATED_BOOKING_APARTMENT_POSITION_3))
                .name(CREATED_BOOKING_NAME_3)
                .startDate(TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_3))
                .endDate(TestUtils.dateStrToInstant(CREATED_BOOKING_END_DATE_3))
                .price(CREATED_BOOKING_PRICE_3)
                .paid(false)
                .state(CREATED_BOOKING_STATE_3)
                .build());

    Booking bk4 =
        bookingRepository.save(
            Booking.builder()
                .apartment(testApartments.get(CREATED_BOOKING_APARTMENT_POSITION_4))
                .name(CREATED_BOOKING_NAME_4)
                .startDate(TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_4))
                .endDate(TestUtils.dateStrToInstant(CREATED_BOOKING_END_DATE_4))
                .price(CREATED_BOOKING_PRICE_4)
                .paid(false)
                .state(CREATED_BOOKING_STATE_4)
                .build());

    Booking bk5 =
        bookingRepository.save(
            Booking.builder()
                .apartment(testApartments.get(CREATED_BOOKING_APARTMENT_POSITION_5))
                .name(CREATED_BOOKING_NAME_5)
                .startDate(TestUtils.dateStrToInstant(CREATED_BOOKING_START_DATE_5))
                .endDate(TestUtils.dateStrToInstant(CREATED_BOOKING_END_DATE_5))
                .price(CREATED_BOOKING_PRICE_5)
                .paid(false)
                .state(CREATED_BOOKING_STATE_5)
                .build());

    testBookings = List.of(bk1, bk2, bk3, bk4, bk5);
  }

  public void resetTestBookings() throws Exception {
    bookingRepository.deleteAll();
    apartmentRepository.deleteAll();
    createTestBookings();
  }

  public void createTestTemplates() {

    if (templateRepository.count() > 0) {
      return;
    }
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

    Template tmpl1 =
        Template.builder()
            .name(CREATED_TEMPLATE_NAME_1)
            .category(CREATED_TEMPLATE_CATEGORY_1)
            .duration(CREATED_TEMPLATE_DURATION_1)
            .steps(new ArrayList<>())
            .build();
    tmpl1 = templateRepository.save(tmpl1);

    Template tmpl2 =
        Template.builder()
            .name(CREATED_TEMPLATE_NAME_2)
            .category(CREATED_TEMPLATE_CATEGORY_2)
            .duration(CREATED_TEMPLATE_DURATION_2)
            .steps(new ArrayList<>())
            .build();
    tmpl2 = templateRepository.save(tmpl2);

    Template tmpl3 =
        Template.builder()
            .name(CREATED_TEMPLATE_NAME_3)
            .category(CREATED_TEMPLATE_CATEGORY_3)
            .duration(CREATED_TEMPLATE_DURATION_3)
            .steps(new ArrayList<>())
            .build();

    tmpl3 = templateRepository.save(tmpl3);
    testTemplates = List.of(tmpl1, tmpl2, tmpl3);
  }

  public void resetTestTemplates() {
    templateRepository.deleteAll();
    createTestTemplates();
  }

  public void createTestTasks() {
    if (taskRepository.count() > 0) {
      return;
    }
    createTestApartments();

    Task task1 =
        Task.builder()
            .name(CREATED_TASK_NAME_1)
            .category(CREATED_TASK_CATEGORY_1)
            .duration(CREATED_TASK_DURATION_1)
            .extra(CREATED_TASK_EXTRA_TASK_1)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_1))
            .steps(new ArrayList<>())
            .build();
    task1 = taskRepository.save(task1);

    Task task2 =
        Task.builder()
            .name(CREATED_TASK_NAME_2)
            .category(CREATED_TASK_CATEGORY_2)
            .duration(CREATED_TASK_DURATION_2)
            .extra(CREATED_TASK_EXTRA_TASK_2)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_2))
            .steps(new ArrayList<>())
            .build();
    task2 = taskRepository.save(task2);

    Task task3 =
        Task.builder()
            .name(CREATED_TASK_NAME_3)
            .category(CREATED_TASK_CATEGORY_3)
            .duration(CREATED_TASK_DURATION_3)
            .extra(CREATED_TASK_EXTRA_TASK_3)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_3))
            .steps(new ArrayList<>())
            .build();
    task3 = taskRepository.save(task3);

    Task task4 =
        Task.builder()
            .name(CREATED_TASK_NAME_4)
            .category(CREATED_TASK_CATEGORY_4)
            .duration(CREATED_TASK_DURATION_4)
            .extra(CREATED_TASK_EXTRA_TASK_4)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_4))
            .steps(new ArrayList<>())
            .build();
    task4 = taskRepository.save(task4);

    Task task5 =
        Task.builder()
            .name(CREATED_TASK_NAME_5)
            .category(CREATED_TASK_CATEGORY_5)
            .duration(CREATED_TASK_DURATION_5)
            .extra(CREATED_TASK_EXTRA_TASK_5)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_5))
            .steps(new ArrayList<>())
            .build();
    task5 = taskRepository.save(task5);

    testTasks = List.of(task1, task2, task3, task4, task5);
  }

  public void resetTestTasks() {
    taskRepository.deleteAll();
    apartmentRepository.deleteAll();
    createTestTasks();
  }

  public void createTestAssignments() throws Exception {

    if (assignmentRepository.count() > 0) {
      return;
    }

    createTestWorkers();
    createTestTasks();
    createTestBookings();

    Assignment assignment1 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_1))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_1))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_1)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_1).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_1))
            .state(CREATED_ASSIGNMENT_STATE_1)
            .build();
    assignment1 = assignmentRepository.save(assignment1);

    Assignment assignment2 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_2))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_2))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_2)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_2).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_2))
            .state(CREATED_ASSIGNMENT_STATE_2)
            .build();
    assignment2 = assignmentRepository.save(assignment2);

    Assignment assignment3 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_3))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_3))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_3)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_3).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_3))
            .state(CREATED_ASSIGNMENT_STATE_3)
            .build();
    assignment3 = assignmentRepository.save(assignment3);

    Assignment assignment4 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_4))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_4))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_4)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_4).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_4))
            .state(CREATED_ASSIGNMENT_STATE_4)
            .build();

    assignment4 = assignmentRepository.save(assignment4);

    Assignment assignment5 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_5))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_5))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_5)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_5).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_5))
            .state(CREATED_ASSIGNMENT_STATE_5)
            .build();
    assignment5 = assignmentRepository.save(assignment5);

    testAssignments = List.of(assignment1, assignment2, assignment3, assignment4, assignment5);
  }

  public void deleteTestAssignments() {
    assignmentRepository.deleteAll();
    taskRepository.deleteAll();
    workerRepository.deleteAll();
    bookingRepository.deleteAll();
    apartmentRepository.deleteAll();
  }

  public void resetAssignments() throws Exception {
    deleteTestAssignments();
    createTestAssignments();
  }
}
