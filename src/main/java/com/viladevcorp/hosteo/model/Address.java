package com.viladevcorp.hosteo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Address {

  private String apartmentNumber;
  private String number;
  private String street;
  private String city;
  private String country;
  private String zipCode;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Address address = (Address) o;

    if (!Objects.equals(apartmentNumber, address.apartmentNumber)) return false;
    if (!Objects.equals(number, address.number)) return false;
    if (!Objects.equals(street, address.street)) return false;
    if (!Objects.equals(city, address.city)) return false;
    if (!Objects.equals(country, address.country)) return false;
    if (!Objects.equals(zipCode, address.zipCode)) return false;
    return true;
  }
}
