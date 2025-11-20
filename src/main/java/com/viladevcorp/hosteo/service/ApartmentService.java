package com.viladevcorp.hosteo.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.forms.ApartmentCreateForm;
import com.viladevcorp.hosteo.model.forms.ApartmentSearchForm;
import com.viladevcorp.hosteo.model.forms.ApartmentUpdateForm;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public Apartment createApartment(ApartmentCreateForm form) {
        User creator = userRepository.findByUsername(AuthUtils.getUsername());
        form.setCreatedBy(creator);
        return apartmentRepository
                .save(new Apartment(form));
    }

    public Apartment updateApartment(ApartmentUpdateForm form)
            throws InstanceNotFoundException, NotAllowedResourceException {
        Apartment apartment = getApartmentById(form.getId());
        BeanUtils.copyProperties(form, apartment, "id");
        return apartmentRepository.save(apartment);
    }

    public Apartment getApartmentById(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[ApartmentService.getApartmentById] - Apartment not found with id: {}", id);
                    return new InstanceNotFoundException("Apartment not found with id: " + id);
                });
        if (apartment.getCreatedBy().getUsername().equals(AuthUtils.getUsername())) {
            return apartment;
        } else {
            log.error("[ApartmentService.getApartmentById] - Not allowed to access apartment with id: {}", id);
            throw new NotAllowedResourceException("You are not allowed to access this apartment.");
        }
    }

    public List<Apartment> findApartments(ApartmentSearchForm form) {
        String apartmentName = form.getName() == null || form.getName().isEmpty() ? null
                : "%" + form.getName().toLowerCase() + "%";

        PageRequest pageRequest = null;
        if (form.getPageSize() > 0) {
            int pageNumber = form.getPageNumber() <= 0 ? 0 : form.getPageNumber();
            pageRequest = PageRequest.of(pageNumber, form.getPageSize());
        }
        return apartmentRepository.advancedSearch(AuthUtils.getUsername(),
                apartmentName, form.getState(), null, pageRequest);
    }

    public PageMetadata getApartmentsMetadata(ApartmentSearchForm form) {
        String apartmentName = form.getName() == null || form.getName().isEmpty() ? null
                : "%" + form.getName().toLowerCase() + "%";
        int totalRows = apartmentRepository.advancedCount(AuthUtils.getUsername(),
                apartmentName, form.getState(), null);
        int totalPages = form.getPageSize() <= 0 ? 10
                : ((Double) Math.ceil((double) totalRows /
                        form.getPageSize())).intValue();
        return new PageMetadata(totalPages, totalRows);
    }

    public void deleteApartment(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Apartment apartment = getApartmentById(id);
        apartmentRepository.delete(apartment);
    }

}
