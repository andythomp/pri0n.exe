package com.paradopolis.global;

import java.util.Calendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * The ScanTimer class will tell it's listeners to activate when ever the alarm triggers.
 * This class is used to save battery life, so that the device can sleep, and only wake up when
 * required.
 * 
 * This class requires a callback, and so should not be used as a manifest receiver.
 * 
 * @author Andrew Thompson
 */
public class WakeManager{
	public static String CANCEL = "CANCEL";
    
	private HashMap<String, PendingIntent> intents;
    private  AlarmManager alarmMgr;

    public WakeManager(Context context){
		alarmMgr= (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		intents = new HashMap<String, PendingIntent>();
    }

    /**
     * Adds an intent to the alarm manager that will trigger when an amount of elapsed time passes equal
     * to the given delay. 
     * @param context
     * @param intentString
     * @param delay
     */
    public void addRepeatingIntent(Context context,String intentString, int delay){
    	Intent intent = new Intent(intentString);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    
	    Calendar time = Calendar.getInstance();
	    time.setTimeInMillis(System.currentTimeMillis());
	    time.add(Calendar.MILLISECOND, delay);
	    
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), delay, pendingIntent);
		intents.put(intentString, pendingIntent);
    }
     
    /**
     * Cancels an intent from starting again. Requires the original intent string used to
     * add in the first place.
     * 
     * @param intentString 
     */
    public void cancelIntent(String intentString){
    	if (!intents.containsKey(intentString)){
    		return;
    	}
    	alarmMgr.cancel(intents.get(intentString));
    	intents.remove(intentString);
    }
    
}