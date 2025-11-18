package com.viladevcorp.hosteo.controller;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
// import com.viladevcorp.hosteo.forms.ApartmentSearchForm;
import com.viladevcorp.hosteo.forms.CreateApartmentForm;
import com.viladevcorp.hosteo.forms.SearchApartmentForm;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.service.ApartmentService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.ValidationError;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @Autowired
    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @PostMapping("/apartment")
    public ResponseEntity<ApiResponse<Apartment>> createApartment(@Valid @RequestBody CreateApartmentForm form,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ValidationError> validationErrors = bindingResult.getAllErrors().stream()
                    .map(error -> new ValidationError(
                            error.getObjectName(),
                            error.getDefaultMessage()))
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>("Validation Failed", validationErrors));
        }
        Apartment apartment = apartmentService.createApartment(form);
        return ResponseEntity.ok().body(new ApiResponse<>(apartment));
    }

    @GetMapping("/apartment/{id}")
    public ResponseEntity<ApiResponse<Apartment>> getApartment(@PathVariable UUID id)
            throws InstanceNotFoundException, NotAllowedResourceException {
        Apartment apartment;
        try {
            apartment = apartmentService.getApartmentById(id);
            return ResponseEntity.ok().body(new ApiResponse<>(apartment));
        } catch (NotAllowedResourceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null, e.getMessage()));
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, e.getMessage()));
        }
    }

    @PostMapping("/apartments/search")
    public ResponseEntity<ApiResponse<Page<Apartment>>> searchApartments(@RequestBody SearchApartmentForm form) {
        List<Apartment> apartments = apartmentService.findApartments(form);
        PageMetadata pageMetadata = apartmentService.getApartmentsMetadata(form);
        Page<Apartment> page = new Page<>(apartments, pageMetadata.getTotalPages(), pageMetadata.getTotalRows());
        return ResponseEntity.ok().body(new ApiResponse<>(page));
    }

}