package com.paradopolis.pri0n;

import java.util.ArrayList;
import java.util.HashMap;

public enum Pri0nLootManager {
	INSTANCE;
	

    private HashMap<String, Pri0nNetwork> wifiMap;
    
    private Pri0nLootManager(){
		wifiMap = new HashMap<String, Pri0nNetwork>();
    }
    

   
    
    public HashMap<String, Pri0nNetwork> getWifiMap() {
		return wifiMap;
	}

	public void setWifiMap(HashMap<String, Pri0nNetwork> wifiList) {
		this.wifiMap = wifiList;
	}
	
	public ArrayList<Pri0nNetwork> getWifiList() {
		return new ArrayList<Pri0nNetwork>(wifiMap.values());
	}

}
