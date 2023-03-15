package com.example.notionwidget.Database;

public class KanbanState extends Item{

    public String name;
    public int backgroundResource;
    public int numberOfItems;

    public KanbanState(String name, int backgroundResource) {
        this.name = name;
        this.backgroundResource = backgroundResource;
    }

}
