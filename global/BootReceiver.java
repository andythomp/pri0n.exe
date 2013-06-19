package com.paradopolis.global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This boot receiver does literally nothing, but when it is triggered the application is loaded.
 * As long as the application loads, that is all that the receiver requires.
 * @author Andrew Thompson
 *
 */
public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		
	}

}
