package com.github.miho73.ion.repository;

import com.github.miho73.ion.dto.LnsReservation;
import com.github.miho73.ion.dto.NsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LnsRepository extends JpaRepository<LnsReservation, Integer> {
    List<LnsReservation> findByLnsDateAndGrade(LocalDate lnsDate, int grade);

    List<LnsReservation> findByLnsTimeAndSeatAndLnsDateAndGrade(NsRecord.NS_TIME lnsTime, String seat, LocalDate lnsDate, int grade);
}
