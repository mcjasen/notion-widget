package com.example.notionwidget.Database;


import android.annotation.SuppressLint;
import android.util.Log;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.notionwidget.Helper.MJSON;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

@Entity
public class Item {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String notion_id;
    public String icon;
    public String name;
    public String state;
    @Embedded public MDate due;
    public boolean done;
    public String kanban_state;
    public boolean edited = true;
    public String url;

    public Item(){}

    public static ArrayList<Item> getItems(JSONArray jsons){
        ArrayList<Item> items = new ArrayList<>();
        for (int i = 0; i < jsons.length(); i++) {
            JSONObject json = MJSON.getJSONObject(jsons, i);
            Item item = new Item(json);
            items.add(item);
        }
        return items;
    }

    public Item(JSONObject json){
        notion_id = MJSON.getString(json, "id");
        icon = jsonToIcon(json);
        name = jsonToName(json);
        state = jsonToState(json);
        due = jsonToDue(json);
        done = jsonToDone(json);
        kanban_state = jsonToKanbanState(json);
        url = MJSON.getString(json, "url");
    }

    private String jsonToIcon(JSONObject json) {
        JSONObject icon = MJSON.getJSONObject(json, "icon");
        if(icon != null) return MJSON.getString(icon, "emoji");
        return null;
    }

    private String jsonToKanbanState(JSONObject json) {
        JSONObject properties_json = getProperties(json);
        JSONObject kanban_status_json = MJSON.getJSONObject(properties_json, "Kanban - Status");
        JSONObject select_json = MJSON.getJSONObject(kanban_status_json, "select");
        String name = MJSON.getString(select_json, "name");
        return name;
    }

    private boolean jsonToDone(JSONObject json) {
        JSONObject properties_json = getProperties(json);
        JSONObject erledigt_json = MJSON.getJSONObject(properties_json, "Erledigt");
        return MJSON.getBoolean(erledigt_json, "checkbox", true);
    }

    @SuppressLint("SimpleDateFormat")
    private MDate jsonToDue(JSONObject json) {
        JSONObject properties_json = getProperties(json);
        JSONObject faellig_json = MJSON.getJSONObject(properties_json, "Fällig");
        JSONObject date_json = MJSON.getJSONObject(faellig_json, "date");
        if(date_json == null) return null;
        String start = MJSON.getString(date_json, "start");
        return new MDate(start);
    }

    private String jsonToState(JSONObject json) {
        JSONObject properties_json = getProperties(json);
        JSONObject status_json = MJSON.getJSONObject(properties_json, "Status");
        JSONObject formula_json = MJSON.getJSONObject(status_json, "formula");
        return MJSON.getString(formula_json, "string");
    }

    private String jsonToName(JSONObject json) {
        JSONObject properties_json = getProperties(json);
        JSONObject aufgabe_json = MJSON.getJSONObject(properties_json, "Aufgabe");
        JSONArray titles_json = MJSON.getJSONArray(aufgabe_json, "title");
        JSONObject title_json;
        if(titles_json.length() > 0) {
            title_json = MJSON.getJSONObject(titles_json, 0);
        }
        else return null;
        JSONObject text_json = MJSON.getJSONObject(title_json, "text");
        return MJSON.getString(text_json, "content");
    }

    private JSONObject getProperties(JSONObject json) {
        return MJSON.getJSONObject(json, "properties");
    }

    public Long getDueValue() {
        if(due != null) return due.value;
        else return null;
    }
}
