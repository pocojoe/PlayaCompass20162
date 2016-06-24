package com.hoppercodes.PlayaCompass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	  private TextView latitude;
	  private TextView longitude;
	  private TextView choice;
	  private CheckBox fineAcc;
	  private Button choose;
	  private TextView provText;
	  private LocationManager locationManager;
	  private String provider;
	  private MyLocationListener mylistener;
	  private Criteria criteria;
	  
	/** Called when the activity is first created. */

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_main);
		  latitude = (TextView) findViewById(R.id.lat);
		  longitude = (TextView) findViewById(R.id.lon);
		  provText = (TextView) findViewById(R.id.prov);
		  choice = (TextView) findViewById(R.id.choice);
		  fineAcc = (CheckBox) findViewById(R.id.fineAccuracy);
		  choose = (Button) findViewById(R.id.chooseRadio);

		  // Get the location manager
		  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		  // Define the criteria how to select the location provider
		  criteria = new Criteria();
		  criteria.setAccuracy(Criteria.ACCURACY_COARSE);	//default
		  
		  // user defines the criteria
		  choose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(fineAcc.isChecked()){
					  criteria.setAccuracy(Criteria.ACCURACY_FINE);
					  choice.setText("fine accuracy selected");
				 }else {
					 criteria.setAccuracy(Criteria.ACCURACY_COARSE);
					 choice.setText("coarse accuracy selected");
				 }  
			}
		  });
		  criteria.setCostAllowed(false); 
		  // get the best provider depending on the criteria
		  provider = locationManager.getBestProvider(criteria, false);
	    
		  // the last known location of this provider
		  Location location = locationManager.getLastKnownLocation(provider);

		  mylistener = new MyLocationListener();
	
		  if (location != null) {
			  mylistener.onLocationChanged(location);
		  } else {
			  // leads to the settings because there is no last known location
			  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			  startActivity(intent);
		  }
		  // location updates: at least 1 meter and 200millsecs change
		  locationManager.requestLocationUpdates(provider, 200, 1, mylistener);
	  }

	  private class MyLocationListener implements LocationListener {
	
		  @Override
		  public void onLocationChanged(Location location) {
			// Initialize the location fields
			  latitude.setText("Latitude: "+String.valueOf(location.getLatitude()));
			  longitude.setText("Longitude: "+String.valueOf(location.getLongitude()));
			  provText.setText(provider + " provider has been selected.");
			  PlayaDisplayBasics();
			  here.update(location.getLatitude(),location.getLongitude(),location.getAccuracy());
			  Toast.makeText(MainActivity.this,  "Location changed!",
				        Toast.LENGTH_SHORT).show();
		  }
	
		  @Override
		  public void onStatusChanged(String provider, int status, Bundle extras) {
			  Toast.makeText(MainActivity.this, provider + "'s status changed to "+status +"!",
				        Toast.LENGTH_SHORT).show();
		  }
	
		  @Override
		  public void onProviderEnabled(String provider) {
			  Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
		        Toast.LENGTH_SHORT).show();
	
		  }
	
		  @Override
		  public void onProviderDisabled(String provider) {
			  Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
		        Toast.LENGTH_SHORT).show();
		  }
	  }

///////////////////////////////////////////////////////////////
GoldenSpike gs = new GoldenSpike(40.786975, -119.204391, 315.0, "GS Google Maps Man");
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
	//
	// class and methods to get from here to there  - the stuff to navigate
	PlayaNavigate here2there = new PlayaNavigate();

	void PlayaDisplayBasics() {
		setContentView(R.layout.activity_main);
		// golden spike
		TextView gsname = (TextView) findViewById(R.id.gsName);
		gsname.setText(String.valueOf(gs.datum));
		TextView gslatlon = (TextView) findViewById(R.id.gsLatLon);
		gslatlon.setText(String.valueOf(gs.latlon));
		// here
		TextView herename = (TextView) findViewById(R.id.hereName);
		herename.setText("Here: " + String.valueOf(here.name));
		TextView herelatlon = (TextView) findViewById(R.id.hereLatLon);
		TextView heremcadmdf = (TextView) findViewById(R.id.heremcadmdf);
		herelatlon.setText(String.valueOf(here.latlon));
		heremcadmdf.setText(String.valueOf(here.mcadmdf));
		TextView hereaddress = (TextView) findViewById(R.id.hereAddress);
		hereaddress.setText(here.address);    // can increase precision if desired
		// there
		TextView therename = (TextView) findViewById(R.id.thereName);
		therename.setText("There: " + String.valueOf(there.name));
		TextView therelatlon = (TextView) findViewById(R.id.thereLatLon);
		TextView theremcadmdf = (TextView) findViewById(R.id.theremcadmdf);
		therelatlon.setText(String.valueOf(there.latlon));
		theremcadmdf.setText(String.valueOf(there.mcadmdf));
		TextView thereaddress = (TextView) findViewById(R.id.thereAddress);
		thereaddress.setText(there.address);    // can increase precision if desired
		// navigation route
		TextView navigation = (TextView) findViewById(R.id.navigation);
		navigation.setText("Navigation");
		TextView navtnbearingdist = (TextView) findViewById(R.id.navTNBearingDist);
		navtnbearingdist.setText(String.valueOf(here2there.tnbearbdf));
		TextView navdistfeet = (TextView) findViewById(R.id.navTNBearingDist);
		navdistfeet.setText(String.valueOf(here2there.tnbearbdf));
		TextView navbeardf = (TextView) findViewById(R.id.navCompassBearingDist);
		navbeardf.setText("Compass: " + String.valueOf(here2there.compassbdf));
	}

	// stuff below here should be encapsulated into a class called PlayaDatum.  A datum is "a coordinate system and set of reference points used to locate places on earth".

	class PlayaPoint {  // Playa Point:  a complete position description of a named point within the borders of Black Rock City
		// normal notation for speaking a coordinate is time distance, where time is hh:mm and distance is feet.
		String name;       // name of point: text descriptor of location; eg "Man" or "Artery" or "Center Camp"
		String address;    // playa address - distance from the man, in feet, followed by time on clock or if in city, interval between streets
		String latlon;     // latitude and longitude string, formatted to 5 places, with comma separation
		String mcadmdf;    // string of man clock angle in degrees and man clock distance, feet
		double lat;        // latitude, degrees and decimal fraction
		double lon;        // longitude, degrees and azimuth fraction
		double precision;  // estimate of how (in)accurately lat and lon is known-- more likely resolution  (from gps, else 0 if assigned, then lat lon is determined from mdm and mcad)
		double mcad;       // Center of an clock angle, degrees, polar from 12:00 straight "up"
		double mdf;        // man distance, feet
		double mdm;        // man distance, meters
		int mcah;       // man clock angle, Hours: 0 to 360 degrees maps from 00:00 to 11:59:59, allowing 1 ft resolution
		int mcam;       // man clock angle, Minutes
		int mcas;       // man clock angle, Seconds  (for the obsessive among us, gives about 1 foot of resolution along the perimeter fence)

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
			//this.mcad = 360 - ((Math.atan2(dyf, dxf) * 180 / Math.PI) + gs.tncad);    // mcad = 360-((atan2(dyf,dxf)*180/PI)+GSTNAD);
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
			this.lat = gs.lat + dyf / gs.fpdlat;
			this.lon = gs.lon + dxf / gs.fpdlon;
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
			//this.mcad = 360 - ((Math.atan2(dyf, dxf) * 180 / Math.PI) + gs.tncad);    // mcad = 360-((atan2(dyf,dxf)*180/PI)+GSTNAD);
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
				if (this.mdf >= AMDF - 20) street = "A Street";
				if (this.mdf >= AMDF + 20) street = "A-B Block";
				if (this.mdf >= BMDF - 20) street = "B Street";
				if (this.mdf >= BMDF + 20) street = "B-C Block";
				if (this.mdf >= CMDF - 20) street = "C Street";
				if (this.mdf >= CMDF + 20) street = "C-D Block";
				if (this.mdf >= DMDF - 20) street = "D Street";
				if (this.mdf >= DMDF + 20) street = "D-E Block";
				if (this.mdf >= EMDF - 20) street = "E Street";
				if (this.mdf >= EMDF + 20) street = "E-F Block";
				if (this.mdf >= FMDF - 20) street = "F Street";
				if (this.mdf >= FMDF + 20) street = "F-G Block";
				if (this.mdf >= GMDF - 20) street = "G Street";
				if (this.mdf >= GMDF + 20) street = "G-H Block";
				if (this.mdf >= HMDF - 20) street = "H Street";
				if (this.mdf >= HMDF + 20) street = "H-J Block";
				if (this.mdf >= JMDF - 20) street = "J Street";
				if (this.mdf >= JMDF + 20) street = "J-K Block";
				if (this.mdf >= KMDF - 20) street = "K Street";
				if (this.mdf >= KMDF + 20) street = "K-L Block";
				if (this.mdf >= LMDF - 20) street = "L Street";
			}
			if (((this.mcad > 75) & (mcad <= 105)) | ((this.mcad > 120) & (this.mcad <= 150)) | ((this.mcad > 210) & (this.mcad <= 240)) | ((this.mcad > 255) & (this.mcad <= 285))) {
				// if (this.mdf < SMDF - 20) street = "Playa";
				if (this.mdf >= SMDF - 20) street = "Esplanade";
				if (this.mdf >= SMDF + 20) street = "Esp-A Block";
				if (this.mdf >= AMDF - 20) street = "A Street";
				if (this.mdf >= AMDF + 20) street = "A-B Block";
				if (this.mdf >= BMDF - 20) street = "B Street";
				if (this.mdf >= BMDF + 20) street = "B-C Block";
				if (this.mdf >= CMDF - 20) street = "C Street";
				if (this.mdf >= CMDF + 20) street = "C-D Block";
				if (this.mdf >= DMDF - 20) street = "D Street";
				if (this.mdf >= DMDF + 20) street = "D-E Block";
				if (this.mdf >= EMDF - 20) street = "E Street";
				if (this.mdf >= EMDF + 20) street = "E-F Block";
				if (this.mdf >= FMDF - 20) street = "F Street";
				if (this.mdf >= FMDF + 20) street = "F-G Block";
				if (this.mdf >= GMDF - 20) street = "G Street";
				if (this.mdf >= GMDF + 20) street = "G-H Block";
				if (this.mdf >= HMDF - 20) street = "H Street";
				if (this.mdf >= HMDF + 20) street = "H-I Block";
				if (this.mdf >= IMDF - 20) street = "I Street";
				if (this.mdf >= IMDF + 20) street = "I-J Block";
				if (this.mdf >= JMDF - 20) street = "J Street";
				if (this.mdf >= JMDF + 20) street = "J-K Block";
				if (this.mdf >= KMDF - 20) street = "K Street";
				if (this.mdf >= KMDF + 20) street = "K-L Block";
				if (this.mdf >= LMDF - 20) street = "L Street";
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

	class GoldenSpike { // the golden spike is the latitude and longitude of the location on the playa where the man is constructed.
		// first three are same as in PlayaPoint - but no precision is associated.
		String datum;       // name of datum - each year the datum changes with golden spike
		String latlon;      // latitude longitude string, formatted to 5 decimal places, separated by comma
		double lat;         // latitude of golden spike
		double lon;         // longitude of golden spike
		double tncad;         // true north clock angle, degrees, from golden spike - defines relation between playa clock angle (12:00 reference) and True North
		double declination; // compass declination from true north
		double mpdlat;      // meters per degree latitude
		double mpdlon;      // meters per degree longitude
		double fpdlat;      // feet per degree latitude
		double fpdlon;      // feet per degree longitude

		public GoldenSpike(double tlat, double tlon, double ttncad, String tname) {   // constructor
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

	class PlayaNavigate {                // navigate from point here to point there
		String tnbearbdf;           // string true north bearing in degrees and distance in feet
		String compassbdf;          // compass bearing, rose string in 45 degree steps - eg NW - and distance in feet, declination corrected.
		String rose;                // compass rose abbreviation - eg NW
		double compassBearing;      // the direction to navigate by compass magnetic field
		double trueNorthBearing;    // true north bearing - the direction to navigate (declination corrected)
		double compassHeading;      // the direction the handset is pointed by compass sensor magnetic field
		double trueNorthHeading;    // the direction the handset is pointed with respect to true north (declination corrected)
		double navdm;               // navigation (here to there) destingation distance, meters
		double navdf;               // navigation (here to there) destination distance, feet

		public PlayaNavigate() {
			this.update();
		}

		void updatelocation() {

		}

		void update() {
			double dxf;     // east-west navigation vector in feet
			double dyf;     // north-south navigation vector in feet
			double ucad;    // unit circle angle, degrees

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



}
