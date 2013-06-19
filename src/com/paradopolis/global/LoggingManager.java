package com.paradopolis.global;

import android.util.Log;

/**
 * Static class that is used to log messages. It is easier than using the Log.i interface,
 * and also gives some extra control to the developer. If the developer would like to disable
 * output programatically, that can be added.
 * 
 * Right now this class does not do that, but it makes it easier to change later if the feature
 * is desired. 
 * 
 * Common calls are generally log*(this, [MESSAGE]);
 * 
 * @author Andrew Thompson
 *
 */
public class LoggingManager {
	
	/**
	 * Logs an error to the Android LogCat. This function is necessary for static classes.
	 * @param logger - The class of the logger will be displayed as the message bearer
	 * @param message - Message to be logged
	 */
	public static void logErr(Class<?> logger, String message){
		Log.e(logger.getSimpleName(), message);
	}
	
	/**
	 * Logs an error to the Android LogCat.
	 * @param logger - The class of the logger will be displayed as the message bearer
	 * @param message - Message to be logged
	 */
	public static void logErr(Object logger, String message){
		Log.e(logger.getClass().getSimpleName(), message);
	}
	
	/**
	 * Logs a line to the Android LogCat. This function is necessary for static classes.
	 * @param logger - The class of the logger will be displayed as the message bearer
	 * @param message - Message to be logged
	 */
	public static void logLn(Class<?> logger, String message){
		Log.i(logger.getSimpleName(), message);
	}
	
	/**
	 * Logs a line to the Android LogCat.
	 * @param logger - The class of the logger will be displayed as the message bearer
	 * @param message - Message to be logged
	 */
	public static void logLn(Object logger, String message){
		Log.i(logger.getClass().getSimpleName(), message);
	}

}
