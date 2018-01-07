package com.alarm.emurph.alarmba;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by eddie on 05/01/18.
 */

public class AlarmData {

    String FILENAME = "alarmData.json";
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
