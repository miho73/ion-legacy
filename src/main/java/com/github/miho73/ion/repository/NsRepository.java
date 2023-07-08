package com.github.miho73.ion.repository;

import com.github.miho73.ion.dto.NsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NsRepository extends JpaRepository<NsRecord, Integer> {
    List<NsRecord> findByUuidAndNsDateOrderByNsTimeAsc(int uuid, LocalDate nsDate);
    List<NsRecord> findByUuidAndNsDateAndNsTime(int uuid, LocalDate nsDate, NsRecord.NS_TIME nsTime);

    void deleteByUuidAndNsTime(int uuid, NsRecord.NS_TIME nsTime);
}
