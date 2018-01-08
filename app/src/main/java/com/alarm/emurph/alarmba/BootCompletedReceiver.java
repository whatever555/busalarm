package com.alarm.emurph.alarmba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by eddie on 07/01/18.
 */

public class BootCompletedReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {
        Alarm alarm = new Alarm();
        alarm.setAlarms(context, 0);
    }
}
