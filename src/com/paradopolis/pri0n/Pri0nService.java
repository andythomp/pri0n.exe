package com.paradopolis.pri0n;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.paradopolis.global.LoggingManager;
import com.paradopolis.global.WakeManager;
import com.paradopolis.global.WifiScanner;
import com.paradopolis.global.WifiStateReceiver;

public class Pri0nService extends IntentService implements 	LocationListener, 
															WifiScanner.Listener{

	public Pri0nService(){
		super("Pri0nService");
	}
	
	
	public Pri0nService(String name) {
		super(name);
	}

	public static final String ACTION_NEW_NETWORKS = "new_networks";
	
	public static final String TOTAL_NETWORKS = "tNetworks";
	public static final String NEW_NETWORKS = "newNetworks";
	public static final String BUNDLE = "bundle";
	

	private LocationManager locationManager;
	private String provider;
	private Pri0nBinder binder = new Pri0nBinder();
	private WifiScanner wifiScanner;
	private WakeManager wakeManager;
	private WifiStateReceiver wifiStateReceiver;
	private IntentFilter wifiWakeFilter, wifiResultsFilter, wifiStateFilter;
	private int scanDelay = 1000;
		
	
	  // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class Pri0nBinder extends Binder {
        Pri0nService getService() {
            return Pri0nService.this;
        }
    }
	
	@Override
	public void onCreate(){
		super.onCreate();
		LoggingManager.logLn(this, "On Create!");
		
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle(getString(R.string.app_name));
		notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
		notificationBuilder.setContentText("Pri0n Service");
		notificationBuilder.setOngoing(true);
		
		Intent showIntent = new Intent(this, MainActivity.class);
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, showIntent, 0); 
		notificationBuilder.setContentIntent(contentIntent);
		
		startForeground(NOTIFICATION, notificationBuilder.build());
	}
	
	@Override
	public void onDestroy(){
        LoggingManager.logLn(this, "Service Destroyed");
		if (locationManager != null)
		    locationManager.removeUpdates(this);
		disableWifiScanner();
		unregisterReceiver(wifiStateReceiver);
		wakeManager.cancelIntent(WifiScanner.WIFI_WAKE_INTENT);
        super.onDestroy();
    }
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
		LoggingManager.logLn(this, "On Start!");
		initialize();
		startWifiScanner();
        return START_NOT_STICKY;
    }
	
	public Location getCurrentLocation(){
		return locationManager.getLastKnownLocation(provider);
	}
	
	public void initialize(){
		Pri0nNetworkManager.INSTANCE.loadNetworks(getApplicationContext());
		
		//Initialize Location Manager
		Criteria criteria = new Criteria();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		provider = locationManager.getBestProvider(criteria, false);
		final LocationListener listener = this;
		
		locationManager.requestLocationUpdates(provider, 1000, 10, listener);
		
		//Create our intent filters
	    wifiWakeFilter = new IntentFilter(WifiScanner.WIFI_WAKE_INTENT);
	    wifiResultsFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
	    wifiStateFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    
	    //Register the wifi Scanner
	    wifiScanner = new WifiScanner(this);
	    wifiScanner.registerListener(Integer.toString(this.hashCode()), this);
	    
		//Pri0nLootManager.INSTANCE.setWifiMap(Pri0nNetworkManager.getNetworks());
		
		//Enable the wake manager
	    wakeManager= new WakeManager(this);
	    wakeManager.addRepeatingIntent(this, WifiScanner.WIFI_WAKE_INTENT, scanDelay);
	    
	    //Register the state receiver
	    wifiStateReceiver = new WifiStateReceiver();
	    registerReceiver(wifiStateReceiver, wifiStateFilter);
		    
	}
	 
	public void startWifiScanner(){
		registerReceiver(wifiScanner, wifiWakeFilter);
		registerReceiver(wifiScanner , wifiResultsFilter);
	}
		
	public void disableWifiScanner(){
		unregisterReceiver(wifiScanner);
	}
	
	

	@Override
	public void networksFound(ArrayList<Pri0nNetwork> networks) {
		ArrayList<Pri0nNetwork> newNetworks = Pri0nNetworkManager.INSTANCE.processNetworks(networks);
		Pri0nNetworkManager.INSTANCE.saveData(this);
		Intent intent = new Intent(ACTION_NEW_NETWORKS);
		intent.putParcelableArrayListExtra(TOTAL_NETWORKS, networks);
		intent.putParcelableArrayListExtra(NEW_NETWORKS, newNetworks);
		sendBroadcast(new Intent(ACTION_NEW_NETWORKS));
	}

	
	public List<Pri0nNetwork> getNetworks(){
		return new ArrayList<Pri0nNetwork>(Pri0nNetworkManager.INSTANCE.getNetworks().values());
	}

	@Override
	public void onLocationChanged(Location location) {
		LoggingManager.logLn(this, "On Location Changed!");
	}

	@Override
	public void onProviderDisabled(String provider) {
		LoggingManager.logLn(this, "On Provider Disabled!");
	}

	@Override
	public void onProviderEnabled(String provider) {
		LoggingManager.logLn(this, "On Provider Enabled!");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		/* This is called when the GPS status alters */
	    switch (status) {
	    case LocationProvider.OUT_OF_SERVICE:
	        LoggingManager.logLn(this, "Status Changed: Out of Service");
	        break;
	    case LocationProvider.TEMPORARILY_UNAVAILABLE:
	        LoggingManager.logLn(this,  "Status Changed: Temporarily Unavailable");
	        break;
	    case LocationProvider.AVAILABLE:
	        LoggingManager.logLn(this,"Status Changed: Available");
	        break;
	    }
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}


}
