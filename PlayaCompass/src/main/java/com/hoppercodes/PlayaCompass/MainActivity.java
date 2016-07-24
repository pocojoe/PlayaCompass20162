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
import android.widget.TextView;
import android.widget.Toast;

import com.hoppercodes.PlayaPositioningSystem.GoldenSpike;
import com.hoppercodes.PlayaPositioningSystem.PlayaHeading;
import com.hoppercodes.PlayaPositioningSystem.PlayaNavigate;
import com.hoppercodes.PlayaPositioningSystem.PlayaPoint;
import com.hoppercodes.PlayaPositioningSystem.PlayaPositioningSystem;

public class MainActivity extends Activity implements SensorEventListener {
    private LocationManager ppsLocationManager;
    private SensorManager ppsSensorManager;
    private Sensor ppsAccelerometer;
    private Sensor ppsMagnetometer;
    private String ppsProvider;
    private PPSLocationListener ppsListener;
    private PlayaPositioningSystem playaPositioningSystem;

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

        // electronic compass  uses ppsAccelerometer and magnetic field sensors
        TextView Rose = (TextView) findViewById(R.id.Rose);
        Rose.setText("Compass Rose");
        TextView RoseInt = (TextView) findViewById(R.id.RoseDeg);
        RoseInt.setText(String.format("%03d",heading.roseDeg));
        TextView RoseHeading = (TextView) findViewById(R.id.RoseHeading);
        RoseHeading.setText(String.valueOf(heading.roseHeading));
        TextView RoseWind = (TextView) findViewById(R.id.RoseWind);
        RoseWind.setText(String.valueOf(heading.roseWind));

        // navigation route
        TextView navigation = (TextView) findViewById(R.id.navigation);
        navigation.setText("Navigation");
        TextView navtnbearingdist = (TextView) findViewById(R.id.navTNBearingDist);
        navtnbearingdist.setText(String.valueOf(hereToThere.navDegNDist));
        TextView navdistfeet = (TextView) findViewById(R.id.navTNBearingDist);
        navdistfeet.setText(String.valueOf(hereToThere.navRoseDist));
        TextView navbeardf = (TextView) findViewById(R.id.navCompassBearing);
        navbeardf.setText("North Bearing: " + String.valueOf(hereToThere.navDegNDist));
    }
}
