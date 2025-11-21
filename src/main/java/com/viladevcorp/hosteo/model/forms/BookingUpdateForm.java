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
public class BookingUpdateForm {

    @NotNull
    private UUID id;

    @NotNull
    private Calendar startDate;

    @NotNull
    private Calendar endDate;

    private Double price;

    @NotBlank
    @NotNull
    private String name;

    private boolean paid;

    @NotNull
    private BookingState state;

    @NotNull
    private BookingSource source;

}
