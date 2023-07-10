package com.github.miho73.ion.controller;

import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.service.IonIdManageService;
import com.github.miho73.ion.service.SessionService;
import com.github.miho73.ion.service.UserService;
import com.github.miho73.ion.service.ns.NsService;
import com.github.miho73.ion.utils.RestResponse;
import com.github.miho73.ion.utils.Validation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/manage/api")
public class ManageController {
    @Autowired
    SessionService sessionService;

    @Autowired
    UserService userService;

    @Autowired
    IonIdManageService ionIdManageService;

    @Autowired
    NsService nsService;

    /**
     *  0: OK
     *  1: Invalid session
     *  2: insufficient parameter
     *  3: invalid new status
     *  4: no user with such id
     *  5: no self modify
     */
    @PatchMapping("/ionid/active/patch")
    @Transactional
    public String activePath(
            HttpServletResponse response,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.FACULTY_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }
        if(!Validation.checkKeys(body, "id", "ac")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }

        String uid = body.get("id");
        int status = Integer.parseInt(body.get("ac"));
        if(status < 0 || status > 2) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 3);
        }
        if(userService.existsUserById(uid)) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 4);
        }
        if(sessionService.getId(session).equals(uid)) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 5);
        }

        ionIdManageService.updateActiveState(uid, status);

        JSONObject ret = new JSONObject();
        ret.put("sub", uid);
        String nst = switch (status) {
            case 0 -> "INACTIVE";
            case 1 -> "ACTIVE";
            case 2 -> "BANNED";
            default -> "unknown";
        };
        ret.put("act", nst);
        return RestResponse.restResponse(HttpStatus.OK, ret);
    }

    /**
     * [data]: success
     *  1: invalid session
     *  2: no user with such id
     */
    @GetMapping("/ionid/get")
    public String getUser(
            HttpSession session,
            HttpServletResponse response,
            @RequestParam("id") String id
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.FACULTY_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        User user;
        try {
            user = userService.getUserById(id);

            JSONObject ret = new JSONObject();
            ret.put("ui", user.getUid());
            ret.put("na", user.getName());
            ret.put("gr", user.getGrade());
            ret.put("cl", user.getClas());
            ret.put("sc", user.getScode());
            ret.put("sf", user.isScodeCFlag());
            ret.put("id", user.getId());
            ret.put("ll", user.getLastLogin() == null ? "N/A" : user.getLastLogin().toString());
            ret.put("jd", user.getJoinDate().toString());
            ret.put("st", user.getStatus());
            ret.put("pr", user.getPrivilege());
            return RestResponse.restResponse(HttpStatus.OK, ret);
        } catch (IonException e) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 2);
        }
    }

    /**
     *  [data]: success
     *  1: invalid session
     *  2: no user with such id
     */
    @GetMapping("/privilege/get")
    public String getPrivilege(
            HttpSession session,
            HttpServletResponse response,
            @RequestParam("id") String id
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.ROOT_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        try {
            User user = userService.getUserById(id);
            return RestResponse.restResponse(HttpStatus.OK, user.getPrivilege());
        } catch (IonException e) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }
    }

    /**
     *  [data]: success
     *  1: invalid session
     *  2: no user with such id
     *  3: no self modify
     */
    @PatchMapping("/privilege/patch")
    @Transactional
    public String setPrivilege(
            HttpSession session,
            HttpServletResponse response,
            @RequestBody Map<String, String> body
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.ROOT_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }
        if(!Validation.checkKeys(body, "id", "pr")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }

        String id = body.get("id");
        int privilege = Integer.parseInt(body.get("pr"));

         if(userService.existsUserById(id)) {
             response.setStatus(400);
             return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
         }
         if(sessionService.getId(session).equals(id)) {
             response.setStatus(400);
             return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 3);
         }

         userService.updatePrivilege(id, privilege);

         JSONObject ret = new JSONObject();
         ret.put("id", id);
         ret.put("pr", privilege);
         return RestResponse.restResponse(HttpStatus.OK, ret);
    }

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     *  [data]: success
     *  1: invalid session
     */
    @GetMapping("/ns/get")
    public String getNs(
            HttpSession session,
            HttpServletResponse response
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.FACULTY_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        String sname = sessionService.getName(session);
        JSONArray lst = nsService.getNsBySupervisor(sname);
        JSONObject ret = new JSONObject();
        ret.put("nss", lst);
        ret.put("date", LocalDate.now().format(dtf));
        return RestResponse.restResponse(HttpStatus.OK, ret);
    }

    /**
     *  0: ok
     *  1: invalid session
     *  2: insufficient parameter
     *  3: no ns with such id
     */
    @PatchMapping("/ns/accept")
    @Transactional
    public String changeAccept(
            HttpSession session,
            HttpServletResponse response,
            @RequestBody Map<String, String> body
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.FACULTY_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }
        if(!Validation.checkKeys(body, "id", "ac")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }

        int id = Integer.parseInt(body.get("id"));
        boolean accept = Boolean.parseBoolean(body.get("ac"));

        if(!nsService.existsNsById(id)) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 3);
        }

        nsService.acceptNs(id, accept);
        return RestResponse.restResponse(HttpStatus.OK);
    }
}
