package com.viladevcorp.hosteo.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cascade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.viladevcorp.hosteo.model.jsonconverters.AddressJsonConverter;
import com.viladevcorp.hosteo.model.types.ApartmentState;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
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

    @NotNull
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String airbnbId;
    @Column(nullable = false, unique = true)
    private String bookingId;

    @Convert(converter = AddressJsonConverter.class)
    private Address address;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApartmentState state;

    @Builder.Default
    private boolean visible = false;

}
