package com.paradopolis.pri0n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.paradopolis.global.LoggingManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Pri0nServiceReceiver extends BroadcastReceiver{

	
	private HashMap<String, Listener> listeners;
	
	public Pri0nServiceReceiver(){
		super();
		listeners = new HashMap<String, Listener>();
	}
	
	public void registerListener(String key, Listener listener){
		listeners.put(key, listener);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction() == null){
			return;
		}
		
		ArrayList<Pri0nNetwork> newNetworks = intent.getParcelableArrayListExtra(Pri0nService.NEW_NETWORKS);
		ArrayList<Pri0nNetwork> totalNetworks = intent.getParcelableArrayListExtra(Pri0nService.TOTAL_NETWORKS);
		
		if (intent.getAction().equals(Pri0nService.ACTION_NEW_NETWORKS)){
			for (Iterator<Listener> iterator = listeners.values().iterator(); iterator.hasNext();) {
				Listener listener = (Listener) iterator.next();
				listener.pri0nNetworksReceived(totalNetworks, newNetworks);
			}
		}/*
		else if (intent.getAction().equals(Pri0nService.ACTION_REQUEST_NETWORKS)){
			for (Iterator<Listener> iterator = listeners.values().iterator(); iterator.hasNext();) {
				Listener listener = (Listener) iterator.next();
				listener.pri0nNetworksRequested(totalNetworks);
			}
		}
		*/
		else{
			LoggingManager.logErr(this, "Unknown Intent Received");
		}
	}

	
	public interface Listener{
		public void pri0nNetworksReceived(ArrayList<Pri0nNetwork> totalNetworks, ArrayList<Pri0nNetwork> newNetworks);
		public void pri0nNetworksRequested(ArrayList<Pri0nNetwork> networks);
	}
}
