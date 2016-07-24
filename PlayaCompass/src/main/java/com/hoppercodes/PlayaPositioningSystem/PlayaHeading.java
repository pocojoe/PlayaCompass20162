package com.hoppercodes.PlayaPositioningSystem;

/**
 * Created by jmiller on 7/23/2016.
 */
public class PlayaHeading {
    public double headingN;   // true north heading
    public double headingM;   // magnetic north heading
    int roseError;
    int roseN;              // which compass rose value 1:15
    public int roseInt;     // integer value of hysteresis stabilized compass reading
    public int roseDeg;     // integer value of azimuth - fluctuating compass reading
    public String roseWind;
    public String roseHeading;
    GoldenSpike goldenSpike = GoldenSpike.getInstance();        // need this for declination of golden spike

    public PlayaHeading(double azimuth) {
        this.headingM = AverageAzimuth(azimuth);  // provides average of last 16 samples
        this.headingN = (((this.headingM - goldenSpike.declination) + 360.0) % 360.0);
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

    double AverageAzimuth(double azIn)     // compute the average of N compass readings and give the rolling average
    {
        int r = 0;      // ring buffer index
        int rSize=16;
        int ndiv = 0;
        double azVal[] = new double[rSize];
        double ra;      //angle in radians
        double newSin = 0f;
        double newCos = 0f;
        double oldSin = 0f;
        double oldCos = 0f;
        double sumSin = 0f;
        double sumCos = 0f;
        double mSin = 0f;
        double mCos = 0f;
        double mAvg = 0f;

        if (ndiv >= rSize) {
            ra = Math.toRadians(azVal[r]);    // out with the old
            oldSin = Math.sin(ra);
            oldCos = Math.cos(ra);
            ndiv = rSize;                      // ndiv gets capped at 16
        } else {
            ndiv += 1;
        }
        ra = Math.toRadians(azIn);            // and in with the new
        azVal[r] = ra;
        r=(r+1)%rSize;                         // ring around the rose (ie)
        newSin = Math.sin(ra);
        newCos = Math.cos(ra);
        sumSin = sumSin - oldSin + newSin;
        sumCos = sumCos - oldCos + newCos;
        mSin = sumSin / ndiv;
        mCos = sumCos / ndiv;
        mAvg = Math.toDegrees(Math.atan2(mSin, mCos));  // see averaging circular quantities
        mAvg=(mAvg+360f)%360f;
        return mAvg;
    }
}