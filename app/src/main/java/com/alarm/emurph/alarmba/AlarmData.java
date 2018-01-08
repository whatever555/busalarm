package com.alarm.emurph.alarmba;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by eddie on 05/01/18.
 */

public class AlarmData {

    String FILENAME = "alarmData3.json";
    String jsonString;
    JSONArray jsonArray;


    public String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.getApplicationContext().openFileInput(FILENAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }

        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public boolean isActive(JSONObject currentAlarmData)
    {
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int currentDayInt = calendar.get(Calendar.DAY_OF_WEEK);

        try {
            int hrs = Integer.parseInt(currentAlarmData.getString("hrs"));
            int mins = Integer.parseInt(currentAlarmData.getString("mins"));
            int duration = Integer.parseInt(currentAlarmData.getString("duration"));
            hrs += currentAlarmData.getString("ampm").equals("PM") ? 12 : 0;

            int alarmTime = (hrs * 60) + mins;

            int nowHour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
            int nowMinute = calendar.get(Calendar.MINUTE);

            int calendarTime = (nowHour * 60) + nowMinute;

            int active = Integer.parseInt(currentAlarmData.getString("active"));

            return currentAlarmData.getString("selected_days").
                    contains(Integer.toString(currentDayInt))
                    && active == 1
                    && calendarTime < alarmTime + duration
                    && calendarTime >= alarmTime;

        }
        catch (JSONException e){}
        return false;
    }


    public boolean deleteAlarm(Context context, int alarmId) {
        String jsonString = readFromFile(context);

        try {
            jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                    final JSONObject row = jsonArray.getJSONObject(i);
                    if (Integer.toString(alarmId).equals(row.getString("alarm_id"))) {
                        jsonArray.remove(i);
                        return writeToFile(context, jsonArray.toString());
                    }
                }
            }
            catch (JSONException e) {
            System.out.println(e.getMessage());
            }

        return false;
    }

    public int getAlarmIndex(Context context, int alarmId)
    {
        String jsonString = readFromFile(context);

        try {
            jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject row = jsonArray.getJSONObject(i);
                if (Integer.toString(alarmId).equals(row.getString("alarm_id"))) {
                    return i;
                }
            }
        }
        catch (JSONException e) {
            System.out.println(e.getMessage());
        }

        return -1;

    }


    public boolean writeToFile(Context context, String data) {
        try {
            FileOutputStream fou = context.getApplicationContext().openFileOutput(FILENAME, context.getApplicationContext().MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fou);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            return false;
        }
        return true;
    }


}
