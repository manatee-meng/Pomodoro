package com.example.manatee.pomodoro;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by manatee on 2016/4/21.
 */
public class EventDAO {
    private MyDataBaseHelper dbHelper;
    private Context mContext;

    public EventDAO() {
        Context context = MyApplication.getContext();
        dbHelper = new MyDataBaseHelper(context, "Pomodoro.db", null, 1);
    }

    public void insert(long id, long startTime, long endTime, String Msg, long overflow, boolean isFinish) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        long flag = 0;
        if (isFinish) flag = 1;
        String[] SqlArg = new String[6];
        SqlArg[0] = Long.toString(id);
        SqlArg[1] = Long.toString(startTime);
        SqlArg[2] = Long.toString(endTime);
        SqlArg[3] = Msg;
        SqlArg[4] = Long.toString(overflow);
        SqlArg[5] = Long.toString(flag);
        try {
            db.execSQL("insert into events (id, start, end, message, overflow, isfinish) values(?,?,?,?,?,?)", SqlArg);
        } catch (Exception e) {
            Log.e("dbHelper", e.getMessage());
            Toast.makeText(mContext, "本地数据库添加失败！", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void insert(Calendar startTime, Calendar endTime, String Msg) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        String[] SqlArg = new String[5];
        SqlArg[0] = Long.toString(startTime.getTimeInMillis());
        SqlArg[1] = Long.toString(endTime.getTimeInMillis());
        SqlArg[2] = Msg;
        SqlArg[3] = Long.toString(0);
        SqlArg[4] = Long.toString(0);
        try {
            db.execSQL("insert into events (start, end, message, overflow, isfinish) values(?,?,?,?,?)", SqlArg);
        } catch (Exception e) {
            Log.e("dbHelper", e.getMessage());
            Toast.makeText(mContext, "本地数据库添加失败！", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void insert(ScheduleUnit scheduleUnit) {
        boolean isFinish = false;
        if (scheduleUnit.getIsfinish() == 1) isFinish = true;
        insert(scheduleUnit.getUnitId(), scheduleUnit.getStartTime(), scheduleUnit.getEndTime(),
                scheduleUnit.getMessage(), scheduleUnit.getOverflow(), isFinish);
    }

    public void update(long id, Calendar startTime, Calendar endTime, String Msg, long overflow, boolean isFinish) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        long flag = 0;
        if (isFinish) flag = 1;
        String[] SqlArg = new String[6];
        SqlArg[0] = Long.toString(startTime.getTimeInMillis());
        SqlArg[1] = Long.toString(endTime.getTimeInMillis());
        SqlArg[2] = Msg;
        SqlArg[3] = Long.toString(overflow);
        SqlArg[4] = Long.toString(flag);
        SqlArg[5] = Long.toString(id);
        try {
            db.execSQL("Update events set start=?, end=?, message=?, overflow=?, isfinish=? where id=?", SqlArg);
        } catch (Exception e) {
            Log.e("dbHelper", e.getMessage());
            Toast.makeText(mContext, "修改失败！", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(mContext, "修改成功！", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public void delete(long id) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        String[] SqlArg = new String[1];
        SqlArg[0] = Long.toString(id);
        try {
            db.execSQL("delete from events where id=?", SqlArg);
        } catch (Exception e) {
            String ExceptionName = e.getMessage();
            Toast.makeText(mContext, "删除失败！", Toast.LENGTH_SHORT).show();
            Log.e(this.getClass().getName(), ExceptionName);
        }
        Toast.makeText(mContext, "已删除！", Toast.LENGTH_SHORT).show();
        db.close();
    }
}
