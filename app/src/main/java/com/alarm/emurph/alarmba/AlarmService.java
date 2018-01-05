package com.alarm.emurph.alarmba;

/**
 * Created by eddie on 05/01/18.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service
{
    Alarm alarm = new Alarm();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.setAlarm(this, intent.getIntExtra("requestCode", 0), intent.getStringExtra("name"));
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        alarm.setAlarm(this, intent.getIntExtra("requestCode", 0), intent.getStringExtra("name"));
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}