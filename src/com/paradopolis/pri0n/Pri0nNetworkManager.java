package com.paradopolis.pri0n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.paradopolis.global.LoggingManager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

public enum Pri0nNetworkManager {
	INSTANCE;

	private boolean initialized = false;
	private int saveCounter = 0;
	private HashMap<String, Pri0nNetwork> wifiMap;
	
	//CONSTANTS
	private int NUM_SAVE_CYCLES = 20;
	private String FILE_NAME = "data.json";
	private String FILE_DIR = "/pri0n/data/";
	
	
	public boolean loadNetworks(Context context){
		long time = System.currentTimeMillis();
    	if (initialized){
			System.out.println(Pri0nNetworkManager.class.getSimpleName() + ": Already initialized.");
			return false;
		}
		
		if (!isExternalReadable()){
			System.out.println(Pri0nNetworkManager.class.getSimpleName() + "Error: Unable to read from external storage.");
			return false;
		}
		
		wifiMap = new HashMap<String, Pri0nNetwork> ();
		File dir = new File(context.getExternalFilesDir(null), FILE_DIR);
		File file = new File(dir, FILE_NAME);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuilder text = new StringBuilder();
			while (reader.ready()){
				text = text.append(reader.readLine());
			}
			
			JSONArray tempArray = new JSONArray(text.toString());
			for (int i = 0; i < tempArray.length(); i++){
				JSONObject tempObject = tempArray.getJSONObject(i);
				Pri0nNetwork tempNetwork = new Pri0nNetwork(tempObject);
				wifiMap.put(tempNetwork.getAddress(), tempNetwork);
			}
			reader.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		
		LoggingManager.logLn(Pri0nNetworkManager.class, "Initialization complete. Took:" + (System.currentTimeMillis() - time) + "ms to complete.");
		LoggingManager.logLn(Pri0nNetworkManager.class, "Loaded " + wifiMap.size() + " networks into memory.");
		initialized = true;
		return true;
    }
    
    public boolean isExternalReadable(){
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    return true;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    // to know is we can neither read nor write
		   	return false;
		}
	} 
    
    public boolean isExternalWritable(){
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    return false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    // to know is we can neither read nor write
		   	return false;
		}
	}
    
    
    
    public boolean saveData(Context context){

    	
    	//REMOVE THIS LATER
    	if (saveCounter < NUM_SAVE_CYCLES){
    		saveCounter++;
    		return true;
    	}
    	saveCounter = 0;
    	SaveTask task = new SaveTask(context);
    	task.execute();
		
		
		return true;
    }
    
    public HashMap<String, Pri0nNetwork> getNetworks(){
		return wifiMap;
    }
    
    public void updateNetwork(Pri0nNetwork oldNetwork, Pri0nNetwork newNetwork){
    	oldNetwork.updateLocation(newNetwork.getLatitude(), newNetwork.getLongitude());
		oldNetwork.found();
    }
    
	
    public ArrayList<Pri0nNetwork> processNetworks(List<Pri0nNetwork> networks){
    	ArrayList<Pri0nNetwork> newNetworks = new ArrayList<Pri0nNetwork>();
    	for (int i = 0; i < networks.size(); i++){
    		Pri0nNetwork newNetwork = networks.get(i);
    		if (newNetwork.isUnknownLocation()){
    			continue;
    		}
    		Pri0nNetwork oldNetwork = wifiMap.get(networks.get(i).getAddress());
    		//If we haven't seen the network before
    		if (oldNetwork == null){
    			//Add the network with no questions asked
    			wifiMap.put(newNetwork.getAddress(), newNetwork);
    			newNetworks.add(newNetwork);
    			continue;
    		}
    		//Else we've seen the network before
    		else{
    			//If we never got a good location to begin with
    			if (oldNetwork.isUnknownLocation()){
    					//Update the location
	    				oldNetwork.updateLocation(newNetwork.getLatitude(), newNetwork.getLongitude());
	    				oldNetwork.found();
    			}
    			//If the old network has a stronger power level at the current location, then this is a more accurate
    			//prediction of where the network is
    			else if (oldNetwork.getLevel() < newNetwork.getLevel()){
    				//Update the location
    				oldNetwork.updateLocation(newNetwork.getLatitude(), newNetwork.getLongitude());
    				oldNetwork.found();
    			}
    			//If the new network is significantly far away from the old network, we may want to consider
    			//updating the old location as it may have moved.
    			else if (oldNetwork.hasSignificantDistanceFrom(newNetwork)){
    				//Update the location
    				oldNetwork.updateLocation(newNetwork.getLatitude(), newNetwork.getLongitude());
    				oldNetwork.found();
    			}
    			//Otherwise, the location has no need for an update.
    			else{
    				
    			}
    		}
    	}
    	return newNetworks;
    }
    
    private class SaveTask extends AsyncTask<Void, Void, Boolean> {

    	private Context context;
    	
    	public SaveTask(Context context){
    		this.context = context;
    	}
    	
		@Override
		protected Boolean doInBackground(Void... params) {
			long time = System.currentTimeMillis();
	    	if (!isExternalWritable()){
				System.out.println("Error: Unable to write to external storage.");
				return false;
			}
			try {
				int savedNetworks = 0;
				File dir = new File(context.getExternalFilesDir(null), FILE_DIR);
				dir.mkdirs();
				System.out.println("WRITING TO: " + dir.getAbsolutePath());
				File file = new File(dir, FILE_NAME);
				file.createNewFile();
				
				//Build the json file in a string.
				StringBuilder outputString = new StringBuilder();
				outputString.append("[");
				for (Iterator<Pri0nNetwork> iterator = wifiMap.values().iterator(); iterator.hasNext();) {
					Pri0nNetwork network = (Pri0nNetwork) iterator.next();
					try {
						if (savedNetworks != 0){
							outputString.append(",\n");
						}
						outputString.append(network.toJson().toString());
						savedNetworks++;
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				outputString.append("]");
				
				//Write the string to a file
				FileOutputStream outStream = new FileOutputStream(file);
				outStream.write(outputString.toString().getBytes());
				outStream.close();
				LoggingManager.logLn(Pri0nNetworkManager.class, "Number of networks saved: " + savedNetworks);
				LoggingManager.logLn(Pri0nNetworkManager.class, "Saving complete. Took:" + (System.currentTimeMillis() - time) + "ms to complete.");
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
			return true;
		}

      }
    
    public interface Listener{
    	
    }
    
    
}
