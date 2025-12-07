package com.viladevcorp.hosteo.model;

import java.time.Instant;

import com.viladevcorp.hosteo.model.types.AssignmentState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Assignment extends BaseEntity {

  @NotNull
  @ManyToOne(optional = false)
  private Task task;

  @NotNull
  @ManyToOne(optional = false)
  private Booking booking;

  @NotNull
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Instant startDate;

  @NotNull
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Instant endDate;

  @NotNull
  @ManyToOne(optional = false)
  private Worker worker;

  @NotNull
  @Column(nullable = false)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private AssignmentState state = AssignmentState.PENDING;
}
