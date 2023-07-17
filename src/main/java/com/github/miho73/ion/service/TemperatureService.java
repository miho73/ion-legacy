package com.github.miho73.ion.service;

import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.utils.Requests;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
@Slf4j
public class TemperatureService {
    int uHourSeoul;
    int uHourIcn;
    JSONObject seoul;
    JSONObject icn;
    JSONObject fallBack;

    @Value("${ion.temperature.seoul-api-key}")
    String SEOUL_KEY;
    @Value("${ion.temperature.incheon-api-key}")
    String ICN_KEY;

    @PostConstruct
    public void init() {
        fallBack = new JSONObject();
        fallBack.put("ok", false);
        uHourIcn = -1;
        uHourSeoul = -1;
    }

    public JSONObject getHanGangTemp() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int ch = calendar.get(Calendar.HOUR_OF_DAY);
        if(uHourSeoul == ch) return seoul;
        else {
            try {
                String jsn = Requests.sendGetRequest("http://openapi.seoul.go.kr:8088/"+SEOUL_KEY+"/json/WPOSInformationTime/1/5/");
                JSONObject r = (JSONObject) new JSONParser().parse(jsn);
                JSONObject res = (JSONObject) r.get("WPOSInformationTime");
                if(!((JSONObject)res.get("RESULT")).get("CODE").toString().equals("INFO-000")) {
                    throw new IonException();
                }
                JSONObject data = (JSONObject)((JSONArray) res.get("row")).get(1);
                JSONObject k = new JSONObject();
                k.put("ok", true);
                k.put("loc", "중량천");
                k.put("dat", data.get("MSR_DATE").toString().substring(4, 8));
                k.put("tim", data.get("MSR_TIME"));
                k.put("tem", data.get("W_TEMP"));
                seoul = k;
                uHourSeoul = ch;
                return seoul;
            } catch (IOException | ParseException | IonException e) {
                log.error("Failed to update Hangang temperature", e);
                return fallBack;
            }
        }
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public JSONObject getIncheonTemp() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int ch = calendar.get(Calendar.HOUR_OF_DAY);
        if(uHourIcn == ch) return icn;
        else {
            try {
                String jsn = Requests.sendGetRequest("http://www.khoa.go.kr/api/oceangrid/tideObsTemp/search.do?ServiceKey="+ICN_KEY+"&ObsCode=DT_0001&Date=20230717&ResultType=json");
                JSONObject r = (JSONObject) new JSONParser().parse(jsn);
                JSONObject res = (JSONObject) r.get("result");
                JSONArray data = (JSONArray) res.get("data");
                JSONObject e = (JSONObject) data.get(data.size()-1);
                JSONObject k = new JSONObject();

                LocalDateTime dateTime = LocalDateTime.parse(e.get("record_time").toString(), formatter);
                String mon = Integer.toString(dateTime.getMonthValue());
                if(mon.length() == 1) mon = " "+mon;
                k.put("ok", true);
                k.put("loc", "인천 조위관측소");
                k.put("dat", mon+dateTime.getDayOfMonth());
                k.put("tim", Integer.toString(dateTime.getHour()));
                k.put("tem", e.get("water_temp"));
                icn = k;
                uHourIcn = ch;
                return icn;
            } catch (IOException | ParseException e) {
                log.error("Failed to update Incheon temperature", e);
                return fallBack;
            }
        }
    }
}
