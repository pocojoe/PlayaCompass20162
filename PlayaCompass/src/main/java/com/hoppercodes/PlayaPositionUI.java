package com.hoppercodes;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hoppercodes.playacompass.R;

import java.text.DateFormat;
import java.util.Date;

// PlayaPositionUI:  User Interface for PlayaPositioningSystem

public class PlayaPositionUI {
    private ActivityMain activityMain;
    static String sMan;             // man
    static String sHere;             // here
    static String sThere;             // there
    static String sBearing;             // bearing
    static String sHeading;             // heading
    static String sTouch;             // update  as of
    static String sDisplay;        // the concatenated display string

    PlayaPositionUI(ActivityMain activityMain) {
        this.activityMain = activityMain;
        updateMan();
        updateHere();
        updateThere();
        updateHeading();
        updateBearing();
        touch();    // tag the date and time
        sDisplay = "\n" + sMan + "\n" + sHere + "\n" + sThere + "\n" + sBearing + "\n" + sHeading + "\n" + sTouch;
        //sDisplay = sHere;
        //update();
    }



    public void update() {
        activityMain.setContentView(R.layout.pds_layout);
        TextView tvDisplay;

        tvDisplay = (TextView) activityMain.findViewById(R.id.pdsDisplay);
        touch();
        sDisplay = "\n" + sMan + "\n" + sHere + "\n" + sThere + "\n" + sBearing + "\n" + sHeading + "\n" + sTouch;
        //sDisplay="Hi Mom";
        tvDisplay.setText(sDisplay);
    }

    public void updateMan() {
        // man location - the center of the reference systemgsName)
        sMan = ("*** Man: " + String.valueOf(activityMain.pps.manLabel));
        sMan = sMan + "\n" + (String.format("%9.4f", activityMain.pps.manLat) + " " + String.format("%9.4f", activityMain.pps.manLon) + " " + String.format("%4.1f", activityMain.pps.manNorthAngle));
        Log.i("info", "pdsUpdateMan: " + sMan);
    }

    public void updateHere() {
        // Here - the most recently received GPS data
        sHere = ("*** Here: " + String.valueOf(activityMain.pps.hereLabel));
        sHere = sHere + "\n" + (String.format("%9.4f", activityMain.pps.hereLat) + " " + String.format("%9.4f", activityMain.pps.hereLon));
        sHere = sHere + "\n" + (String.format("%02d", activityMain.pps.hereHour) + ":" + String.format("%02d", activityMain.pps.hereMinute) + " & " + String.format("%4.0f", activityMain.pps.hereDistFeet));
        sHere = sHere + "\n" + (String.valueOf(activityMain.pps.hereStreet));
        Log.i("info", "pdsUpdateHere: " + sHere);
    }

    public void updateThere() {
        // there - the location being navigated to
        sThere = ("*** There: " + String.valueOf(activityMain.pps.thereLabel));
        sThere = sThere + "\n" + (String.format("%9.4f", activityMain.pps.thereLat) + " " + String.format("%9.4f", activityMain.pps.thereLon));
        sThere = sThere + "\n" + (String.format("%02d", activityMain.pps.thereHour) + ":" + String.format("%02d", activityMain.pps.thereMinute) + " & " + String.format("%4.0f", activityMain.pps.thereDistFeet));
        sThere = sThere + "\n" + (String.valueOf(activityMain.pps.thereStreet));
        Log.i("info", "pdsUpdateThere: " + sThere);
    }

    public void updateBearing() {
        sBearing = "*** Bearing: " + String.valueOf(activityMain.pps.bearingLabel);
        sBearing = sBearing + ("Distance:" + String.format("%6.0f", activityMain.pps.bearingDistFeet));
        sBearing = sBearing + "\n" + ("Mag:" + String.format("%05.1f", activityMain.pps.bearingDegMag) + " North:" + String.format("%05.1f", activityMain.pps.bearingDegNorth));
        sBearing = sBearing + "\n" + (String.valueOf(activityMain.pps.bearingRose));
        sBearing = sBearing + "\n" + (String.valueOf(activityMain.pps.bearingLabel));
        Log.i("info", "pdsUpdateBearing: " + sBearing);
    }

    public void updateHeading() {
        // electronic compass  uses ppsAccelerometer and magnetic field sensors;eadingDegMag) + " North:" + String.format("%05.1f", pps.headingDegNorth));
        sHeading = "*** Heading: " + String.valueOf(activityMain.pps.headingLabel);
        sHeading = sHeading + "\n" + ("Mag:" + String.format("%05.1f", activityMain.pps.headingDegMag) + " North:" + String.format("%05.1f", activityMain.pps.headingDegNorth));
        sHeading = sHeading + "\n" + (String.valueOf(activityMain.pps.headingRose));
        sHeading = sHeading + "\n" + (String.valueOf(activityMain.pps.headingLabel));
        Log.i("info", "pdsUpdateHeading"+ sHeading);  // updates constantly....
    }

    private void touch() {
        // uses java.util.date and java.text.DateFormat
        sTouch = DateFormat.getDateInstance().format(new Date());
    }

    public void btnMarkLocationClicked(View view) {
        Log.i("info", "btnMarkLocationClicked");
        activityMain.pps.thereUpdate(activityMain.pps.hereLat, activityMain.pps.hereLon, activityMain.pps.hereAcc, activityMain.pps.hereLabel);
        activityMain.pps.update();
        updateThere();
        update();
    }
}
