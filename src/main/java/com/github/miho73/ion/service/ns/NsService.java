package com.github.miho73.ion.service.ns;

import com.github.miho73.ion.dto.LnsReservation;
import com.github.miho73.ion.dto.NsRecord;
import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.repository.LnsRepository;
import com.github.miho73.ion.repository.NsRepository;
import com.github.miho73.ion.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class NsService {
    @Autowired
    NsRepository nsRepository;

    @Autowired
    LnsRepository lnsRepository;

    @Autowired
    UserRepository userRepository;

    public void saveNsRequest(int uuid, NsRecord.NS_TIME nsTime, boolean lnsReq, int lnsReqUid, Map<String, String> body) {
        NsRecord nsRecord = new NsRecord();

        nsRecord.setNsDate(LocalDate.now());
        nsRecord.setNsReqTime(new Timestamp(System.currentTimeMillis()));

        nsRecord.setNsTime(nsTime);
        nsRecord.setNsPlace(body.get("place"));
        nsRecord.setNsSupervisor(body.get("supervisor"));
        nsRecord.setNsReason(body.get("reason"));

        nsRecord.setNsState(NsRecord.NS_STATE.REQUESTED);

        nsRecord.setLnsReq(lnsReq);
        if(lnsReq) {
            nsRecord.setLnsReqUid(lnsReqUid);
        }

        nsRecord.setUuid(uuid);

        nsRepository.save(nsRecord);
    }

    public JSONArray getNsList(int uuid) {
        List<NsRecord> rec = nsRepository.findByUuidAndNsDateOrderByNsTimeAsc(uuid, LocalDate.now());

        JSONArray ret = new JSONArray();
        for (NsRecord nsRecord : rec) {
            JSONObject ele = new JSONObject();
            ele.put("time", nsRecord.getNsTime());
            ele.put("supervisor", nsRecord.getNsSupervisor());
            ele.put("reason", nsRecord.getNsReason());
            ele.put("lnsReq", nsRecord.isLnsReq());
            ele.put("status", nsRecord.getNsState());
            ele.put("place", nsRecord.getNsPlace());
            if(nsRecord.isLnsReq()) {
                Optional<LnsReservation> lr = lnsRepository.findById(nsRecord.getLnsReqUid());
                if(lr.isEmpty()) ele.put("lnsSeat", "No Record");
                else ele.put("lnsSeat", lr.get().getSeat());
            }
            ret.put(ele);
        }
        return ret;
    }

    public LnsReservation saveLnsReservation(int uuid, NsRecord.NS_TIME nsTime, int grade, String seat) {
        if(existsLnsBySeat(nsTime, seat, grade)) {
            return null;
        }

        LnsReservation lnsRev = new LnsReservation();
        lnsRev.setLnsDate(LocalDate.now());
        lnsRev.setLnsTime(nsTime);
        lnsRev.setUuid(uuid);
        lnsRev.setSeat(seat);
        lnsRev.setGrade(grade);

        return lnsRepository.save(lnsRev);
    }

    public boolean existsLnsBySeat(NsRecord.NS_TIME nsTime, String seat, int grade) {
        return !lnsRepository.findByLnsTimeAndSeatAndLnsDateAndGrade(nsTime, seat, LocalDate.now(), grade).isEmpty();
    }

    public boolean existsNsByUuid(int uuid, NsRecord.NS_TIME nsTime) {
        return nsRepository.findByUuidAndNsDateAndNsTime(uuid, LocalDate.now(), nsTime).isPresent();
    }

    public void deleteNs(int uuid, NsRecord.NS_TIME time) throws IonException {
        Optional<NsRecord> nsRecord = nsRepository.findByUuidAndNsDateAndNsTime(uuid, LocalDate.now(), time);
        if(nsRecord.isPresent()) {
            NsRecord toDel = nsRecord.get();
            if(toDel.isLnsReq()) {
                lnsRepository.deleteById(toDel.getLnsReqUid());
            }
            nsRepository.deleteByUuidAndNsTimeAndNsDate(uuid, time, LocalDate.now());
        }
        else {
            throw new IonException();
        }
    }

    public JSONArray getLnsSeat(int grade) {
        List<LnsReservation> lrev = lnsRepository.findByLnsDateAndGrade(LocalDate.now(), grade);

        JSONArray[] byNsTime = new JSONArray[3];

        byNsTime[0] = new JSONArray();
        byNsTime[1] = new JSONArray();
        byNsTime[2] = new JSONArray();

        lrev.forEach(e -> {
            JSONObject rev = new JSONObject();
            Optional<User> reserver = userRepository.findById(e.getUuid());
            if(reserver.isEmpty()) {
                rev.put("v", false);
            }
            else {
                User user = reserver.get();
                rev.put("v", true);
                rev.put("name", user.getName());
                rev.put("scode", user.getGrade()*1000+user.getClas()*100+user.getScode());
                rev.put("sn", e.getSeat());
                byNsTime[NsRecord.nsTimeToInt(e.getLnsTime())].put(rev);
            }
        });

        JSONArray ret = new JSONArray();
        ret.put(byNsTime[0]);
        ret.put(byNsTime[1]);
        ret.put(byNsTime[2]);
        return ret;
    }

    public JSONArray getNsBySupervisor(String sname) {
        List<NsRecord> rec = nsRepository.findByNsDateAndNsSupervisorContainsOrderByNsStateAsc(LocalDate.now(), sname);
        JSONArray lst = new JSONArray();
        rec.forEach(r -> {
            Optional<User> pla = userRepository.findById(r.getUuid());
            JSONObject e = new JSONObject();
            if(pla.isPresent()) {
                User u = pla.get();
                e.put("id", r.getUid());
                e.put("time", r.getNsTime());
                e.put("name", u.getName());
                e.put("rscode", u.getGrade()*1000+u.getClas()*100+u.getScode());
                e.put("place", r.getNsPlace());
                e.put("super", r.getNsSupervisor());
                e.put("reason", r.getNsReason());
                e.put("status", r.getNsState());
                e.put("v", true);
            }
            else {
                e.put("v", false);
            }
            lst.put(e);
        });
        return lst;
    }

    public void acceptNs(int id, boolean accept) {
        int ns = accept ? 1 : 2;
        nsRepository.updateAccept(id, ns);
    }

    public boolean existsNsById(int id) {
        return nsRepository.existsById(id);
    }

    public List<NsRecord> findByUuid(int uuid) {
        return nsRepository.findByUuidAndNsDateOrderByNsTimeAsc(uuid, LocalDate.now());
    }
}
