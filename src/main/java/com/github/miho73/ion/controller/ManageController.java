package com.github.miho73.ion.controller;

import com.github.miho73.ion.dto.NsRecord;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @PatchMapping(
            value = "/ionid/active/patch",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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
    @GetMapping(
            value = "/ionid/get",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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
        Optional<User> userOptional = userService.getUserById(id);
        if(userOptional.isEmpty()) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }

        user = userOptional.get();
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
    }

    /**
     *  0: ok
     *  1: invalid session
     *  2: insufficient parameter
     *  3: user not found
     */
    @PatchMapping(
            value = "/ionid/eliminate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public String removeGrade(
            HttpSession session,
            HttpServletResponse response,
            @RequestBody Map<String, String> body
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.ROOT_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }
        if(!Validation.checkKeys(body, "id")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }
        String id = body.get("id");

        Optional<User> userOptional = userService.getUserById(id);
        if(userOptional.isEmpty()) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 3);
        }
        User user = userOptional.get();
        userService.resetGrade(user.getUid());
        return RestResponse.restResponse(HttpStatus.OK);
    }

    /**
     *  [data]: success
     *  1: invalid session
     *  2: no user with such id
     */
    @GetMapping(
            value = "/privilege/get",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getPrivilege(
            HttpSession session,
            HttpServletResponse response,
            @RequestParam("id") String id
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.ROOT_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        Optional<User> userOptional = userService.getUserById(id);
        if(userOptional.isEmpty()) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }
        return RestResponse.restResponse(HttpStatus.OK, userOptional.get().getPrivilege());
    }

    /**
     *  [data]: success
     *  1: invalid session
     *  2: no user with such id
     *  3: no self modify
     */
    @PatchMapping(
            value = "/privilege/patch",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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
    @GetMapping(
            value = "/ns/get",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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
    @PatchMapping(
            value = "/ns/accept",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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

    /**
     *  [data]: success
     *  1: invalid session
     *  2: ionid not found
     */
    @GetMapping(
            value = "/ns/get-user",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getUserNs(
            HttpSession session,
            HttpServletResponse response,
            @RequestParam("code") int scode
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.FACULTY_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        Optional<User> userOptional = userService.getUserByScode(scode);
        if(userOptional.isEmpty()) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }
        JSONArray ret = nsService.getNsList(userOptional.get().getUid());

        JSONObject reply = new JSONObject();
        reply.put("reqs", ret);
        reply.put("date", LocalDate.now().format(dtf));
        return RestResponse.restResponse(HttpStatus.OK, reply);
    }

    /**
     *  0: ok
     *  1: invalid session
     *  2: insufficient property
     *  3: no user with such scode
     *  4: already requested
     */
    @PostMapping(
            value = "/ns/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String createNs(
            HttpSession session,
            HttpServletResponse response,
            @RequestBody Map<String, String> body
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.FACULTY_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        if(!Validation.checkKeys(body, "scode", "time", "place", "reason")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }

        int scode = Integer.parseInt(body.get("scode"));
        NsRecord.NS_TIME nsTime = NsRecord.NS_TIME.valueOf(body.get("time"));

        Optional<User> userOptional = userService.getUserByScode(scode);
        if(userOptional.isEmpty()) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 3);
        }
        User user = userOptional.get();

        if(nsService.existsNsByUuid(user.getUid(), nsTime)) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 4);
        }

        body.put("supervisor", sessionService.getName(session));
        nsService.saveNsRequest(user.getUid(), nsTime, false, -1, body);

        response.setStatus(201);
        return RestResponse.restResponse(HttpStatus.CREATED, 0);
    }

    /**
     *  0: ok
     *  1: invalid session
     *  2: insufficient property
     *  3: no user with such scode
     *  4: no ns found
     */
    @DeleteMapping(
            value = "/ns/delete",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public String deleteNs(
            HttpSession session,
            HttpServletResponse response,
            @RequestParam("code") int scode,
            @RequestParam("time") NsRecord.NS_TIME nsTime
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.FACULTY_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        Optional<User> userOptional = userService.getUserByScode(scode);
        if(userOptional.isEmpty()) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 3);
        }
        User user = userOptional.get();

        try {
            nsService.deleteNs(user.getUid(), nsTime);
            return RestResponse.restResponse(HttpStatus.OK, 0);
        } catch (IonException e) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 4);
        }
    }

    /**
     *  [data]: ok
     *  1: invalid session
     */
    @GetMapping(
            value = "/ns/print",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getPrintData(
            HttpSession session,
            HttpServletResponse response,
            @RequestParam("grade") int grade
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.FACULTY_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        List<User> users = userService.getUserByGrade(grade);
        JSONArray ret = new JSONArray();
        users.forEach(e -> {
            List<NsRecord> records = nsService.findByUuid(e.getUid());
            JSONObject element = new JSONObject();
            element.put("code", e.getGrade()*1000+e.getClas()*100+e.getScode());
            element.put("name", e.getName());
            records.forEach(s -> {
                String str = s.getNsPlace()+"/"+s.getNsSupervisor()+"/"+s.getNsReason();
                boolean aprv = ( s.getNsState() == NsRecord.NS_STATE.APPROVED );
                JSONObject pt = new JSONObject();
                pt.put("c", str);
                pt.put("a", aprv);
                if(s.getNsTime() == NsRecord.NS_TIME.N8) element.put("n8", pt);
                if(s.getNsTime() == NsRecord.NS_TIME.N1) element.put("n1", pt);
                if(s.getNsTime() == NsRecord.NS_TIME.N2) element.put("n2", pt);
            });
            if(!element.has("n8")) element.put("n8", JSONObject.NULL);
            if(!element.has("n1")) element.put("n1", JSONObject.NULL);
            if(!element.has("n2")) element.put("n2", JSONObject.NULL);
            ret.put(element);
        });

        JSONObject reply = new JSONObject();
        reply.put("ns", ret);
        reply.put("qtime", new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date()));
        return RestResponse.restResponse(HttpStatus.OK, reply);
    }
}
