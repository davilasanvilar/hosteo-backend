package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class SimpleApartmentDto extends BaseEntityDto {

  public SimpleApartmentDto(Apartment apartment) {
    if (apartment == null) {
      return;
    }
    BeanUtils.copyProperties(apartment, this);
  }

  private String name;

  private String airbnbId;

  private String bookingId;

  private Address address;

  private ApartmentState state = ApartmentState.READY;

  private boolean visible = true;
}
