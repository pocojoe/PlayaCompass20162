package com.hoppercodes.PlayaPositioningSystem;

public final class PlayaPositioningSystem {
    // these values represent the "public interface" of Playa Positioning System
    // man location: iniitalizes PlayaMan
    public static double manLat;
    public static double manLon;
    public static double manNorthAngle;
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
    // bearing:  direction and distance from present location (here) to destination (there)
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

    static {
        // todo - initialize from configuration values
        manLat = 40.7864;
        manLon = -119.2065;
        manNorthAngle = 315.0;
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


    ///////////////////////////////////////////////////////////////
    //PlayaMan gs; // PlayaMan(40.7864, -119.20065, 315.0, "GS 2016");
    PlayaMan gs = new PlayaMan(this.manLat, this.manLon, this.manNorthAngle, this.manLabel);
    PlayaHeading heading = new PlayaHeading(0f);
    PlayaPoint here = new PlayaPoint(40.7864, -119.2065, 10.0, "GS 2016");
    // PlayaPoint there = new PlayaPoint(40.7864, -119.2065, 10.0, "GS 2016");
    PlayaPoint there = new PlayaPoint(32.250018, -110.944092, 10.0, "Bookmans");

    private static PlayaPositioningSystem instance;

    public static synchronized PlayaPositioningSystem getInstance() {
        if (instance == null) {
            instance = new PlayaPositioningSystem();
        }
        return instance;
    }

    private PlayaPositioningSystem() {
        this.gs = PlayaMan.getInstance();
    }

    public PlayaMan getGs() {
        return gs;
    }

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
}
