package com.example.notionwidget.Database.Tasks;

import android.content.Context;

import com.example.notionwidget.Database.AppDatabase;
import com.example.notionwidget.Database.Item;
import com.example.notionwidget.Database.MDate;
import com.example.notionwidget.WidgetProvider;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class InsertItems implements Callable<Void> {

    private final AppDatabase db;
    private final ArrayList<Item> items_new;

    public InsertItems(Context context, ArrayList<Item> items){
        this.db = AppDatabase.getInstance(context);
        this.items_new = items;
    }

    @Override
    public Void call() {
        boolean itemsUpdated = false;
        for(Item item_new : items_new){
            Item item_old = db.itemDao().getByNotionId(item_new.notion_id);
            if(item_old == null) {
                db.itemDao().insertItem(item_new);
                itemsUpdated = true;
            } else {
                if (item_changed(item_old, item_new)) {
                    db.itemDao().removeItem(item_old.notion_id);
                    db.itemDao().insertItem(item_new);
                    itemsUpdated = true;
                }
                db.itemDao().setEdited(item_old.notion_id);
            }
        }
        if(itemsUpdated) WidgetProvider.updateRemoteViews();
        return null;
    }

    private boolean item_changed(Item item_old, Item item_new) {
        return element_changed(item_old.icon, item_new.icon) ||
                element_changed(item_old.name, item_new.name) ||
                element_changed(item_old.state, item_new.state) ||
                due_changed(item_old.due, item_new.due) ||
                item_old.done != item_new.done ||
                element_changed(item_old.kanban_state, item_new.kanban_state);
    }

    private boolean due_changed(MDate element1, MDate element2) {
        if(element1 == null && element2 == null) return false;
        else if(element1 == null) return true;
        else if(element2 == null) return true;
        else return element_changed(element1.value, element2.value);
    }

    private boolean element_changed(Object element1, Object element2) {
        if(element1 == null && element2 == null) return false;
        else if(element1 == null) return true;
        else if(element2 == null) return true;
        else return !element1.equals(element2);
    }

}
