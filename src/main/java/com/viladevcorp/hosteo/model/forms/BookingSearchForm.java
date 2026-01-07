package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.BookingState;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingSearchForm {

  private String apartmentName;

  private List<BookingState> states;

  private Instant startDate;

  private Instant endDate;

  private int pageNumber;

  private int pageSize;
}
