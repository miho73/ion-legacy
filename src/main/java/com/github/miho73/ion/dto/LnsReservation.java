package com.github.miho73.ion.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(schema = "ns", name = "lns_reservation")
public class LnsReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;

    @Column(name = "at_date", nullable = false)
    private LocalDate lnsDate;

    @Column(name = "at_time", nullable = false)
    private NsRecord.NS_TIME lnsTime;

    @Length(min = 2, message = "invalid seat")
    @Length(max = 2, message = "invalid seat")
    @Column(name = "seat", nullable = false)
    private String seat;

    @Column(name = "uuid", nullable = false)
    private int uuid;
}
