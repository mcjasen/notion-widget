package com.example.notionwidget.Database.Tasks;

import android.content.Context;

import com.example.notionwidget.Database.AppDatabase;
import com.example.notionwidget.Database.Item;
import com.example.notionwidget.WidgetProvider;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class RemoveDeletedItems implements Callable<Void> {

    private final AppDatabase db;

    public RemoveDeletedItems(Context context) {
        db = AppDatabase.getInstance(context);
    }

    @Override
    public Void call() {
        boolean items_update = false;
        ArrayList<Item> allNotEditedItems = new ArrayList<>(db.itemDao().getAllNotEdited());
        for(Item item : allNotEditedItems){
            db.itemDao().removeItem(item.notion_id);
            items_update = true;
        }
        ArrayList<Item> items = new ArrayList<>(db.itemDao().getAll());
        for (Item item : items){
            db.itemDao().setNotEdited(item.notion_id);
        }
        if(items_update) WidgetProvider.updateRemoteViews();
        return null;
    }
}
