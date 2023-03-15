package com.example.notionwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.notionwidget.DataProvider.DataProviderItem;
import com.example.notionwidget.DataProvider.WidgetServiceItem;
import com.example.notionwidget.Database.Tasks.RemoveDeletedItems;
import com.example.notionwidget.Database.Tasks.ResetItems;
import com.example.notionwidget.Database.Tasks.TaskRunner;

import java.util.Timer;
import java.util.TimerTask;

public class WidgetProvider extends AppWidgetProvider {

    public static AppWidgetManager appWidgetManager;
    public static int[] appWidgetIds;
    private static Context context;

    private static Timer timer;
    public static final String RELOAD_ON_CLICK_TAG = "com.example.android.widgetProvider.RELOAD_ON_CLICK";
    public static final String OPEN_PRIVATE_HOME_PAGE = "com.example.android.widgetProvider.OPEN_PRIVATE_HOME_PAGE";
    public static final String PROCEED_ITEM_CLICK = "com.example.android.widgetProvider.PROCEED_ITEM_CLICK";
    public static final String EXTRA_ACTION = "com.example.android.widgetProvider.EXTRA_ACTION";
    public static final String OPEN_NOTION_PAGE = "com.example.android.widgetProvider.OPEN_NOTION_PAGE";
    public static final String SET_ITEM_DONE = "com.example.android.widgetProvider.SET_ITEM_DONE";
    public static final String EXTRA_URL = "com.example.android.widgetProvider.EXTRA_URL";
    public static final String EXTRA_ID = "com.example.android.widgetProvider.EXTRA_ID";
    public static final String EXTRA_NAME = "com.example.android.widgetProvider.EXTRA_NAME";
    public static final String CREATE_NEW_ITEM = "com.example.android.widgetProvider.CREATE_NEW_ITEM";
    public static final String EXTRA_KANBAN_STATE = "com.example.android.widgetProvider.KANBAN_STATE";

    public static void updateRemoteViews(){
        if (WidgetProvider.appWidgetManager != null && WidgetProvider.appWidgetIds != null) {
            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(WidgetProvider.appWidgetIds, R.id.item_container);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()){
            case RELOAD_ON_CLICK_TAG:
                new TaskRunner().executeAsync(new ResetItems(context), (data) -> {});
                createWidgetUpdaterThread(context);
                break;
            case OPEN_PRIVATE_HOME_PAGE:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.notion.so/Privat-8d1bc98729b04809abe538c1f8a736d7"));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(browserIntent);
                break;
            case PROCEED_ITEM_CLICK:
                String action = intent.getStringExtra(EXTRA_ACTION);
                if(action == null) break;
                switch(action){
                    case OPEN_NOTION_PAGE:
                        String url = intent.getStringExtra(EXTRA_URL);
                        if(url != null){
                            Intent browserIntent2 = new Intent(Intent.ACTION_VIEW);
                            browserIntent2.setData(Uri.parse(url));
                            browserIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(browserIntent2);
                        }
                        break;
                    case SET_ITEM_DONE:
                        String id = intent.getStringExtra(EXTRA_ID);
                        String name = intent.getStringExtra(EXTRA_NAME);
                        if(id != null){
                            Notion.setItemDone(context, id);
                            Toast.makeText(context, name + " erledigt", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case CREATE_NEW_ITEM:
                        String kanban_state = intent.getStringExtra(EXTRA_KANBAN_STATE);
                        if(kanban_state != null) Notion.createNewItem(context, kanban_state);
                        break;
                }
                break;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        Toast.makeText(context,"onEnabled called", Toast.LENGTH_LONG).show();
        new TaskRunner().executeAsync(new ResetItems(context), (data) -> {});
        createWidgetUpdaterThread(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d("debug", "onUpdate");
        WidgetProvider.context = context;
        WidgetProvider.appWidgetManager = appWidgetManager;
        WidgetProvider.appWidgetIds = appWidgetIds;
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, WidgetServiceItem.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_provider);
            remoteViews.setRemoteAdapter(R.id.item_container, intent);

            remoteViews.setOnClickPendingIntent(R.id.title, getPendingSelfIntent(context, OPEN_PRIVATE_HOME_PAGE));

            remoteViews.setOnClickPendingIntent(R.id.reload_widget, getPendingSelfIntent(context, RELOAD_ON_CLICK_TAG));

            remoteViews.setOnClickPendingIntent(R.id.new_item, getPendingSelfIntent(context, CREATE_NEW_ITEM));

            Intent itemIntent = new Intent(context, WidgetProvider.class);
            itemIntent.setAction(PROCEED_ITEM_CLICK);
            itemIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent openNotionPagePendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                openNotionPagePendingIntent = PendingIntent.getBroadcast(context, 0, itemIntent, PendingIntent.FLAG_MUTABLE);
            else
                openNotionPagePendingIntent = PendingIntent.getBroadcast(context, 0, itemIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.item_container, openNotionPagePendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        Toast.makeText(context,"onDisabled called", Toast.LENGTH_LONG).show();
        new TaskRunner().executeAsync(new ResetItems(context), (data) -> {});
        removeWidgetUpdateThread();
    }

    private void createWidgetUpdaterThread(Context context) {
        removeWidgetUpdateThread();
        if(timer == null) timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Notion.getItems(context, Notion.getFilter_aktuellePrivateAufgaben_1());
                Notion.getItems(context, Notion.getFilter_aktuellePrivateAufgaben_2());
            }
        }, 0, 2000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new TaskRunner().executeAsync(new RemoveDeletedItems(context), (data) -> {});
            }
        },5000, 5000);
    }

    private void removeWidgetUpdateThread() {
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

}
