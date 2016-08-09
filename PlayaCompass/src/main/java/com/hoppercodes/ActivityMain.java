package com.hoppercodes;
// playa positioning system
// Joe Miller
// Dave Aslanian
// don't blame Dave, it's Joe's fault
// first refactor 6/24/16
// prototype in processing / ketai for about a month to get design down; Processing incredibly nasty with persistent data
// 7/22/2016 - Sensors integrated, initialization routines; glad to be back in Android Studio learning Java
// 7/24/2016

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import android.util.Log;


import com.hoppercodes.playacompass.R;

public class ActivityMain extends Activity implements SensorEventListener {
    private LocationManager ppsLocationManager;
    private SensorManager ppsSensorManager;
    private Sensor ppsAccelerometer;
    private Sensor ppsMagnetometer;
    private String ppsProvider;
    private PPSLocationListener ppsListener;

    public PlayaPositioningSystem pps = new PlayaPositioningSystem();
    public PlayaPositionUI pds = new PlayaPositionUI(this);  // display and UI

    // these need to be persistent, when I moved them within onSensorChanged things quit working
    float[] lastAccelerometer = new float[3];
    float[] lastMagnetometer = new float[3];
    boolean lastAccelerometerSet = false;
    boolean lastMagnetometerSet = false;
    float[] mR = new float[9];
    float[] mOrientation = new float[3];

    // http://www.techrepublic.com/article/pro-tip-create-your-own-magnetic-compass-using-androids-internal-sensors/
    // http://www.ymc.ch/en/smooth-true-north-compass-values  nice discussion of smoothing.
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pps_state);
        this.getPreferences();
        // TODO: 7/30/2016
        pps.update();       // reinitializes pps
        ppsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ppsSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ppsAccelerometer = ppsSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ppsMagnetometer = ppsSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Criteria criteria;

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        ppsProvider = ppsLocationManager.getBestProvider(criteria, false);
        Location location = ppsLocationManager.getLastKnownLocation(ppsProvider);
        ppsListener = new PPSLocationListener();
        if (location != null) {
            ppsListener.onLocationChanged(location);
        } else {
            // leads to the settings because there is no last known location
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            // TO DO
            // need something here saying be patient, waiting for GPS
        }
        ppsLocationManager.requestLocationUpdates(ppsProvider, 200, 1, ppsListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getPreferences();
        ppsLocationManager.requestLocationUpdates(ppsProvider, 200, 1, ppsListener);
        ppsSensorManager.registerListener(this, ppsAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        ppsSensorManager.registerListener(this, ppsMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.savePreferences();
        ppsLocationManager.removeUpdates(ppsListener);
        ppsSensorManager.unregisterListener(this, ppsAccelerometer);
        ppsSensorManager.unregisterListener(this, ppsMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == ppsAccelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor == ppsMagnetometer) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }
        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            pps.headingUpdate(azimuthInDegrees);
            //Log.i("info", "onSensorChange azimuth update to pps");
            pds.updateHeading();
            //pds.update();
            Log.i("info", "onSensorChange: ppsStateDisplay called");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    private class PPSLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(ActivityMain.this, "GPS Update",
                    Toast.LENGTH_SHORT).show();
            pps.hereUpdate(location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getProvider());
            pps.update();
            pds.updateMan();
            pds.updateHere();
            pds.updateBearing();
            pds.update();
            Log.i("info", "PPSLocationListener: GPS Update");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(ActivityMain.this, provider + "'s status changed to " + status,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(ActivityMain.this, "Provider " + provider + " enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(ActivityMain.this, "Provider " + provider + " disabled!",
                    Toast.LENGTH_SHORT).show();
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // buffering the state of the machine here:
    protected void savePreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        //man
        edit.putFloat("manLat", (float) PlayaPositioningSystem.manLat);
        edit.putFloat("manLon", (float) PlayaPositioningSystem.manLon);
        edit.putFloat("manNorthAngle", (float) PlayaPositioningSystem.manNorthAngle);
        edit.putFloat("manFPDLat", (float) PlayaPositioningSystem.manFPDLat);
        edit.putFloat("manFPDLon", (float) PlayaPositioningSystem.manFPDLon);
        edit.putFloat("manDeclination", (float) PlayaPositioningSystem.manDeclination);
        edit.putString("manLable", PlayaPositioningSystem.manLabel);
        //here
        edit.putFloat("hereLat", (float) PlayaPositioningSystem.hereLat);
        edit.putFloat("hereLon", (float) PlayaPositioningSystem.hereLon);
        edit.putFloat("hereAcc", (float) PlayaPositioningSystem.hereAcc);
        edit.putString("hereLabel", PlayaPositioningSystem.hereLabel);
        edit.putInt("hereHour", PlayaPositioningSystem.hereHour);
        edit.putInt("hereMinute", PlayaPositioningSystem.hereMinute);
        edit.putFloat("hereDistFeet", (float) PlayaPositioningSystem.hereDistFeet);
        edit.putString("hereStreet", PlayaPositioningSystem.hereStreet);
        //there
        edit.putFloat("thereLat", (float) PlayaPositioningSystem.thereLat);
        edit.putFloat("thereLon", (float) PlayaPositioningSystem.thereLon);
        edit.putFloat("thereAcc", (float) PlayaPositioningSystem.thereAcc);
        edit.putString("thereLabel", PlayaPositioningSystem.thereLabel);
        edit.putInt("thereHour", PlayaPositioningSystem.thereHour);
        edit.putInt("thereMinute", PlayaPositioningSystem.thereMinute);
        edit.putFloat("thereDistFeet", (float) PlayaPositioningSystem.thereDistFeet);
        edit.putString("thereStreet", PlayaPositioningSystem.thereStreet);
        //bearing
        edit.putFloat("bearingDegMag", (float) PlayaPositioningSystem.bearingDegMag);
        edit.putFloat("bearingDegNorth", (float) PlayaPositioningSystem.bearingDegNorth);
        edit.putFloat("bearingDistFeet", (float) PlayaPositioningSystem.bearingDistFeet);
        edit.putString("bearingRose", PlayaPositioningSystem.bearingRose);
        edit.putString("bearingLabel", PlayaPositioningSystem.bearingLabel);
        //heading
        edit.putFloat("headingDegMag", (float) PlayaPositioningSystem.headingDegMag);
        edit.putFloat("headingDegNorth", (float) PlayaPositioningSystem.headingDegNorth);
        edit.putString("headingRose", PlayaPositioningSystem.headingRose);
        edit.putString("headingLabel", PlayaPositioningSystem.headingLabel);
        // save the edits
        edit.commit();
    }

    protected void getPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //man@string/btnLabelMarkLocation
        PlayaPositioningSystem.manLat = preferences.getFloat("manLat",Float.parseFloat(getApplicationContext().getString(R.string.manLat)));
        PlayaPositioningSystem.manLon = preferences.getFloat("manLon",Float.parseFloat(getApplicationContext().getString(R.string.manLon)));
        PlayaPositioningSystem.manNorthAngle = preferences.getFloat("manNorthAngle",Float.parseFloat(getApplicationContext().getString(R.string.manNorthAngle)));
        PlayaPositioningSystem.manFPDLat = preferences.getFloat("manFPDLat",Float.parseFloat(getApplicationContext().getString(R.string.manFPDLat)));
        PlayaPositioningSystem.manFPDLon = preferences.getFloat("manFPDLon", Float.parseFloat(getApplicationContext().getString(R.string.manFPDLon)));
        PlayaPositioningSystem.manDeclination = preferences.getFloat("manDeclination", Float.parseFloat(getApplicationContext().getString(R.string.manDeclination)));
        PlayaPositioningSystem.manLabel = preferences.getString("manLabel",getApplicationContext().getString(R.string.manLabel));
        //here
        PlayaPositioningSystem.hereLat = preferences.getFloat("hereLat", 0.0f);
        PlayaPositioningSystem.hereLon = preferences.getFloat("hereLon", 0.0f);
        PlayaPositioningSystem.hereAcc = preferences.getFloat("hereAcc", 0.0f);
        PlayaPositioningSystem.hereLabel = preferences.getString("hereLabel", "HereLabelDefault");
        PlayaPositioningSystem.hereHour = preferences.getInt("hereHour", 0);
        PlayaPositioningSystem.hereMinute = preferences.getInt("hereMinute", 0);
        PlayaPositioningSystem.hereDistFeet = preferences.getFloat("hereDistFeet", 0.0f);
        PlayaPositioningSystem.hereStreet = preferences.getString("hereStreet", "HereStreetDefault");
        //there
        PlayaPositioningSystem.thereLat = preferences.getFloat("thereLat", 0.0f);
        PlayaPositioningSystem.thereLon = preferences.getFloat("thereLon", 0.0f);
        PlayaPositioningSystem.thereAcc = preferences.getFloat("thereAcc", 0.0f);
        PlayaPositioningSystem.thereLabel = preferences.getString("thereLabel", "ThereLabelDefault");
        PlayaPositioningSystem.thereHour = preferences.getInt("thereHour", 0);
        PlayaPositioningSystem.thereMinute = preferences.getInt("thereMinute", 0);
        PlayaPositioningSystem.thereDistFeet = preferences.getFloat("thereDistFeet", 0.0f);
        PlayaPositioningSystem.thereStreet = preferences.getString("thereStreet", "ThereStreetDefault");
        //bearing
        PlayaPositioningSystem.bearingDegMag = preferences.getFloat("bearingDegMag", 0.0f);
        PlayaPositioningSystem.bearingDegNorth = preferences.getFloat("bearingDegNorth", 0.0f);
        PlayaPositioningSystem.bearingDistFeet = preferences.getFloat("bearingDistFeet", 0.0f);
        PlayaPositioningSystem.bearingRose = preferences.getString("bearingRose", "bearingRoseDefault");
        PlayaPositioningSystem.bearingLabel = preferences.getString("bearingLabel", "bearingLabelDefault");
        //heading
        PlayaPositioningSystem.headingDegMag = preferences.getFloat("headingDegMag", 0.0f);
        PlayaPositioningSystem.headingDegNorth = preferences.getFloat("headingDegNorth", 0.0f);
        PlayaPositioningSystem.headingRose = preferences.getString("headingRose", "headingRoseDefault");
        PlayaPositioningSystem.headingLabel = preferences.getString("headingLabel", "headingLabelDefault");
    }
}