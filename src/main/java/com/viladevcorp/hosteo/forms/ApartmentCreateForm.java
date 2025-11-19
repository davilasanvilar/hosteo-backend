package com.viladevcorp.hosteo.forms;

import com.viladevcorp.hosteo.model.Address;
import com.viladevcorp.hosteo.model.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ApartmentCreateForm {

    @NotNull
    @NotBlank
    private String name;
    private String airbnbId;
    private String bookingId;
    private Address address;
    private boolean visible;
    private User createdBy;
}
