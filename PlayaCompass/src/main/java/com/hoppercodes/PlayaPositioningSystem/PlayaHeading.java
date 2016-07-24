package com.hoppercodes.PlayaPositioningSystem;

/**
 * Created by jmiller on 7/23/2016.
 */
public class PlayaHeading {
    public double headingN;   // true north heading
    public double headingM;   // magnetic north heading
    public int roseInt;
    public String roseWind;
    GoldenSpike goldenSpike = GoldenSpike.getInstance();        // need this for declination of golden spike

    public PlayaHeading(double azimuth){
        // todo:  generate running average of last ten values
        this.headingM=azimuth;
        this.headingN=this.headingM+goldenSpike.declination;
        // todo
        this.roseInt=9;
        // todo
        this.roseWind=String.format("%3.0f",azimuth);
    }
}
