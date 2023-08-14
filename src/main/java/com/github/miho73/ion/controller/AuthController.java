package com.github.miho73.ion.controller;

import com.github.miho73.ion.dto.RecaptchaReply;
import com.github.miho73.ion.dto.ResetPasswordReq;
import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.service.*;
import com.github.miho73.ion.utils.RestResponse;
import com.github.miho73.ion.utils.Validation;
import com.google.api.Http;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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
    ResetPasswordService resetPasswordService;

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
     * 6: client recaptcha failed (low score)
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

    /**
     * 0: ok
     * 1: already logged in
     * 2: user not found
     * 3: already requested
     */
    @GetMapping(
            value = "/reset-passwd/verify",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String queryResetPasswd(
            HttpSession session,
            @RequestParam("id") String id,
            HttpServletResponse response
    ) {
        if(sessionService.isLoggedIn(session)) {
            return RestResponse.restResponse(HttpStatus.OK, 1);
        }
        log.info("Password reset verified. id="+id);
        return RestResponse.restResponse(HttpStatus.OK, resetPasswordService.getState(id));
    }

    /**
     *  0: ok
     *  1: already logged in
     *  2: insufficient parameter(s)
     *  3: recaptcha failed
     *  4: client recaptcha failed (low score)
     *  5: user not found
     *  6: bad identity
     *  7: already requested
     */
    @PostMapping(
            value = "/reset-passwd/request",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String createResetPasswordRequest(
            HttpSession session,
            @RequestBody Map<String, String> body,
            HttpServletResponse response
    ) {
        if(sessionService.isLoggedIn(session)) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 1);
        }
        if(!Validation.checkKeys(body, "id", "name", "scode", "ctoken")) {
            log.info("reset password request failed: insufficient parameter(s).");
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }

        try {
            RecaptchaReply recaptchaReply = reCaptchaAssessment.performAssessment(body.get("ctoken"), "reset_password_request");
            if (!recaptchaReply.isOk()) {
                log.info("reset password request failed: recaptcha failed.");
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 3);
            }

            if (recaptchaReply.getScore() <= CAPTCHA_THRESHOLD) {
                log.info("reset password request failed: client recaptcha failed (low score).");
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 4);
            }

            String name = body.get("name");
            int scode = Integer.parseInt(body.get("scode"));

            Optional<User> userOptional = userService.getUserById(body.get("id"));
            if(userOptional.isEmpty()) {
                reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), false);
                log.info("reset password request failed: user not found.");
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 5);
            }
            User user = userOptional.get();

            if(!user.getName().equals(name) || !(user.getGrade()*1000+user.getClas()+user.getScode() == scode)) {
                reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), false);
                log.info("reset password request failed: bad identity.");
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 6);
            }

            if(resetPasswordService.checkExistsForUser(user.getUid())) {
                reCaptchaAssessment.addAssessmentComment(recaptchaReply.getAssessmentName(), false);
                log.info("reset password request failed: already requested.");
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 7);
            }

            resetPasswordService.createRequest(user.getUid());
            log.info("reset password request success. id="+user.getId());
            return RestResponse.restResponse(HttpStatus.OK, 0);
        } catch (IOException e) {
            log.error("recaptcha failed(IOException).", e);
            response.setStatus(500);
            return RestResponse.restResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * [data]: success
     * 1: user not found
     * 2: request not found
     */
    @GetMapping(
            value = "/reset-passwd/query",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public String queryResetPassword(
            @RequestParam("id") String id,
            HttpServletResponse response
    ) {
        // 1. check if user exists
        Optional<User> userOptional = userService.getUserById(id);
        if(userOptional.isEmpty()) {
            log.info("reset password query failed: user not found.");
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 1);
        }
        User user = userOptional.get();

        // 2. check if request exists
        Optional<ResetPasswordReq> rpqOptional = resetPasswordService.getRequest(user.getUid());
        if(rpqOptional.isEmpty()) {
            log.info("reset password query failed: request not found.");
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }
        ResetPasswordReq req = rpqOptional.get();

        // 3. return
        log.info("reset password query success. id="+user.getId());
        JSONObject ret = new JSONObject();
        ret.put("status", req.getStatus());
        if(req.getStatus() == ResetPasswordReq.RESET_PWD_STATUS.REQUESTED) {
            ret.put("privateCode", req.getPrivateCode());
        }
        ret.put("reqDate", req.getRequestDate());
        return RestResponse.restResponse(HttpStatus.OK, ret);
    }
}
