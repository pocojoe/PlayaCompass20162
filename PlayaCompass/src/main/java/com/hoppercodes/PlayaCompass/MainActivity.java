package com.hoppercodes.PlayaCompass;
// playa positioning system
// Joe Miller
// Dave Aslanian
// don't blame Dave, it's Joe's fault
// first refactor 6/24/16
// prototype in processing / ketai for about a month to get design down; Processing incredibly nasty with persistent data
// 7/22/2016 - Sensors integrated, initialization routines; glad to be back in Android Studio learning Java
// 7/24/2016

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hoppercodes.PlayaCompass.R;

import com.hoppercodes.PlayaPositioningSystem.*;

public class MainActivity extends Activity {
    public PlayaPositioningSystem pps;

    TextView gsname = (TextView) findViewById(R.id.gsName);
    TextView gslatlon = (TextView) findViewById(R.id.gsLatLon);
    TextView herename = (TextView) findViewById(R.id.hereName);
    TextView herelatlon = (TextView) findViewById(R.id.hereLatLon);
    TextView heremcadmdf = (TextView) findViewById(R.id.heremcadmdf);
    TextView hereaddress = (TextView) findViewById(R.id.hereAddress);
    TextView therename = (TextView) findViewById(R.id.thereName);
    TextView therelatlon = (TextView) findViewById(R.id.thereLatLon);
    TextView theremcadmdf = (TextView) findViewById(R.id.theremcadmdf);
    TextView thereaddress = (TextView) findViewById(R.id.thereAddress);
    TextView RoseInt = (TextView) findViewById(R.id.RoseDeg);
    TextView RoseHeading = (TextView) findViewById(R.id.RoseHeading);
    TextView RoseWind = (TextView) findViewById(R.id.RoseWind);
    TextView navmagbearing = (TextView) findViewById(R.id.navMagBearing);
    TextView navnorthbearing = (TextView) findViewById(R.id.navNorthBearing);
    TextView navdistfeet = (TextView) findViewById(R.id.navDistFeet);

    void PlayaDisplayBasics() {
        //PlayaNavigate hereToThere = new PlayaNavigate(); // TODO should this be in PPS? Circular

        setContentView(R.layout.activity_main);
        // golden spike
        gsname.setText(pps.gsdatum);
        gslatlon.setText(pps.gslatlon);

        // here
        herename.setText(pps.hereName);
        herelatlon.setText(pps.hereLatLon);
        heremcadmdf.setText(pps.hereMcadMdf);
        hereaddress.setText(pps.hereAddress);    // can increase precision if desired

        // there
        therename.setText(pps.destName);
        therelatlon.setText(pps.destLatLon);
        theremcadmdf.setText(pps.destMcadMdf);
        thereaddress.setText(pps.destAddress);    // can increase precision if desired

        // electronic compass  uses ppsAccelerometer and magnetic field sensors
        RoseInt.setText(pps.roseDeg);
        RoseHeading.setText(pps.roseHeading);
        RoseWind.setText(pps.roseWind);

        // navigation route
        navmagbearing.setText(pps.navBearingM);
        navnorthbearing.setText(pps.navBearingN);
        navdistfeet.setText(pps.navdf);
    }

    void MarkButtonClicked(View v) {
        Log.i("info", "MarkButtonClicked");
    }
}

