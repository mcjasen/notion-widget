package com.example.notionwidget.Database.Tasks;

import android.content.Context;

import com.example.notionwidget.Database.AppDatabase;
import com.example.notionwidget.WidgetProvider;

import java.util.concurrent.Callable;

public class ResetItems implements Callable<Void> {

    private final AppDatabase db;

    public ResetItems(Context context) {
        this.db = AppDatabase.getInstance(context);
    }

    @Override
    public Void call() {
        db.itemDao().reset();
        WidgetProvider.updateRemoteViews();
        return null;
    }
}
