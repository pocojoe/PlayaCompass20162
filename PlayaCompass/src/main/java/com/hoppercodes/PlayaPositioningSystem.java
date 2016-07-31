package com.hoppercodes;

import android.hardware.GeomagneticField;

public final class PlayaPositioningSystem {
    // these values represent the "public interface" of Playa Positioning System
    // man location: iniitalizes PlayaMan
    public static double manLat;
    public static double manLon;
    public static double manNorthAngle;
    public static double manFPDLat;
    public static double manFPDLon;
    public static double manDeclination;
    public static String manLabel;
    // present location description: initializes point Here
    public static double hereLat;
    public static double hereLon;
    public static double hereAcc;
    public static String hereLabel;
    public static int hereHour;
    public static int hereMin;
    public static double hereDistFeet;
    public static String hereStreet;
    // destination location description:  Initializes point There
    public static double thereLat;
    public static double thereLon;
    public static double thereAcc;
    public static String thereLabel;
    public static int thereHour;
    public static int thereMin;
    public static double thereDistFeet;
    public static String thereStreet;
    // bearing:  navigation direction and distance from present location (here) to destination (there)
    public static double bearingDegMag;
    public static double bearingDegNorth;
    public static double bearingDistFeet;
    public static String bearingRose;
    public static String bearingLabel;
    // heading: present compass heading of device (long axis), used to orient user which way to go.
    public static double headingDegMag;
    public static double headingDegNorth;
    public static String headingRose;
    public static String headingLabel;

    static PlayaMan gs;
    static PlayaHeading heading;
    static PlayaPoint here;
    static PlayaPoint there;
    static PlayaNavigate nav;  // nav: here to there

    static {
        // TODO: 7/30/2016  initialize from configuration values
        // TODO: 7/30/2016  MainActivity writes to these, and then does "update" to push from here into classes
        // TODO: 7/30/2016 Main activity reads from these when data is requested
        manLat = 40.7864;
        manLon = -119.2065;
        manNorthAngle = 315.0;
        manFPDLat = 364336.0;
        manFPDLon = 276923.0;
        manDeclination = 9.94;
        manLabel = "GS 2016";

        hereLat = 40.7864;
        hereLon = -119.2065;
        hereAcc = 10.0;
        hereLabel = "GS 2016";
        hereHour = 12;
        hereMin = 0;
        hereDistFeet = 0;
        hereStreet = "Street";

        thereLat = 40.7864;
        thereLon = -119.2065;
        thereAcc = 10.0;
        thereLabel = "GS 2016";
        thereHour = 12;
        thereMin = 0;
        thereDistFeet = 0;
        thereStreet = "Street";

        bearingDegNorth = 0;
        bearingDegMag = 0;
        bearingDistFeet = 0;
        bearingRose = "N";
        bearingLabel = "Destination";

        headingDegMag = 0;
        headingDegNorth = 0;
        headingRose = "N";
        headingLabel = "Scirroco";
        // TODO: 7/29/2016  save to configuration values
    }

    PlayaPositioningSystem() {      // constructor for PlayaPositioningSystem
        //PlayaMan gs = new PlayaMan(this.manLat, this.manLon, this.manNorthAngle, this.manLabel);
        this.gs = new PlayaMan(this.manLat, this.manLon, this.manNorthAngle, this.manLabel);
        this.heading = new PlayaHeading(this.headingDegMag);
        this.here = new PlayaPoint(this.hereLat, this.hereLon, this.hereAcc, this.hereLabel);
        this.there = new PlayaPoint(this.thereLat, this.thereLon, this.thereAcc, this.thereLabel);
        this.nav = new PlayaNavigate();
    }

    public void update() {
        this.gs = new PlayaMan(this.manLat, this.manLon, this.manNorthAngle, this.manLabel);
        this.heading = new PlayaHeading(this.headingDegMag);
        this.here = new PlayaPoint(this.hereLat, this.hereLon, this.hereAcc, this.hereLabel);
        this.there = new PlayaPoint(this.thereLat, this.thereLon, this.thereAcc, this.thereLabel);
        this.nav = new PlayaNavigate();
    }

    public void headingUpdate(double azimuth) {
        PlayaHeading heading = new PlayaHeading(azimuth);
        this.headingDegMag = heading.headingM;
        this.headingDegNorth = heading.headingN;
        this.headingRose = heading.roseHeading;
        this.headingLabel = heading.roseWind;
        // change in heading does not require navigation update
    }

    public void hereUpdate(double lat, double lon, double acc, String label) {
        PlayaPoint here = new PlayaPoint(lat, lon, acc, label);
        this.hereLat = here.lat;
        this.hereLon = here.lon;
        this.hereAcc = here.precision;
        this.hereLabel = here.name;
        this.hereHour = here.mcah;
        this.hereMin = here.mcam;
        this.hereDistFeet = here.mdf;
        this.hereStreet = here.address;
        // change in present location changes navigation
        PlayaNavigate h2t = new PlayaNavigate();
        this.navUpdate();
    }

    public void thereUpdate(double lat, double lon, double acc, String label) {
        PlayaPoint there = new PlayaPoint(lat, lon, acc, label);
        this.thereLat = there.lat;
        this.thereLon = there.lon;
        this.thereAcc = there.precision;
        this.thereLabel = there.name;
        this.thereHour = there.mcah;
        this.thereMin = there.mcam;
        this.thereDistFeet = there.mdf;
        this.thereStreet = there.address;
        // change in destination changes navigation
        PlayaNavigate nav = new PlayaNavigate();
        this.navUpdate();
    }

    public void navUpdate() {
        this.bearingDegMag = nav.navBearingM;
        this.bearingDegNorth = nav.navBearingN;
        this.bearingDistFeet = nav.navdf;
        this.bearingRose = nav.roseM;
        this.bearingLabel = nav.navLabel;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////  HEADING  - does electronic compass averaging
    private static int rbi = 0;      // ring buffer index
    private static int rSize = 16;     // size of ring buffer
    private static int rDiv = 0;        // ring divisor; builds to ring size with repeated calls
    private static double azValSin[] = new double[rSize];
    private static double azValCos[] = new double[rSize];
    private static double oldSin = 0f;
    private static double oldCos = 0f;
    private static double sumSin = 0f;
    private static double sumCos = 0f;

    class PlayaHeading {
        public double headingN;   // true north heading
        public double headingM;   // magnetic north heading
        int roseError;
        int roseN;              // which compass rose value 1:15
        public int roseInt;     // integer value of hysteresis stabilized compass reading
        public int roseDeg;     // integer value of azimuth - fluctuating compass reading
        public String roseWind;
        public String roseHeading;
        /*
        // these need to be static to persist from one measurment to the next to get running average
        static int rbi = 0;      // ring buffer index
        static int rSize = 16;     // size of ring buffer
        static int rDiv = 0;        // ring divisor; builds to ring size with repeated calls
        static double azValSin[] = new double[rSize];
        static double azValCos[] = new double[rSize];
        static double oldSin = 0f;
        static double oldCos = 0f;
        static double sumSin = 0f;
        static double sumCos = 0f;
        static {
            rbi = 0;      // ring buffer index
            rDiv = 0;
            oldSin = 0f;
            oldCos = 0f;
            sumSin = 0f;
            sumCos = 0f;
        }
        */

        public PlayaHeading(double azimuth) {

            this.headingM = AverageAzimuth(azimuth);  // provides running average samples
            // TODO: 7/30/2016 fix assignmnet of pps
            // this.headingN = (((this.headingM - pps.declination) + 360.0) % 360.0);
            this.headingN = (((this.headingM - 13.5) + 360.0) % 360.0);
            this.roseDeg = (int) this.headingM;  // the rose reports magnetic compass heading since that is what should agree with compass if hand-held
            roseError = 180 - Math.abs((Math.abs(this.roseInt - this.roseDeg) - 180));
            // angle error = 180-abs(abs(a1-a2)-180)  thanks some guy on stack overflow!
            // using a 16 point rose; 360/16=22.5; half would be 11.25
            // estimating 2 SD to be 3.75 degrees so bounding the 11.25 + 3.75 = 15 degrees.
            if (roseError > 15) {   // assumes about 2.5 degrees for SD of bounce; seems to work ok
                // the heading has "jumped" from the constrained heading by more than allowed by hysteresis
                // determine the new target heading as 1 of 16 possible rose positions
                roseN = (int) ((this.headingN + 11.25f) / 22.5f);
                roseN = roseN % 16;
            }
            setRose(roseN);
        }

        void setRose(int target) {
            switch (target) {
                case 0:
                    this.roseInt = 360;      // apparently pilots use 360 and sailors use 0 to indicate north
                    this.roseHeading = "N";
                    this.roseWind = "Tramontano";
                    break;
                case 1:
                    this.roseInt = 23;
                    this.roseHeading = "NNE";
                    this.roseWind = "Greco-Tramantano";
                    break;
                case 2:
                    this.roseInt = 45;
                    this.roseHeading = "NE";
                    this.roseWind = "Greco";
                    break;
                case 3:
                    this.roseInt = 68;
                    this.roseHeading = "ENE";
                    this.roseWind = "Greco-Levante";
                    break;
                case 4:
                    this.roseInt = 90;
                    this.roseHeading = "E";
                    this.roseWind = "Levante";
                    break;
                case 5:
                    this.roseInt = 113;
                    this.roseHeading = "ESE";
                    this.roseWind = "Levante-Scirocco";
                    break;
                case 6:
                    this.roseInt = 135;
                    this.roseHeading = "SE";
                    this.roseWind = "Scirocco";
                    break;
                case 7:
                    this.roseInt = 158;
                    this.roseHeading = "SSE";
                    this.roseWind = "Ostro-Scirocco";
                    break;
                case 8:
                    this.roseInt = 180;
                    this.roseHeading = "S";
                    this.roseWind = "Ostro";
                    break;
                case 9:
                    this.roseInt = 203;
                    this.roseHeading = "SSW";
                    this.roseWind = "Ostro-Libeccio";
                    break;
                case 10:
                    this.roseInt = 225;
                    this.roseHeading = "SW";
                    this.roseWind = "Libeccio";
                    break;
                case 11:
                    this.roseInt = 248;
                    this.roseHeading = "WSW";
                    this.roseWind = "Ponente-Libeccio";
                    break;
                case 12:
                    this.roseInt = 270;
                    this.roseHeading = "W";
                    this.roseWind = "Ponente";
                    break;
                case 13:
                    this.roseInt = 293;
                    this.roseHeading = "WNW";
                    this.roseWind = "Maestro-Ponente";
                    break;
                case 14:
                    this.roseInt = 315;
                    this.roseHeading = "NW";
                    this.roseWind = "Maestro";
                    break;
                case 15:
                    this.roseInt = 338;
                    this.roseHeading = "NNW";
                    this.roseWind = "Maestro-Tramontana";
                    break;
                default:
                    this.roseInt = 0;      // apparently pilots use 360 and sailors use 0 to indicate north
                    this.roseHeading = "?";
                    this.roseWind = "?";
                    break;
            }
        }

        double AverageAzimuth(double azInDegrees)     // compute the average of N compass readings and give the rolling average
        {
            int rSize = 16;           // number of samples in running average
            double azInRadians;      //angle in radians
            double newSin = 0f;
            double newCos = 0f;
            double mSin = 0f;
            double mCos = 0f;
            double mAvg = 0f;

            if (rDiv >= rSize) {
                oldSin = azValSin[rbi];
                oldCos = azValCos[rbi];
                rDiv = rSize;                      // rDiv gets capped at ring size
            } else {
                oldSin = 0;
                oldCos = 0;
                rDiv += 1;
            }
            azInRadians = Math.toRadians(azInDegrees);            // and in with the new
            newSin = Math.sin(azInRadians);
            newCos = Math.cos(azInRadians);
            azValSin[rbi] = newSin;
            azValCos[rbi] = newCos;
            rbi = (rbi + 1) % rSize;                         // ring around the rose (ie)
            sumSin = sumSin - oldSin + newSin;
            sumCos = sumCos - oldCos + newCos;
            mSin = sumSin / rDiv;
            mCos = sumCos / rDiv;
            mAvg = Math.toDegrees(Math.atan2(mSin, mCos));  // see averaging circular quantities
            mAvg = (mAvg + 360f) % 360f;
            return mAvg;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////  PlayaMan - contains information about location of center of camp, and orientation of camp with respect to north

    class PlayaMan {         // the Man is positioned at the Golden Spike: a stake driven on the playa at latitude and longitude where the man is constructed.
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

        public PlayaMan(double tlat, double tlon, double ttncad, String tname) {   // constructor
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

        private void LatLonLen() {  // routine to determine the feet per degree of latitude and longitude for any latitude
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
            // for debug purposes; if lat = 40.7864, fpdlat = 364336, fpdlon = 276923
            rlat = (double) Math.toRadians(this.lat);
            this.mpdlat = (m1 + (m2 * Math.cos(2 * rlat)) + (m3 * Math.cos(4 * rlat)) + (m4 * Math.cos(6 * rlat)));
            this.mpdlon = ((p1 * Math.cos(rlat)) + (p2 * Math.cos(3 * rlat)) + (p3 * Math.cos(5 * rlat)));
            this.fpdlat = this.mpdlat * 3.28084;
            this.fpdlon = this.mpdlon * 3.28084;
        }

        private void DecDeg() {         // routine to determine the magnetic declination for this lat and lon by a call to Geomagnetic field
            // http://www.ngdc.noaa.gov/geomag-web/#declination
            // debug purposes: if lat=32.250008, lon=-110.944092, dec=9.94 E +/- 0.33 changing by 0.09 deg W / yr, for 2016-07-30 (Black Rock City)
            // debug purposes: if lat=40.7864, lon=-119.2065, dec=13.70 E +/- 0.35 changing by 0.11 deg W / yr, for 2016-07-30 (Tucson)
            // debug purposes: if lat=435852, lon=-71.2128, dec= 15.01 W +/- 0.37, changing by 0.07 E per year, for 2016-07-30  (Wolfeboro)
            GeomagneticField gmf = new GeomagneticField((float) this.lat, (float) this.lon, 0, System.currentTimeMillis());
            this.declination = gmf.getDeclination();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////  PlayaNavigate

    class PlayaNavigate {           // navigate from point here to point there
        public String navRoseDist;         // string compass rose (mag) and distance in feet
        public String navDegNDist;         // string North Degrees and distance in feet
        public String navLabel;     // nav display label
        public String roseM;               // compass rose abbreviation - eg NW
        public double navBearingM;    // magnetic bearing:  true north plus declincation
        public double navBearingN;    // true north bearing - the map, not compass, direction to navigate
        public double navdm;               // navigation (here to there) destingation distance, meters
        public double navdf;               // navigation (here to there) destination distance, feet

        public PlayaNavigate() {
            this.update();
        }

        void update() {
            double dxf;     // east-west navigation vector in feet
            double dyf;     // north-south navigation vector in feet
            double ucad;    // unit circle angle, degrees
            // TODO: 7/30/2016
            dxf = (here.lon - there.lon) * gs.fpdlon;
            dyf = (here.lat - there.lat) * gs.fpdlat;
            this.navdf = Math.sqrt(dxf * dxf + dyf * dyf);
            this.navdm = this.navdf * 0.305;      // one foot = 0.305 meters
            ucad = (Math.toDegrees(Math.atan2(dyf, dxf)));  // unit circle angle degrees
            this.navBearingN = ((360 - ucad) - 90);
            this.navBearingN = (this.navBearingN + 360f) % 360f;
            // determine compass bearing from true north bearing.  Compass is about 12 degrees east of true north.
            this.navBearingM = this.navBearingN + PlayaPositioningSystem.manDeclination;   // when going from true north to magnetic, you add the dec to tn to get the mag bearing
            this.navBearingM = (this.navBearingM + 360f) % 360;
            if (this.navBearingM >= 11.25) this.roseM = "NNE";
            if (this.navBearingM >= 33.75) this.roseM = "NE";
            if (this.navBearingM >= 56.25) this.roseM = "ENE";
            if (this.navBearingM >= 78.75) this.roseM = "E";
            if (this.navBearingM >= 101.25) this.roseM = "ESE";
            if (this.navBearingM >= 123.75) this.roseM = "SE";
            if (this.navBearingM >= 146.25) this.roseM = "SSE";
            if (this.navBearingM >= 168.75) this.roseM = "S";
            if (this.navBearingM >= 191.25) this.roseM = "SSW";
            if (this.navBearingM >= 213.75) this.roseM = "SW";
            if (this.navBearingM >= 236.25) this.roseM = "WSW";
            if (this.navBearingM >= 258.75) this.roseM = "W";
            if (this.navBearingM >= 281.25) this.roseM = "WNW";
            if (this.navBearingM >= 303.75) this.roseM = "NW";
            if (this.navBearingM >= 326.25) this.roseM = "NNW";
            if (this.navBearingM >= 348.75) this.roseM = "N";
            // build the display strings
            this.navRoseDist = this.roseM + "  " + String.format("%5.0f", this.navdf) + " ft";
            this.navDegNDist = String.format("%3.0f", this.navBearingN) + " deg " + String.format("%5.0f", this.navdf) + " ft";
            //this.ndl2=this.mmcats+" & "+trim(String.format("%4.0f", this.mmdf))+"   "+Integer.toString(this.mmonth)+"/"+Integer.toString(this.mday)+"  "+Integer.toString(this.mhour) + ":" + String.format("%02d", this.mminute);
            //this.ndl3=String.format("%10.6f", this.mlat)+","+String.format("%10.6f", this.mlon);
        }

        double haversine(double lat1, double lon1, double lat2, double lon2) {
            // https://rosettacode.org/wiki/Haversine_formula#Java
            // result is in kilometers;, convert to miles at bottom
            double R = 6371f;    // mean radius of earth - which is not a sphere.  Pretty close
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            lat1 = Math.toRadians(lat1);
            lat2 = Math.toRadians(lat2);

            float a = (float) (Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2));
            float c = (float) (2 * Math.asin(Math.sqrt(a)));
            return (R * c) * 0.621371;    // radius is in km so result is km till converted to miles
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////  PlayaPoint

    class PlayaPoint {  // Playa Point:  a complete position description of a named point within the borders of Black Rock City
        // normal notation for speaking a coordinate is time distance, where time is hh:mm and distance is feet.
        public String name;       // name of point: text descriptor of location; eg "Man" or "Artery" or "Center Camp"
        public String address;    // playa address - distance from the man, in feet, followed by time on clock or if in city, interval between streets
        public String latlon;     // latitude and longitude string, formatted to 5 places, with comma separation
        public String mcadmdf;    // string of man clock angle in degrees and man clock distance, feet
        public double lat;        // latitude, degrees and decimal fraction
        public double lon;        // longitude, degrees and azimuth fraction
        double precision;  // estimate of how (in)accurately lat and lon is known-- more likely resolution  (from gps, else 0 if assigned, then lat lon is determined from mdm and mcad)
        double mcad;       // Center of an clock angle, degrees, polar from 12:00 straight "up"
        double mdf;        // man distance, feet
        double mdm;        // man distance, meters
        int mcah;       // man clock angle, Hours: 0 to 360 degrees maps from 00:00 to 11:59:59, allowing 1 ft resolution
        int mcam;       // man clock angle, Minutes
        int mcas;       // man clock angle, Seconds  (for the obsessive among us, gives about 1 foot of resolution along the perimeter fence)
        //PlayaMan goldenSpike = PlayaMan.getInstance();

        // Degree Madness:  There are four different methods of measuring angles in this system.
        // Picture a clock- 12:00 is vertical, 3:00 to right, 6:00 down, and 9:00 to left of the center of the clock
        // Unit Circle Angular (UCA) measurements, invoked in trig functions, measure angles from the 3:00 hand COUNTER-CLOCKWISE (ie, starts at 3, goes to 12, then to 9, then 6 then back to 3
        // Man Clock Angular (MCA) measurements are made with Man the center of the clock, from 12:00 (Temple), then clockwise around with center camp cafe at 6:00 and plazas at 3 and 9
        // True North Angular (TNA) measurments are made from the 10:30 position on the "clock" - this is the orienation for maps, gps, etc.  North is between the temple and 9:00 plaza from Man
        // Magnetic North Angular Measurement corrects for the difference between true north and what compass reads.  EG: Magnetic North is 13.7 degees EAST of true north in 2016 for BRC
        // Man Clock Angle Degrees is internally stored, others are derived as needed.
        // polymorphic constructors
        public PlayaPoint(double tlat, double tlon, double tprecision, String tname) {    // given lat / lon / precision from gps; determine angle and distance
            double dxf = 0;   // delta lon (x) feet
            double dyf = 0;   // delta lat (y) feet
            double tnad = 0;   // true north clock angle, degrees - what we get from lat and lon
            double ucad = 0;   // unit circle angle, degrees  - what trig functions deal with

            this.lat = tlat;     // latitude assignment
            this.lon = tlon;     // longitude assignment
            this.precision = tprecision; // precision assignment
            this.name = tname;   // text description of position assignment, may be void or could be time of assignment
            // determine the man coordinates of distance in feet and angle in degrees from lat/lon of given position, and lat/lon of golden spike
            dxf = (tlon - gs.lon) * gs.fpdlon;  // dxf = (tlon-GSLON)*FPDLON;
            dyf = (tlat - gs.lat) * gs.fpdlat;  // dyf = (tlat-GSLAT)*FPDLAT;
            this.mdf = Math.sqrt(dxf * dxf + dyf * dyf);
            this.mdm = this.mdf * 0.305;      // one foot = 0.305 meters
            //this.mcad = 360 - ((Math.atan2(dyf, dxf) * 180 / Math.PI) + goldenSpike.tncad);    // mcad = 360-((atan2(dyf,dxf)*180/PI)+GSTNAD);
            ucad = (Math.toDegrees(Math.atan2(dyf, dxf)));
            tnad = (360 - ucad) + 90;   // unit cicle and clock rotate differently, the 360- reverses the direction, the +90 moves it from 3 to 1
            this.mcad = tnad + gs.tncad;
            if (this.mcad < 0) {
                this.mcad = this.mcad + 360;
            }
            if (this.mcad >= 360) {
                this.mcad = this.mcad % 360.0;
            }
            buildAddress();
        }

        PlayaPoint(double tmcad, double tmdf, String tname) {     // given angle and distance, determine lat, lon, assign precision
            double dxf, dyf, uca;

            this.name = tname;
            this.mcad = tmcad;
            this.mdf = tmdf;
            this.mdm = this.mdf * 0.305;    // 1 meter is 0.305 feet
            uca = (360 - this.mcad) - gs.tncad;  // convert to unit circle angle for trig functions
            dxf = this.mdf * Math.cos(Math.toRadians(uca));
            dyf = this.mdf * Math.sin(Math.toRadians(uca));
            this.lat = gs.lat + dyf / gs.fpdlat;
            this.lon = gs.lon + dxf / gs.fpdlon;
            this.precision = 0;
            buildAddress();
        }

        PlayaPoint(String taddress, String tname) {              // parse string of form hh:mm:ss distanceft
            int hrs, min, sec;
            double tmcad, tmdf, uca, dxf, dyf;

            // BIG TIME WARNING - NEEDS EXCEPTION HANDLING!  Needs type checking!  Needs bullet proofing!
            String[] parts = taddress.split(" ");
            String time = parts[0];
            String dist = parts[1];
            String[] tbits = time.split(":");
            hrs = Integer.valueOf(tbits[0]);
            min = Integer.valueOf(tbits[1]);
            // tbits[2] needs to be optional
            sec = Integer.valueOf(tbits[2]);
            tmcad = (double) ((hrs * 3600 + min * 60 + sec) / 120);
            tmdf = Double.parseDouble(dist);
            this.name = tname;
            this.mcad = tmcad;
            this.mdf = tmdf;
            this.mdm = this.mdf * 0.305;    // 1 meter is 0.305 feet
            uca = (360 - this.mcad) - gs.tncad;  // convert to unit circle angle for trig functions
            dxf = this.mdf * Math.cos(Math.toRadians(uca));
            dyf = this.mdf * Math.sin(Math.toRadians(uca));
            this.lat = gs.lat + (dyf / gs.fpdlat);
            this.lon = gs.lon + (dxf / gs.fpdlon);
            this.precision = 0;
            buildAddress();
        }

        void update(double tlat, double tlon, double tprecision) {
            double dxf, dyf, ucad, tnad;

            this.lat = tlat;     // latitude assignment
            this.lon = tlon;     // longitude assignment
            this.precision = tprecision; // precision assignment
            // determine the man coordinates of distance in feet and angle in degrees from lat/lon of given position, and lat/lon of golden spike
            dxf = (tlon - gs.lon) * gs.fpdlon;  // dxf = (tlon-GSLON)*FPDLON;
            dyf = (tlat - gs.lat) * gs.fpdlat;  // dyf = (tlat-GSLAT)*FPDLAT;
            this.mdf = Math.sqrt(dxf * dxf + dyf * dyf);
            this.mdm = this.mdf * 0.305;      // one foot = 0.305 meters
            //this.mcad = 360 - ((Math.atan2(dyf, dxf) * 180 / Math.PI) + goldenSpike.tncad);    // mcad = 360-((atan2(dyf,dxf)*180/PI)+GSTNAD);
            ucad = (Math.toDegrees(Math.atan2(dyf, dxf)));
            tnad = (360 - ucad) + 90;   // unit cicle and clock rotate differently, the 360- reverses the direction, the +90 moves it from 3 to 1
            this.mcad = tnad + gs.tncad;
            if (this.mcad < 0) {
                this.mcad = this.mcad + 360;
            }
            if (this.mcad >= 360) {
                this.mcad = this.mcad % 360.0;
            }
            buildAddress();
        }

        void buildAddress() {
            // addresses are of form time distance, so first build time.  The clock is a 12 hour clock so each hour is 360/12=30 degrees
            // also constructs latlon string, a formatted latitude longitude (5 decimal places, or about 4 feet resolution give or take
            int hrs;
            int min;
            int sec;
            // radial measurement from man to middle of 40' wide street
            float SMDF = 2500;    // Center of eSplanade
            float AMDF = 2940;    // Center of A
            float BMDF = 3180;    // Center of B
            float CMDF = 3420;    // Center of C
            float DMDF = 3660;    // Center of D
            float EMDF = 3900;    // Center of E
            float FMDF = 4140;    // Center of F
            float GMDF = 4380;    // Center of G
            float HMDF = 4620;    // Center of H
            float IMDF = 4860;    // Center of I
            float JMDF = 5100;    // Center of J
            float KMDF = 5340;    // Center of K
            float LMDF = 5580;    // Center of L
            float PPMDF = 8160;    // DISTANCE FROM MAN TO PENTAGON POINTS OF TRASH FENCE

            String timeangle = "";
            String distfeet = Integer.toString((int) this.mdf);
            String street = "";
            this.mcas = (int) (this.mcad * 120);  // there are 360 degrees in 12*60*60 seconds, so each degree is 360/(12*60*60) seconds or 120 seconds
            this.mcah = this.mcas / (3600);
            this.mcam = (this.mcas - this.mcah * 3600) / 60;
            this.mcas = this.mcas - this.mcah * 3600 - this.mcam * 60;
            if (this.mcah == 0) {    // range from 0 to 1 hour is called "12"
                this.mcah = 12;
            }
            timeangle = String.format("%02d", this.mcah) + ":" + String.format("%02d", this.mcam) + ":" + String.format("%02d", this.mcas);
            this.address = timeangle + " & " + distfeet;
            this.latlon = String.format("%11.5f", this.lat) + "," + String.format("%11.5f", this.lon);
            this.mcadmdf = String.format("%7.1f", this.mcad) + " & " + String.format("%5.0f", this.mdf);
            // build hours, minutes and seconds from clock angle in degrees
            // 2016 : reviewed, correct: these are to center of street// 2:00 to 2:30 - no F
            // 3:30 to 4:00 - no F
            // 5:00 to 7:00 - no F
            // 8:00 to 8:30 - no F
            // 9:30 to 10:00 - no F
            // 10:00 to 2:00 - no roads at all!
            if (((this.mcad > 60) & (mcad <= 75)) | ((this.mcad > 105) & (this.mcad <= 120)) | ((this.mcad > 150) & (this.mcad <= 210)) | ((this.mcad > 240) & (this.mcad <= 255)) | ((this.mcad > 285) & (this.mcad <= 300))) {
                // if (this.mdf < SMDF - 20) street = "Playa";
                if (this.mdf >= SMDF - 20) street = "Esplanade";
                if (this.mdf >= SMDF + 20) street = "Esp-A Block";
                if (this.mdf >= AMDF - 20) street = "Arno";
                if (this.mdf >= AMDF + 20) street = "A-B Block";
                if (this.mdf >= BMDF - 20) street = "Botticelli";
                if (this.mdf >= BMDF + 20) street = "B-C Block";
                if (this.mdf >= CMDF - 20) street = "Cosimo";
                if (this.mdf >= CMDF + 20) street = "C-D Block";
                if (this.mdf >= DMDF - 20) street = "Donatello";
                if (this.mdf >= DMDF + 20) street = "D-E Block";
                if (this.mdf >= EMDF - 20) street = "Effigiare";
                if (this.mdf >= EMDF + 20) street = "E-F Block";
                if (this.mdf >= FMDF - 20) street = "Florin";
                if (this.mdf >= FMDF + 20) street = "F-G Block";
                if (this.mdf >= GMDF - 20) street = "Guild";
                if (this.mdf >= GMDF + 20) street = "G-H Block";
                if (this.mdf >= HMDF - 20) street = "High Ren";
                if (this.mdf >= HMDF + 20) street = "H-J Block";
                if (this.mdf >= JMDF - 20) street = "Justice";
                if (this.mdf >= JMDF + 20) street = "J-K Block";
                if (this.mdf >= KMDF - 20) street = "Knowledge";
                if (this.mdf >= KMDF + 20) street = "K-L Block";
                if (this.mdf >= LMDF - 20) street = "Lorenzo";
                if (this.mdf >= LMDF + 20) street = "Perimeter";
            }
            if (((this.mcad > 75) & (mcad <= 105)) | ((this.mcad > 120) & (this.mcad <= 150)) | ((this.mcad > 210) & (this.mcad <= 240)) | ((this.mcad > 255) & (this.mcad <= 285))) {
                // if (this.mdf < SMDF - 20) street = "Playa";
                if (this.mdf >= SMDF - 20) street = "Esplanade";
                if (this.mdf >= SMDF + 20) street = "Esp-A Block";
                if (this.mdf >= AMDF - 20) street = "Arno";
                if (this.mdf >= AMDF + 20) street = "A-B Block";
                if (this.mdf >= BMDF - 20) street = "Botticelli";
                if (this.mdf >= BMDF + 20) street = "B-C Block";
                if (this.mdf >= CMDF - 20) street = "Cosimo";
                if (this.mdf >= CMDF + 20) street = "C-D Block";
                if (this.mdf >= DMDF - 20) street = "Donatello";
                if (this.mdf >= DMDF + 20) street = "D-E Block";
                if (this.mdf >= EMDF - 20) street = "Effigiare";
                if (this.mdf >= EMDF + 20) street = "E-F Block";
                if (this.mdf >= FMDF - 20) street = "Florin";
                if (this.mdf >= FMDF + 20) street = "F-G Block";
                if (this.mdf >= GMDF - 20) street = "Guild";
                if (this.mdf >= GMDF + 20) street = "G-H Block";
                if (this.mdf >= HMDF - 20) street = "High Ren";
                if (this.mdf >= HMDF + 20) street = "H-I Block";
                if (this.mdf >= IMDF - 20) street = "Italic";
                if (this.mdf >= IMDF + 20) street = "I-J Block";
                if (this.mdf >= JMDF - 20) street = "Justice";
                if (this.mdf >= JMDF + 20) street = "J-K Block";
                if (this.mdf >= KMDF - 20) street = "Knowledge";
                if (this.mdf >= KMDF + 20) street = "K-L Block";
                if (this.mdf >= LMDF - 20) street = "Lorenzo";
                if (this.mdf >= LMDF + 20) street = "Perimeter";

            }
            if (street.length() == 0) {
                this.address = timeangle + " & " + distfeet;
            } else {
                this.address = timeangle + " & " + street;
            }
            if ((this.mdf > PPMDF)) this.address = "Default World";
            if ((this.mdf < 20)) this.address = "Man Base";
        }
    }
}
/////////////////////////////////////////////////////////////////////////////////////////////

