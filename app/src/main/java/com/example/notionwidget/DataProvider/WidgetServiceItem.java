package com.example.notionwidget.DataProvider;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetServiceItem extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DataProviderItem(this, intent);
    }
}
