package com.github.miho73.ion.repository;

import com.github.miho73.ion.dto.StudentCodeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentCodeRecordRepository extends JpaRepository<StudentCodeRecord, Integer> {
}
