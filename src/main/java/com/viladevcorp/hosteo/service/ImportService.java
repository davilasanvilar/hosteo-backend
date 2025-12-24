package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.*;
import com.viladevcorp.hosteo.model.types.Alert;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.BookingState;
import com.viladevcorp.hosteo.repository.*;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.ServiceUtils;
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
public class ImportService {

  private final ImpBookingRepository impBookingRepository;
  private final BookingRepository bookingRepository;

  @Autowired
  public ImportService(
      ImpBookingRepository impBookingRepository, BookingRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
    this.impBookingRepository = impBookingRepository;
  }
}
