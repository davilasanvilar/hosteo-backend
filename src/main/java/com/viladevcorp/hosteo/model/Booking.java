package com.viladevcorp.hosteo.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  private double price;

  @NotNull
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private boolean paid;

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
}
