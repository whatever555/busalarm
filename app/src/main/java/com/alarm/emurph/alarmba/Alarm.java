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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class Alarm extends BroadcastReceiver {
    AlarmData alarmData = new AlarmData();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");

            // setAlarms(context);

            wl.acquire();

            // Put here YOUR code.
            //Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
            String name; int requestCode = 0;
            try {
                requestCode = intent.getIntExtra("requestCode", 0);
                name = intent.getStringExtra("name");
            }catch(Exception e){ return;}
            String jsonString = alarmData.readFromFile(context);
            try {
                JSONObject currentAlarmData = null;
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    final JSONObject row = jsonArray.getJSONObject(i);
                    if (requestCode == Integer.parseInt(row.getString("alarm_id"))) {
                        currentAlarmData = row;
                        break;
                    }
                }

                if (alarmData.isActive(currentAlarmData)) {
                    try {
                        //    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        //    StrictMode.setThreadPolicy(policy);

                        String stopNumber = currentAlarmData.getString("stop_number");

                        // String jsonBusString = getStopInfo(stopNumber);
                        AsyncLoader RF = new AsyncLoader(context, stopNumber, currentAlarmData);
                        RF.execute();

                    } catch (Exception e) {
                        setAlarms(context, 0);
                    }
                } else {
                    setAlarms(context, 0);
                    cancelAlarm(context, requestCode, name, false);

                    if (currentAlarmData.getString("repeat_toggle").equals("No")) {
                        String days = currentAlarmData.getString("selected_days");
                        List<String> selectedDays = Arrays.asList(
                                days.split("\\s*,\\s*")
                        );

                        int lastDay = Integer.parseInt(selectedDays.get(selectedDays.size() - 1));

                        Date now = new Date();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);
                        int currentDayInt = calendar.get(Calendar.DAY_OF_WEEK);
                        if (lastDay == currentDayInt) {
                            disableNonRepeatingAlarm(context, requestCode, 0);
                        }
                    }
                }
            } catch (Exception e) {
            } finally {
                //  setAlarms(context, 0);
                //  cancelAlarm(context, requestCode, name, false);
            }

            wl.release();
        }catch(Exception e){}
    }


    public void setAlarm(Context context, int requestCode, String name, int hrs, int mins) {
        try {
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

            if (calendar.getTimeInMillis() < calendar2.getTimeInMillis() - 1000) {
                calendar.add(Calendar.DATE, 1);
            }

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(context, Alarm.class);
            i.putExtra("requestCode", requestCode);
            i.putExtra("hrs", hrs);
            i.putExtra("mins", mins);
            i.putExtra("name", name);

            PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 26, pi); // Millisec * Second * Minute
        }catch(Exception e){}
    }

    public void cancelAlarm(Context context, int requestCode, String name, boolean beforeSetting) {
        try {
            Intent intent = new Intent(context, Alarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
            //  if (!beforeSetting) {
            //      setAlarms(context, 0);
            //  }
        }catch(Exception e){}

}

    public void sendNotification(Context context, String message) {
        try{
            Intent mIntent = new Intent(context, MainActivity.class);
            Bundle extras = mIntent.getExtras();
            PugNotification.with(context)
                    .load()
                    .title("Transport notification")
                    .message(message)
                    .bigTextStyle(message)
                    .smallIcon(R.drawable.pugnotification_ic_launcher)
                    .largeIcon(R.drawable.pugnotification_ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .click(MainActivity.class, extras)
                    .color(android.R.color.background_dark)
                    .simple()
                    .build();
        }catch(Exception e){}

    }


    private class AsyncLoader extends AsyncTask<String, Integer, String> {

        // static String FILENAME = "test.txt";
        HttpURLConnection conn;
        URL url;
        String stopNumber;
        Context context;
        int READ_TIMEOUT = 2200;
        int CONNECTION_TIMEOUT = 2200;
        String jsonBusString;
        JSONObject currentAlarmData;

        public AsyncLoader(Context context, String stopNumber, JSONObject currentAlarmData) {
            super();
            this.currentAlarmData=currentAlarmData;
            this.context = context;
            this.stopNumber = stopNumber;
        }

        @Override
        protected String doInBackground(String... str) {
            try {
                // Enter URL address where your php file resides
                url = new URL("https://data.dublinked.ie/cgi-bin/rtpi/realtimebusinformation?stopid=" + stopNumber + "&format=json");
                //url = new URL("https://imaga.me/test.php");
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoOutput to true as we recieve data from json file
                conn.setDoOutput(false);

            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();

                return "";
            }

            try {
                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    try {
                        // Read data sent from server
                        InputStream input = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        StringBuilder result = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }

                        jsonBusString = result.toString();
                        // Pass data to onPostExecute method
                        return (result.toString());
                    }
                    catch(Exception e) {

                        return ("unsuccessful");
                    }

                } else {

                    return ("unsuccessful");
                }

            }catch(Exception e){
                //sendNotification(context, "ERROR 4" +  e.getClass().getSimpleName());
                //e.printStackTrace();

                return "";
            } finally {
              //  setAlarms(context, 0);
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try{
            super.onPostExecute(s);

            if (jsonBusString != null)
            if (jsonBusString.length() > 1) {
                try {
                    JSONObject routes = this.currentAlarmData.getJSONObject("routes");

                    String route1 = routes.getString("r1");
                    String route2 = routes.getString("r2");
                    String route3 = routes.getString("r3");

                    try {
                        JSONObject busArray = new JSONObject(jsonBusString);
                        JSONArray stopDataArray = busArray.getJSONArray("results");
                        boolean allRoutes = route1.equals(route2) && route2.equals(route3) && (route3.equals("select") || (route3.equals("----------")));

                        if (stopDataArray != null)
                        for (int i = 0; i < stopDataArray.length(); i++) {
                            final JSONObject row = stopDataArray.getJSONObject(i);

                            String busRoute = row.getString("route");
                            try {
                                String dt = row.getString("duetime");
                                if (!dt.equals("Due")) {
                                    int duetime = Integer.parseInt(dt);

                                    int notificationPrelay = Integer.parseInt(currentAlarmData.getString("notification_prelay"));

                                    if (allRoutes || (busRoute.equals(route1) || busRoute.equals(route2) || busRoute.equals(route3))) {
                                        if (duetime == notificationPrelay || duetime == notificationPrelay - 1) {
                                            sendNotification(context, busRoute + " arriving to stop " + stopNumber + " in " + notificationPrelay + " mins");
                                        }
                                    }
                                }
                            }catch(Exception e){}
                        }
                    }catch(Exception e){}
                    //content.setText(sb.toString());
                }catch(Exception e){}
            }
            }catch(Exception e){}
        }
    }

    public void disableNonRepeatingAlarm(Context context, int alarmId, int attempt) {
        try{
        String jsonString = alarmData.readFromFile(context);
        try {
            JSONObject currentAlarmData = null;
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject row = jsonArray.getJSONObject(i);
                if (alarmId == Integer.parseInt(row.getString("alarm_id")))
                {
                    row.put("active", "0");
                    alarmData.deleteAlarm(context, alarmId);
                    alarmData.writeToFile(context, jsonArray.toString());

                }
            }
        }catch(JSONException e){
            if (attempt < 3) {
                disableNonRepeatingAlarm(context, alarmId, attempt++);
            }
            else {
              //  setAlarms(context, 0);
            }
        }

        }catch(Exception e){}
    }


    public void setAlarms(Context context, int attempt) {
        try{
        String jsonString = alarmData.readFromFile(context);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject row = jsonArray.getJSONObject(i);
                int alarmId = Integer.parseInt(row.getString("alarm_id"));
                int hrs = Integer.parseInt(row.getString("hrs"));
                int mins = Integer.parseInt(row.getString("mins"));
                String ampm = row.getString("ampm");
                if(ampm.equals("PM"))
                {
                    hrs+=12;
                }
                String alarmName = row.getString("name");
                cancelAlarm(context, alarmId, alarmName, true);
                setAlarm(context, alarmId, alarmName, hrs, mins);
            }
        }catch(JSONException e){
            if (attempt < 3) {
                setAlarms(context, attempt++);
            }

        }

        }catch(Exception e){}
    }
}