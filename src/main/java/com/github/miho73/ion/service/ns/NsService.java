package com.github.miho73.ion.service.ns;

import com.github.miho73.ion.dto.LnsReservation;
import com.github.miho73.ion.dto.NsRecord;
import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.repository.LnsRepository;
import com.github.miho73.ion.repository.NsRepository;
import com.github.miho73.ion.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
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
            ret.add(ele);
        }
        return ret;
    }

    public LnsReservation saveLnsReservation(int uuid, NsRecord.NS_TIME nsTime, String seat) {
        if(existsLnsBySeat(nsTime, seat)) {
            return null;
        }

        LnsReservation lnsRev = new LnsReservation();
        lnsRev.setLnsDate(LocalDate.now());
        lnsRev.setLnsTime(nsTime);
        lnsRev.setUuid(uuid);
        lnsRev.setSeat(seat);

        return lnsRepository.save(lnsRev);
    }

    public boolean existsLnsBySeat(NsRecord.NS_TIME nsTime, String seat) {
        return !lnsRepository.findByLnsTimeAndSeatAndLnsDate(nsTime, seat, LocalDate.now()).isEmpty();
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

    public JSONArray getLnsSeat() {
        List<LnsReservation> lrev = lnsRepository.findByLnsDate(LocalDate.now());

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
                byNsTime[NsRecord.nsTimeToInt(e.getLnsTime())].add(rev);
            }
        });

        JSONArray ret = new JSONArray();
        ret.add(byNsTime[0]);
        ret.add(byNsTime[1]);
        ret.add(byNsTime[2]);
        return ret;
    }
}
