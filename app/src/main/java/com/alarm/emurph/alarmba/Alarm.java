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
import android.os.PowerManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
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
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");


       // setAlarms(context);

        wl.acquire();

        // Put here YOUR code.
        //Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example

        int requestCode = intent.getIntExtra("requestCode", 0);
        String name = intent.getStringExtra("name");

        if (requestCode == 0) {
            cancelAlarm(context, 0, name);
        } else {
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
                if (currentAlarmData == null) {
                    cancelAlarm(context, requestCode, name);
                } else {
                    Date now = new Date();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(now);
                    int currentDayInt = calendar.get(Calendar.DAY_OF_WEEK);

                    int active = Integer.parseInt(currentAlarmData.getString("active"));

                    if (active == 1)
                    if (currentAlarmData.getString("selected_days").contains(Integer.toString(currentDayInt))) {
                        int hrs = Integer.parseInt(currentAlarmData.getString("hrs"));
                        int mins = Integer.parseInt(currentAlarmData.getString("mins"));
                        int duration = Integer.parseInt(currentAlarmData.getString("duration"));
                        hrs += currentAlarmData.getString("ampm").equals("PM") ? 12 : 0;

                        int alarmTime = (hrs * 60) + mins;

                        int nowHour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
                        int nowMinute = calendar.get(Calendar.MINUTE);

                        int calendarTime = (nowHour * 60) + nowMinute;

                        if (calendarTime < alarmTime + duration && calendarTime >= alarmTime) {

                            try {
                                //    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                //    StrictMode.setThreadPolicy(policy);

                                String stopNumber = currentAlarmData.getString("stop_number");

                               // String jsonBusString = getStopInfo(stopNumber);
                                AsyncLoader RF = new AsyncLoader(context, stopNumber, currentAlarmData);
                                RF.execute();

                            } catch (Exception e) {
                                sendNotification(context, " fucked up inmores");
                            }
                        } else {
                            cancelAlarm(context, requestCode, name);

                            if (currentAlarmData.getString("repeat_toggle").equals("No")) {
                                String days = currentAlarmData.getString("selected_days");
                                List<String> selectedDays = Arrays.asList(
                                        days.split("\\s*,\\s*")
                                );

                                int lastDay = Integer.parseInt(selectedDays.get(selectedDays.size() - 1));
                                if (lastDay == currentDayInt) {
                                    disableNonRepeatingAlarm(context, requestCode);
                                }
                            }
                            //sendNotification(context, " CANCELLINGs " +currentAlarmData.getString("repeat_toggle"));
                        }
                    }
                }
            } catch (JSONException e) {
            }
        }

        wl.release();
    }


    public void setAlarm(Context context, int requestCode, String name, int hrs, int mins) {

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
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 11, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context, int requestCode, String name) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        setAlarms(context);
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
            URL busList = new URL("https://data.dublinked.ie/cgi-bin/rtpi/realtimebusinformation?stopid=" + stopId + "&format=json");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            busList.openStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null)
                retStr += inputLine;

            in.close();
            return retStr;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
               // url = new URL("https://data.dublinked.ie/cgi-bin/rtpi/realtimebusinformation?stopid=" + stopNumber + "&format=json");
                url = new URL("https://imaga.me/test.php");
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

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();

                return "";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

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

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (jsonBusString.length() > 1) {
                try {

                    JSONObject routes = this.currentAlarmData.getJSONObject("routes");

                    String route1 = routes.getString("r1");
                    String route2 = routes.getString("r2");
                    String route3 = routes.getString("r3");

                    try {
                        JSONObject busArray = new JSONObject(jsonBusString);
                        JSONArray stopDataArray = busArray.getJSONArray("results");
                        boolean allRoutes = route1.equals(route2) && route2.equals(route3) && route3.equals("select");

                        for (int i = 0; i < stopDataArray.length(); i++) {
                            final JSONObject row = stopDataArray.getJSONObject(i);

                            String busRoute = row.getString("route");
                            int duetime = Integer.parseInt(row.getString("duetime"));

                            int notificaionPrelay = Integer.parseInt(currentAlarmData.getString("notification_prelay"));


                            if (allRoutes || (busRoute.equals(route1) || busRoute.equals(route2) || busRoute.equals(route3))) {
                                if (duetime == notificaionPrelay) {
                                    sendNotification(context, busRoute + " arriving to stop " + stopNumber + " in " + notificaionPrelay + " mins");
                                }
                            }
                                                    }
                    } catch (JSONException e) {
                        sendNotification(context, " fucked up in 5 mins");

                    }
                    //content.setText(sb.toString());
                } catch (JSONException e) {

                }
            }
        }

    }

    public void disableNonRepeatingAlarm(Context context, int alarmId) {
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
        }catch(JSONException e){}
    }


    public void setAlarms(Context context) {
        String jsonString = alarmData.readFromFile(context);
        try {
            JSONObject currentAlarmData = null;
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

                setAlarm(context, alarmId, alarmName, hrs, mins);
            }
        }catch(JSONException e){}

    }
}