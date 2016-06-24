package com.hoppercodes.PlayaPositioningSystem;

/**
 * Created by hexagon on 6/24/16.
 */
public class GoldenSpike { // the golden spike is the latitude and longitude of the location on the playa where the man is constructed.
    // first three are same as in PlayaPoint - but no precision is associated.
    public String datum;       // name of datum - each year the datum changes with golden spike
    public String latlon;      // latitude longitude string, formatted to 5 decimal places, separated by comma
    public double lat;         // latitude of golden spike
    public double lon;         // longitude of golden spike
    public double tncad;         // true north clock angle, degrees, from golden spike - defines relation between playa clock angle (12:00 reference) and True North
    public double declination; // compass declination from true north
    public double mpdlat;      // meters per degree latitude
    public double mpdlon;      // meters per degree longitude
    public double fpdlat;      // feet per degree latitude
    public double fpdlon;      // feet per degree longitude

    private static GoldenSpike instance;

    public static synchronized GoldenSpike getInstance(){
        if(instance == null){
            instance = new GoldenSpike(43.58521, -71.21275, 315.0, "Seven Suns");
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
        this.mpdlat = 110891.8;  //  motion up and down a latitude occurs along a longitude.  Longitudes run from pole to pole, bigger than latitude.
        this.mpdlon = mpdlat * Math.cos(Math.toRadians(lat));        // change in longitude occurs along a latitude, circumference is max at equator, and decreases above and below
        this.fpdlat = 363815.47;     // meters per degree change in latitude
        this.fpdlon = fpdlat * Math.cos(Math.toRadians(lat));
        // this is flat earth approximation; for more accuracy, enter the Haversine formula for distance along sphere.
        // also, can go to gis site for more exact values of feet per degree lat and lon
    }
}

