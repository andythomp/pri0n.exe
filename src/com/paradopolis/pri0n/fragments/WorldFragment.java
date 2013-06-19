package com.paradopolis.pri0n.fragments;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.paradopolis.global.LoggingManager;
import com.paradopolis.pri0n.Pri0nNetwork;
import com.paradopolis.pri0n.R;

public class WorldFragment extends Fragment implements  OnClickListener,
														OnCameraChangeListener {
	
	private List<Pri0nNetwork> markers;
	private ArrayList<Pri0nNetwork> multiplayerMarkers;
	private Listener listener;
	private GoogleMap map;
	private SupportMapFragment mapFragment;
	private ToggleButton scanButton, centerButton;
	private boolean centerView, nextCameraChangeIsManual;
	private Handler handler;
	private BitmapDescriptor icon;
	private LocationManager locationManager;
	private String provider;
	private HashMap<String, Marker> players;
	

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.fragment_world, container, false);
	    
	    if (mapFragment == null){
	    	mapFragment = new SupportMapFragment();
	    }
	    getChildFragmentManager().beginTransaction()
	    	.replace(R.id.world_map_container, mapFragment)
	        .commit();
	    if (getChildFragmentManager().executePendingTransactions()){
	    	LoggingManager.logLn(this, "TRUE");
	    }
	    else{
	    	LoggingManager.logLn(this, "false");
	    }

	    final OnCameraChangeListener listener = this;
	    handler = new Handler();
	    handler.post(new Runnable(){

			@Override
			public void run() {
			    map = mapFragment.getMap();
		 	    map.setOnCameraChangeListener(listener);
		 	    map.setMyLocationEnabled(true);
		 	    map.moveCamera(CameraUpdateFactory.zoomTo(18));
		 	    icon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.x_red));
		 	    if (markers != null){
		 	    	updateMap(markers);
		 	    }
		 	    updateLocation(getCurrentLocation());
			}
	    	
	    });
	 	
	 		
	 	players = new HashMap<String, Marker>();
 	        
	    scanButton = (ToggleButton) view.findViewById(R.id.world_scanButton);
	    scanButton.setOnClickListener(this);
	    toggleScanning(true);
	    
	    centerButton = (ToggleButton) view.findViewById(R.id.world_centerButton);
	    centerButton.setOnClickListener(this);
	    toggleCenterView(true);
	    
		return view;
	}
	
	/**
	 * If you look at the implementation of Fragment, you'll see that when moving to the detached state, 
	 * it'll reset its internal state. However, it doesn't reset mChildFragmentManager 
	 * (this is a bug in the current version of the support library). 
	 * This causes it to not re-attach the child fragment manager when the Fragment is reattached, 
	 * causing an exception.
	 * 
	 * @author Some Guy on Stack Overflow - Praise Be Unto Him
	 */
	@Override
	public void onDetach() {
	    super.onDetach();

	    try {
	        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManager.setAccessible(true);
	        childFragmentManager.set(this, null);

	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + Listener.class);
        }
    }
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	
	private void animateCamera(LatLng latLng){
		if (map == null){
			return;
		}
  		map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 1000, new GoogleMap.CancelableCallback() {
	        @Override
	        public void onCancel() {
	            nextCameraChangeIsManual = true;
	        }

	        @Override
	        public void onFinish() {
	            nextCameraChangeIsManual = false;
	        }});
  	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
		if (nextCameraChangeIsManual) {
	        toggleCenterView(false);
	    } else {
	        // The next map move will be caused by user, unless we
	        // do another move programmatically
	        nextCameraChangeIsManual = true;

	         //toggleCenterView(true);
	    }
	}
	
	@Override
	public void onClick(View view) {
		if (view == null){
			return;
		}
		switch (view.getId()) {
        case R.id.world_scanButton:
			toggleScanning(scanButton.isChecked());
            break;
        case R.id.world_centerButton:
			toggleCenterView(centerButton.isChecked());
            break;
		}
       
	}
	
	private Location getCurrentLocation(){
		if (map == null){
			return new Location("");
		}
		return map.getMyLocation();
	}
	
	public void toggleCenterView(boolean enabled){
		centerButton.setChecked(enabled);
		centerView = enabled;
		if (enabled){
			updateLocation(getCurrentLocation());
		}
	}
	
	
	
	public void toggleScanning(boolean enabled){
		scanButton.setChecked(enabled);
		listener.worldEnableWifiScanButtonPressed(enabled);
	}
	
	public void updateLocation(Location location){
  		if (!centerView){
  			return;
  		}
		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			LatLng latLng = new LatLng(lat, lng);
			animateCamera(latLng);
		} 
  	}
	
	public void updateMap(List<Pri0nNetwork> networks) {
		if (networks == null){
			return;
		}
		if (map == null){
			return;
		}
		for (int i = 0; i < networks.size(); i++){
			Pri0nNetwork network = networks.get(i);
			if (network.isUnknownLocation()){
				continue;
			}
			else{
				double latOffSet = (new Random().nextDouble() -.5);
				latOffSet = latOffSet * 0.0005;
				double lngOffSet = (new Random().nextDouble() -.5);
				lngOffSet = lngOffSet * 0.0005;
				map.addMarker(new MarkerOptions()
					.icon(icon)
					.position(new LatLng(network.getLatitude() + latOffSet, network.getLongitude() + lngOffSet))
				    .title(network.getName()));
			}
		}
		
	}
	
	public void setNetworks(List<Pri0nNetwork> networks){
		markers = networks;
	}
	
	public void addNetworks(List<Pri0nNetwork> networks){
		markers.addAll(networks);
		updateMap(networks);
	}
	
	public interface Listener{
		void worldEnableWifiScanButtonPressed(boolean enabled);
	}

	public void updatePlayer(String id, String name,  Double latitude, Double longitude, Bitmap icon2) {
		if (map == null){
			return;
		}
		else{
			if (players.get(id) != null){
				players.get(id).setPosition(new LatLng(latitude, longitude));
				return;
			}
			BitmapDescriptor playerIcon;
			if (icon2 != null){
				playerIcon = BitmapDescriptorFactory.fromBitmap(icon2);
			}
			else{
				playerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
			}
			Marker player = map.addMarker(new MarkerOptions()
								.icon(playerIcon)
								.position(new LatLng(latitude, longitude))
							    .title(name));
			players.put(id, player);
			
		}
	}

}
