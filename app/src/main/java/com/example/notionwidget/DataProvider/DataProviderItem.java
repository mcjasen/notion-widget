package com.example.notionwidget.DataProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.notionwidget.Database.AppDatabase;
import com.example.notionwidget.Database.Item;
import com.example.notionwidget.Database.KanbanState;
import com.example.notionwidget.Database.MDate;
import com.example.notionwidget.R;
import com.example.notionwidget.WidgetProvider;

import java.util.ArrayList;
import java.util.Comparator;

public class DataProviderItem implements RemoteViewsService.RemoteViewsFactory {

    protected final Context context;
    private final ArrayList<KanbanState> kanban_states;
    private final static ArrayList<Item> items = new ArrayList<>();

    public DataProviderItem(Context context, Intent intent) {
        this.context = context;
        kanban_states = new ArrayList<>();
        kanban_states.add(new KanbanState("To Do", R.drawable.to_do));
        kanban_states.add(new KanbanState("Doing", R.drawable.doing));
        kanban_states.add(new KanbanState("On Hold", R.drawable.on_hold));
    }

    @Override
    public void onCreate() {
        WidgetProvider.updateRemoteViews();
    }

    @Override
    public void onDataSetChanged() {
        items.clear();
        for(KanbanState kanban_state : kanban_states){
            ArrayList<Item> items_per_kanban_state = new ArrayList<>(AppDatabase.getInstance(context).itemDao().getAllByKanbanState(kanban_state.name));
            kanban_state.numberOfItems = items_per_kanban_state.size();
            items_per_kanban_state.sort(Comparator.comparing(Item::getDueValue, Comparator.nullsLast(Comparator.comparingLong(due -> due))));
            items.add(kanban_state);
            items.addAll(items_per_kanban_state);
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public void onDestroy() {}

    @SuppressLint("RemoteViewLayout")
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.item);
        Item item = items.get(position);
        if(item.getClass() == KanbanState.class){
            KanbanState kanbanState = (KanbanState) item;
            setNameForKanbanState(remoteViews, kanbanState);
            setBackgroundForKanbanState(remoteViews, kanbanState.backgroundResource);
            setState(remoteViews, null);
            setDue(remoteViews, null);
            setOnClickEvent(remoteViews, null);
            setDoneButton(remoteViews, null);
            setAddItemButton(remoteViews, kanbanState.name);
            setNumberOfItems(remoteViews, kanbanState.numberOfItems);
        }
        else {
            setName(remoteViews, item);
            setBackground(remoteViews);
            setState(remoteViews, item.state);
            setDue(remoteViews, item.due);
            setDoneButton(remoteViews, item);
            setOnClickEvent(remoteViews, item.url);
            setAddItemButton(remoteViews, null);
            setNumberOfItems(remoteViews, null);
        }
        return remoteViews;
    }

    private void setNumberOfItems(RemoteViews remoteViews, Integer numberOfItems) {
        if(numberOfItems == null) remoteViews.setViewVisibility(R.id.number_of_items, View.GONE);
        else {
            remoteViews.setViewVisibility(R.id.number_of_items, View.VISIBLE);
            remoteViews.setTextViewText(R.id.number_of_items, Integer.toString(numberOfItems));
        }
    }

    private void setAddItemButton(RemoteViews remoteViews, String kanban_state) {
        if(kanban_state == null) remoteViews.setViewVisibility(R.id.new_item, View.GONE);
        else{
            remoteViews.setViewVisibility(R.id.new_item, View.VISIBLE);
            Bundle bundle = new Bundle();
            bundle.putString(WidgetProvider.EXTRA_ACTION, WidgetProvider.CREATE_NEW_ITEM);
            bundle.putString(WidgetProvider.EXTRA_KANBAN_STATE, kanban_state);
            remoteViews.setOnClickFillInIntent(R.id.new_item, getFillInIntent(bundle));
        }
    }

    private void setDoneButton(RemoteViews remoteViews, Item item) {
        if(item == null){
            remoteViews.setViewVisibility(R.id.done, View.GONE);
        }
        else{
            remoteViews.setViewVisibility(R.id.done, View.VISIBLE);
            remoteViews.setOnClickFillInIntent(R.id.done, getFillInIntent(WidgetProvider.SET_ITEM_DONE, null, item.notion_id, item.name));
        }
    }

    private void setBackgroundForKanbanState(RemoteViews remoteViews, int backgroundResource) {
        remoteViews.setInt(R.id.root, "setBackgroundResource", 0);
        remoteViews.setInt(R.id.name, "setBackgroundResource", backgroundResource);
    }

    private void setBackground(RemoteViews remoteViews) {
        remoteViews.setInt(R.id.root, "setBackgroundResource", R.drawable.item);
        remoteViews.setInt(R.id.name, "setBackgroundResource", R.drawable.item_name);
    }

    private void setNameForKanbanState(RemoteViews remoteViews, KanbanState kanbanState) {
        remoteViews.setTextViewText(R.id.name, kanbanState.name);
        remoteViews.setTextColor(R.id.name, Color.parseColor("#FFFFFF"));
    }

    protected void setName(RemoteViews remoteViews, Item item) {
        String name = "";
        if(item.icon != null) name += item.icon + " ";
        if(item.name != null){
            name += item.name;
            remoteViews.setTextColor(R.id.name, Color.parseColor("#FFFFFF"));
        }
        else{
            name += "Unbenannt";
            remoteViews.setTextColor(R.id.name, Color.parseColor("#7F7F7F"));
        }
        remoteViews.setTextViewText(R.id.name, name);
    }

    protected void setState(RemoteViews remoteViews, String state) {
        if(state == null){
            remoteViews.setTextViewText(R.id.state, "");
            remoteViews.setViewVisibility(R.id.state, View.GONE);
        }
        else{
            remoteViews.setTextViewText(R.id.state, state);
            remoteViews.setViewVisibility(R.id.state, View.VISIBLE);
        }
    }

    protected void setDue(RemoteViews remoteViews, MDate due) {
        if(due == null){
            remoteViews.setTextViewText(R.id.due, "");
            remoteViews.setViewVisibility(R.id.due, View.GONE);
        }
        else{
            if(due.include_time){
                remoteViews.setTextViewText(R.id.due, MDate.timeInMillisToString(due.value, "dd. MMM yyyy HH:mm"));
            }
            else {
                remoteViews.setTextViewText(R.id.due, MDate.timeInMillisToString(due.value, "dd. MMM yyyy"));
            }
            remoteViews.setViewVisibility(R.id.due, View.VISIBLE);
        }
    }

    protected void setOnClickEvent(RemoteViews remoteViews, String url) {
        remoteViews.setOnClickFillInIntent(R.id.name, getFillInIntent(WidgetProvider.OPEN_NOTION_PAGE, url, null, null));
        remoteViews.setOnClickFillInIntent(R.id.state, getFillInIntent(WidgetProvider.OPEN_NOTION_PAGE, url, null, null));
        remoteViews.setOnClickFillInIntent(R.id.due, getFillInIntent(WidgetProvider.OPEN_NOTION_PAGE, url, null, null));
    }

    private Intent getFillInIntent(Bundle bundle){
        Intent fillIntent = new Intent();
        fillIntent.putExtras(bundle);
        return fillIntent;
    }

    private Intent getFillInIntent(String action, String url, String id, String name) {
        Bundle bundle = new Bundle();
        bundle.putString(WidgetProvider.EXTRA_ACTION, action);
        if(url != null) bundle.putString(WidgetProvider.EXTRA_URL, url);
        if(id != null) {
            bundle.putString(WidgetProvider.EXTRA_ID, id);
            bundle.putString(WidgetProvider.EXTRA_NAME, name);
        }
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(bundle);
        return fillInIntent;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }



}
