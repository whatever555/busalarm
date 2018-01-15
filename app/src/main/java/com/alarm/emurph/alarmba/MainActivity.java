package com.alarm.emurph.alarmba;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dpro.widgets.WeekdaysPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;


public class MainActivity extends AppCompatActivity {
    Context mContext = this;
    int currentInc = 0;
    Button routeBtn1,routeBtn2,routeBtn3;
    String[] stopsList = new String[]{

    };

    String[] allBusesArray = new String[]{
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
            "270","747","757", "red", "green"
    };
    String stopVal = "0";
    String r1 = "select",r2="select",r3 = "select";
    MySpinnerDialog searchSpinnerDialog;

    ArrayList<String> allBuses = new ArrayList<>(Arrays.asList(allBusesArray));
    ArrayList<String> allBusesDisplay = new ArrayList<String>(allBuses);

    ArrayList<String> allStops = new ArrayList<>(Arrays.asList(stopsList));
    ArrayList<String> allStopsDisplay = new ArrayList<String>(allStops);

    AlarmData alarmData;
    String jsonString;

    JSONArray jsonArray;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmData = new AlarmData();
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

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
                final RelativeLayout parentLayout = new RelativeLayout(this);
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

                    final TableRow mainRow = (TableRow)alarmView.findViewById(R.id.editAlarmTR);
                    mainRow.setBackgroundColor(Color.GREEN);

                    final TableLayout tl = (TableLayout)alarmView.findViewById(R.id.live_data);
                    // String jsonBusString = getStopInfo(stopNumber);
                    AsyncLoader RF = new AsyncLoader(mContext, row.getString("stop_number"), row, tl);
                    RF.execute();
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

    //private method of your class
    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }


    public int getCurrentTimestamp(){
        return  (int) (new Date().getTime()/1000);
    }

    public void openAlarmWindow(JSONObject row) {
        // Parent layout
        int resID = getResources().getIdentifier("main_layout", "id", getPackageName());
        final RelativeLayout parentLayout = ((RelativeLayout) findViewById(resID));
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

        notificationPrelay.setFilters(new InputFilter[]{new InputFilterMinMax("1", "59")});
        duration.setFilters(new InputFilter[]{new InputFilterMinMax("1", "45")});

        minsNumPick.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });

        final EditText stopNumberText = (EditText) customView.findViewById(R.id.stop_number);

        final RouteSpinnerDialog routeSpinner1 = new RouteSpinnerDialog(
                MainActivity.this,
                "Select route:",
                R.style.DialogAnimations_SmileWindow
        );// With 	Animation

        final RouteSpinnerDialog routeSpinner2 = new RouteSpinnerDialog(
                MainActivity.this,
                "Select route:",
                R.style.DialogAnimations_SmileWindow
        );// With 	Animation
        final RouteSpinnerDialog routeSpinner3 = new RouteSpinnerDialog(
                MainActivity.this,
                "Select route:",
                R.style.DialogAnimations_SmileWindow
        );// With 	Animation




        // Get a reference for the custom view close button
        Button closeButton = (Button) customView.findViewById(R.id.cancel_button);
        // Get a reference for the custom view save button
        final Button saveButton = (Button) customView.findViewById(R.id.save_button);
        // Get a reference for the custom view delete button
        Button deleteButton = (Button) customView.findViewById(R.id.delete_button);
        // Get a reference for the custom view delete button
        final Button stopSearchButton = (Button) customView.findViewById(R.id.stop_search_button);

        // Get a reference for the custom view delete button
        routeBtn1 = (Button) customView.findViewById(R.id.route1_button);
        // Get a reference for the custom view delete button
        routeBtn2 = (Button) customView.findViewById(R.id.route2_button);
        // Get a reference for the custom view delete button
        routeBtn3 = (Button) customView.findViewById(R.id.route3_button);

        //searchSpinnerDialog=new MYSpinnerDialog(MainActivity.this,allStopsDisplay,"Search for stop",mContext);// With No Animation
        searchSpinnerDialog=new MySpinnerDialog(MainActivity.this,allStopsDisplay,"Search for stop",R.style.DialogAnimations_SmileWindow,mContext);// With 	Animation


        if (!isNetworkAvailable()){
            stopSearchButton.setVisibility(View.GONE);
        }else{
            stopNumberText.setVisibility(View.GONE);
        }
        routeSpinner1.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                routeBtn1.setText(item);
                r1 = item;
            }
        });
        routeSpinner2.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                routeBtn2.setText(item);
                r2 = item;
            }
        });
        routeSpinner3.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                routeBtn3.setText(item);
                r3 = item;
            }
        });

        searchSpinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                String[] stopData=item.split(":");
                stopNumberText.setText(stopData[0]);
                stopSearchButton.setText(stopData[0]);
            }
        });
        stopSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSpinnerDialog.showSpinerDialog();
                stopSearchBox.setText(stopNumberText.getText().toString());
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(customView, 0);
            }
        });
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });


        routeBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSpinner1.showSpinerDialog();

            }
        });
        routeBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSpinner2.showSpinerDialog();
            }
        });
        routeBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSpinner3.showSpinerDialog();
            }
        });

        //TODO should this be commented out
        //allBusesDisplay.addAll(allBusesDisplay);
        try {
        stopVal = (row == null) ? "0" : row.getString("stop_number");

        stopNumberText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                try {
                    String stStr = stopNumberText.getText().toString();
                    System.out.println("String : " + stStr);
                    if (stStr.length() > 0) {
                        System.out.println("OK : " + stStr);

                        if (!stStr.equals("0") && !stStr.equals(stopVal)) {
                            stopVal = stStr;

                            System.out.println("S1 : " + r1 + "+" + r2 + "+" + r3);

                            customView.requestLayout();
                            routeSpinner1.setEnabled(false);
                            routeSpinner2.setEnabled(false);
                            routeSpinner3.setEnabled(false);
                            routeSpinner1.setClickable(false);
                            routeSpinner2.setClickable(false);
                            routeSpinner3.setClickable(false);

                            saveButton.setEnabled(false);
                            saveButton.setClickable(false);

                            customView.requestLayout();

                            allBusesDisplay.removeAll(allBusesDisplay);

                            customView.requestLayout();
                            currentInc++;
                            final int snapInc = currentInc;
                            try {
                                if (s.length() > 0) {
                                    RouteLister RL = new RouteLister(
                                            mContext,
                                            stopNumberText.getText().toString(),
                                            customView,
                                            snapInc
                                    );
                                    RL.execute();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        System.out.println("CANNOT CONVERT ================  : " + stStr);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }catch (Exception e){
            e.printStackTrace();
        }

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
                minsNumPick.setValue(Integer.parseInt(row.getString("mins")));
                hrsNumPick.setValue(Integer.parseInt(row.getString("hrs")));

                ToggleButton ampm = (ToggleButton) customView.findViewById(R.id.ampm);
                ampm.setChecked(row.getString("ampm").equals("PM"));

                ToggleButton repeatToggle = (ToggleButton) customView.findViewById(R.id.repeat_toggle);
                repeatToggle.setChecked(row.getString("repeat_toggle").equals("No"));

                duration.setText(row.getString("duration"));
                notificationPrelay.setText(row.getString("notification_prelay"));
                stopNumberText.setText(row.getString("stop_number"));
                stopSearchButton.setText(row.getString("stop_number"));
                JSONObject routes = row.getJSONObject("routes");

                r1 = routes.getString("r1").toLowerCase();
                r2 = routes.getString("r2").toLowerCase();
                r3 = routes.getString("r3").toLowerCase();

                routeBtn1.setText(r1);
                routeBtn2.setText(r2);
                routeBtn3.setText(r3);

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
    else
        {
            stopNumberText.setText("");
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

            JSONObject routes = new JSONObject();

            System.out.println("R1 " +r1+" R2 "+r2+" R3 "+r3);
            routes.put("r1",r1);
            routes.put("r2",r2);
            routes.put("r3",r3);

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

            String title = name.getText().toString();
            if(name.length() == 0)
            {
                title = "Notifications for stop: "+stopNumberText.getText();
            }

            jsonObject.put("alarm_id", Integer.toString(alarmId));
            jsonObject.put("active", name.getTag());
            jsonObject.put("name", title);
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



    private class RouteLister extends AsyncTask<String, Integer, String> {

        // static String FILENAME = "test.txt";
        HttpURLConnection conn;
        URL url;
        String stopNumber;
        Context context;
        int READ_TIMEOUT = 2200;
        int CONNECTION_TIMEOUT = 2200;
        String routeList = "";
        ArrayAdapter<String> spinnerData;
        View customView;
        int snapInc=0;

        public RouteLister(
                Context context,
                String stopNumber,
                View customView,
                int snapInc) {
            super();
            this.snapInc=snapInc;
            this.customView=customView;
            this.context = context;
            this.spinnerData = spinnerData;
            this.stopNumber = stopNumber;
        }

        @Override
        protected String doInBackground(String... str) {

            if (snapInc == currentInc) {
                try {

                    // Enter URL address where your php file resides
                    url = new URL("https://tippit.eu/public/json/stop_" + stopNumber + ".json");
                    //url = new URL("https://imaga.me/test.php");
                } catch (MalformedURLException e) {

                    // TODO Auto-generated catch block
                    // e.printStackTrace();

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

                            routeList = result.toString();

                            // Pass data to onPostExecute method
                            return (result.toString());
                        } catch (Exception e) {
                            return ("unsuccessful");
                        }
                    } else {

                        return ("unsuccessful");
                    }

                } catch (Exception e) {
                    //sendNotification(context, "ERROR 4" +  e.getClass().getSimpleName());
                    //e.printStackTrace();

                    return "";
                } finally {
                    //  setAlarms(context, 0);
                    conn.disconnect();
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            if (snapInc == currentInc) {
                try {
                    super.onPostExecute(s);
                    if (routeList != null)
                        if (routeList.length() > 1) {
                            customView.requestLayout();
                            System.out.println("ROUTELIST: "+routeList);
                            allBusesDisplay.removeAll(allBusesDisplay);
                            JSONArray stopDataJson = new JSONArray(routeList);
                            if (stopDataJson != null) {
                                JSONArray ja = stopDataJson.getJSONArray(0);
                                if (ja != null)
                                for (int i = 0; i < ja.length(); i++) {
                                    customView.requestLayout();
                                    String valueString = ja.get(i).toString();
                                    allBusesDisplay.add(valueString);
                                }
                            }

                        }
                    allBusesDisplay.add("----------");
                    customView.requestLayout();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    customView.requestLayout();
                    allBusesDisplay.addAll(allBuses);
                    allBusesDisplay.remove("select");
                    allBusesDisplay.add(0, "select");


                    final RouteSpinnerDialog routeSpinner1 = new RouteSpinnerDialog(
                            MainActivity.this,
                            "1",
                            R.style.DialogAnimations_SmileWindow
                    );// With 	Animation

                    final RouteSpinnerDialog routeSpinner2 = new RouteSpinnerDialog(
                            MainActivity.this,
                            "1",
                            R.style.DialogAnimations_SmileWindow
                    );// With 	Animation

                    final RouteSpinnerDialog routeSpinner3 = new RouteSpinnerDialog(
                            MainActivity.this,
                            "1",
                            R.style.DialogAnimations_SmileWindow
                    );// With 	Animation

                    final Button saveButton = (Button) customView.findViewById(R.id.save_button);

                    routeSpinner1.setEnabled(true);
                    routeSpinner2.setEnabled(true);
                    routeSpinner3.setEnabled(true);
                    routeSpinner1.setClickable(true);
                    routeSpinner2.setClickable(true);
                    routeSpinner3.setClickable(true);

                    saveButton.setEnabled(true);
                    saveButton.setClickable(true);
                }
            }
        }
    }

    ArrayAdapter<String> stopAdaptor;
    EditText stopSearchBox;
    ListView stopListView;
    private class MySpinnerDialog {
        Activity context;
        String dTitle;
        OnSpinerItemClick onSpinerItemClick;
        AlertDialog alertDialog;
        int pos;
        int style;
        Context mContext;


        public MySpinnerDialog(Activity activity, ArrayList<String> items, String dialogTitle, Context mContext) {

            this.context = activity;
            this.dTitle = dialogTitle;
            this.mContext = mContext;
        }

        public MySpinnerDialog(Activity activity, ArrayList<String> items, String dialogTitle, int style, Context mContext) {

            this.context = activity;
            this.dTitle = dialogTitle;
            this.style = style;
            this.mContext = mContext;
        }

        public void bindOnSpinerListener(OnSpinerItemClick onSpinerItemClick1) {
            this.onSpinerItemClick = onSpinerItemClick1;
        }

        public void showSpinerDialog() {
            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            final View v = context.getLayoutInflater().inflate(R.layout.dialog_layout, null);
            TextView rippleViewClose = (TextView) v.findViewById(R.id.close);
            TextView title = (TextView) v.findViewById(R.id.spinerTitle);
            title.setText(dTitle);
            stopListView = (ListView) v.findViewById(R.id.list);
            stopSearchBox = (EditText) v.findViewById(R.id.searchBox);
            stopSearchBox.setHint("Search for STOP name/id");

            stopAdaptor = new ArrayAdapter<String>(context, R.layout.items_view, allStopsDisplay);
            stopListView.setAdapter(stopAdaptor);

            adb.setView(v);
            alertDialog = adb.create();
            alertDialog.getWindow().getAttributes().windowAnimations = style;//R.style.DialogAnimations_SmileWindow;
            alertDialog.setCancelable(false);

            stopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView t = (TextView) view.findViewById(R.id.text1);
                    for (int j = 0; j < allStopsDisplay.size(); j++) {
                        if (t.getText().toString().equalsIgnoreCase(allStopsDisplay.get(j).toString())) {
                            pos = j;
                        }
                    }

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    onSpinerItemClick.onClick(t.getText().toString(), pos);
                    alertDialog.dismiss();
                }
            });

            stopSearchBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    System.out.println("TEXT CHANGED");

                    allStopsDisplay.removeAll(allStopsDisplay);

                    currentInc++;
                    final int snapInc = currentInc;
                    try {
                        StopLister RL = new StopLister(
                                mContext,
                                stopSearchBox.getText().toString(),
                                snapInc,
                                v
                        );
                        RL.execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //stopAdaptor.getFilter().filter(stopSearchBox.getText().toString());
                }
            });

            rippleViewClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }

    }


    private class StopLister extends AsyncTask<String, Integer, String> {

        // static String FILENAME = "test.txt";
        HttpURLConnection conn;
        URL url;
        String searchText;
        Context context;
        int READ_TIMEOUT = 2200;
        int CONNECTION_TIMEOUT = 2200;
        String stopList = "";
        int snapInc=0;
        View v;

        public StopLister(
                Context context,
                String searchText,
                int snapInc,
                View v) {
            super();
            this.v=v;
            this.snapInc=snapInc;
            this.context = context;
            this.searchText = searchText;
        }

        @Override
        protected String doInBackground(String... str) {

            if (snapInc == currentInc) {
                try {
                    // Enter URL address where your php file resides
                    url = new URL("https://tippit.eu/find?s=" + URLEncoder.encode(searchText, "UTF-8"));

                    //url = new URL("https://imaga.me/test.php");
                } catch (Exception e) {

                    // TODO Auto-generated catch block
                    // e.printStackTrace();

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

                            stopList = result.toString();

                            // Pass data to onPostExecute method
                            return (result.toString());
                        } catch (Exception e) {
                            return ("unsuccessful");
                        }
                    } else {

                        return ("unsuccessful");
                    }

                } catch (Exception e) {
                    //sendNotification(context, "ERROR 4" +  e.getClass().getSimpleName());
                    //e.printStackTrace();

                    return "";
                } finally {
                    //  setAlarms(context, 0);
                    conn.disconnect();
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            if (snapInc == currentInc) {
                try {
                    super.onPostExecute(s);
                    if (stopList != null)
                        if (stopList.length() > 1) {
                            System.out.println("STOPLOST: "+stopList);

                            allStopsDisplay.removeAll(allStopsDisplay);

                            JSONArray ja = new JSONArray(stopList);
                            if (ja != null) {
                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject jo = ja.getJSONObject(i);
                                    String listText= jo.getString("id");
                                    listText += ": "+jo.getString("name");
                                    System.out.println(listText);
                                    allStopsDisplay.add(listText);
                                }
                            }

                        }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stopListView.setAdapter(stopAdaptor);

                    stopListView.requestLayout();
                    //stopAdaptor.getFilter().filter(stopSearchBox.getText().toString());
                    v.requestLayout();
                }
            }
        }
    }

    
    private class RouteSpinnerDialog {
        Activity context;
        String dTitle;
        OnSpinerItemClick onSpinerItemClick;
        AlertDialog alertDialog;
        int pos;
        int style;

        public RouteSpinnerDialog(Activity activity, String dialogTitle) {
            this.context = activity;
            this.dTitle = dialogTitle;
        }

        public RouteSpinnerDialog(Activity activity, String dialogTitle, int style) {
            this.context = activity;
            this.dTitle = dialogTitle;
            this.style = style;
        }

        public void setEnabled(boolean b){

        }

        public void setClickable(boolean b){

        }

        public boolean isEnabled(){
            return true;
        }

        public void bindOnSpinerListener(OnSpinerItemClick onSpinerItemClick1) {
            this.onSpinerItemClick = onSpinerItemClick1;
        }

        public void showSpinerDialog() {
            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            final View v = context.getLayoutInflater().inflate(R.layout.dialog_layout, null);
            TextView rippleViewClose = (TextView) v.findViewById(R.id.close);
            TextView title = (TextView) v.findViewById(R.id.spinerTitle);
            title.setText(dTitle);
            final ListView listView = (ListView) v.findViewById(R.id.list);
            final EditText searchBox = (EditText) v.findViewById(R.id.searchBox);


            adb.setView(v);
            alertDialog = adb.create();
            alertDialog.getWindow().getAttributes().windowAnimations = style;//R.style.DialogAnimations_SmileWindow;
            alertDialog.setCancelable(true);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView t = (TextView) view.findViewById(R.id.text1);
                    for (int j = 0; j < allBusesDisplay.size(); j++) {
                        if (t.getText().toString().equalsIgnoreCase(allBusesDisplay.get(j).toString())) {
                            pos = j;
                        }
                    }
                    onSpinerItemClick.onClick(t.getText().toString(), pos);

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    alertDialog.dismiss();
                }
            });

            final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, R.layout.items_view, allBusesDisplay);
            listView.setAdapter(spinnerArrayAdapter);

            searchBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    spinnerArrayAdapter.getFilter().filter(searchBox.getText().toString());
                }
            });

            rippleViewClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
        TableLayout tl;

        public AsyncLoader(Context context, String stopNumber, JSONObject currentAlarmData, TableLayout tl) {
            super();
            this.tl=tl;
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
                            System.out.println(routes);

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
                                            String duetime = row.getString("duetime");

                                            if (allRoutes || (busRoute.equals(route1) || busRoute.equals(route2) || busRoute.equals(route3))) {

                                                    TableRow tr = new TableRow(context);
                                                    TextView tv = new TextView(context);
                                                    tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                                    tv.setText(busRoute + " arriving to stop " + stopNumber + " in " + duetime + " mins");
                                                    tr.addView(tv);
                                                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                                    tl.addView(tr);
                                            }
                                        }catch(Exception e){e.printStackTrace();}
                                    }
                            }catch(Exception e){e.printStackTrace();}
                            //content.setText(sb.toString());
                        }catch(Exception e){e.printStackTrace();}
                    }
            }catch(Exception e){e.printStackTrace();}
        }
    }
}
