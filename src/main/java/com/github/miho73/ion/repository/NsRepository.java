package com.github.miho73.ion.repository;

import com.github.miho73.ion.dto.NsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NsRepository extends JpaRepository<NsRecord, Integer> {
    List<NsRecord> findByUuidAndNsDateOrderByNsTimeAsc(int uuid, LocalDate nsDate);
    Optional<NsRecord> findByUuidAndNsDateAndNsTime(int uuid, LocalDate nsDate, NsRecord.NS_TIME nsTime);

    void deleteByUuidAndNsTimeAndNsDate(int uuid, NsRecord.NS_TIME time, LocalDate nsDate);
}
