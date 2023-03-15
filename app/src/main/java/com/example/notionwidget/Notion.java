package com.example.notionwidget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.notionwidget.Database.Item;
import com.example.notionwidget.Database.KanbanState;
import com.example.notionwidget.Database.Tasks.InsertItems;
import com.example.notionwidget.Database.Tasks.RemoveItem;
import com.example.notionwidget.Database.Tasks.TaskRunner;
import com.example.notionwidget.Helper.Data;
import com.example.notionwidget.Helper.MJSON;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Notion {

    private static final String url_root = "https://api.notion.com/v1/";
    private static String db_id;
    private static String secret;

    private static void load_notion_data(Context context){
        JSONObject notion_data = MJSON.newJSONObject(Data.loadStringFromAssets(context, "notion_data.json"));
        db_id = MJSON.getString(notion_data, "db_id");
        secret = MJSON.getString(notion_data, "secret");
    }

    public static void setItemDone(Context context, String notion_id){
        if(db_id == null || secret == null) load_notion_data(context);
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = url_root + "pages/" + notion_id;
        StringRequest stringRequest = new StringRequest(Request.Method.PATCH, url,
                response -> new TaskRunner().executeAsync(new RemoveItem(context, notion_id), (data) -> {}),
                error -> Log.e("setItemDone", "" + error)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer " + secret);
                params.put("Notion-Version", "2022-06-28");
                return params;
            }
            @Override
            public byte[] getBody() {
                return ("{\n" +
                        "    \"properties\" : {\n" +
                        "        \"Erledigt\" : {\n" +
                        "            \"checkbox\" : true\n" +
                        "        }\n" +
                        "    }\n" +
                        "}").getBytes(StandardCharsets.UTF_8);
            }
        };
        queue.add(stringRequest);
    }

    public static void getItems(Context context, String body) {
        if(db_id == null || secret == null) load_notion_data(context);
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = url_root + "databases/" + db_id + "/query";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            if (response != null) updateRemoteViews(response, context);
        }, error -> Log.e("get Items", "" + error)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer " + secret);
                params.put("Notion-Version", "2022-06-28");
                return params;
            }
            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }

            @Override
            public byte[] getBody() {
                return body.getBytes(StandardCharsets.UTF_8);
            }
        };
        queue.add(stringRequest);
    }

    private static void updateRemoteViews(String response, Context context){
        JSONObject data_json = MJSON.newJSONObject(response);
        if (data_json != null) {
            JSONArray results = MJSON.getJSONArray(data_json, "results");
            if(results != null){
                ArrayList<Item> items = Item.getItems(results);
                new TaskRunner().executeAsync(new InsertItems(context, items), (data) -> {});
            }
        }
    }

    public static void createNewItem(Context context, String kanbanState){
        if(db_id == null || secret == null) load_notion_data(context);
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = url_root + "pages/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            if (response != null) {
                JSONObject json = MJSON.newJSONObject(response);
                if(json != null){
                    String item_url = MJSON.getString(json, "url");
                    if(item_url != null) openNewItem(item_url, context);
                }
            }
        }, error -> Log.e("createNewItem", "" + error)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer " + secret);
                params.put("Notion-Version", "2022-06-28");
                return params;
            }
            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }

            @Override
            public byte[] getBody() {
                return ("{\n" +
                        "    \"parent\" :{\n" +
                        "        \"database_id\": \""+ db_id + "\"\n" +
                        "    },\n" +
                        "    \"properties\" : {\n" +
                        "        \"Kanban - Status\": {\n" +
                        "            \"select\": {\n" +
                        "                \"name\": \"" + kanbanState + "\"\n" +
                        "            }\n" +
                        "        },\n" +
                        "        \"Aktuelles\":{\n" +
                        "            \"checkbox\": true\n" +
                        "        }\n" +
                        "    }\n" +
                        "}").getBytes(StandardCharsets.UTF_8);
            }
        };
        queue.add(stringRequest);
    }

    private static void openNewItem(String item_url, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(item_url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }

    public static String getFilter_aktuellePrivateAufgaben_1(){
        return "{\n" +
                "    \"filter\" : {\n" +
                "        \"and\" : [\n" +
                "            {\n" +
                "                \"property\" : \"Erledigt\",\n" +
                "                \"checkbox\" : {\n" +
                "                    \"equals\" : false\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"property\" : \"Aktuelles\",\n" +
                "                \"checkbox\" : {\n" +
                "                    \"equals\" : true\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"or\" : [\n" +
                "                    {\n" +
                "                        \"property\" : \"Fällig\",\n" +
                "                        \"date\" : {\n" +
                "                            \"is_empty\" : true\n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"property\" : \"Fällig\",\n" +
                "                        \"date\" : {\n" +
                "                            \"on_or_before\" : \"" + getToday() + "\"\n" +
                "                        } \n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"property\" : \"Start\",\n" +
                "                        \"date\" : {\n" +
                "                            \"on_or_before\" : \"" + getToday() + "\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"property\" : \"Fach (Uni)\",\n" +
                "                \"select\": {\n" +
                "                    \"is_empty\" : true\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"sorts\" : [\n" +
                "        {\n" +
                "            \"property\" : \"Fällig\",\n" +
                "            \"direction\" : \"ascending\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }

    public static String getFilter_aktuellePrivateAufgaben_2(){
        return "{\n" +
                "    \"filter\" : {\n" +
                "        \"and\" : [\n" +
                "            {\n" +
                "                \"property\" : \"Erledigt\",\n" +
                "                \"checkbox\" : {\n" +
                "                    \"equals\" : false\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"or\" : [\n" +
                "                    {\n" +
                "                        \"property\" : \"Fällig\",\n" +
                "                        \"date\" : {\n" +
                "                            \"on_or_before\" : \"" + getToday() + "\"\n" +
                "                        } \n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"property\" : \"Start\",\n" +
                "                        \"date\" : {\n" +
                "                            \"on_or_before\" : \"" + getToday() + "\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"property\" : \"Fach (Uni)\",\n" +
                "                \"select\": {\n" +
                "                    \"is_empty\" : true\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"sorts\" : [\n" +
                "        {\n" +
                "            \"property\" : \"Fällig\",\n" +
                "            \"direction\" : \"ascending\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }

    @SuppressLint("SimpleDateFormat")
    private static String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }

}
