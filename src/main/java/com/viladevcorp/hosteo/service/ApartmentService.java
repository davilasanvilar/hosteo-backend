package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.repository.BookingRepository;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.forms.ApartmentCreateForm;
import com.viladevcorp.hosteo.model.forms.ApartmentSearchForm;
import com.viladevcorp.hosteo.model.forms.ApartmentUpdateForm;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ApartmentService {

  private final ApartmentRepository apartmentRepository;

  @Autowired
  public ApartmentService(ApartmentRepository apartmentRepository) {
    this.apartmentRepository = apartmentRepository;
  }

  public Apartment createApartment(ApartmentCreateForm form) {
    Apartment apartment =
        Apartment.builder()
            .name(form.getName())
            .airbnbId(form.getAirbnbId())
            .bookingId(form.getBookingId())
            .address(form.getAddress())
            .state(ApartmentState.READY)
            .visible(form.isVisible())
            .build();
    return apartmentRepository.save(apartment);
  }

  public Apartment updateApartment(ApartmentUpdateForm form)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Apartment apartment = getApartmentById(form.getId());
    BeanUtils.copyProperties(form, apartment, "id");
    return apartmentRepository.save(apartment);
  }

  public Apartment getApartmentById(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Apartment apartment =
        apartmentRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error(
                      "[ApartmentService.getApartmentById] - Apartment not found with id: {}", id);
                  return new InstanceNotFoundException("Apartment not found with id: " + id);
                });
    try {
      AuthUtils.checkIfCreator(apartment, "apartment");
    } catch (NotAllowedResourceException e) {
      log.error(
          "[ApartmentService.getApartmentById] - Not allowed to access apartment with id: {}", id);
      throw e;
    }
    return apartment;
  }

  public List<Apartment> findApartments(ApartmentSearchForm form) {
    String apartmentName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";

    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return apartmentRepository.advancedSearch(
        AuthUtils.getUsername(), apartmentName, form.getState(), null, pageRequest);
  }

  public PageMetadata getApartmentsMetadata(ApartmentSearchForm form) {
    String apartmentName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    int totalRows =
        apartmentRepository.advancedCount(
            AuthUtils.getUsername(), apartmentName, form.getState(), null);
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteApartment(UUID id)
      throws InstanceNotFoundException, NotAllowedResourceException {
    Apartment apartment = getApartmentById(id);
    apartmentRepository.delete(apartment);
  }
}
