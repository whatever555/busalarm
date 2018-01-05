package com.alarm.emurph.alarmba;

/**
 * Created by eddie on 05/01/18.
 */

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class Alarm extends BroadcastReceiver
{
    AlarmData alarmData = new AlarmData();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // Put here YOUR code.
        //Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example

        int requestCode = intent.getIntExtra("requestCode",0);
        String name = intent.getStringExtra("name");

        if (requestCode == 0)
        {
            cancelAlarm(context, 0, name);
            sendNotification(context, "Cancelling alarm");
        }else{
            String jsonString = alarmData.readFromFile(context);
            try {
                JSONObject currentAlarmData = null;
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    final JSONObject row = jsonArray.getJSONObject(i);
                    if(requestCode == Integer.parseInt(row.getString("alarm_id")))
                    {
                        currentAlarmData = row;
                        break;
                    }
                }
                if(currentAlarmData == null){
                    cancelAlarm(context, requestCode, name);
                }else{
                    Date now = new Date();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(now);
                    int currentDayInt = calendar.get(Calendar.DAY_OF_WEEK);

                    if (currentAlarmData.getString("selected_days").indexOf(Integer.toString(currentDayInt)) > -1)
                    {
                        int hrs = Integer.parseInt(currentAlarmData.getString("hrs"));
                        int mins = Integer.parseInt(currentAlarmData.getString("mins"));
                        int duration = Integer.parseInt(currentAlarmData.getString("duration"));
                        hrs += currentAlarmData.getString("ampm").equals("PM") ? 12 : 0 ;

                        int alarmTime = (hrs * 60) + mins;

                        int nowHour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
                        int nowMinute = calendar.get(Calendar.MINUTE);

                        int calendarTime = (nowHour * 60) + nowMinute;

                        if ((alarmTime + duration) < calendarTime && alarmTime > calendarTime) {

                            JSONObject routes = currentAlarmData.getJSONObject("routes");

                            String route1 = routes.getString("r1");
                            String route2 = routes.getString("r2");
                            String route3 = routes.getString("r3");

                            String jsonBusString = getStopInfo(currentAlarmData.getString("stop_number"));

                            System.out.println(jsonBusString);

                            try {

                                JSONArray busArray = new JSONArray(jsonBusString);


                                for (int i = 0; i < busArray.length(); i++) {

                                }
                            }catch(JSONException e){

                            }

                        }
                        else {
                            cancelAlarm(context, requestCode, name);
                        }

                    }
                }
            }
            catch (JSONException e) {}
        }

        System.out.println();
        wl.release();
    }



    public void setAlarm(Context context, int requestCode, String name)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        i.putExtra("requestCode", requestCode);
        i.putExtra("name", name);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 30, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context, int requestCode, String name)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sendNotification(context, "Alarm cancelled" + name);
    }

    public void sendNotification(Context context, String message) {
        PugNotification.with(context)
                .load()
                .title(message)
                .message(message)
                .bigTextStyle(message)
                .smallIcon(R.drawable.pugnotification_ic_launcher)
                .largeIcon(R.drawable.pugnotification_ic_launcher)
                .flags(Notification.DEFAULT_ALL)
                .simple()
                .build();

    }


    public String getStopInfo(String stopId) {
        try {
            String retStr = "";
            URL busList = new URL("https://data.dublinked.ie/cgi-bin/rtpi/realtimebusinformation?stopid="+stopId+"&format=json");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            busList.openStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null)
                retStr+=inputLine;

            in.close();
            return retStr;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}