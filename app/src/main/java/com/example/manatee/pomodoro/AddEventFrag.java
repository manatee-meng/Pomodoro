package com.example.manatee.pomodoro;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by manatee on 2016/4/1.
 */
public class AddEventFrag extends Fragment implements View.OnClickListener {
    private MyDataBaseHelper dbHelper;
    private TextView dataTextView;
    private TextView startTextView;
    private TextView endTextView;
    private EditText msgEdit;
    private View fragView;
    private TimePicker timePicker;
    private DatePicker datePicker;
    private Button confirmButton;
    private Button deleteButton;

    private Calendar startTime;
    private Calendar endTime;
    private String EventMsg;
    private long eventId;

    private boolean NeedUpdate;

    public void setEventId(long id) {
        eventId = id;
    }

    public void setNeedUpdate(boolean flag) {
        NeedUpdate = flag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        fragView = view;
        confirmButton = (Button) view.findViewById(R.id.Button_AddEvent);
        confirmButton.setOnClickListener(this);
        deleteButton = (Button) view.findViewById(R.id.Button_DeleteEvent);
        deleteButton.setOnClickListener(this);
        dataTextView = (TextView) view.findViewById(R.id.select_date);
        dataTextView.setOnClickListener(this);
        startTextView = (TextView) view.findViewById(R.id.select_start);
        startTextView.setOnClickListener(this);
        endTextView = (TextView) view.findViewById(R.id.select_end);
        endTextView.setOnClickListener(this);
        msgEdit = (EditText) view.findViewById(R.id.EventMsgEdit);

        dbHelper = new MyDataBaseHelper(view.getContext(), "Pomodoro.db", null, 1);

        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        endTime.add(Calendar.MINUTE, 30);
        EventMsg = "";
        if (NeedUpdate) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String[] SqlArg = new String[1];
            SqlArg[0] = Long.toString(eventId);
            Cursor cursor = db.rawQuery("select * from events where id=?", SqlArg);
            if ( cursor.moveToFirst() ) {
                startTime.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("start")));
                endTime.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("end")));
                EventMsg = cursor.getString(cursor.getColumnIndex("message"));
            }
        }
        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
        startTextView.setText("" + dfTime.format(startTime.getTime()));
        endTextView.setText("" + dfTime.format(endTime.getTime()));
        SimpleDateFormat dfData = new SimpleDateFormat("yyyy-MM-dd");
        dataTextView.setText("" + dfData.format(startTime.getTime()));
        msgEdit.setText(EventMsg);
        return view;
    }

    @Override
    public void onClick(View v) {
        LayoutInflater inflater = LayoutInflater.from(fragView.getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(fragView.getContext());
        View timePickerView;
        View datePickerView;
        switch (v.getId()) {
            case R.id.select_start:
                timePickerView = inflater.inflate(R.layout.dialog_timepicker, null);
                builder.setView(timePickerView);
                timePicker = (TimePicker) timePickerView.findViewById(R.id.Timepicker_on_Dialog);
                timePicker.setIs24HourView(true);
                timePicker.setCurrentHour(startTime.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(startTime.get(Calendar.MINUTE));
                builder.setTitle("起始时间");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                        startTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
                        startTextView.setText("" + dfTime.format(startTime.getTime()));
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
                break;
            case R.id.select_end:
                timePickerView = inflater.inflate(R.layout.dialog_timepicker, null);
                builder.setView(timePickerView);
                timePicker = (TimePicker) timePickerView.findViewById(R.id.Timepicker_on_Dialog);
                timePicker.setIs24HourView(true);
                timePicker.setCurrentHour(endTime.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(endTime.get(Calendar.MINUTE));
                builder.setTitle("结束时间");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        endTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                        endTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
                        endTextView.setText("" + dfTime.format(endTime.getTime()));
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
                break;
            case R.id.select_date:
                datePickerView = inflater.inflate(R.layout.dialog_datepicker, null);
                builder.setView(datePickerView);
                datePicker = (DatePicker) datePickerView.findViewById(R.id.DatePicker_on_Dialog);
                datePicker.init(startTime.get(Calendar.YEAR),
                        startTime.get(Calendar.MONTH),
                        startTime.get(Calendar.DAY_OF_MONTH),
                        null);
                builder.setTitle("选择日期");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startTime.set(Calendar.YEAR, datePicker.getYear());
                        startTime.set(Calendar.MONTH, datePicker.getMonth());
                        startTime.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                        endTime.set(Calendar.YEAR, datePicker.getYear());
                        endTime.set(Calendar.MONTH, datePicker.getMonth());
                        endTime.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                        SimpleDateFormat dfTime = new SimpleDateFormat("yyyy-MM-dd");
                        dataTextView.setText("" + dfTime.format(startTime.getTime()));
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
                break;
            case R.id.Button_AddEvent:
                EventMsg = msgEdit.getText().toString();
                if (EventMsg.equals("")) {
                    Toast.makeText(v.getContext(), "请添加文字描述", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (NeedUpdate){
                    dbHelper.UpdateEvent(eventId, startTime, endTime, EventMsg, 0, false);
                } else {
                    if (dbHelper.addEvent(startTime, endTime, EventMsg))
                        Toast.makeText(v.getContext(), "添加成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.Button_DeleteEvent:
                if (NeedUpdate)
                    dbHelper.deleteRecord(eventId);
                break;
            default:
        }
    }
}
