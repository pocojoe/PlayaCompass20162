package com.hoppercodes.PlayaPositioningSystem;

/**
 * Created by hexagon on 6/24/16.
 */
public final class PlayaPositioningSystem {
    ///////////////////////////////////////////////////////////////
    GoldenSpike gs; // GoldenSpike(40.7864, -119.20065, 315.0, "GS 2016");
    PlayaHeading heading = new PlayaHeading(0f);
    PlayaPoint here = new PlayaPoint(40.7864, -119.2065, 10.0, "GS 2016");
    PlayaPoint there = new PlayaPoint(40.7864, -119.2065, 10.0, "GS 2016");
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

    public PlayaHeading getHeading()
    {
        return heading;
    }

    public void setHeading(PlayaHeading heading) {
        this.heading=heading;
    }

    public PlayaPoint getHere() {
        return here;
    }

    public void setHere(PlayaPoint here)
    {
        this.here = here;
    }

    public PlayaPoint getThere() {
        return there;
    }

    public void setThere(PlayaPoint there) {
        this.there = there;
    }
}
