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

import com.hoppercodes.PlayaPositioningSystem.GoldenSpike;
import com.hoppercodes.PlayaPositioningSystem.PlayaHeading;
import com.hoppercodes.PlayaPositioningSystem.PlayaNavigate;
import com.hoppercodes.PlayaPositioningSystem.PlayaPoint;
import com.hoppercodes.PlayaPositioningSystem.PlayaPositioningSystem;
import com.hoppercodes.playacompass.R;


public class MainActivity extends Activity implements SensorEventListener {
    TextView tvGSName;
    TextView tvGSLatLon;
    TextView tvHereName;
    TextView tvHereLatLon;
    TextView tvHereMcadmdf;
    TextView tvHereAddress;
    TextView tvThereName;
    TextView tvThereLatLon;
    TextView tvThereMcadmdf;
    TextView tvRose;
    TextView tvThereAddress;
    TextView tvRoseInt;
    TextView tvRoseHeading;
    TextView tvRoseWind;
    TextView tvNavigation;
    TextView tvNavNBearing;
    TextView tvNavMBearing;
    TextView tvNavDistFeet;
    Button   markLocationButton;

    private LocationManager ppsLocationManager;
    private SensorManager ppsSensorManager;
    private Sensor ppsAccelerometer;
    private Sensor ppsMagnetometer;
    private String ppsProvider;
    private PPSLocationListener ppsListener;
    private PlayaPositioningSystem playaPositioningSystem;

    // these need to be persistent, when I moved them within onSensorChanged things quit working
    float[] lastAccelerometer = new float[3];
    float[] lastMagnetometer = new float[3];
    boolean lastAccelerometerSet = false;
    boolean lastMagnetometerSet = false;
    float[] mR = new float[9];
    float[] mOrientation = new float[3];


    /**
     * Called when the activity is first created.
     */

    // http://www.techrepublic.com/article/pro-tip-create-your-own-magnetic-compass-using-androids-internal-sensors/
    // http://www.ymc.ch/en/smooth-true-north-compass-values  nice discussion of smoothing.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        markLocationButton = (Button) findViewById(R.id.markLocationButton);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playaPositioningSystem = PlayaPositioningSystem.getInstance();
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
        /*
        these did not work to be declared here
        float[] lastAccelerometer = new float[3];
        float[] lastMagnetometer = new float[3];
        boolean lastAccelerometerSet = false;
        boolean lastMagnetometerSet = false;
        float[] mR = new float[9];
        float[] mOrientation = new float[3];
        */
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
            PlayaHeading updatedHere = new PlayaHeading(azimuthInDegrees);
            playaPositioningSystem.setHeading(updatedHere);
            PlayaDisplayBasics();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    private class PPSLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            PlayaPoint updatedHere = new PlayaPoint(location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getProvider());
            playaPositioningSystem.setHere(updatedHere);
            Toast.makeText(MainActivity.this, "GPS Update",
                    Toast.LENGTH_SHORT).show();
            PlayaDisplayBasics();
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

    void PlayaDisplayBasics() {
        GoldenSpike gs = playaPositioningSystem.getGs();
        PlayaHeading heading = playaPositioningSystem.getHeading();
        PlayaPoint here = playaPositioningSystem.getHere();
        PlayaPoint there = playaPositioningSystem.getThere();
        PlayaNavigate hereToThere = new PlayaNavigate(); // TODO should this be in PPS? Circular
        setContentView(R.layout.activity_main);

        // ok if declared up top for global but
        tvGSName = (TextView) findViewById(R.id.gsName);
        tvGSLatLon = (TextView) findViewById(R.id.gsLatLon);
        tvHereName = (TextView) findViewById(R.id.hereName);
        tvHereLatLon = (TextView) findViewById(R.id.hereLatLon);
        tvHereMcadmdf = (TextView) findViewById(R.id.heremcadmdf);
        tvHereAddress = (TextView) findViewById(R.id.hereAddress);
        tvThereName = (TextView) findViewById(R.id.thereName);
        tvThereLatLon = (TextView) findViewById(R.id.thereLatLon);
        tvThereMcadmdf = (TextView) findViewById(R.id.theremcadmdf);
        tvRose = (TextView) findViewById(R.id.Rose);
        tvThereAddress = (TextView) findViewById(R.id.thereAddress);
        tvRoseInt = (TextView) findViewById(R.id.RoseDeg);
        tvRoseHeading = (TextView) findViewById(R.id.RoseHeading);
        tvRoseWind = (TextView) findViewById(R.id.RoseWind);
        tvNavigation = (TextView) findViewById(R.id.navigation);
        tvNavMBearing = (TextView) findViewById(R.id.navMagBearing);
        tvNavNBearing = (TextView) findViewById(R.id.navNorthBearing);
        tvNavDistFeet = (TextView) findViewById(R.id.navDistFeet);
        markLocationButton = (Button) findViewById(R.id.markLocationButton);

        // golden spike
        tvGSName.setText("Golden Spike: " + String.valueOf(gs.datum));
        tvGSLatLon.setText(String.valueOf(gs.latlon));

        // here
        tvHereName.setText("Here: " + String.valueOf(here.name));
        tvHereLatLon.setText(String.valueOf(here.latlon));
        tvHereMcadmdf.setText(String.valueOf(here.mcadmdf));
        tvHereAddress.setText(here.address);    // can increase precision if desired

        // there
        tvThereName.setText("There: " + String.valueOf(there.name));
        tvThereLatLon.setText(String.valueOf(there.latlon));
        tvThereMcadmdf.setText(String.valueOf(there.mcadmdf));
        tvThereAddress.setText(there.address);    // can increase precision if desired

        // electronic compass  uses ppsAccelerometer and magnetic field sensors
        tvRose.setText("Compass Rose");
        tvRoseInt.setText(String.format("%03d", heading.roseDeg));
        tvRoseHeading.setText(String.valueOf(heading.roseHeading));
        tvRoseWind.setText(String.valueOf(heading.roseWind));

        // navigation route
        tvNavigation.setText("Navigation");
        tvNavNBearing.setText("Nav Magnetic Bearing: "+ String.format("%4.1f",hereToThere.navBearingM));
        tvNavMBearing.setText("Nav North Bearing: " + String.format("%4.1f",hereToThere.navBearingN));
        tvNavDistFeet.setText("Nav Dist Feet: " + String.format("%8.0f",hereToThere.navdf));

    }

    public void markButtonClicked(View view) {
        Log.i("info", "MarkButtonClicked");
        PlayaPoint updatedThere = new PlayaPoint(playaPositioningSystem.getHere().lat, playaPositioningSystem.getHere().lon, 0.0, "markClick");
        playaPositioningSystem.setThere(updatedThere);
    }
}

