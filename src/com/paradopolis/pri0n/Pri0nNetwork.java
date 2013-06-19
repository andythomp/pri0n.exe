package com.paradopolis.pri0n;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

public class Pri0nNetwork implements Parcelable{

	private static final String NAME = "name";
	private static final String ADDRESS = "address";
	private static final String TIMES_FOUND = "timesfound";
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";
	private static final String CAPABILITIES = "capabilities";
	private static final String FREQUENCY = "frequency";
	private static final String LEVEL = "level";
	private static final String TIME_STAMP = "time_stamp";
	
	
	public static final double UNKNOWN_LOCATION = Double.POSITIVE_INFINITY;
	
	
	
	
	private String address, capabilities, name;
	private Integer timesFound, frequency, level;
	private Double longitude, latitude;
	private Long timeStamp;
	
	public Pri0nNetwork(ScanResult scanResult){
		this.setName(scanResult.SSID);
		this.address = scanResult.BSSID;
		this.frequency = scanResult.frequency;
		this.capabilities = scanResult.capabilities;
		this.level = scanResult.level;
		this.timesFound = 1;
		this.longitude = UNKNOWN_LOCATION;
		this.latitude = UNKNOWN_LOCATION;
		this.timeStamp = (new java.util.Date()).getTime();
	}
	
	
	public Pri0nNetwork(JSONObject tempObject){
		try {setName(tempObject.getString(NAME));}
		catch (JSONException e) {e.printStackTrace();}
		
		try {address = tempObject.getString(ADDRESS);}
		catch (JSONException e) {e.printStackTrace();}
		
		try {timesFound = tempObject.getInt(TIMES_FOUND);}
		catch (JSONException e) {e.printStackTrace();}
		
		try {longitude = tempObject.getDouble(LONGITUDE);}
		catch (JSONException e) {e.printStackTrace();}
		
		try {latitude = tempObject.getDouble(LATITUDE);}
		catch (JSONException e) {e.printStackTrace();}
		
		try {capabilities = tempObject.getString(CAPABILITIES);}
		catch (JSONException e) {e.printStackTrace();}
		
		try {frequency = tempObject.getInt(TIMES_FOUND);}
		catch (JSONException e) {e.printStackTrace();}
		
		try {level = tempObject.getInt(LEVEL);}
		catch (JSONException e) {e.printStackTrace();}
		
		try {timeStamp = tempObject.getLong(TIME_STAMP);}
		catch (JSONException e) {e.printStackTrace();}
	}
	
	public Pri0nNetwork(Parcel in) {
		name = in.readString();
		address = in.readString();
		capabilities = in.readString();
		timesFound = in.readInt();
		longitude = in.readDouble();
		latitude = in.readDouble();
		frequency = in.readInt();
		level = in.readInt();
		timeStamp = in.readLong();
	}


	public String getAddress(){
		return address;
	}
	
	public void found(){
		timesFound = timesFound + 1;
	}
	
	public void setLongitude(double lng){
		this.longitude = lng;
	}
	
	public void setLatidue(double latitude){
		this.latitude = latitude;
	}
	
	
	
	public String getCapabilities() {
		return capabilities;
	}


	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
	}
	

	public Integer getFrequency() {
		return frequency;
	}


	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}


	public Integer getLevel() {
		return level;
	}


	public void setLevel(Integer level) {
		this.level = level;
	}


	public Long getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	

	public Double getLongitude() {
		return longitude;
	}


	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}


	public Double getLatitude() {
		return latitude;
	}


	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	
	public String toString(){
		return "Address: " + address + " found " + timesFound + " times.\n";
	}
	

	public JSONObject toJson() throws JSONException{
		JSONObject temp = new JSONObject();
		temp.put(NAME, name);
		temp.put(ADDRESS, address);
		temp.put(TIMES_FOUND, timesFound.intValue());
		temp.put(LONGITUDE, longitude.doubleValue());
		temp.put(LATITUDE, latitude.doubleValue());
		temp.put(CAPABILITIES, capabilities);
		temp.put(FREQUENCY, frequency.intValue());
		temp.put(LEVEL, level.intValue());
		temp.put(TIME_STAMP, timeStamp.longValue());
		return temp;
	}


	public boolean isUnknownLocation() {
		if (latitude.isInfinite() || longitude.isInfinite()){
			return true;
		}
		else if (latitude == 0 || longitude == 0){
			return true;
		}
		else{
			return false;
		}
	}


	public void updateLocation(Double newLatitude, Double newLongitude) {
		setLatidue(newLatitude);
		setLongitude(newLongitude);
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public boolean hasSignificantDistanceFrom(Pri0nNetwork newNetwork) {
		
		return false;
	}


	@Override
	public int describeContents() {
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(address);
		dest.writeString(capabilities);
		dest.writeInt(timesFound);
		dest.writeDouble(longitude);
		dest.writeDouble(latitude);
		dest.writeInt(frequency);
		dest.writeInt(level);
		dest.writeLong(timeStamp);
	}


	 public static final Parcelable.Creator<Pri0nNetwork> CREATOR
		     = new Parcelable.Creator<Pri0nNetwork>() {
		 public Pri0nNetwork createFromParcel(Parcel in) {
		     return new Pri0nNetwork(in);
		 }
		
		 public Pri0nNetwork[] newArray(int size) {
		     return new Pri0nNetwork[size];
		 }
	};
	
	
}
