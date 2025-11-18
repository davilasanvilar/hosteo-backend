package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.forms.CreateApartmentForm;
import com.viladevcorp.hosteo.model.jsonconverters.AddressJsonConverter;
import com.viladevcorp.hosteo.model.types.ApartmentState;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
public class Apartment extends BaseEntity {

    public Apartment(CreateApartmentForm form) {
        this.name = form.getName();
        this.airbnbId = form.getAirbnbId();
        this.bookingId = form.getBookingId();
        this.address = form.getAddress();
        this.state = ApartmentState.READY;
        this.visible = form.isVisible();
        this.setCreatedBy(form.getCreatedBy());
    }

    @NotNull
    @NotBlank
    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String airbnbId;
    @Column(unique = true)
    private String bookingId;

    @Convert(converter = AddressJsonConverter.class)
    private Address address;

    @NotNull
    private ApartmentState state;

    private boolean visible = false;

}
