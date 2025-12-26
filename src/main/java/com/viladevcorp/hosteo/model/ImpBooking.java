package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.model.dto.BaseEntityDto;
import com.viladevcorp.hosteo.model.dto.BookingDto;
import com.viladevcorp.hosteo.model.dto.ImpBookingDto;
import com.viladevcorp.hosteo.model.jsonconverters.ConflictJsonConverter;
import com.viladevcorp.hosteo.model.types.BookingSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "imp_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpBooking extends BaseEntity {

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
  @Enumerated(EnumType.STRING)
  private BookingSource source;

  @Convert(converter = ConflictJsonConverter.class)
  @Column(columnDefinition = "TEXT")
  private Conflict conflict;

  private String creationError;

  @Override
  public BaseEntityDto toDto() {
    return new ImpBookingDto(this);
  }
}
