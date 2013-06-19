package com.paradopolis.global;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;

public enum MediaManager {
	INSTANCE;
	
	private MediaPlayer player, tester;
	private File currentlyPlaying;
	
	private MediaManager(){
		player = new MediaPlayer();
		tester = new MediaPlayer();
		currentlyPlaying = null;
	}
	
	public void playAsset(Context context, String file){
		try {
		    AssetFileDescriptor afd = context.getAssets().openFd(file);
		    player.reset();
		    player.setDataSource(
		            afd.getFileDescriptor(),
		            afd.getStartOffset(),
		            afd.getLength()
		        );
		    afd.close();
		    player.prepareAsync();
		    player.setOnPreparedListener(new OnPreparedListener() {
		        public void onPrepared(MediaPlayer mp) {
		            mp.start();
		        }
		    });
		} catch (IllegalArgumentException e) {
		    e.printStackTrace();
		} catch (IllegalStateException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public void playFile(Context context, File file){
		//Now we play the file if its not a directory anyway
		if (file == null){
			LoggingManager.logErr(this, "Attempted to play a null file.");
			return;
		}
		if (file.isDirectory()){
			LoggingManager.logErr(this, "Attempted to play a directory.");
			return;
		}
		//Attempt to play the file as a ringtone.
		
		//If the player is already playing a file
		if (player.isPlaying()){
			
		}
		//Otherwise we arent already playing a file so just play it.
		else{
			startPlayer(context, file);
		}
	}
	
	
	/**
	 * Uses a tester media player to test if the file passed is playable.
	 * @param file - File to test if playable or not
	 * @return - Whether or not it is playable
	 */
	public boolean isPlayable(Context context, File file){
		try {
			Uri uri = Uri.fromFile(file);
			tester.setAudioStreamType(AudioManager.STREAM_MUSIC);
			tester.setDataSource(context, uri);
			tester.reset();
			return true;
		} catch (Exception e){
			LoggingManager.logErr(this, "Invalid File: " + file.getName());
			return false;
		}
	}
	
	/**
	 * Checks to see if a given file name is currently being played on the media player.
	 * @param name - Name of the file to check
	 * @return - True if it is, False if its not
	 */
	public boolean isFilePlaying(String name){
		if (currentlyPlaying == null){
			return false;
		}
		else if (currentlyPlaying == null){
			return false;
		}
		if (!player.isPlaying()){
			return false;
		}
		else if (currentlyPlaying.getName().equals(name)){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Private function - Takes a given file and loads it into the media player. 
	 * @param file
	 */
	private synchronized void startPlayer(Context context, File file){
		Uri uri = Uri.fromFile(file);
		try {
			player.reset();
		    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(context, uri);
			player.prepare();
			player.start();
			currentlyPlaying = file;
		} catch (Exception e){
			player.reset();
			e.printStackTrace();
		}
	}
	
	/**
	 * Private function to stop the player. Stops and resets the media player 
	 */
	private synchronized void stopPlayer(){
		if (player.isPlaying()){
			player.stop();
			player.reset();
		}
	}
	
	/**
	 * Publicly available function to stop the media player. 
	 * @param 
	 */
	public void stopPlaying(){
		stopPlayer();
	}
	
	

}
