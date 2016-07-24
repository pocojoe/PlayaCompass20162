package com.hoppercodes.PlayaCompass;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.hoppercodes.PlayaPositioningSystem.GoldenSpike;
import com.hoppercodes.PlayaPositioningSystem.PlayaHeading;
import com.hoppercodes.PlayaPositioningSystem.PlayaNavigate;
import com.hoppercodes.PlayaPositioningSystem.PlayaPoint;
import com.hoppercodes.PlayaPositioningSystem.PlayaPositioningSystem;

public class MainActivity extends Activity implements SensorEventListener {
    private TextView latitude;
    private TextView longitude;
    private TextView choice;
    private CheckBox fineAcc;
    private Button choose;
    private TextView provText;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private String provider;
    private MyLocationListener mylistener;
    private Criteria criteria;
    private PlayaPositioningSystem playaPositioningSystem;

    /**
     * Called when the activity is first created.
     */

    // http://www.techrepublic.com/article/pro-tip-create-your-own-magnetic-compass-using-androids-internal-sensors/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playaPositioningSystem = PlayaPositioningSystem.getInstance();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        mylistener = new MyLocationListener();
        if (location != null) {
            mylistener.onLocationChanged(location);
        } else {
            // leads to the settings because there is no last known location
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            // TO DO
            // need something here saying be patient, waiting for GPS
        }
        locationManager.requestLocationUpdates(provider, 200, 1, mylistener);
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 200, 1, mylistener);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(mylistener);
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor == magnetometer) {
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

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            PlayaPoint updatedHere = new PlayaPoint(location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getProvider());

            playaPositioningSystem.setHere(updatedHere);

            Toast.makeText(MainActivity.this, "Location changed!",
                    Toast.LENGTH_SHORT).show();
            PlayaDisplayBasics();
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(MainActivity.this, provider + "'s status changed to " + status + "!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
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
        // golden spike
        TextView gsname = (TextView) findViewById(R.id.gsName);
        gsname.setText("Golden Spike: " + String.valueOf(gs.datum));
        TextView gslatlon = (TextView) findViewById(R.id.gsLatLon);
        gslatlon.setText(String.valueOf(gs.latlon));

        // here
        TextView herename = (TextView) findViewById(R.id.hereName);
        herename.setText("Here: " + String.valueOf(here.name));
        TextView herelatlon = (TextView) findViewById(R.id.hereLatLon);
        TextView heremcadmdf = (TextView) findViewById(R.id.heremcadmdf);
        herelatlon.setText(String.valueOf(here.latlon));
        heremcadmdf.setText(String.valueOf(here.mcadmdf));
        TextView hereaddress = (TextView) findViewById(R.id.hereAddress);
        hereaddress.setText(here.address);    // can increase precision if desired

        // there
        TextView therename = (TextView) findViewById(R.id.thereName);
        therename.setText("There: " + String.valueOf(there.name));
        TextView therelatlon = (TextView) findViewById(R.id.thereLatLon);
        TextView theremcadmdf = (TextView) findViewById(R.id.theremcadmdf);
        therelatlon.setText(String.valueOf(there.latlon));
        theremcadmdf.setText(String.valueOf(there.mcadmdf));
        TextView thereaddress = (TextView) findViewById(R.id.thereAddress);
        thereaddress.setText(there.address);    // can increase precision if desired

        // electronic compass  uses accelerometer and magnetic field sensors
        TextView Compass = (TextView) findViewById(R.id.Compass);
        Compass.setText("Compass Heading (Direction Faced)");
        TextView CompassMag = (TextView) findViewById(R.id.CompassMag);
        CompassMag.setText(heading.roseWind);

        // navigation route
        TextView navigation = (TextView) findViewById(R.id.navigation);
        navigation.setText("Navigation");
        TextView navtnbearingdist = (TextView) findViewById(R.id.navTNBearingDist);
        navtnbearingdist.setText(String.valueOf(hereToThere.tnbearbdf));
        TextView navdistfeet = (TextView) findViewById(R.id.navTNBearingDist);
        navdistfeet.setText(String.valueOf(hereToThere.tnbearbdf));
        TextView navbeardf = (TextView) findViewById(R.id.navCompassBearingDist);
        navbeardf.setText("Compass Bearing: " + String.valueOf(hereToThere.compassbdf));
    }
}
