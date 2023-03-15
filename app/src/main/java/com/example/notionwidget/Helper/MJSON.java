package com.example.notionwidget.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MJSON {


    public static JSONObject getJSONObject(JSONArray jsons, int i) {
        try {
            return jsons.getJSONObject(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getString(JSONObject json, String key) {
        try {
            if(json.getString(key).equals("null")) return null;
            return json.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getJSONObject(JSONObject json, String key) {
        try {
            if(json.has(key) && !json.isNull(key)) return json.getJSONObject(key);
            else return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getJSONArray(JSONObject json, String key) {
        try {
            return json.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean getBoolean(JSONObject json, String key, boolean default_value) {
        try {
            return json.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return default_value;
    }

    public static JSONObject newJSONObject(String string) {
        try {
            return new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
