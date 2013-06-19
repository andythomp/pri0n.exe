package com.paradopolis.pri0n;

import java.io.Serializable;

public class Pri0nPacket implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1055726989157732352L;
	
	public static final char TYPE_UNKNOWN = 'U';
	public static final char TYPE_LOCATION = 'L';
	public static final char TYPE_NETWORK = 'N';
	public static final char TYPE_START = 'S';
	
	
	public Pri0nPacket(){
		this(TYPE_UNKNOWN);
	}
	
	public Pri0nPacket(char type){
		this.type = type;
		name = "";
		address = "";
		longitude = 0d;
		latitude = 0d;
	}
	
	public Character type;
	public String name;
	public String address;
	public Double longitude, latitude;

}
