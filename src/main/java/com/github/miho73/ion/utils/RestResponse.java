package com.github.miho73.ion.utils;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RestResponse {
    public static String restResponse(HttpStatus status, Object result) {
        JSONObject res = new JSONObject();
        res.put("status", status.value());
        res.put("result", result);
        return res.toJSONString(JSONStyle.LT_COMPRESS);
    }

    public static String restResponse(HttpStatus status) {
        JSONObject res = new JSONObject();
        res.put("status", status.value());
        return res.toJSONString(JSONStyle.LT_COMPRESS);
    }
}