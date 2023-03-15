package com.example.notionwidget.Database.Tasks;

import android.content.Context;

import com.example.notionwidget.Database.AppDatabase;
import com.example.notionwidget.WidgetProvider;

import java.util.concurrent.Callable;

public class RemoveItem implements Callable<Void> {

    private final AppDatabase db;
    private final String notion_id;

    public RemoveItem(Context context, String notion_id) {
        this.db = AppDatabase.getInstance(context);
        this.notion_id = notion_id;
    }

    @Override
    public Void call() {
        db.itemDao().removeItem(notion_id);
        WidgetProvider.updateRemoteViews();
        return null;
    }
}
