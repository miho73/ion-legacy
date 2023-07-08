package com.github.miho73.ion.controller;

import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.service.AuthService;
import com.github.miho73.ion.service.SessionService;
import com.github.miho73.ion.service.UserService;
import com.github.miho73.ion.utils.RestResponse;
import com.github.miho73.ion.utils.Validation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * 0: ok. 1: user not found. 2: inactivated. 3: banned. 4: incorrect passcode
     */
    @PostMapping(
            value = "authenticate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String performLogin(
            HttpServletResponse response,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        if(!Validation.checkKeys(body, "id", "pwd")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST);
        }

        User user;
        try {
            user = userService.getUserById(body.get("id"));
        } catch (IonException e) {
            log.info("login failed: user not found");
            return RestResponse.restResponse(HttpStatus.OK, 1);
        }

        boolean auth = authService.authenticate(body.get("pwd"), user);
        int active = authService.checkActiveStatus(user);

        if(auth) {
            if(active == 0) {
                log.info("login success: "+user.getId());

                JSONObject ident = new JSONObject();
                ident.put("uid", user.getUid());
                ident.put("id", user.getId());
                ident.put("priv", user.getPrivilege());
                ident.put("name", user.getName());
                ident.put("ccd", user.getGrade()+user.getClas()+user.getScode());
                String msg = ident.toJSONString(JSONStyle.LT_COMPRESS);

                session.setAttribute("login", true);
                session.setAttribute("uid", user.getUid());
                session.setAttribute("id", user.getId());
                session.setAttribute("name", user.getName());
                session.setAttribute("priv", user.getPrivilege());

                return RestResponse.restResponse(HttpStatus.OK, 0);
            }
            else if (active == 1) {
                log.info("login blocked(inactive): "+user.getId());
                return RestResponse.restResponse(HttpStatus.OK, 2);
            }
            else if(active == 2) {
                log.info("login blocked(banned): "+user.getId());
                return RestResponse.restResponse(HttpStatus.OK, 3);
            }
            else {
                log.info("login blocked(unknown status): "+user.getId());
                return RestResponse.restResponse(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else {
            return RestResponse.restResponse(HttpStatus.OK, 4);
        }
    }

    @GetMapping(
            value = "signout",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String Signout(HttpSession session, HttpServletResponse response) {
        if(sessionService.isLoggedIn(session)) {
            session.setAttribute("login", false);
            return RestResponse.restResponse(HttpStatus.OK);
        }
        else {
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
}
