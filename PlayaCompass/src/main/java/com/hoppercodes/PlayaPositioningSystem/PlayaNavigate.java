package com.hoppercodes.PlayaPositioningSystem;

public class PlayaNavigate {           // navigate from point here to point there
    public String navRoseDist;         // string compass rose (mag) and distance in feet
    public String navDegNDist;         // string North Degrees and distance in feet
    public String ndl1;     // nav display line 1
    public String ndl2;     // nav display line 2
    public String ndl3;     // nav display line 3
    public String roseM;               // compass rose abbreviation - eg NW
    public double navBearingM;    // magnetic bearing:  true north plus declincation
    public double navBearingN;    // true north bearing - the map, not compass, direction to navigate
    public double navdm;               // navigation (here to there) destingation distance, meters
    public double navdf;               // navigation (here to there) destination distance, feet
    PlayaPositioningSystem playaPositioningSystem = PlayaPositioningSystem.getInstance();

    public PlayaNavigate() {
        this.update();
    }

    void update() {
        double dxf;     // east-west navigation vector in feet
        double dyf;     // north-south navigation vector in feet
        double ucad;    // unit circle angle, degrees
        PlayaMan gs = playaPositioningSystem.getGs();
        PlayaPoint here = playaPositioningSystem.getHere();
        PlayaPoint there = playaPositioningSystem.getThere();
        PlayaHeading heading = playaPositioningSystem.getHeading();  // heading is the direction one is currently facing; bearing is the direction to there from here

        // determine the man coordinates of distance in feet and angle in degrees from lat/lon of given position, and lat/lon of golden spike
        dxf = (here.lon - there.lon) * gs.fpdlon;
        dyf = (here.lat - there.lat) * gs.fpdlat;
        this.navdf = Math.sqrt(dxf * dxf + dyf * dyf);
        this.navdm = this.navdf * 0.305;      // one foot = 0.305 meters

        ucad = (Math.toDegrees(Math.atan2(dyf, dxf)));  // unit circle angle degrees
        this.navBearingN = ((360 - ucad) - 90);
        this.navBearingN = (this.navBearingN+360f)%360f;
        // determine compass bearing from true north bearing.  Compass is about 12 degrees east of true north.
        this.navBearingM = this.navBearingN + gs.declination;   // when going from true north to magnetic, you add the dec to tn to get the mag bearing
        this.navBearingM = (this.navBearingM+360f)%360;
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
        this.navRoseDist =this.roseM + "  " + String.format("%5.0f", this.navdf) + " ft";
        this.navDegNDist = String.format("%3.0f",this.navBearingN)+ " deg " +String.format("%5.0f",this.navdf) +" ft";
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

        float a =(float) (Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2));
        float c =(float) (2 * Math.asin(Math.sqrt(a)));
        return (R * c)*0.621371;    // radius is in km so result is km till converted to miles
    }
}
