package com.example.manatee.pomodoro;


import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.TypedValue;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by manatee on 2016/3/31.
 */
public abstract class BaseCalendarFrag extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {
    protected WeekView mWeekView;
    protected MyDataBaseHelper dbHelper;
    private EditEventListener mEditEventListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        mWeekView = (WeekView) view.findViewById(R.id.weekView);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setEmptyViewLongPressListener(this);
        mWeekView.setNumberOfVisibleDays(3);
        // Lets change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));

        dbHelper = new MyDataBaseHelper(view.getContext(), "Pomodoro.db", null, 1);
        dbHelper.getWritableDatabase();

        return view;
    }

    protected WeekViewEvent DrawEvent(long startMs, long endMs, String Message, long overflow, int isfinish, long id) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(startMs);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(endMs);
        Calendar now = Calendar.getInstance();

        WeekViewEvent event = new WeekViewEvent(1, Message, startCalendar, endCalendar);
        event.setId(id);
        if (isfinish == 0) {
            if (endCalendar.after(now))                                             //正在进行和未开始的任务显示蓝色
                event.setColor(getResources().getColor(R.color.event_color_01));
            else                                                                    //超过计划时间未完成的任务显示红色
                event.setColor(getResources().getColor(R.color.event_color_02));
        } else {
            if (overflow <= 0)                                                      //任务按时完成显示绿色
                event.setColor(getResources().getColor(R.color.event_color_03));
            else                                                                    //任务超时完成显示黄色
                event.setColor(getResources().getColor(R.color.event_color_04));
        }

        return event;
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Calendar startTime = event.getStartTime();
        Calendar endTime = event.getEndTime();
        String eventMsg = event.getName();
        final long eventId = event.getId();

        AlertDialog.Builder builder = new AlertDialog.Builder(mWeekView.getContext());
        LayoutInflater inflater = LayoutInflater.from(mWeekView.getContext());
        View EventInfoView = inflater.inflate(R.layout.dialog_eventinfo, null);
        builder.setView(EventInfoView);
        builder.setNeutralButton("未完成", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.UpdateFinish(eventId, false);
            }
        });
        builder.setNegativeButton("已完成", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.UpdateFinish(eventId, true);
            }
        });
        builder.setPositiveButton("返回", null);

        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
        TextView startTimeView = (TextView) EventInfoView.findViewById(R.id.EventInfo_StartTime);
        startTimeView.setText(dfTime.format(startTime.getTime()));
        TextView endTimeView = (TextView) EventInfoView.findViewById(R.id.EventInfo_EndTime);
        endTimeView.setText(dfTime.format(endTime.getTime()));
        TextView eventMsgView = (TextView) EventInfoView.findViewById(R.id.EventInfo_Msg);
        eventMsgView.setText(eventMsg);

        builder.setTitle("任务明细");
        builder.create().show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        long id = event.getId();
        mEditEventListener.OnEditEvent(id);
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {

    }

    public void setOnEditEventListener(EditEventListener editEventListener){
        this.mEditEventListener = editEventListener;
    }

    public interface EditEventListener{
        void OnEditEvent(long id);
    }
}
