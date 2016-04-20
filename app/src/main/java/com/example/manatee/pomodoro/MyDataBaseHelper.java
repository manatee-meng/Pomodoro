package com.example.manatee.pomodoro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.BaseCallback;
import cn.bmob.v3.listener.FindCallback;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SQLQueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by manatee on 2016/3/31.
 */
public class MyDataBaseHelper extends SQLiteOpenHelper {
    private Context mContext;
    public static final String CREATE_EVENTS = "create table events("
            + "id integer primary key autoincrement, "
            + "start integer, "
            + "end integer, "
            + "message text, "
            + "overflow integer, "
            + "isfinish integer)";

    public MyDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EVENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addEvent(Calendar start, Calendar end, String msg) {
        long startMs = start.getTimeInMillis();
        long endMs = end.getTimeInMillis();
        if (startMs > endMs) {
            Toast.makeText(mContext, "end time is earlier than start time!", Toast.LENGTH_SHORT).show();
            return false;
        }

        //检查记录是否已经添加过
        SQLiteDatabase db = this.getWritableDatabase();
        if (db == null) {
            Toast.makeText(mContext, "database is read-only!", Toast.LENGTH_SHORT).show();
            return false;
        }
        String[] strSelectCondition = new String[3];
        strSelectCondition[0] = Long.toString(startMs);
        strSelectCondition[1] = Long.toString(endMs);
        strSelectCondition[2] = msg;
        Cursor cursor = db.rawQuery("select * from events where start=? and end=? and message=?", strSelectCondition);
        if (cursor.moveToFirst()) {
            Toast.makeText(mContext, "任务已存在！", Toast.LENGTH_SHORT).show();
            return false;
        }
        cursor.close();

        //生成记录
        ContentValues values = new ContentValues();
        values.put("start", startMs);
        values.put("end", endMs);
        values.put("message", msg);
        values.put("overflow", (long) 0);
        values.put("isfinish", 0);

        //添加记录
        try {
            db.insertOrThrow("events", null, values);
        } catch (Exception e) {
            String ExceptionName = e.getMessage();
            Toast.makeText(mContext, "添加失败！", Toast.LENGTH_SHORT).show();
            Log.e(this.getClass().getName(), ExceptionName);
            return false;
        }
        return true;
    }

    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("drop table events");
        db.execSQL("create table events("
                + "id integer primary key autoincrement, "
                + "start integer, "
                + "end integer, "
                + "message text, "
                + "overflow integer, "
                + "isfinish integer)");
    }

    public void deleteRecord() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from events");
    }

    public void deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
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
    }

    public void UpdateFinish(long id, boolean isFinish) {
        SQLiteDatabase db = this.getWritableDatabase();
        long flag = 0;
        if (isFinish) flag = 1;
        String[] SqlArg = new String[2];
        SqlArg[0] = Long.toString(flag);
        SqlArg[1] = Long.toString(id);
        try {
            db.execSQL("Update events set isfinish=? where id=?", SqlArg);
        } catch (Exception e) {
            Log.e("dbHelper", e.getMessage());
            Toast.makeText(mContext, "修改失败！", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(mContext, "修改成功！", Toast.LENGTH_SHORT).show();
    }

    public void UpdateEvent(long id, Calendar startTime, Calendar endTime, String Msg, long overflow, boolean isFinish) {
        SQLiteDatabase db = this.getWritableDatabase();
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
    }

    public void insert(long id, long startTime, long endTime, String Msg, long overflow, boolean isFinish){
        SQLiteDatabase db = this.getWritableDatabase();
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
    }
}
