package com.paradopolis.global;

import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;

public class WifiStateReceiver extends BootReceiver{

	private HashMap<String, Listener> listeners;
	
	public WifiStateReceiver(){

		//Handle Listeners
		listeners = new HashMap<String, Listener>();
	}
	
	public void updateListeners(boolean wifiEnabled){
		for (Iterator<Listener> iterator = listeners.values().iterator(); iterator.hasNext();) {
			Listener listener = (Listener) iterator.next();
			if (listeners == null){
				continue;
			}
			listener.notifyWifiChange(wifiEnabled);
		}
	}

	
	public void registerListener(String key, Listener listener){
		if (listener == null){
			return;
		}
		listeners.put(key,  listener);
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
	}
	
	public interface Listener{
		public void notifyWifiChange(boolean wifiEnabled);
	}

}
