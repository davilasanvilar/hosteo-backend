package com.viladevcorp.hosteo.model.forms;

import java.util.Calendar;

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

    private Calendar startDate;

    private Calendar endDate;

    private int pageNumber;

    private int pageSize;

}
