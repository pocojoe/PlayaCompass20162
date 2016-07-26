package com.hoppercodes.PlayaPositioningSystem;

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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hoppercodes.PlayaCompass.R;


/**
 * Created by hexagon on 6/24/16.
 */
public final class PlayaPositioningSystem extends Activity implements SensorEventListener {
    ///////////////////////////////////////////////////////////////
    GoldenSpike gs; // GoldenSpike(40.7864, -119.20065, 315.0, "GS 2016");
    PlayaHeading heading = new PlayaHeading(0f);  // compass module
    PlayaPoint here = new PlayaPoint(40.7864, -119.2065, 10.0, "GS 2016");
    // PlayaPoint there = new PlayaPoint(40.7864, -119.2065, 10.0, "GS 2016");
    PlayaPoint there = new PlayaPoint(32.250018, -110.944092, 10.0, "Bookmans");
    //GoldenSpike gs = playaPositioningSystem.getGs();
    //PlayaHeading heading = playaPositioningSystem.getHeading();
    //PlayaPoint here = playaPositioningSystem.getHere();
    //PlayaPoint there = playaPositioningSystem.getThere();
    PlayaNavigate hereToThere = new PlayaNavigate();  // navigation module

    public String gsdatum = "1";
    public String gslatlon = "2";
    public String hereName = "3";
    public String hereLatLon = "4";
    public String hereMcadMdf = "5";
    public String hereAddress = "6";
    public String destName = "7";
    public String destLatLon = "8";
    public String destMcadMdf = "9";
    public String destAddress = "10";
    public String roseDeg = "11";
    public String roseHeading = "12";
    public String roseWind = "13";
    public String navBearingN = "14";
    public String navBearingM = "15";
    public String navdf = "16";


    private static PlayaPositioningSystem instance;

    public static synchronized PlayaPositioningSystem getInstance() {
        if (instance == null) {
            instance = new PlayaPositioningSystem();
        }
        return instance;
    }

    private LocationManager ppsLocationManager;
    private SensorManager ppsSensorManager;
    private Sensor ppsAccelerometer;
    private Sensor ppsMagnetometer;
    private String ppsProvider;
    private PPSLocationListener ppsListener;
    private PlayaPositioningSystem playaPositioningSystem;

    // variables for the compass module to function
    float[] lastAccelerometer = new float[3];
    float[] lastMagnetometer = new float[3];
    boolean lastAccelerometerSet = false;
    boolean lastMagnetometerSet = false;
    float[] mR = new float[9];
    float[] mOrientation = new float[3];


    private PlayaPositioningSystem() {
        this.gs = GoldenSpike.getInstance();
        Log.i("info", "PlayaPositioningSystem Constructor");
    }

    public GoldenSpike getGs() {
        return gs;
    }

    public void setGoldenSpike(GoldenSpike gs) {
        this.gs = gs
    }

    ;

    public PlayaHeading getHeading() {
        return heading;
    }

    public void setHeading(PlayaHeading heading) {
        this.heading = heading;
    }

    public PlayaPoint getHere() {
        return here;
    }

    public void setHere(PlayaPoint here) {
        this.here = here;
    }

    public PlayaPoint getThere() {
        return there;
    }

    public void setThere(PlayaPoint there) {
        this.there = there;
    }

    public PlayaPoint getHereToThere() {
        return hereToThere;
    }

    public void setHereToThere(PlayaPoint hereToThere) {
        this.hereToThere = hereToThere;
    }


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
        Log.i("info", "onCreate override of pps exit");
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
            PlayaHeading updatedHere = new PlayaHeading(azimuthInDegrees);
            playaPositioningSystem.setHeading(updatedHere);
            //PlayaDisplayBasics();
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
            Log.i("info", "LocationUpdate");
            //Toast.makeText(MainActivity.this, "GPS Update",
            //Toast.LENGTH_SHORT).show();
            //PlayaDisplayBasics();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Toast.makeText(MainActivity.this, provider + "'s status changed to " + status,
            //Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Toast.makeText(MainActivity.this, "Provider " + provider + " enabled",
            //Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
            //Toast.LENGTH_SHORT).show();
        }
    }
}