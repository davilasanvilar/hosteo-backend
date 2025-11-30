package com.viladevcorp.hosteo.model.forms;

import java.time.Instant;

import com.viladevcorp.hosteo.model.types.BookingState;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingSearchForm {

    private String apartmentName;

    private BookingState state;

    private Instant startDate;

    private Instant endDate;

    private int pageNumber;

    private int pageSize;

}
