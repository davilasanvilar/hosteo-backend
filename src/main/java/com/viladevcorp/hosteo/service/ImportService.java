package com.viladevcorp.hosteo.service;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.types.ConflictType;
import com.viladevcorp.hosteo.repository.*;
import com.viladevcorp.hosteo.utils.ServiceUtils;

import java.io.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ImportService {

  private final ImpBookingRepository impBookingRepository;
  private final BookingRepository bookingRepository;
  private final AssignmentRepository assignmentRepository;
  private final ApartmentRepository apartmentRepository;

  @Autowired
  public ImportService(
      ImpBookingRepository impBookingRepository,
      BookingRepository bookingRepository,
      AssignmentRepository assignmentRepository,
      ApartmentRepository apartmentRepository) {
    this.apartmentRepository = apartmentRepository;
    this.bookingRepository = bookingRepository;
    this.impBookingRepository = impBookingRepository;
    this.assignmentRepository = assignmentRepository;
  }

  public static final int AIRBNB_START_DATE_POSITION = 4;
  public static final int AIRBNB_END_DATE_POSITION = 5;
  public static final int AIRBNB_GUEST_POSITION = 7;
  public static final int AIRBNB_APARTMENT_POSITION = 8;

  public static final String AIRBNB_CHECKIN_TIME = "15:00";
  public static final String AIRBNB_CHECKOUT_TIME = "11:00";

  public static final String AIRBNB_DATE_FORMAT = "MM/dd/yyyy HH:mm";
  public static final char AIRBNB_SEPARATOR = ',';

  public static final int BOOKING_START_DATE_POSITION = 4;
  public static final int BOOKING_END_DATE_POSITION = 5;
  public static final int BOOKING_GUEST_POSITION = 2;
  public static final int BOOKING_APARTMENT_POSITION = 0;
  public static final int BOOKING_STATUS_POSITION = 7;

  public static final String BOOKING_CHECKIN_TIME = "15:00";
  public static final String BOOKING_CHECKOUT_TIME = "11:00";
  public static final String BOOKING_DATE_FORMAT = "d MMMM yyyy HH:mm";
  public static final char BOOKING_SEPARATOR = ';';

  public List<ImpBooking> importAirbnbBookings(File importFile) throws IOException, CsvException {
    try (BufferedReader br = new BufferedReader(new FileReader(importFile))) {
      CSVParser parser = new CSVParserBuilder().withSeparator(AIRBNB_SEPARATOR).build();
      try (CSVReader csvReader =
          new CSVReaderBuilder(br).withSkipLines(1).withCSVParser(parser).build(); ) {

        Map<String, Apartment> apartmentCache = new HashMap<>();
        DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(AIRBNB_DATE_FORMAT).withZone(ZoneOffset.UTC);

        return csvReader.readAll().stream()
            .map(
                line -> {
                  Apartment apartment =
                      apartmentCache.computeIfAbsent(
                          line[AIRBNB_APARTMENT_POSITION],
                          airbnbId -> apartmentRepository.findByAirbnbId(airbnbId).orElse(null));
                  if (apartment == null) {
                    return null;
                  }
                  Instant startDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[AIRBNB_START_DATE_POSITION] + " " + AIRBNB_CHECKIN_TIME));
                  Instant endDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[AIRBNB_END_DATE_POSITION] + " " + AIRBNB_CHECKOUT_TIME));
                  Conflict conflict;
                  Booking bookingConflict =
                      ServiceUtils.getBookingConflict(
                          bookingRepository, apartment.getId(), startDate, endDate, null);
                  if (bookingConflict != null) {
                    conflict = new Conflict(ConflictType.BOOKING_CONFLICT, bookingConflict);
                  } else {
                    Assignment assignmentConflict =
                        ServiceUtils.getAssignmentConflict(
                            assignmentRepository, apartment.getId(), startDate, endDate, null);
                    if (assignmentConflict != null) {
                      conflict = new Conflict(ConflictType.ASSIGNMENT_CONFLICT, assignmentConflict);
                    } else {
                      conflict = null;
                    }
                  }

                  ImpBooking impBooking = new ImpBooking();
                  impBooking.setApartment(apartment);
                  impBooking.setStartDate(startDate);
                  impBooking.setEndDate(endDate);
                  impBooking.setName(line[AIRBNB_GUEST_POSITION]);
                  impBooking.setConflict(conflict);
                  return impBooking;
                })
            .filter(Objects::nonNull)
            .toList();
      }
    } catch (IOException | CsvException e) {
      log.error("Error reading import file: {}", e.getMessage());
      throw e;
    }
  }

  public List<ImpBooking> importBookingBookings(File importFile) throws IOException, CsvException {
    try (BufferedReader br = new BufferedReader(new FileReader(importFile))) {
      CSVParser parser = new CSVParserBuilder().withSeparator(BOOKING_SEPARATOR).build();
      try (CSVReader csvReader =
          new CSVReaderBuilder(br).withSkipLines(1).withCSVParser(parser).build(); ) {
        Map<String, Apartment> apartmentCache = new HashMap<>();
        DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(BOOKING_DATE_FORMAT)
                .withZone(ZoneOffset.UTC)
                .withLocale(Locale.ENGLISH);
        List<String[]> allLines = csvReader.readAll();
        return allLines.stream()
            .map(
                line -> {
                  if (line[BOOKING_STATUS_POSITION].equalsIgnoreCase("Cancelled")) {
                    return null;
                  }
                  Apartment apartment =
                      apartmentCache.computeIfAbsent(
                          line[BOOKING_APARTMENT_POSITION],
                          bookingId -> apartmentRepository.findByBookingId(bookingId).orElse(null));
                  if (apartment == null) {
                    return null;
                  }
                  Instant startDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[BOOKING_START_DATE_POSITION] + " " + BOOKING_CHECKIN_TIME));
                  Instant endDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[BOOKING_END_DATE_POSITION] + " " + BOOKING_CHECKOUT_TIME));
                  Conflict conflict;
                  Booking bookingConflict =
                      ServiceUtils.getBookingConflict(
                          bookingRepository, apartment.getId(), startDate, endDate, null);
                  if (bookingConflict != null) {
                    conflict = new Conflict(ConflictType.BOOKING_CONFLICT, bookingConflict);
                  } else {
                    Assignment assignmentConflict =
                        ServiceUtils.getAssignmentConflict(
                            assignmentRepository, apartment.getId(), startDate, endDate, null);
                    if (assignmentConflict != null) {
                      conflict = new Conflict(ConflictType.ASSIGNMENT_CONFLICT, assignmentConflict);
                    } else {
                      conflict = null;
                    }
                  }

                  ImpBooking impBooking = new ImpBooking();
                  impBooking.setApartment(apartment);
                  impBooking.setStartDate(startDate);
                  impBooking.setEndDate(endDate);
                  impBooking.setName(line[BOOKING_GUEST_POSITION]);
                  impBooking.setConflict(conflict);
                  return impBooking;
                })
            .filter(Objects::nonNull)
            .toList();
      }
    } catch (IOException | CsvException e) {
      log.error("Error reading import file: {}", e.getMessage());
      throw e;
    }
  }
}
