package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ApartmentDto extends BaseEntityDto {

  public ApartmentDto(Apartment apartment) {
    if (apartment == null) {
      return;
    }
    BeanUtils.copyProperties(apartment, this, "tasks");
    this.tasks =
        apartment.getTasks().stream()
            .sorted(Comparator.comparing(Task::getCreatedAt))
            .map(TaskDto::new)
            .toList();
  }

  private String name;

  private String airbnbId;

  private String bookingId;

  private Address address;

  private ApartmentState state = ApartmentState.READY;

  private boolean visible = true;

  private List<TaskDto> tasks = new ArrayList<>();
}
