package com.paradopolis.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.paradopolis.errors.WifiDisabledError;
import com.paradopolis.pri0n.Pri0nNetwork;

public class WifiScanner  extends BroadcastReceiver{

	public static final String WIFI_WAKE_INTENT = "wifi_wake_intent";
	
	private HashMap<String, Listener> listeners;
    private WakeLock wakeLock;
    private WifiLock wifiLock;
    private WifiManager wifiManager;
	private LocationManager locationManager;
	private String provider;
    private boolean scanning;
    
    
    /**
     * Constructor for a WifiScanner
     * @param listener - Call back listener, interface defined in class
     * @param context - Application Context
     */
    public WifiScanner(Context context){
		
		//Handle Listeners
		listeners = new HashMap<String, Listener>();

		//Handle Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
        //Handle Locks
		wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).
                createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY , this.getClass().getSimpleName());
		wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getSimpleName());
		
		//Handle Wifi Manager
	    wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
    
    /**
     * Attempts to acquire the wake and wifi locks
     */
    public void lock() {
        try {
        	wakeLock.acquire();
            wifiLock.acquire();
        } catch(Exception e) {
        	LoggingManager.logErr(this,"Error getting Lock: "+e.getMessage());
        }
    }
    
    /**
     * Releases the held power and wifi locks.
     */
    public void release() {
        if(wakeLock.isHeld())
            wakeLock.release();
        if(wifiLock.isHeld())
            wifiLock.release();
    }


    
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Make sure the action is real.
		String action = intent.getAction();
		if (action == null){
			return;
		}
		
		//We have to wake up and make a scan
		if (action.equals(WIFI_WAKE_INTENT)){
			try{
				startScan();
				return;
			}
			catch(WifiDisabledError e){
				MediaManager.INSTANCE.playAsset(context, "test.m4a");
			}
		}
		
		
		//We got a list of Wifi Networks
		else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
			Criteria criteria = new Criteria();
			provider = locationManager.getBestProvider(criteria, false);
			Location location = locationManager.getLastKnownLocation(provider);
			double lat = 0;
			double lng = 0;
			// Initialize the location fields
			if (location != null) {
				//LoggingManager.logLn(this, "Provider " + provider + " has been selected.");
				lat = location.getLatitude();
				lng = location.getLongitude();
			} else {
				
			}
			
	        List<ScanResult> tempList = wifiManager.getScanResults();
	        ArrayList<Pri0nNetwork> networkList = new ArrayList<Pri0nNetwork>();
	        
	        for(int i = 0; i < tempList.size(); i++){
	        	Pri0nNetwork pri0nNet = new Pri0nNetwork(tempList.get(i));
	        	pri0nNet.setLatidue(lat);
	        	pri0nNet.setLongitude(lng);
	        	networkList.add(pri0nNet);
	        }
	        release();
	        scanning = false;
	        updateListeners(networkList);
		}
		
	}
	
	
	
	public void updateListeners(ArrayList<Pri0nNetwork> networks){
		for (Iterator<Listener> iterator = listeners.values().iterator(); iterator.hasNext();) {
			Listener listener = (Listener) iterator.next();
			if (listeners == null){
				continue;
			}
			listener.networksFound(networks);
		}
	}

	
	public void registerListener(String key, Listener listener){
		if (listener == null){
			return;
		}
		listeners.put(key,  listener);
	}
	
	public void startScan() throws WifiDisabledError{
		if (wifiManager.isWifiEnabled()){
			lock();
			wifiManager.startScan();
			scanning = true;
		}
		else{
			throw new WifiDisabledError();
		}
	}

	
	public boolean isScanning() {
		return scanning;
	}
	
	

	

	public interface Listener{
		public void networksFound(ArrayList<Pri0nNetwork> networks);
	}
}
