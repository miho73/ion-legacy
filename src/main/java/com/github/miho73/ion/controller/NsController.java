package com.github.miho73.ion.controller;

import com.github.miho73.ion.dto.LnsReservation;
import com.github.miho73.ion.dto.NsRecord;
import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.service.SessionService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/ns/api")
public class NsController {
    @Autowired
    SessionService sessionService;

    @Autowired
    NsService nsService;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     *  [data]: success
     *  1: invalid session
     */
    @GetMapping(
            value = "/nsr/get",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getNsRecord(
            HttpSession session,
            HttpServletResponse response
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.USER_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        try {
            JSONArray ret = nsService.getNsList(sessionService.getUid(session));

            JSONObject reply = new JSONObject();
            reply.put("reqs", ret);
            reply.put("name", sessionService.getName(session));
            reply.put("id", sessionService.getId(session));
            reply.put("grade", sessionService.getGrade(session));
            reply.put("date", LocalDate.now().format(dtf));
            return RestResponse.restResponse(HttpStatus.OK, reply);
        } catch (IonException e) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }
    }

    /**
     *  0: success
     *  1: internal server error
     *  2: insufficient parameters
     *  3: invalid session
     *  4: user already requested
     *  5: seat already reserved
     */
    @PostMapping(
            value = "/nsr/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public String createNsRequest(
            HttpSession session,
            HttpServletResponse response,
            @RequestBody Map<String, String> body
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.USER_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 3);
        }

        // check key
        if(!Validation.checkKeys(body, "time", "supervisor", "reason", "place", "lnsReq", "lnsSeat")) {
            response.setStatus(400);
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }

        try {
            int uuid = sessionService.getUid(session);
            NsRecord.NS_TIME nsTime = NsRecord.NS_TIME.valueOf(body.get("time"));

            // if user already has request on same time
            if(nsService.existsNsByUuid(uuid, nsTime)) {
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 4);
            }

            boolean lnsReq = Boolean.parseBoolean(body.get("lnsReq"));
            int lnsReqUid = -1;

            if(lnsReq) {
                int grade = sessionService.getGrade(session);
                LnsReservation lnsRev = nsService.saveLnsReservation(uuid, nsTime, grade, body.get("lnsSeat"));
                if(lnsRev == null) {
                    response.setStatus(400);
                    return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 5);
                }

                lnsReqUid = lnsRev.getUid();
            }

            nsService.saveNsRequest(uuid, nsTime, lnsReq, lnsReqUid, body);

            log.info("created ns req uuid="+uuid+", time="+nsTime);
            response.setStatus(201);
            return RestResponse.restResponse(HttpStatus.CREATED, 0);
        } catch (IonException e) {
            response.setStatus(500);
            return RestResponse.restResponse(HttpStatus.INTERNAL_SERVER_ERROR, 1);
        }
    }

    /**
     *  0: success
     *  1: invalid session
     *  2: no request on that time
     */
    @DeleteMapping(
            value = "/nsr/delete",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public String deleteNs(
            HttpSession session,
            HttpServletResponse response,
            @RequestParam("time") NsRecord.NS_TIME time
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.USER_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        try {
            int uuid = sessionService.getUid(session);

            if(nsService.existsNsByUuid(uuid, time)) {
                nsService.deleteNs(uuid, time);
                log.info("deleted ns req uuid="+uuid+", time="+time);
                return RestResponse.restResponse(HttpStatus.OK, 0);
            }
            else {
                response.setStatus(400);
                return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
            }
        } catch (IonException e) {
            return RestResponse.restResponse(HttpStatus.BAD_REQUEST, 2);
        }
    }

    /**
     *  [data]: success
     *  1: invalid session
     */
    @GetMapping(
            value = "/lns/get",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getLnsSeatState(
            HttpSession session,
            HttpServletResponse response
    ) {
        if(!sessionService.checkPrivilege(session, SessionService.USER_PRIVILEGE)) {
            response.setStatus(401);
            return RestResponse.restResponse(HttpStatus.UNAUTHORIZED, 1);
        }

        JSONArray ist = nsService.getLnsSeat(sessionService.getGrade(session));
        return RestResponse.restResponse(HttpStatus.OK, ist);
    }
}
