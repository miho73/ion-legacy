package com.github.miho73.ion.controller;

import com.github.miho73.ion.dto.StudentCodeRecord;
import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.service.SessionService;
import com.github.miho73.ion.service.StudentCodeRecordService;
import com.github.miho73.ion.service.UserService;
import com.github.miho73.ion.utils.RestResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(
            value = "/validation/id-duplication",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String checkIdDuplication(@RequestParam(name = "id") String id) {
        boolean ok = userService.existsUserById(id);
        return RestResponse.restResponse(HttpStatus.OK, ok ? 0 : 1);
    }

    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public String createUser(@Valid @RequestBody User user, HttpServletResponse response) {
        if(user.getGrade() == 0 || user.getScode() == 0 || user.getClas() == 0) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, "on grade/class/code");
        }

        user.setPwd(passwordEncoder.encode(user.getPwd()));
        User created = userService.createUser(user);
        StudentCodeRecord scr = new StudentCodeRecord();
        scr.setUuid(created.getUid());
        scr.setRecord("/");
        studentCodeRecordService.createRecord(scr);
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
