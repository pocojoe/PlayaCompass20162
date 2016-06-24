package com.hoppercodes.PlayaPositioningSystem;

/**
 * Created by hexagon on 6/24/16.
 */
public class PlayaNavigate {                // navigate from point here to point there
    public String tnbearbdf;           // string true north bearing in degrees and distance in feet
    public String compassbdf;          // compass bearing, rose string in 45 degree steps - eg NW - and distance in feet, declination corrected.
    String rose;                // compass rose abbreviation - eg NW
    double compassBearing;      // the direction to navigate by compass magnetic field
    double trueNorthBearing;    // true north bearing - the direction to navigate (declination corrected)
    double compassHeading;      // the direction the handset is pointed by compass sensor magnetic field
    double trueNorthHeading;    // the direction the handset is pointed with respect to true north (declination corrected)
    double navdm;               // navigation (here to there) destingation distance, meters
    double navdf;               // navigation (here to there) destination distance, feet
    PlayaPositioningSystem playaPositioningSystem = PlayaPositioningSystem.getInstance();

    public PlayaNavigate() {
        this.update();
    }

    void update() {
        double dxf;     // east-west navigation vector in feet
        double dyf;     // north-south navigation vector in feet
        double ucad;    // unit circle angle, degrees
        GoldenSpike gs = playaPositioningSystem.getGs();
        PlayaPoint here = playaPositioningSystem.getHere();
        PlayaPoint there = playaPositioningSystem.getThere();

        // get compass heading from sensor module in handset
        this.compassHeading = 999;
        this.trueNorthHeading = this.compassHeading + gs.declination;
        while (this.trueNorthHeading < 0) {      // this silliness normalizes the angle to the range of 0 to 360
            if (this.trueNorthHeading < 0) {
                this.trueNorthHeading = this.trueNorthHeading + 360;
            }
        }
        if (this.trueNorthHeading >= 360) {
            this.trueNorthHeading = this.trueNorthHeading % 360.0;
        }
        // determine the man coordinates of distance in feet and angle in degrees from lat/lon of given position, and lat/lon of golden spike
        dxf = (here.lon - there.lon) * gs.fpdlon;
        dyf = (here.lat - there.lat) * gs.fpdlat;
        this.navdf = Math.sqrt(dxf * dxf + dyf * dyf);
        this.navdm = this.navdf * 0.305;      // one foot = 0.305 meters

        ucad = (Math.toDegrees(Math.atan2(dyf, dxf)));
        this.trueNorthBearing = ((360 - ucad) - 90);
        while (this.trueNorthBearing < 0) {      // this silliness normalizes the angle to the range of 0 to 360
            if (this.trueNorthBearing < 0) {
                this.trueNorthBearing = this.trueNorthBearing + 360;
            }
        }
        if (this.trueNorthBearing >= 360) {
            this.trueNorthBearing = this.trueNorthBearing % 360.0;
        }
        this.tnbearbdf = "True North: " + String.format("%5.1f", this.trueNorthBearing) + " Deg " + String.format("%5.0f", this.navdf) + " Ft";
        //
        // determine compass bearing from true north bearing.  Compass is about 12 degrees east of true north.
        this.compassBearing = this.trueNorthBearing + gs.declination;   // when going from true north to magnetic, you add the dec to tn to get the mag bearing
        while (this.compassBearing < 0) {      // this silliness normalizes the angle to the range of 0 to 360
            if (this.compassBearing < 0) {
                this.compassBearing = this.compassBearing + 360;
            }
        }
        if (this.compassBearing >= 360) {
            this.compassBearing = this.compassBearing % 360.0;
        }
        if (this.compassBearing >= -22.5) this.rose = "N";
        if (this.compassBearing >= 22.5) this.rose = "NE";
        if (this.compassBearing >= 67.5) this.rose = "E";
        if (this.compassBearing >= 112.5) this.rose = "SE";
        if (this.compassBearing >= 157.5) this.rose = "S";
        if (this.compassBearing >= 202.5) this.rose = "SW";
        if (this.compassBearing >= 247.5) this.rose = "W";
        if (this.compassBearing >= 292.5)
            if (this.compassBearing >= 337.5) this.rose = "N";
        this.compassbdf = String.format("%3.0f", this.compassBearing) + " " + this.rose + " " + String.format("%5.0f", this.navdf) + " Ft";
    }
}
