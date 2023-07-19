package com.github.miho73.ion.controller;

import com.github.miho73.ion.dto.RecaptchaReply;
import com.github.miho73.ion.dto.StudentCodeRecord;
import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.service.RecaptchaService;
import com.github.miho73.ion.service.SessionService;
import com.github.miho73.ion.service.StudentCodeRecordService;
import com.github.miho73.ion.service.UserService;
import com.github.miho73.ion.utils.RestResponse;
import com.github.miho73.ion.utils.Validation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user/api")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    StudentCodeRecordService studentCodeRecordService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    SessionService sessionService;

    @Autowired
    RecaptchaService reCaptchaAssessment;

    @Value("${ion.recaptcha.block-threshold}")
    float CAPTCHA_THRESHOLD;

    @GetMapping(
            value = "/validation/id-duplication",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String checkIdDuplication(@RequestParam(name = "id") String id) {
        boolean ok = userService.existsUserById(id);
        return RestResponse.restResponse(HttpStatus.OK, ok ? 0 : 1);
    }

    /**
     * 0 : success
     * 1 : insufficient parameter
     * 2 : invalid parameter(s)
     * 3 : captcha failed
     * 4 : too low captcha score
     */
    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public String createUser(
            @RequestBody Map<String, String> body,
            HttpServletResponse response
    ) {
        if(!Validation.checkKeys(body, "clas", "ctoken", "grade", "id", "name", "pwd", "scode")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 1);
        }

        User user = new User();
        user.setGrade(Integer.parseInt(body.get("grade")));
        user.setClas(Integer.parseInt(body.get("clas")));
        user.setScode(Integer.parseInt(body.get("scode")));
        user.setPwd(body.get("pwd"));
        user.setId(body.get("id"));
        user.setName(body.get("name"));

        if(user.getGrade() == 0 || user.getScode() == 0 || user.getClas() == 0) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }

        try {
            RecaptchaReply recaptchaReply = reCaptchaAssessment.performAssessment(body.get("ctoken"), "signup");
            if (!recaptchaReply.isOk()) {
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 3);
            }
            if(recaptchaReply.getScore() <= CAPTCHA_THRESHOLD) {
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 4);
            }
        } catch (IOException e) {
            log.error("recaptcha failed(IOException).", e);
            response.setStatus(500);
            return RestResponse.restResponse(HttpStatus.INTERNAL_SERVER_ERROR, 3);
        }

        user.setPwd(passwordEncoder.encode(user.getPwd()));
        User created = userService.createUser(user);
        StudentCodeRecord scr = new StudentCodeRecord();
        scr.setUuid(created.getUid());
        scr.setRecord("/");
        studentCodeRecordService.createRecord(scr);
        log.info("user created. uid="+user.getUid()+", id="+user.getId());
        return RestResponse.restResponse(HttpStatus.CREATED, created.getId());
    }

    @GetMapping(
            value = "/idx-iden",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getIdxIden(
            HttpSession session
    ) {
        if(!sessionService.isLoggedIn(session)) {
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        JSONObject ret = new JSONObject();
        ret.put("name", sessionService.getName(session));
        ret.put("id", sessionService.getId(session));
        ret.put("priv", sessionService.getPrivilege(session));
        return RestResponse.restResponse(HttpStatus.OK, ret);
    }
}
