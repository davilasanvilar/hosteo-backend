package com.viladevcorp.hosteo.model;

import java.time.Instant;

import com.viladevcorp.hosteo.model.dto.BaseEntityDto;
import com.viladevcorp.hosteo.model.dto.BookingWithAssignmentsDto;
import com.viladevcorp.hosteo.model.dto.BookingDto;
import jakarta.persistence.*;

import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Booking extends BaseEntity {

  @NotNull
  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Apartment apartment;

  @NotNull
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Instant startDate;

  @NotNull
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Instant endDate;

  @NotNull
  @Column(nullable = false)
  private String name;

  @NotNull
  @Column(nullable = false)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private BookingState state = BookingState.PENDING;

  @NotNull
  @Column(nullable = false)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private BookingSource source = BookingSource.NONE;

  @Override
  public BaseEntityDto toDto() {
    return new BookingDto(this);
  }
}
