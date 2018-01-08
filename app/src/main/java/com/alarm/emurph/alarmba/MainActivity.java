package com.alarm.emurph.alarmba;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dpro.widgets.WeekdaysPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String[] allBuses = new String[]{
            "select","1","1c","4","7","7a","7b","7d","9","11","13","14","14c",
            "15","15a","15b","15d","16","16c","17","17a","18","25",
            "25a","25b","25d","25x","26","27","27a","27b","27x","29a",
            "31","31a","31b","31d","32","32x","33","33a","33b","33d","33x",
            "37","38","38a","38b","38d","39","39a","39x","40","40b","40d",
            "41","41a","41b","41c","41x","42","42d","43","44","44b","45a","46a",
            "46e","47","49","51d","51x","53","54a","56a","59","61","63","65","65b",
            "66","66a","66b","66x","67","67x","68","68a","68x","69","69x","70","70d",
            "75","76","76a","77a","77x","79","79a","83","83a","84","84a","84x",
            "102","104","111","114","116","118","120","122","123","130","140",
            "142","145","150","151","161","184","185","220","236","238","239",
            "270","747","757"
    };

    AlarmData alarmData;
    String jsonString;

    JSONArray jsonArray;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("OPENEND");
        alarmData = new AlarmData();
        System.out.println("OPENEND2");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        System.out.println("OPENEND3");

        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlarmWindow(null);
            }
        });

        timer = new CountDownTimer(20000, 20) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                try{
                    loadApp();
                }catch(Exception e){
                    Log.e("Error", "Error: " + e.toString());
                }
            }
        }.start();

        loadApp();
    }

    public void loadApp(){

        LinearLayout alarmListHolder = (LinearLayout) findViewById(R.id.alarmListHolder); alarmListHolder.removeAllViews();

        jsonString = alarmData.readFromFile(this);

        View view;
        // Layout inflater
        LayoutInflater layoutInflater;
        jsonArray = new JSONArray();

        try {
            jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                // Parent layout
                RelativeLayout parentLayout = new RelativeLayout(this);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                parentLayout.setLayoutParams(rlp);
                alarmListHolder.addView(parentLayout);

                layoutInflater = getLayoutInflater();

                final JSONObject row = jsonArray.getJSONObject(i);

                String name = row.getString("name");
                final int alarmId = Integer.parseInt(row.getString("alarm_id"));

                // Add the text layout to the parent layout
                view = layoutInflater.inflate(R.layout.alarm_listing, parentLayout, false);

                // In order to get the view we have to use the new view with text_layout in it
                LinearLayout alarmView = (LinearLayout)view.findViewById(R.id.alarmView);

                TableRow editAlarmTR = (TableRow)view.findViewById(R.id.editAlarmTR);
                TextView alarmNameText = (TextView) view.findViewById(R.id.editAlarm);

                int active = Integer.parseInt(row.getString("active"));
                final Switch activeToggle = (Switch)view.findViewById(R.id.active_toggle);

                if (active == 0)
                    activeToggle.setChecked(false);

                alarmNameText.setText(name);

                // Add the text view to the parent layout
                parentLayout.addView(alarmView);

                activeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // do something, the isChecked will be
                        // true if the switch is in the On position
                        toggleActive(alarmId, activeToggle.isChecked());
                    }
                });

                if (alarmData.isActive(row))
                {
                    alarmView.setBackgroundColor(Color.RED);
                }

                editAlarmTR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAlarmWindow(row);
                    }
                });
                alarmNameText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAlarmWindow(row);
                    }
                });
            }
        }
        catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        timer.start();
    }




    public int getCurrentTimestamp(){
        return  (int) (new Date().getTime()/1000);
    }

    public void openAlarmWindow(JSONObject row) {
        final Context mContext = this;
        // Parent layout
        int resID = getResources().getIdentifier("main_layout", "id", getPackageName());
        RelativeLayout parentLayout = ((RelativeLayout) findViewById(resID));
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        final View customView = inflater.inflate(R.layout.alarm_view, null);

        // Initialize a new instance of popup window
        final PopupWindow mPopupWindow = new PopupWindow(
                customView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                true
        );
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        EditText name = (EditText) customView.findViewById((R.id.name));
        name.setTag("1");

        NumberPicker minsNumPick = (NumberPicker) customView.findViewById(R.id.mins);
        NumberPicker hrsNumPick = (NumberPicker) customView.findViewById(R.id.hrs);

        EditText duration = (EditText) customView.findViewById(R.id.alarm_duration);
        EditText notificationPrelay = (EditText) customView.findViewById(R.id.notification_prelay);

        minsNumPick.setMinValue(0);
        minsNumPick.setMaxValue(59);
        hrsNumPick.setMinValue(0);
        hrsNumPick.setMaxValue(11);
        hrsNumPick.setValue(7);
        minsNumPick.setValue(30);

        notificationPrelay.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "59")});
        duration.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "45")});

        minsNumPick.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });

        EditText stopNumberText = (EditText) customView.findViewById(R.id.stop_number);

        // Selection of the spinner
        Spinner routeSpinner1 = (Spinner) customView.findViewById(R.id.bus_routes_list1);
        Spinner routeSpinner2 = (Spinner) customView.findViewById(R.id.bus_routes_list2);
        Spinner routeSpinner3 = (Spinner) customView.findViewById(R.id.bus_routes_list3);

        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, allBuses);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view

        routeSpinner1.setAdapter(spinnerArrayAdapter);
        routeSpinner2.setAdapter(spinnerArrayAdapter);
        routeSpinner3.setAdapter(spinnerArrayAdapter);

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


        System.out.println("LADOMG TAG : "+name.getTag());

        // Get a reference for the custom view save button
        Button saveButton = (Button) customView.findViewById(R.id.save_button);

        final JSONObject rowToSend = row;
        // Set a click listener for the popup window save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveAlarm(customView, rowToSend)) {
                    mPopupWindow.dismiss();
                    loadApp();
                }
            }
        });

        if (row != null) {
            // Get a reference for the custom view delete button
            Button deleteButton = (Button) customView.findViewById(R.id.delete_button);
            try {

                deleteButton.setBackgroundColor(Color.parseColor("#990000"));
                final int alarmId = Integer.parseInt(row.getString("alarm_id"));
                // Set a click listener for the popup window delete button
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (alarmData.deleteAlarm(mContext, alarmId)) {
                            mPopupWindow.dismiss();
                            loadApp();
                        }
                    }
                });

                name.setText(row.getString("name"));
                name.setTag(row.getString("active"));
                System.out.println("SETTING TAG : "+row.getString("active"));
                System.out.println("GETTING TAG : "+name.getTag());
                minsNumPick.setValue(Integer.parseInt(row.getString("mins")));
                hrsNumPick.setValue(Integer.parseInt(row.getString("hrs")));

                ToggleButton ampm = (ToggleButton) customView.findViewById(R.id.ampm);
                ampm.setChecked(row.getString("ampm").equals("PM"));

                ToggleButton repeatToggle = (ToggleButton) customView.findViewById(R.id.repeat_toggle);
                repeatToggle.setChecked(row.getString("repeat_toggle").equals("No"));

                duration.setText(row.getString("duration"));
                notificationPrelay.setText(row.getString("notification_prelay"));
                stopNumberText.setText(row.getString("stop_number"));

                JSONObject routes = row.getJSONObject("routes");

                int spinnerPosition = spinnerArrayAdapter.getPosition(routes.getString("r1"));
                routeSpinner1.setSelection(spinnerPosition);
                spinnerPosition = spinnerArrayAdapter.getPosition(routes.getString("r2"));
                routeSpinner2.setSelection(spinnerPosition);
                spinnerPosition = spinnerArrayAdapter.getPosition(routes.getString("r3"));
                routeSpinner3.setSelection(spinnerPosition);

                WeekdaysPicker widget = (WeekdaysPicker) customView.findViewById(R.id.weekdays);

                List<String> selectedDays = Arrays.asList(
                        row.getString("selected_days").split("\\s*,\\s*")
                );

                List<Integer> selectedDaysInts = new ArrayList<>();
                for (String day : selectedDays) {
                    selectedDaysInts.add(Integer.valueOf(day));
                }
                widget.setSelectedDays(selectedDaysInts);
            }
            catch(JSONException e){
            System.out.println(e.getMessage());
        }
    }
        mPopupWindow.showAtLocation(parentLayout, Gravity.CENTER,0,0);
    }

    public void reloadApp(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    public boolean saveAlarm(View customView, JSONObject row) {
        try{
            JSONObject jsonObject = new JSONObject();

            EditText name = (EditText) customView.findViewById(R.id.name);
            NumberPicker minsNumPick = (NumberPicker) customView.findViewById(R.id.mins);
            NumberPicker hrsNumPick = (NumberPicker) customView.findViewById(R.id.hrs);
            ToggleButton ampm = (ToggleButton) customView.findViewById(R.id.ampm);
            ToggleButton repeatToggle = (ToggleButton) customView.findViewById(R.id.repeat_toggle);
            EditText duration = (EditText) customView.findViewById(R.id.alarm_duration);
            EditText notificationPrelay = (EditText) customView.findViewById(R.id.notification_prelay);
            EditText stopNumberText = (EditText) customView.findViewById(R.id.stop_number);

            // Selection of the spinner
            Spinner routeSpinner1 = (Spinner) customView.findViewById(R.id.bus_routes_list1);
            Spinner routeSpinner2 = (Spinner) customView.findViewById(R.id.bus_routes_list2);
            Spinner routeSpinner3 = (Spinner) customView.findViewById(R.id.bus_routes_list3);

            String route1 = routeSpinner1.getSelectedItem().toString();
            String route2 = routeSpinner2.getSelectedItem().toString();
            String route3 = routeSpinner3.getSelectedItem().toString();

            JSONObject routes = new JSONObject();
            routes.put("r1",route1);
            routes.put("r2",route2);
            routes.put("r3",route3);

            WeekdaysPicker widget = (WeekdaysPicker) customView.findViewById(R.id.weekdays);
            List<Integer> selectedDaysList = widget.getSelectedDays();

            String selectedDays = TextUtils.join(",", selectedDaysList);


            int alarmId;
            if (row == null) {
                alarmId = getCurrentTimestamp();
            }
            else {
                alarmId = Integer.parseInt(row.getString("alarm_id"));
                jsonArray.remove(alarmData.getAlarmIndex(this, alarmId));
              //  alarmData.deleteAlarm(this, alarmId);
            }

            jsonObject.put("alarm_id", Integer.toString(alarmId));
            jsonObject.put("active", name.getTag());
            jsonObject.put("name", name.getText());
            jsonObject.put("routes", routes);
            jsonObject.put("hrs", hrsNumPick.getValue());
            jsonObject.put("mins", minsNumPick.getValue());
            jsonObject.put("ampm", ampm.isChecked() ? ampm.getTextOn() : ampm.getTextOff());
            jsonObject.put("repeat_toggle", repeatToggle.isChecked() ? repeatToggle.getTextOn() : repeatToggle.getTextOff());
            jsonObject.put("duration", duration.getText());
            jsonObject.put("notification_prelay", notificationPrelay.getText());
            jsonObject.put("stop_number", stopNumberText.getText());
            jsonObject.put("selected_days", selectedDays);

            String nameStr = name.getText().toString();
            jsonArray.put(jsonObject);

            int hrs = hrsNumPick.getValue();

            if (ampm.isChecked()) {
                hrs+=12;
            }

            Alarm alarm = new Alarm();

          //  alarm.setAlarm(this, alarmId, nameStr, hrs, minsNumPick.getValue());

          //  alarmData.writeToFile(this, jsonArray.toString());


            alarmData.writeToFile(this, jsonArray.toString());

            alarm.setAlarms(this, 0);

            return true;

        }
        catch (JSONException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean toggleActive(int alarmId, boolean isChecked) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                final JSONObject row = jsonArray.getJSONObject(i);
                if(Integer.toString(alarmId).equals(row.getString("alarm_id")))
                {
                    if (isChecked)
                    {
                        row.put("active", "1");
                    }
                    else {
                        row.put("active", "0");
                    }
                    return alarmData.writeToFile(this, jsonArray.toString());
                }
            }
            catch (JSONException e) {
                System.out.println(e.getMessage());
            }
        }

        return false;
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
