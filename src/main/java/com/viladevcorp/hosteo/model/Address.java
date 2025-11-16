package com.viladevcorp.hosteo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Address {

    private String apartmentNumber;
    private String number;
    private String street;
    private String city;
    private String country;
    private String zipCode;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Address address = (Address) o;

        if (apartmentNumber != null ? !apartmentNumber.equals(address.apartmentNumber)
                : address.apartmentNumber != null)
            return false;
        if (number != null ? !number.equals(address.number) : address.number != null)
            return false;
        if (street != null ? !street.equals(address.street) : address.street != null)
            return false;
        if (city != null ? !city.equals(address.city) : address.city != null)
            return false;
        if (country != null ? !country.equals(address.country) : address.country != null)
            return false;
        if (zipCode != null ? !zipCode.equals(address.zipCode) : address.zipCode != null)
            return false;
        return true;
    }
}
