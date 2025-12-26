package com.viladevcorp.hosteo.model.forms;

import java.time.Instant;
import java.util.UUID;

import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateForm {

  @NotNull private UUID apartmentId;

  @NotNull private Instant startDate;

  @NotNull private Instant endDate;

  @NotBlank @NotNull private String name;

  @NotNull private BookingState state = BookingState.PENDING;

  @NotNull private BookingSource source = BookingSource.NONE;
}
