package com.github.miho73.ion.service;

import com.github.miho73.ion.dto.ResetPasswordReq;
import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.repository.ResetPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ResetPasswordService {
    @Autowired
    UserService userService;

    @Autowired
    ResetPasswordRepository resetPasswordRepository;

    public int getState(String id) {
        Optional<User> userOptional = userService.getUserById(id);
        if(userOptional.isEmpty()) return 2;
        User user = userOptional.get();
        Optional<ResetPasswordReq> rpq = resetPasswordRepository.findByUuid(user.getUid());
        if(rpq.isEmpty()) return 0;
        else return 3;
    }

    public boolean checkExistsForUser(int uid) {
        return resetPasswordRepository.findByUuid(uid).isPresent();
    }

    public void createRequest(int uid) {
        ResetPasswordReq rpq = new ResetPasswordReq();
        rpq.setUuid(uid);
        rpq.setStatus(ResetPasswordReq.RESET_PWD_STATUS.REQUESTED);
        rpq.setPrivateCode("Honey Moon");
        rpq.setRequestDate(LocalDate.now());
        resetPasswordRepository.save(rpq);
    }

    public Optional<ResetPasswordReq> getRequest(int uid) {
        Optional<ResetPasswordReq> rpq = resetPasswordRepository.findByUuid(uid);
        if(rpq.isPresent() && rpq.get().getStatus() == ResetPasswordReq.RESET_PWD_STATUS.REQUESTED) {
            resetPasswordRepository.watchPrivateCode(rpq.get().getUid());
        }
        return rpq;
    }
}
