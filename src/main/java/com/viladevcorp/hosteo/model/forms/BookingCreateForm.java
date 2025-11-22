package com.viladevcorp.hosteo.model.forms;

import java.util.Calendar;
import java.util.UUID;

import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingCreateForm {

    @NotNull
    private UUID apartmentId;

    @NotNull
    private Calendar startDate;

    @NotNull
    private Calendar endDate;

    private Double price;

    @NotBlank
    @NotNull
    private String name;

    private boolean paid = false;

    @NotNull
    private BookingState state = BookingState.PENDING;

    @NotNull
    private BookingSource source = BookingSource.NONE;
}
