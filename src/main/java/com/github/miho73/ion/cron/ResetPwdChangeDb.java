package com.github.miho73.ion.cron;

import com.github.miho73.ion.repository.ResetPasswordRepository;
import jakarta.transaction.Transactional;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResetPwdChangeDb {
    @Autowired
    ResetPasswordRepository resetPasswordRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDb() {
        resetPasswordRepository.truncateTable();
        log.info("Password reset table truncated");
    }
}
