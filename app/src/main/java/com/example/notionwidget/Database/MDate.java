package com.example.notionwidget.Database;

import android.annotation.SuppressLint;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MDate {

    public long value;
    public boolean include_time;

    public MDate() {}

    @SuppressLint("SimpleDateFormat")
    public MDate(String string){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date date = null;
        try{
            date = simpleDateFormat.parse(string);
            include_time = true;
        } catch (ParseException e) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                date = simpleDateFormat.parse(string);
                include_time = false;
            } catch (ParseException ignore) {}
        }
        if(date != null) value = date.getTime();
    }

    public static String timeInMillisToString(Long timeInMillis, String pattern){
        Date date = new Date(timeInMillis);
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}
