package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.types.Alert;
import com.viladevcorp.hosteo.model.types.EventSource;
import java.time.Instant;
import java.util.UUID;

import com.viladevcorp.hosteo.model.types.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class EventSchedulerDto extends BaseEntityDto {

  public EventSchedulerDto(Event event, Alert alert) {
    if (event == null) {
      return;
    }
    BeanUtils.copyProperties(event, this);
    this.alert = alert;
  }

  private UUID id;

  private EventType type;

  private Instant startDate;

  private Instant endDate;

  private String name;

  private EventSource source;

  private Alert alert;
}
