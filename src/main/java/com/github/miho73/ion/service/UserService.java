package com.github.miho73.ion.service;

import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    final
    PasswordEncoder passwordEncoder;

    final
    UserRepository userRepository;

    final
    SessionService sessionService;

    final
    RecaptchaService recaptchaService;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, SessionService sessionService, RecaptchaService recaptchaService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.recaptchaService = recaptchaService;
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public boolean existsUserById(String id) {
        return userRepository.findById(id).isEmpty();
    }

    public User.USER_STATUS DEFAULT_USER_STATE;

    @PostConstruct
    public void init() {
        DEFAULT_USER_STATE = User.USER_STATUS.INACTIVATED;
    }

    public User createUser(User user) {
        user.setJoinDate(new Timestamp(System.currentTimeMillis()));
        user.setStatus(DEFAULT_USER_STATE);
        user.setPrivilege(1);
        user.setScodeCFlag(false);
        return userRepository.save(user);
    }

    public void updatePrivilege(String id, int privilege) {
        log.info("IonID set privilege. id="+id+", priv="+privilege);
        userRepository.updatePrivilegeById(id, privilege);
    }

    public void updateLastLogin(int uid) {
        userRepository.updateLastLogin(uid, new Timestamp(System.currentTimeMillis()));
    }

    public Optional<User> getUserByScode(int scode) {
        int s = scode;
        int code = s % 100;
        s = (s - code)/100;
        int clas = s % 10;
        s = (s - clas) / 10;
        int grade = s;

        return userRepository.findByGradeAndClasAndScode(grade, clas, code);
    }

    public List<User> getUserByGrade(int grade) {
        return userRepository.findByGradeAndStatusOrderByClasAscScodeAsc(grade, User.USER_STATUS.ACTIVATED);
    }

    public void resetGrade(int uid) {
        log.info("IonID reset grade. uid="+uid);
        userRepository.resetGradeByUid(uid);
    }

    public void promoteAll() {
        userRepository.deleteThirdGrades();
        userRepository.resetGradeOnPromote();
    }

    public int updateScode(HttpSession session, int clas, int scode, String captchaCode) throws IonException, IOException {
        int uid = sessionService.getUid(session);

        Optional<User> userOptional = userRepository.findById(uid);
        if(userOptional.isEmpty()) {
            recaptchaService.addAssessmentComment(captchaCode, false);
            return 4;
        }
        User user = userOptional.get();
        if(!user.isScodeCFlag()) {
            recaptchaService.addAssessmentComment(captchaCode, false);
            return 5;
        }
        userRepository.updateScode(uid, clas, scode);
        userRepository.updateScodeFlag(uid, false);

        recaptchaService.addAssessmentComment(captchaCode, true);
        session.setAttribute("schange", false);
        session.setAttribute("login", true);
        session.setAttribute("id", user.getId());
        session.setAttribute("name", user.getName());
        session.setAttribute("priv", user.getPrivilege());
        return 0;
    }

    public void setDefaultIonIDState(int defaultState) throws IonException {
        switch (defaultState) {
            case 0:
                DEFAULT_USER_STATE = User.USER_STATUS.INACTIVATED;
                break;
            case 1:
                DEFAULT_USER_STATE = User.USER_STATUS.ACTIVATED;
                break;
            case 2:
                DEFAULT_USER_STATE = User.USER_STATUS.BANNED;
                break;
            default:
                throw new IonException();
        }
        log.info("IonID default state set to "+DEFAULT_USER_STATE);
    }
    public User.USER_STATUS getDefaultUserState() {
        return DEFAULT_USER_STATE;
    }

    public int updatePassword(int uid, String pwd) {
        log.info("IonID update password. uid="+uid);
        Optional<User> user = userRepository.findById(uid);
        if(user.isEmpty()) return 6;
        user.get().setPwd(passwordEncoder.encode(pwd));
        return 0;
    }
}
