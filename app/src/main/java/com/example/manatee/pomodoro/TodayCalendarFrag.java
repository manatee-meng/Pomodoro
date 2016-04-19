package com.example.manatee.pomodoro;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by manatee on 2016/3/31.
 */
public class TodayCalendarFrag extends BaseCalendarFrag {

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        Calendar monthStart = Calendar.getInstance();
        monthStart.set(newYear, newMonth-1, 1);                      //月份要减一才是正常月份，大概因为是枚举类型吧
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);
        monthStart.set(Calendar.MILLISECOND, 0);
        Calendar monthEnd = Calendar.getInstance();
        monthEnd.set(newYear, newMonth, 1);                          //这里有一个大bug，跨天的任务没法显示
        monthEnd.set(Calendar.DAY_OF_MONTH, 1);
        monthEnd.set(Calendar.HOUR, 0);
        monthEnd.set(Calendar.MINUTE, 0);
        monthEnd.set(Calendar.SECOND, 0);
        monthEnd.set(Calendar.MILLISECOND, 0);
        String[] strSelectCondition = new String[2];
        strSelectCondition[0] = Long.toString(monthStart.getTimeInMillis());
        strSelectCondition[1] = Long.toString(monthEnd.getTimeInMillis());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from events where start>=? and end<=?", strSelectCondition);
        //Cursor cursor = db.rawQuery("select * from events", null);
        if (cursor.moveToFirst()) {
            long id;
            long startMs;
            long endMs;
            String eventMessage;
            long overflowTime;
            int isfinish;
            do {
                id = cursor.getLong(cursor.getColumnIndex("id"));
                startMs = cursor.getLong(cursor.getColumnIndex("start"));
                endMs = cursor.getLong(cursor.getColumnIndex("end"));
                eventMessage = cursor.getString(cursor.getColumnIndex("message"));
                overflowTime = cursor.getLong(cursor.getColumnIndex("overflow"));
                isfinish = cursor.getInt(cursor.getColumnIndex("isfinish"));
                WeekViewEvent event = DrawEvent(startMs, endMs, eventMessage, overflowTime, isfinish, id);
                events.add(event);
            } while (cursor.moveToNext());
        }

        return events;
    }

}
