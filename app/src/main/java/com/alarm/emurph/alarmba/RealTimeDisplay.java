package com.alarm.emurph.alarmba;

import android.content.Context;
import android.graphics.Color;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by eddie on 15/01/18.
 */

public class RealTimeDisplay {
    TextView route;
    TextView stop;
    TextView duetime;
    TextView dest;
    TableRow tr;
    Context context;

    public RealTimeDisplay(
            Context context,
            String routeStr,
            String stopStr,
            String dueStr,
            String destStr
    ) {
        this.context = context;
        this.tr = new TableRow(this.context);

        TableRow.LayoutParams params1 = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1.0f
        );

        TableRow.LayoutParams params2 = new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        this.tr.setLayoutParams(params2);

        this.route = new TextView(this.context);
        this.stop = new TextView(this.context);
        this.duetime = new TextView(this.context);
        this.dest = new TextView(this.context);

        this.route.setLayoutParams(params1);
        this.stop.setLayoutParams(params1);
        this.duetime.setLayoutParams(params1);
        this.dest.setLayoutParams(params1);

        TableRow.LayoutParams params = (TableRow.LayoutParams) this.route.getLayoutParams();
        //params.span = 2;
        this.route.setLayoutParams(params);

        params = (TableRow.LayoutParams) this.stop.getLayoutParams();
       // params.span = 2;
        this.stop.setLayoutParams(params);

        params = (TableRow.LayoutParams) this.dest.getLayoutParams();
      //  params.span = 6;
        this.dest.setLayoutParams(params);

        params = (TableRow.LayoutParams) this.duetime.getLayoutParams();
      //  params.span = 2;
        this.duetime.setLayoutParams(params);

        this.route.setTextAppearance(this.context, R.style.fontForNotificationLandingPage);
        this.stop.setTextAppearance(this.context, R.style.fontForNotificationLandingPage);
        this.duetime.setTextAppearance(this.context, R.style.fontForNotificationLandingPage);
        this.dest.setTextAppearance(this.context, R.style.fontForNotificationLandingPage);

        this.route.setText(routeStr);
        this.stop.setText(stopStr);
        this.duetime.setText(dueStr);
        this.dest.setText(destStr);
    }

    public void addToMe(TableRow tr2) {
        tr2.addView(
                this.stop
        );
        tr2.addView(
                this.route
        );
        tr2.addView(
                this.duetime
        );
        tr2.addView(
                this.dest
        );
    }
}
