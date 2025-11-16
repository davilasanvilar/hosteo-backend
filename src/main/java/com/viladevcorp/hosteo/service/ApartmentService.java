package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.forms.CreateApartmentForm;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApartmentService {

    private ApartmentRepository apartmentRepository;

    private UserRepository userRepository;

    @Autowired
    public ApartmentService(ApartmentRepository apartmentRepository, UserRepository userRepository) {
        this.apartmentRepository = apartmentRepository;
        this.userRepository = userRepository;
    }

    public Apartment createApartment(CreateApartmentForm form) {
        User creator = userRepository.findByUsername(AuthUtils.getUsername());
        form.setCreatedBy(creator);
        return apartmentRepository
                .save(new Apartment(form));
    }

    public Apartment getApartmentById(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {

        Apartment apartment = apartmentRepository.findById(id).orElseThrow(InstanceNotFoundException::new);
        if (apartment.getCreatedBy().getUsername().equals(AuthUtils.getUsername())) {
            return apartment;
        } else {
            throw new NotAllowedResourceException("You are not allowed to access this apartment.");
        }
    }

    // public List<ApartmentDto> findActivities(ApartmentSearchForm form) {
    // String apartmentName = form.getName() == null || form.getName().isEmpty() ?
    // null
    // : "%" + form.getName().toLowerCase() + "%";
    // return apartmentRepository.advancedSearch(AuthUtils.getUsername(),
    // apartmentName,
    // PageRequest.of(form.getPage(), form.getPageSize()));
    // }

    // public PageMetadata getActivitiesMetadata(ApartmentSearchForm form) {
    // String apartmentName = form.getName() == null || form.getName().isEmpty() ?
    // null
    // : "%" + form.getName().toLowerCase() + "%";
    // int totalRows = apartmentRepository.advancedCount(AuthUtils.getUsername(),
    // apartmentName);
    // int totalPages = ((Double) Math.ceil((double) totalRows /
    // form.getPageSize())).intValue();
    // return new PageMetadata(totalPages, totalRows);
    // }

}
