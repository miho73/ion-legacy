package com.github.miho73.ion.controller;

import com.github.miho73.ion.dto.RecaptchaReply;
import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.service.AuthService;
import com.github.miho73.ion.service.RecaptchaService;
import com.github.miho73.ion.service.SessionService;
import com.github.miho73.ion.service.UserService;
import com.github.miho73.ion.utils.RestResponse;
import com.github.miho73.ion.utils.Validation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth/api")
public class AuthController {

    @Autowired
    UserService userService;

    @Autowired
    AuthService authService;

    @Autowired
    SessionService sessionService;

    @Autowired
    RecaptchaService reCaptchaAssessment;

    @Value("${ion.recaptcha.block-threshold}")
    float CAPTCHA_THRESHOLD;

    /**
     * 0: ok.
     * 1: user not found.
     * 2: inactivated.
     * 3: banned.
     * 4: incorrect passcode
     * 5: recaptcha failed
     * 6: client recaptcha failed
     * 7: set to change scode mode
     */
    @PostMapping(
            value = "authenticate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public String performLogin(
            HttpServletResponse response,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        if(!Validation.checkKeys(body, "id", "pwd", "ctoken")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST);
        }

        Optional<User> userOptional;
        try {
            RecaptchaReply recaptchaReply = reCaptchaAssessment.performAssessment(body.get("ctoken"), "login");
            if(!recaptchaReply.isOk()) {
                return RestResponse.restResponse(HttpStatus.OK, 6);
            }

            if(recaptchaReply.getScore() <= CAPTCHA_THRESHOLD) {
                return RestResponse.restResponse(HttpStatus.OK, 7);
            }

            userOptional = userService.getUserById(body.get("id"));
            if(userOptional.isEmpty()) {
                log.info("login failed: user not found");
                reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), false);
                return RestResponse.restResponse(HttpStatus.OK, 1);
            }

            User user = userOptional.get();
            boolean auth = authService.authenticate(body.get("pwd"), user);
            int active = authService.checkActiveStatus(user);

            if(auth) {
                if(active == 0) {
                    log.info("login success. id="+user.getId());
                    reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), true);
                    userService.updateLastLogin(user.getUid());

                    session.setAttribute("uid", user.getUid());
                    session.setAttribute("grade", user.getGrade());
                    if(user.isScodeCFlag()) {
                        log.info("scode flag is true. user in schange mode. id="+user.getId());
                        session.setAttribute("schange", true);
                        session.setAttribute("login", false);
                        return RestResponse.restResponse(HttpStatus.OK, 7);
                    }
                    else {
                        session.setAttribute("schange", false);
                        session.setAttribute("login", true);
                        session.setAttribute("id", user.getId());
                        session.setAttribute("name", user.getName());
                        session.setAttribute("priv", user.getPrivilege());
                    }

                    return RestResponse.restResponse(HttpStatus.OK, 0);
                }
                else if (active == 1) {
                    log.info("login blocked(inactive). id="+user.getId());
                    reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), false);
                    return RestResponse.restResponse(HttpStatus.OK, 2);
                }
                else if(active == 2) {
                    log.info("login blocked(banned). id="+user.getId());
                    reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), false);
                    return RestResponse.restResponse(HttpStatus.OK, 3);
                }
                else {
                    log.info("login blocked(unknown status). id="+user.getId());
                    response.setStatus(500);
                    reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), false);
                    return RestResponse.restResponse(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else {
                log.info("login blocked(password mismatch). id="+user.getId());
                reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), false);
                return RestResponse.restResponse(HttpStatus.OK, 4);
            }
        } catch (IOException e) {
            log.error("recaptcha failed(IOException).", e);
            response.setStatus(500);
            return RestResponse.restResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5);
        }
    }

    @GetMapping(
            value = "signout",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String Signout(HttpSession session, HttpServletResponse response) {
        if(sessionService.isLoggedIn(session)) {
            session.setAttribute("login", false);
            log.info("user signed out. id="+sessionService.getId(session));
            return RestResponse.restResponse(HttpStatus.OK);
        }
        else {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(
            value = "/authorize",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String checkAuth(HttpSession session) {
        boolean login = sessionService.isLoggedIn(session);
        return RestResponse.restResponse(HttpStatus.OK, login);
    }
    @GetMapping(
            value = "/authorize-e",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String checkAuthAndPrivilege(HttpSession session, @RequestParam("priv") int priv) {
        boolean login = sessionService.checkPrivilege(session, priv);
        return RestResponse.restResponse(HttpStatus.OK, login);
    }
}
