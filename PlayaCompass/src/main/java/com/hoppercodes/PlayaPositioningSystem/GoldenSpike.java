package com.hoppercodes.PlayaPositioningSystem;

import java.util.Date;
import android.hardware.GeomagneticField;

public class GoldenSpike { // the golden spike is the latitude and longitude of the location on the playa where the man is constructed.
    // first three are same as in PlayaPoint - but no precision is associated.
    public String datum;       // name of datum - each year the datum changes with golden spike
    public String latlon;      // latitude longitude string, formatted to 5 decimal places, separated by comma
    public double lat;         // latitude of golden spike
    public double lon;         // longitude of golden spike
    public double tncad;         // true north clock angle, degrees, from golden spike - defines relation between playa clock angle (12:00 reference) and True North
    public double declination; // compass declination from true north, android call for golden spike.  Assigned here in case GPS not available, still says north.
    public double mpdlat;      // meters per degree latitude
    public double mpdlon;      // meters per degree longitude
    public double fpdlat;      // feet per degree latitude
    public double fpdlon;      // feet per degree longitude

    private static GoldenSpike instance;

    public static synchronized GoldenSpike getInstance(){
        if(instance == null){
            // instance = new GoldenSpike(40.7864, -119.2065, 315.0, "BRC MAN 2016");
            instance = new GoldenSpike(32.250018, -110.944092, 315.0, "Campbell/Grant, Tucson");
            // instance = new GoldenSpike(43.58521, -71.21275, 315.0, "Seven Suns");
            // instance = new GoldenSpike(-37.761865, -122.412137, 315.0, "BM HQ Alabama Street");
            // instance = new GoldenSpike(43.585667,-71.142033, 315.0, "Point House");
        }
        return instance;
    }

    private GoldenSpike(double tlat, double tlon, double ttncad, String tname) {   // constructor
        this.lat = tlat;
        this.lon = tlon;
        this.tncad = ttncad;
        this.datum = tname;
        this.latlon = String.format("%10.5f", this.lat) + "," + String.format("%10.5f", this.lon);
        this.declination = 13.70;        // declination for BRC is 13.70 EAST for 2016, and changes by 0.11 degrees per year.  Subtract declination from true north Bearing to get compassBearing
        //this.mpdlat = 110891.8;  //  motion up and down a latitude occurs along a longitude.  Longitudes run from pole to pole, bigger than latitude.
        //this.mpdlon = mpdlat * Math.cos(Math.toRadians(lat));        // change in longitude occurs along a latitude, circumference is max at equator, and decreases above and below
        //this.fpdlat = 363815.47;     // meters per degree change in latitude
        //this.fpdlon = fpdlat * Math.cos(Math.toRadians(lat));
        LatLonLen();
        // this is flat earth approximation; for more accuracy, enter the Haversine formula for distance along sphere.
        // also, can go to gis site for more exact values of feet per degree lat and lon
        DecDeg();
    }

    private void LatLonLen () {  // routine to determine the feet per degree of latitude and longitude for any latitude
        // http://gis.stackexchange.com/questions/75528
        // http://msi.nga.mil/MSISiteContent/StaticFiles/Calculators/degree.html
        // wgs84 elliptical calcs
        double rlat;                // latitude at which calculation is made, in radians
        double m1 = 111132.92f;     // latitude calculation term 1
        double m2 = -559.82f;       // latitude calculation term 2
        double m3 = 1.175f;         // latitude calculation term 3
        double m4 = -0.0023f;       // latitude calculation term 4
        double p1 = 111412.84f;     // longitude calculation term 1
        double p2 = -93.5f;         // longitude calculation term 2
        double p3 = 0.118f;         // longitude calculation term 3

        // for debug purposes; if lat = 32.250018, fpdlat = 363816, fpdlon = 309173
        rlat = (double) Math.toRadians(this.lat);
        this.mpdlat = (m1 + (m2 * Math.cos(2 * rlat)) + (m3 * Math.cos(4 * rlat)) + (m4 * Math.cos(6 * rlat)));
        this.mpdlon = ((p1 * Math.cos(rlat)) + (p2 * Math.cos(3 * rlat)) + (p3 * Math.cos(5 * rlat)));
        this.fpdlat = this.mpdlat * 3.28084;
        this.fpdlon = this.mpdlon * 3.28084;
    }

    private void DecDeg() {         // routine to determine the magnetic declination for this lat and lon by a call to Geomagnetic field
        GeomagneticField gmf = new GeomagneticField((float)this.lat,(float)this.lon,0,System.currentTimeMillis());
        this.declination = gmf.getDeclination();
    }
}



