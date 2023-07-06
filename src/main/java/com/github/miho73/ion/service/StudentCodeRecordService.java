package com.github.miho73.ion.service;

import com.github.miho73.ion.dto.StudentCodeRecord;
import com.github.miho73.ion.repository.StudentCodeRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StudentCodeRecordService {
    @Autowired
    StudentCodeRecordRepository studentCodeRecordRepository;

    public void createRecord(StudentCodeRecord scr) {
        studentCodeRecordRepository.save(scr);
    }
}
