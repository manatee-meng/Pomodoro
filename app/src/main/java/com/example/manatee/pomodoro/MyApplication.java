package com.example.manatee.pomodoro;

import android.app.Application;
import android.content.Context;

/**
 * Created by manatee on 2016/4/21.
 */
public class MyApplication extends Application {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}