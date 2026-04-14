package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateForm {

  @NotNull private UUID apartmentId;

  @NotNull private Instant startDate;

  @NotNull private Instant endDate;

  @NotBlank @NotNull private String name;

  @NotNull private EventType type;

  @Builder.Default private EventSource source = EventSource.NONE;

  @Builder.Default private EventState state = EventState.PENDING;
}
