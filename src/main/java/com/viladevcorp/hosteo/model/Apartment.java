package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.forms.ApartmentCreateForm;
import com.viladevcorp.hosteo.model.jsonconverters.AddressJsonConverter;
import com.viladevcorp.hosteo.model.types.ApartmentState;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
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

    @Builder.Default
    private boolean visible = false;

}
