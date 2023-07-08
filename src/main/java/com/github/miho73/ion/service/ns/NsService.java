package com.github.miho73.ion.service.ns;

import com.github.miho73.ion.dto.LnsReservation;
import com.github.miho73.ion.dto.NsRecord;
import com.github.miho73.ion.repository.LnsRepository;
import com.github.miho73.ion.repository.NsRepository;
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
        LnsReservation lnsRev = new LnsReservation();
        lnsRev.setLnsDate(LocalDate.now());
        lnsRev.setLnsTime(nsTime);
        lnsRev.setUuid(uuid);
        lnsRev.setSeat(seat);

        return lnsRepository.save(lnsRev);
    }

    public boolean existsAlready(int uuid, NsRecord.NS_TIME nsTime) {
        return !nsRepository.findByUuidAndNsDateAndNsTime(uuid, LocalDate.now(), nsTime).isEmpty();
    }

    public void deleteNs(int uuid, NsRecord.NS_TIME time) {
        nsRepository.deleteByUuidAndNsTime(uuid, time);
    }
}
