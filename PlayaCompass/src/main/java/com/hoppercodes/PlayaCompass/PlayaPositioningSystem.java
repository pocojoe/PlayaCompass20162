package com.hoppercodes.PlayaCompass;

/**
 * Created by hexagon on 6/24/16.
 */
public final class PlayaPositioningSystem {
    ///////////////////////////////////////////////////////////////
    GoldenSpike gs; // GoldenSpike(40.786975, -119.204391, 315.0, "GS Google Maps Man");


    // GoldenSpike gs = new GoldenSpike(32.250223, -110.944095, 315.0,"GS Tuc Campbell Grant" );
    // GoldenSpike gs = new GoldenSpike(40.7864,-119.2065,315.0,"GS 2015 Man");     // Man Location 2015
    // GoldenSpike gs = new GoldenSpike(40.7864,-119.2065,315.0, "GS 2016 MAN");     // Man location 2016
    //
    //  position HERE - my present location, updated by LocationManager with current GPS
    // PlayaPoint here = new PlayaPoint(32.246517, -110.940548, 10.0, "Home");         // present location
    // PlayaPoint here = new PlayaPoint("7:30:00 5580","7:30 and L");
    PlayaPoint here = new PlayaPoint(40.781482, -119.211597, 10.0, "Center Camp Google Maps");  // present location
    // PlayaPoint here = new PlayaPoint(40.787087,-119.204388,10.0,"Man 2015");
    //
    // Position THERE - a target destination retrieved from list, or logged from current position for backtrack purposes
    // for initialization purposes, THERE should be initialized to the location of the Man
    // PlayaPoint there = new PlayaPoint(40.7864,-119.2065,"Man 2016");
    PlayaPoint there = new PlayaPoint(40.786925, -119.223914, 10.0, "7:30 L Google Maps");
    // PlayaPoint there = new PlayaPoint(225,5580,"7:30 and L 2015");
    // PlayaPoint there = new PlayaPoint(32.250329, -110.935390, 10.0, "Circle K");    // destination for navigation
    // PlayaPoint there = new PlayaPoint(180.00,3000.0,"six oclock somehere");
    // PlayaPoint there = new PlayaPoint(32.243616,-112.938259,10.0,"six oclock?");
    // PlayaPoint there = new PlayaPoint(270,2940,"9:00 and A 2015");
    // PlayaPoint there = new PlayaPoint(40.792497,-119.211597, 10.0, "9:00 and A 2015");             // present location
    // PlayaPoint there = new PlayaPoint(40.791636,-119.198294, 10.0, "Temple 2015");             // present location
    private static PlayaPositioningSystem instance;

    public static synchronized PlayaPositioningSystem getInstance(){
        if(instance == null){
            instance = new PlayaPositioningSystem();
        }
        return instance;
    }

    private PlayaPositioningSystem() {
        this.gs = GoldenSpike.getInstance();
    }

    public GoldenSpike getGs() {
        return gs;
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
}
