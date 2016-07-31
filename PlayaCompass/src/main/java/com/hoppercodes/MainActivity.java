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
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.hoppercodes.playacompass.R;

public class MainActivity extends Activity implements SensorEventListener {
    TextView tvGSName;
    TextView tvGSLatLon;
    TextView tvHereName;
    TextView tvHereLatLon;
    TextView tvHereMcadMdf;
    TextView tvHereStreet;
    TextView tvThereName;
    TextView tvThereLatLon;
    TextView tvThereMcadMdf;
    TextView tvThereStreet;
    TextView tvBearingName;
    TextView tvBearingDistFeet;
    TextView tvBearingDeg;
    TextView tvBearingRose;
    TextView tvBearingLabel;
    TextView tvHeadingName;
    TextView tvHeadingDeg;
    TextView tvHeadingRose;
    TextView tvHeadingLabel;
    Button   markLocationButton;


    private LocationManager ppsLocationManager;
    private SensorManager ppsSensorManager;
    private Sensor ppsAccelerometer;
    private Sensor ppsMagnetometer;
    private String ppsProvider;
    private PPSLocationListener ppsListener;


    public PlayaPositioningSystem pps = new PlayaPositioningSystem();

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
        markLocationButton = (Button) findViewById(R.id.markLocationButton);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pps_state);
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
        ppsStateDisplay();
    }

    @Override
    public void onResume() {
        super.onResume();
        ppsLocationManager.requestLocationUpdates(ppsProvider, 200, 1, ppsListener);
        ppsSensorManager.registerListener(this, ppsAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        ppsSensorManager.registerListener(this, ppsMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
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
            Log.i("info", "onSensorChange azimuth update to pps");
            ppsStateDisplay();
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
            // TODO: 7/30/2016
            Toast.makeText(MainActivity.this, "GPS Update",
                    Toast.LENGTH_SHORT).show();
            pps.hereUpdate(location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getProvider());
            Log.i("info", "PPSLocationListener gps update to pps");
            ppsStateDisplay();
            Log.i("info", "PPSLocationListener: ppsStateDisplay called");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(MainActivity.this, provider + "'s status changed to " + status,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    void ppsStateDisplay() {

        setContentView(R.layout.pps_state);
         markLocationButton = (Button) findViewById(R.id.markLocationButton);
        tvGSName = (TextView) findViewById(R.id.gsName);
        tvGSLatLon = (TextView) findViewById(R.id.gsLatLon);
        tvHereName = (TextView) findViewById(R.id.hereName);
        tvHereLatLon = (TextView) findViewById(R.id.hereLatLon);
        tvHereMcadMdf = (TextView) findViewById(R.id.hereMcadMdf);
        tvHereStreet = (TextView) findViewById(R.id.hereStreet);
        tvThereName = (TextView) findViewById(R.id.thereName);
        tvThereLatLon = (TextView) findViewById(R.id.thereLatLon);
        tvThereMcadMdf = (TextView) findViewById(R.id.thereMcadMdf);
        tvThereStreet = (TextView) findViewById(R.id.thereStreet);
        tvBearingName = (TextView) findViewById(R.id.bearingName);
        tvBearingDeg = (TextView) findViewById(R.id.bearingDeg);
        tvBearingDistFeet = (TextView) findViewById(R.id.bearingDistFeet);
        tvBearingRose = (TextView) findViewById(R.id.bearingRose);
        tvBearingLabel = (TextView) findViewById(R.id.bearingLabel);
        tvHeadingName = (TextView) findViewById(R.id.headingName);
        tvHeadingDeg = (TextView) findViewById(R.id.headingDeg);
        tvHeadingRose = (TextView) findViewById(R.id.headingRose);
        tvHeadingLabel = (TextView) findViewById(R.id.headingLabel);

        // golden spike, or man location
        tvGSName.setText("*** Man: " + String.valueOf(pps.manLabel));
        tvGSLatLon.setText(String.format("%9.4f", pps.manLat) + " " + String.format("%9.4f", pps.manLon) + " " + String.format("%4.1f", pps.manNorthAngle));
        // here
        tvHereName.setText("*** Here: " + String.valueOf(pps.hereLabel));
        tvHereLatLon.setText(String.format("%9.4f", pps.hereLat) + " " + String.format("%9.4f", pps.hereLon));
        tvHereMcadMdf.setText(String.format("%02d", pps.hereHour) + ":" + String.format("%02d", pps.hereMin) + " & " + String.format("%4.0f", pps.hereDistFeet));
        tvHereStreet.setText(String.valueOf(pps.hereStreet));
        // there
        tvThereName.setText("*** There: " + String.valueOf(pps.thereLabel));
        tvThereLatLon.setText(String.format("%9.4f", pps.thereLat) + " " + String.format("%9.4f", pps.thereLon));
        tvThereMcadMdf.setText(String.format("%02d", pps.thereHour) + ":" + String.format("%02d", pps.thereMin) + " & " + String.format("%4.0f", pps.thereDistFeet));
        tvThereStreet.setText(String.valueOf(pps.thereStreet));
        // navigation route
        tvBearingName.setText("*** Navigation");
        tvBearingDistFeet.setText("Distance:" + pps.bearingDistFeet);
        tvBearingDeg.setText("Mag:" + String.format("%05.1f", pps.bearingDegMag) + " North:" + String.format("%05.1f", pps.bearingDegNorth));
        tvBearingRose.setText(String.valueOf(pps.bearingRose));
        tvBearingLabel.setText(String.valueOf(pps.bearingLabel));
        // electronic compass  uses ppsAccelerometer and magnetic field sensors
        tvHeadingName.setText("*** Electronic Compass");
        tvHeadingDeg.setText("Mag:" + String.format("%05.1f", pps.headingDegMag) + " North:" + String.format("%05.1f", pps.headingDegNorth));
        tvHeadingRose.setText(String.valueOf(pps.headingRose));
        tvHeadingLabel.setText(String.valueOf(pps.headingLabel));
    }

    public void markButtonClicked(View view) {
        Log.i("info", "MarkButtonClicked");
        pps.thereUpdate(pps.hereLat, pps.hereLon, pps.hereAcc, pps.hereLabel);
        Log.i("info", "markButtonClicked: here location copied to destination");
        ppsStateDisplay();
        Log.i("info", "markButtonClicked: ppsStateDisplay called");
    }
}