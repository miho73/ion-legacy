package com.github.miho73.ion.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(schema = "ns", name = "ns_request")
public class NsRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;

    @Column(name = "ns_date", nullable = false)
    private LocalDate nsDate;

    @Column(name = "ns_req_time", nullable = false)
    private Timestamp nsReqTime;

    @Column(name = "ns_time", nullable = false)
    private NS_TIME nsTime;

    @Length(max = 30, message = "place too long")
    @Column(name = "ns_place", nullable = false)
    private String nsPlace;

    @Length(max = 10, message = "supervisor too long")
    @Column(name = "ns_supervisor", nullable = false)
    private String nsSupervisor;

    @Length(max = 30, message = "reason too long")
    @Column(name = "ns_reason", nullable = false)
    private String nsReason;

    @Column(name = "lns_req", nullable = false)
    private boolean lnsReq;

    @Column(name = "lns_req_uid")
    private int lnsReqUid;

    @Column(name = "uuid", nullable = false)
    private int uuid;

    @Column(name = "status", nullable = false)
    private NS_STATE nsState;

    public enum NS_STATE {
        REQUESTED,
        APPROVED,
        DENIED
    }

    public enum NS_TIME {
        N8,
        N1,
        N2
    }
}
