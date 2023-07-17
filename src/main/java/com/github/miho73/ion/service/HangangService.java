package com.github.miho73.ion.service;

import com.github.miho73.ion.utils.Requests;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
@Slf4j
public class HangangService {
    int uHour;
    JSONObject ret;
    JSONObject fallBack;

    @PostConstruct
    public void init() {
        fallBack = new JSONObject();
        fallBack.put("ok", false);
        uHour = -1;
    }

    public JSONObject getHanGangTemp() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int ch = calendar.get(Calendar.HOUR_OF_DAY);
        if(uHour == ch) return ret;
        else {
            try {
                String jsn = Requests.sendGetRequest("http://openapi.seoul.go.kr:8088/4d516a67706879723833485a727449/json/WPOSInformationTime/1/5/");
                JSONObject r = (JSONObject) new JSONParser().parse(jsn);
                JSONObject ele = (JSONObject) ((JSONArray) r.get("row")).get(1);
                JSONObject k = new JSONObject();
                k.put("ok", true);
                k.put("loc", ele.get("SITE_ID"));
                k.put("dat", ele.get("MSR_DATE").toString().substring(4, 7));
                k.put("tim", ele.get("MSR_TIME"));
                k.put("tem", ele.get("W_TEMP"));
                ret = k;
                return ret;
            } catch (IOException | ParseException e) {
                log.error("Failed to update Hangang temperature", e);
                return fallBack;
            }
        }
    }
}
