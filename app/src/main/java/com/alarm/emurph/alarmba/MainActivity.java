package com.alarm.emurph.alarmba;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String FILENAME = "data.json";
        String string = "";

        writeToFile(FILENAME, "[{\"label\":\"Everyday Alarm\",\"days_string\":\"MON,TUE,WED,THU,FRI\", \"start_time\":\"1:00\",\"end_time\":\"1:15\",\"buses\":[\"14\",\"15\"],\"stops\":[\"667\"]},{\"label\":\"Weekend Alarm\", \"start_time\":\"2:00\",\"days_string\":\"SAT,SUN\",\"end_time\":\"1:15\",\"buses\":[\"14\",\"15\"],\"stops\":[\"667\"]}]");

        String jsonString = readFromFile(FILENAME);

        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addAlarmWindow();

            }
        });

        View view;
        // Layout inflater
        LayoutInflater layoutInflater;

        System.out.println(jsonString);
        try {
            String alarmLabel;
            String start_time;
            String end_time;
            JSONArray buses;
            JSONArray stops;
            System.out.println(jsonString);
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {

                // Parent layout
                int resID = getResources().getIdentifier("layout"+i, "id", getPackageName());
                RelativeLayout parentLayout = ((RelativeLayout) findViewById(resID));

                layoutInflater = getLayoutInflater();
                System.out.println("LOOPING");
                JSONObject row = array.getJSONObject(i);
                start_time = row.getString("start_time");
                end_time = row.getString("end_time");
                alarmLabel = row.getString("label");
                String daysString = row.getString("days_string");
                buses = new JSONArray(row.getString("buses"));
                stops = new JSONArray(row.getString("stops"));

                System.out.println("start_time" + start_time);
                // Add the text layout to the parent layout
                view = layoutInflater.inflate(R.layout.alarm_listing, parentLayout, false);

                // In order to get the view we have to use the new view with text_layout in it
                TableLayout alarmView = (TableLayout)view.findViewById(R.id.alarmView);

                Button editButton = (Button)view.findViewById(R.id.editAlarm);
                editButton.setText(alarmLabel);

                // Add the text view to the parent layout
                parentLayout.addView(alarmView);
            }
        }
        catch (JSONException e) {

        }
    }


    public void addAlarmWindow() {
        Context mContext = this;
        // Parent layout
        int resID = getResources().getIdentifier("add_alarm", "id", getPackageName());
        RelativeLayout parentLayout = ((RelativeLayout) findViewById(resID));
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View customView = inflater.inflate(R.layout.alarm_view,null);

        // Initialize a new instance of popup window
        final PopupWindow mPopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );

        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            mPopupWindow.setElevation(5.0f);
        }

        // Get a reference for the custom view close button
        Button closeButton = (Button) customView.findViewById(R.id.cancel_button);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });

                /*
                    public void showAtLocation (View parent, int gravity, int x, int y)
                        Display the content view in a popup window at the specified location. If the
                        popup window cannot fit on screen, it will be clipped.
                        Learn WindowManager.LayoutParams for more information on how gravity and the x
                        and y parameters are related. Specifying a gravity of NO_GRAVITY is similar
                        to specifying Gravity.LEFT | Gravity.TOP.

                    Parameters
                        parent : a parent view to get the getWindowToken() token from
                        gravity : the gravity which controls the placement of the popup window
                        x : the popup's x location offset
                        y : the popup's y location offset
                */
        // Finally, show the popup window at the center location of root relative layout
        mPopupWindow.showAtLocation(parentLayout, Gravity.CENTER,0,0);

    }

    public String readFromFile(String FILENAME) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(FILENAME);

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

    public void writeToFile(String FILENAME, String data) {
        try {
            FileOutputStream fou = openFileOutput(FILENAME, MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fou);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
