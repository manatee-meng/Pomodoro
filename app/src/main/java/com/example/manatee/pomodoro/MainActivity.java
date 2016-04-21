package com.example.manatee.pomodoro;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.app.FragmentManager;

import cn.bmob.v3.Bmob;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BaseCalendarFrag.EditEventListener {
    private MyDataBaseHelper dbHelper;

    private FragmentManager fManager;
    public TodayCalendarFrag fg_calendar;
    public AddEventFrag fg_addEvent;
    public CloudFrag fg_cloud;
    private Button button_calendar;
    private Button button_addEvent;
    private Button button_deleteRecord;
    private Button button_cloudSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "ee2ff79b97c0b948c25b0042d2d2ed90");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fManager = getFragmentManager();
        fg_addEvent = new AddEventFrag();
        fg_calendar = new TodayCalendarFrag();
        fg_calendar.setOnEditEventListener(this);
        fg_cloud = new CloudFrag();

        button_calendar = (Button) findViewById(R.id.button_calendar);
        button_calendar.setOnClickListener(this);
        button_addEvent = (Button) findViewById(R.id.button_addEvent);
        button_addEvent.setOnClickListener(this);
        button_deleteRecord = (Button) findViewById(R.id.bottom_button3);
        button_deleteRecord.setOnClickListener(this);
        button_cloudSetting = (Button) findViewById(R.id.bottom_button4);
        button_cloudSetting.setOnClickListener(this);
        dbHelper = new MyDataBaseHelper(this, "Pomodoro.db", null, 1);
        dbHelper.getWritableDatabase();

        button_calendar.performClick();                                 //模拟点击一次
    }

    private void showFragment(FragmentTransaction transaction, Fragment fragment) {
        transaction.hide(fg_calendar);
        transaction.hide(fg_addEvent);
        transaction.hide(fg_cloud);
        transaction.show(fragment);
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = fManager.beginTransaction();
        BmobDataBase remoteDB = new BmobDataBase(MainActivity.this);
        switch (v.getId()) {
            case R.id.button_calendar:
                showFragment(fragmentTransaction, fg_calendar);
                fragmentTransaction.replace(R.id.fragment_layout, fg_calendar);
                fragmentTransaction.addToBackStack(null);
                break;
            case R.id.button_addEvent:
                fg_addEvent.setNeedUpdate(false);
                showFragment(fragmentTransaction, fg_addEvent);
                fragmentTransaction.replace(R.id.fragment_layout, fg_addEvent);
                fragmentTransaction.addToBackStack(null);
                break;
            case R.id.bottom_button3:
                AlertDialog.Builder dialog_deleteRecords = new AlertDialog.Builder(this);
                dialog_deleteRecords.setTitle("删除记录");
                dialog_deleteRecords.setMessage("删除记录后无法恢复，是否确认删除？");
                dialog_deleteRecords.setCancelable(false);
                dialog_deleteRecords.setNegativeButton("否", new DialogInterface.OnClickListener() {     //ubuntu风格的确认按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog_deleteRecords.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteRecord();
                    }
                });
                dialog_deleteRecords.show();
                break;
            case R.id.bottom_button4:
                showFragment(fragmentTransaction, fg_cloud);
                fragmentTransaction.replace(R.id.fragment_layout, fg_cloud);
                fragmentTransaction.addToBackStack(null);
                break;
            default:
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_day_view:
                fg_calendar.setVisibleDays(1);
                break;
            case R.id.action_three_day_view:
                fg_calendar.setVisibleDays(3);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnEditEvent(long id) {
        fg_addEvent.setNeedUpdate(true);
        fg_addEvent.setEventId(id);
        FragmentTransaction fragmentTransaction = fManager.beginTransaction();
        showFragment(fragmentTransaction, fg_addEvent);
        fragmentTransaction.replace(R.id.fragment_layout, fg_addEvent);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
