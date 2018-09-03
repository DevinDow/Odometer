package devindow.Odometer;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Odometer extends Activity {

    private RunsDbAdapter mDbHelper;

    private LocationManager locationManager;
    private LocationIntentReceiver intentReceiver = new LocationIntentReceiver();
    private static final String LOCATION_CHANGED_ACTION = new String("android.intent.action.LOCATION_CHANGED");     
    private IntentFilter intentFilter = new IntentFilter(LOCATION_CHANGED_ACTION);
	private Intent intent = new Intent(LOCATION_CHANGED_ACTION);

	private Location startLocation;
	private long startTime; 
	private float distanceKM;
	private Location prevLocation;

	
	// onCreate
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
    

        mDbHelper = new RunsDbAdapter(this);
        mDbHelper.open();
        fillData();

        
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        registerReceiver(intentReceiver, intentFilter);
        
        final Button btnStart = (Button) findViewById(R.id.start);
        btnStart.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
    			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    			//startLocation = prevLocation = locationManager.getCurrentLocation("gps");
    			startLocation = prevLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    			distanceKM = 0;
    			startTime = System.currentTimeMillis(); 
    			updateText(startLocation);
        	}
        });
        
        
        final Button btnStop = (Button) findViewById(R.id.stop);
        btnStop.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		if (startLocation == null)
        			return;
        		
        		// Time
        		long currentTime = System.currentTimeMillis();
                long totalSeconds = (currentTime - startTime) / 1000;
                int hours = (int)(totalSeconds / 60 / 60);
                int minutes = (int)((totalSeconds-hours*3600) / 60);
                int seconds = (int)(totalSeconds-hours*3600-minutes*60);
                String time = String.format("%d:%02d:%02d", hours, minutes, seconds); 

               	// Distance
               	String distance = String.format("%.2f m=%.2f km", distanceKM * 0.62137, distanceKM);

        		mDbHelper.createRun(new Date(startTime).toString(), distance, time);
        		fillData();
        		
    			startLocation = prevLocation = null;
    			distanceKM = 0;
        	}
        });
	}
	
	
    // Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean supRetVal = super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "Clear List");
		return supRetVal;
	}
	
	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()) {
			case 0: // Clear List
			{
				mDbHelper.clearAllRuns();
				fillData();
    	    	return true;
			}
		}
		return false;
	}

	
    // Location Changed
    public class LocationIntentReceiver extends IntentReceiver{
    	@Override
    	public void onReceiveIntent(Context context, Intent intent) {
    		if (startLocation == null)
    			return;
    		
			//Location currentLocation = (Location)intent.getExtra("location");
			Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			updateText(currentLocation);
    	}
    }

    private void updateText(Location currentLocation) {
		distanceKM += currentLocation.distanceTo(prevLocation) / 1000;
		prevLocation = currentLocation;
		
		
		// Distance
       	TextView txtDistanceM = (TextView) findViewById(R.id.distanceM);
        txtDistanceM.setText(String.format("%.2f miles", distanceKM * 0.62137));

       	TextView txtDistanceKM = (TextView) findViewById(R.id.distanceKM);
        txtDistanceKM.setText(String.format("%.2f km", distanceKM));

        
		// Time
        long currentTime = System.currentTimeMillis();
        long totalSeconds = (currentTime - startTime) / 1000;
        int hours = (int)(totalSeconds / 60 / 60);
        int minutes = (int)((totalSeconds-hours*3600) / 60);
        int seconds = (int)(totalSeconds-hours*3600-minutes*60);
        
       	TextView txtTime = (TextView) findViewById(R.id.time);
       	txtTime.setText(String.format("%d:%02d:%02d", hours, minutes, seconds)); 

        
        // Instantaneous Speed
		if (currentLocation.hasSpeed()) {
			float speedKm = currentLocation.getSpeed() * 3.6f; // / 1000 * 60 * 60

			TextView txtSpeedM = (TextView) findViewById(R.id.speedM);
			txtSpeedM.setText(String.format("%.0f m/h", speedKm * 0.62137));

			TextView txtSpeedKM = (TextView) findViewById(R.id.speedKM);
			txtSpeedKM.setText(String.format("%.0f km/h", speedKm));
		}
		
		
		// Distance from Start
		double distanceFromStartKM = currentLocation.distanceTo(startLocation) / 1000;
		
       	TextView txtDistanceToStartM = (TextView) findViewById(R.id.distanceToStartM); 
       	txtDistanceToStartM.setText(String.format("%.2f miles", distanceFromStartKM * 0.62137));

       	TextView txtDistanceToStartKM = (TextView) findViewById(R.id.distanceToStartKM);
       	txtDistanceToStartKM.setText(String.format("%.2f km", distanceFromStartKM));

       	
       	// Average Speed
		float averageSpeedKm = distanceKM / totalSeconds * 3600; 

		TextView txtAverageSpeedM = (TextView) findViewById(R.id.averageSpeedM);
		txtAverageSpeedM.setText(String.format("%.0f m/h", averageSpeedKm * 0.62137));

		TextView txtAverageSpeedKM = (TextView) findViewById(R.id.averageSpeedKM);
		txtAverageSpeedKM.setText(String.format("%.0f km/h", averageSpeedKm));
       	
       	
       	// Altitude
        if (currentLocation.hasAltitude()) {
        	double altitudeM = currentLocation.getAltitude();
        	
			TextView txtAltitudeM = (TextView) findViewById(R.id.altitudeM);
			txtAltitudeM.setText(String.format("%f m", altitudeM));

			TextView txtAltitudeFT = (TextView) findViewById(R.id.altitudeFT);
			txtAltitudeFT.setText(String.format("%f m", altitudeM * 3.2808399)); 
		}
	}
    
    private void fillData() {
    	// Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchAllRuns();
        startManagingCursor(c);
        ListAdapter adp =
            new SimpleCursorAdapter(this, 
            		R.layout.runs_row, 
            		c, 
		new String[] { RunsDbAdapter.KEY_DATE, RunsDbAdapter.KEY_DISTANCE, RunsDbAdapter.KEY_TIME }, 
		new int[] { R.id.date, R.id.distance, R.id.time });
        
        final ListView lst = (ListView) findViewById(R.id.runs);
        lst.setAdapter(adp);
    }
    

    // onResume, onFreeze, & onDestroy
    @Override 
    public void onResume() { 
    	super.onResume(); 
		String locationProvider = locationManager.getBestProvider(new Criteria(), true);
		locationManager.requestLocationUpdates(locationProvider, 0, 0, intent);
    }
    
    public void onFreeze(Bundle icicle) { 
    	locationManager.removeUpdates(intent); 
    	super.onFreeze(icicle); 
    } 

    @Override 
    public void onDestroy() { 
    	unregisterReceiver(intentReceiver); 
    	super.onDestroy(); 
    } 
}