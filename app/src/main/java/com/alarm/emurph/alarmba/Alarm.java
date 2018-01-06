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
import android.os.StrictMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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
            sendNotification(context,"request 0 ");
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
                    sendNotification(context,"null data 0 ");
                    cancelAlarm(context, requestCode, name);
                }else{
                    Date now = new Date();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(now);
                    int currentDayInt = calendar.get(Calendar.DAY_OF_WEEK);

                    if (currentAlarmData.getString("selected_days").contains(Integer.toString(currentDayInt)))
                    {
                        int hrs = Integer.parseInt(currentAlarmData.getString("hrs"));
                        int mins = Integer.parseInt(currentAlarmData.getString("mins"));
                        int duration = Integer.parseInt(currentAlarmData.getString("duration"));
                        hrs += currentAlarmData.getString("ampm").equals("PM") ? 12 : 0 ;

                        int alarmTime = (hrs * 60) + mins;

                        int nowHour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
                        int nowMinute = calendar.get(Calendar.MINUTE);

                        int calendarTime = (nowHour * 60) + nowMinute;

                        if (alarmTime < calendarTime + duration && alarmTime >= calendarTime) {

                            JSONObject routes = currentAlarmData.getJSONObject("routes");

                            String route1 = routes.getString("r1");
                            String route2 = routes.getString("r2");
                            String route3 = routes.getString("r3");

                            try {
                                String jsonBusString = getStopInfo(currentAlarmData.getString("stop_number"));

                                try {
                                    JSONArray busArray = new JSONArray(jsonBusString);
                                    JSONArray stopData = busArray.getJSONArray(5);

                                    for (int i = 0; i < stopData.length(); i++) {
                                        final JSONObject row = stopData.getJSONObject(i);
                                        String busRoute = row.getString("result");
                                        if (!(busRoute.equals(route1) || busRoute.equals(route2) || busRoute.equals(route3)))
                                        {
                                            break;
                                        }
                                        if (row.getString("duetime").equals("5")) {
                                            sendNotification(context, busRoute + " arriving to stop " + currentAlarmData.getString("stop_number") + "in 5 mins");
                                        }
                                    }
                                }catch(JSONException e){

                                }
                            }
                            catch (Exception e) {

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



    public void setAlarm(Context context, int requestCode, String name, int hrs, int mins)
    {

        Calendar calendar = new GregorianCalendar();
        Calendar calendar2 = new GregorianCalendar();

        Date date = new Date();
        date.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        calendar.setTime(date);
        calendar2.setTime(date);

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar2.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hrs);
        calendar.set(Calendar.MINUTE, mins);

        System.out.println("KKKKK: "+calendar.getTime()+ "   "+calendar2.getTime());

        System.out.println("HHHH: "+calendar.getTimeInMillis()+ "   "+calendar2.getTimeInMillis());
        if (calendar.getTimeInMillis() < calendar2.getTimeInMillis() - 1000) {
            calendar.add(Calendar.DATE, 1);
        }
        System.out.println("HHHH: "+calendar.getTimeInMillis()+ "   "+calendar2.getTimeInMillis());

        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, Alarm.class);
        i.putExtra("requestCode", requestCode);
        i.putExtra("hrs", hrs);
        i.putExtra("mins", mins);
        i.putExtra("name", name);
        System.out.println("about to set it");
// calendar.getTimeInMillis()
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 30, pi); // Millisec * Second * Minute

        System.out.println("finihsed set it");
    }

    public void cancelAlarm(Context context, int requestCode, String name)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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