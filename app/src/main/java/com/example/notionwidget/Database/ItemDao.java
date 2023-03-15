package com.example.notionwidget.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemDao {

    @Query("SELECT * FROM Item")
    List<Item> getAll();

    @Query("SELECT * FROM Item WHERE kanban_state = :kanban_state")
    List<Item> getAllByKanbanState(String kanban_state);

    @Query("SELECT * FROM Item WHERE notion_id = :notion_id")
    Item getByNotionId(String notion_id);

    @Insert
    void insertItem(Item item);

    @Query("DELETE FROM Item WHERE notion_id = :notion_id")
    void removeItem(String notion_id);

    @Query("DELETE FROM Item")
    void reset();

    @Query("UPDATE Item SET edited = 1 WHERE notion_id = :notion_id")
    void setEdited(String notion_id);

    @Query("UPDATE Item SET edited = 0 WHERE notion_id = :notion_id")
    void setNotEdited(String notion_id);

    @Query("SELECT * FROM Item WHERE edited = 0")
    List<Item> getAllNotEdited();
}
