package com.hyfata.adofai.server.util;

import org.json.JSONObject;

public class JsonMessageUtil {
    public static String getStatusMessage(String message) {
        JSONObject object = new JSONObject();
        object.put("status",message);
        return object.toString();
    }

    public static String getJsonMessage(String key, String value) {
        JSONObject object = new JSONObject();
        object.put(key, value);
        return object.toString();
    }
}
