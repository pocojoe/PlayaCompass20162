package com.hoppercodes.PlayaPositioningSystem;

import com.hoppercodes.playacompass.R;

/**
 * Created by hexagon on 6/24/16.
 */
// stuff below here should be encapsulated into a class called PlayaDatum.  A datum is "a coordinate system and set of reference points used to locate places on earth".
public class PlayaPoint {  // Playa Point:  a complete position description of a named point within the borders of Black Rock City
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
    GoldenSpike goldenSpike = GoldenSpike.getInstance();

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
        dxf = (tlon - goldenSpike.lon) * goldenSpike.fpdlon;  // dxf = (tlon-GSLON)*FPDLON;
        dyf = (tlat - goldenSpike.lat) * goldenSpike.fpdlat;  // dyf = (tlat-GSLAT)*FPDLAT;
        this.mdf = Math.sqrt(dxf * dxf + dyf * dyf);
        this.mdm = this.mdf * 0.305;      // one foot = 0.305 meters
        //this.mcad = 360 - ((Math.atan2(dyf, dxf) * 180 / Math.PI) + goldenSpike.tncad);    // mcad = 360-((atan2(dyf,dxf)*180/PI)+GSTNAD);
        ucad = (Math.toDegrees(Math.atan2(dyf, dxf)));
        tnad = (360 - ucad) + 90;   // unit cicle and clock rotate differently, the 360- reverses the direction, the +90 moves it from 3 to 1
        this.mcad = tnad + goldenSpike.tncad;
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
        uca = (360 - this.mcad) - goldenSpike.tncad;  // convert to unit circle angle for trig functions
        dxf = this.mdf * Math.cos(Math.toRadians(uca));
        dyf = this.mdf * Math.sin(Math.toRadians(uca));
        this.lat = goldenSpike.lat + dyf / goldenSpike.fpdlat;
        this.lon = goldenSpike.lon + dxf / goldenSpike.fpdlon;
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
        uca = (360 - this.mcad) - goldenSpike.tncad;  // convert to unit circle angle for trig functions
        dxf = this.mdf * Math.cos(Math.toRadians(uca));
        dyf = this.mdf * Math.sin(Math.toRadians(uca));
        this.lat = goldenSpike.lat + (dyf / goldenSpike.fpdlat);
        this.lon = goldenSpike.lon + (dxf / goldenSpike.fpdlon);
        this.precision = 0;
        buildAddress();
    }


    void update(double tlat, double tlon, double tprecision) {
        double dxf, dyf, ucad, tnad;

        this.lat = tlat;     // latitude assignment
        this.lon = tlon;     // longitude assignment
        this.precision = tprecision; // precision assignment
        // determine the man coordinates of distance in feet and angle in degrees from lat/lon of given position, and lat/lon of golden spike
        dxf = (tlon - goldenSpike.lon) * goldenSpike.fpdlon;  // dxf = (tlon-GSLON)*FPDLON;
        dyf = (tlat - goldenSpike.lat) * goldenSpike.fpdlat;  // dyf = (tlat-GSLAT)*FPDLAT;
        this.mdf = Math.sqrt(dxf * dxf + dyf * dyf);
        this.mdm = this.mdf * 0.305;      // one foot = 0.305 meters
        //this.mcad = 360 - ((Math.atan2(dyf, dxf) * 180 / Math.PI) + goldenSpike.tncad);    // mcad = 360-((atan2(dyf,dxf)*180/PI)+GSTNAD);
        ucad = (Math.toDegrees(Math.atan2(dyf, dxf)));
        tnad = (360 - ucad) + 90;   // unit cicle and clock rotate differently, the 360- reverses the direction, the +90 moves it from 3 to 1
        this.mcad = tnad + goldenSpike.tncad;
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