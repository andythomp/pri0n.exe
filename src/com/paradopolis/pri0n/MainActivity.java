package com.paradopolis.pri0n;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.paradopolis.global.LoggingManager;
import com.paradopolis.global.Serializer;
import com.paradopolis.pri0n.fragments.HomeFragment;
import com.paradopolis.pri0n.fragments.MultiplayerFragment;
import com.paradopolis.pri0n.fragments.SplashFragment;
import com.paradopolis.pri0n.fragments.StatsFragment;
import com.paradopolis.pri0n.fragments.WorldFragment;

public class MainActivity extends BaseGameActivity  implements 	SplashFragment.Listener,
																HomeFragment.Listener,
																StatsFragment.Listener,
																WorldFragment.Listener,
																MultiplayerFragment.Listener,
																Pri0nServiceReceiver.Listener, 
																RoomUpdateListener, 
																RealTimeMessageReceivedListener, 
																RoomStatusUpdateListener, 
																OnInvitationReceivedListener{
	private SplashFragment splashFragment;
	private HomeFragment homeFragment;
	private StatsFragment statsFragment;
	private WorldFragment worldFragment;
	private MultiplayerFragment multiplayerFragment;
	private boolean serviceConnected;
	private Pri0nService service;
	private Pri0nServiceReceiver serviceReceiver;

	private ProgressDialog loadingDialog;
	
	// Request codes for the UIs that we show with startActivityForResult:
	private final static int RC_UNUSED = 5001;
	private final static int RC_SELECT_PLAYERS = 10000;
	private final static int RC_INVITATION_INBOX = 10001;
	private final static int RC_WAITING_ROOM = 10002;
	
	
    //MULTIPLAYER VARIABLES
    
	
	private HashMap<String, Bitmap> playerIcons;
    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;
    
    // The participants in the currently active game
    private ArrayList<Participant> mParticipants = null;
    
 // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;
    
 // My participant ID in the currently active game
    private String mMyId = null;
    
    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    //private String mIncomingInvitationId = null;
   
    
 // flag indicating whether we're dismissing the waiting room because the
    // game is starting
    private boolean mWaitRoomDismissedFromCode = false;
    
 // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    final static int GAME_DURATION = 20; // game duration, seconds.
    int mScore = 0; // user's current score
    
    private ArrayList<Pri0nNetwork> multiplayerNetworks;
    
    
	private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
        	LoggingManager.logLn(this, "Connected to service.");
            service = ((Pri0nService.Pri0nBinder) binder).getService();
            serviceConnected = true;
        }
        
        /**
         * Connection dropped.
         */
        @Override
        public void onServiceDisconnected(ComponentName className) {
            LoggingManager.logLn(this, "Disconnected from service.");
            service = null;
            serviceConnected = false;
        }
};  
	
	
	
	  @Override
	public void homeBuildButtonClicked() {
		  if (!isMyServiceRunning()){
			  startPri0nService();
			  bindService(new Intent(this, Pri0nService.class), serviceConnection, Context.BIND_AUTO_CREATE);
			  Toast.makeText(this, "Started Service", Toast.LENGTH_SHORT).show();
			  return;
		  }
		  else{
			  if (serviceConnected){
				  	
			    	unbindService(serviceConnection);
			    	serviceConnected = false;
			    	stopService(new Intent(this, Pri0nService.class));
			    }
			  else{
			    	stopService(new Intent(this, Pri0nService.class));
			  }
			  Toast.makeText(this, "Stopped Service", Toast.LENGTH_SHORT).show();
		  }
		 
		
		  
	}
	
	@Override
	public void homeMultiplayerButtonClicked() {
		switchToFragment(multiplayerFragment);
	}
	
	
	 // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
        	LoggingManager.logLn(this, "*** select players UI cancelled, " + response);
            switchToFragment(homeFragment);
            return;
        }

        LoggingManager.logLn(this, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
        LoggingManager.logLn(this, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            LoggingManager.logLn(this, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        LoggingManager.logLn(this, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        loadingDialog.show();
        keepScreenOn();
        resetGameVars();
        getGamesClient().createRoom(rtmConfigBuilder.build());
        LoggingManager.logLn(this, "Room created, waiting for it to be ready...");
    }
    
 // Broadcast a message indicating that we're starting to play. Everyone else
    // will react
    // by dismissing their waiting room UIs and starting to play too.
    void broadcastStart() {
        if (!mMultiplayer)
            return; // playing single-player mode

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Pri0nPacket message = new Pri0nPacket(Pri0nPacket.TYPE_START);
        byte[] bytes = null;
		try {
			bytes = Serializer.serialize(message);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        

		LoggingManager.logLn(this, "Size of location message: " + bytes.length);
		
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            getGamesClient().sendReliableRealTimeMessage(null, bytes, mRoomId,
                    p.getParticipantId());
        }
        
    }
    
    //Broadcast my location to everybody else.
    void broadcastLocation(){
    	if (!mMultiplayer)
            return; // playing single-player mode
        //CHANGE TO BROADCAST NETWORKS

        // First byte in message indicates whether it's a final score or not
    	Pri0nPacket message = new Pri0nPacket(Pri0nPacket.TYPE_LOCATION);
    	Location location = service.getCurrentLocation();
    	message.latitude = location.getLatitude();
    	message.longitude = location.getLongitude();
    	
    	byte[] bytes = null;
		try {
			bytes = Serializer.serialize(message);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		LoggingManager.logLn(this, "Size of location message: " + bytes.length);
		
        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            getGamesClient().sendUnreliableRealTimeMessage(bytes, mRoomId, p.getParticipantId());
        }
    }
    
    // Broadcast my score to everybody else.
    void broadcastNetworks() {
        if (!mMultiplayer)
            return; // playing single-player mode
        //CHANGE TO BROADCAST NETWORKS

        Pri0nPacket message = new Pri0nPacket(Pri0nPacket.TYPE_NETWORK);


    	byte[] bytes = null;
		try {
			bytes = Serializer.serialize(message);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try{
	        // Send to every other participant.
	        for (Participant p : mParticipants) {
	            if (p.getParticipantId().equals(mMyId))
	                continue;
	            if (p.getStatus() != Participant.STATUS_JOINED)
	                continue;
	            getGamesClient().sendUnreliableRealTimeMessage(bytes, mRoomId, p.getParticipantId());
	        }
		}
		catch(Exception e){
			e.printStackTrace();
		}
        
    }
    
    private class LoadBitmapTask extends AsyncTask<Void, Void, Boolean> {

    	private String id;
    	private HashMap<String, Bitmap> bitmapMap;
    	private String url;
    	
    	public LoadBitmapTask(String id, String url,  HashMap<String, Bitmap> bitmapMap){
    		this.id = id;
    		this.bitmapMap = bitmapMap;
    		this.url = url;
    	}
    	
		@Override
		protected Boolean doInBackground(Void... params) {
			Bitmap bm = null;
	        InputStream is = null;
	        BufferedInputStream bis = null;
	        try 
	        {
	            URLConnection conn = new URL(url).openConnection();
	            conn.connect();
	            is = conn.getInputStream();
	            bis = new BufferedInputStream(is, 8192);
	            bm = BitmapFactory.decodeStream(bis);
	            
	        }
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	        }
	        finally {
	            if (bis != null){
	                try{
	                    bis.close();
	                }
	                catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	            if (is != null) {
	                try {
	                    is.close();
	                }
	                catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
			return true;
			
		}
			
	    	

      }
    
    
    
    
    
 // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {
        mMultiplayer = multiplayer;
        if (serviceConnected){
			List<Pri0nNetwork> networks = service.getNetworks();
			worldFragment.setNetworks(networks);
		}
        switchToFragment(worldFragment);

        
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            try{
	            LoadBitmapTask task = new LoadBitmapTask(p.getParticipantId(), p.getIconImageUri().toString(), playerIcons);
	            task.execute();
            }
            catch(Exception e){
            	
            }
        }
        
        
        /*
        // run the gameTick() method every second to update the game.
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSecondsLeft <= 0)
                    return;
                gameTick();
                h.postDelayed(this, 1000);
            }
        }, 1000);
        */
    }
    
    
	 // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    // Leave the room.
    void leaveRoom() {
    	LoggingManager.logLn(this, "Leaving room.");
        mSecondsLeft = 0;
        stopKeepingScreenOn();
        if (mRoomId != null) {
            getGamesClient().leaveRoom(this, mRoomId);
            mRoomId = null;
            loadingDialog.show();
        } else {
            switchToFragment(homeFragment);
        }
    }
    
 // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            LoggingManager.logLn(this, "*** invitation inbox UI cancelled, " + response);
            switchToFragment(homeFragment);
            return;
        }

        LoggingManager.logLn(this, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(GamesClient.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }
	
	@Override
    public void onActivityResult(int requestCode, int responseCode,
            Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // ignore result if we dismissed the waiting room from code:
                if (mWaitRoomDismissedFromCode) break;

                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // player wants to start playing
                    LoggingManager.logLn(this, "Starting game because user requested via waiting room UI.");

                    // let other players know we're starting.
                    broadcastStart();

                    // start the game!
                    startGame(true);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player actively indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    /* Dialog was cancelled (user pressed back key, for
                     * instance). In our game, this means leaving the room too. In more
                     * elaborate games,this could mean something else (like minimizing the
                     * waiting room UI but continue in the handshake process). */
                    leaveRoom();
                }

                break;
        }
    }

	@Override
	public void homeSettingsButtonClicked() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void homeSignInButtonClicked() {
		if (isSignedIn()){
			signOut();
		}
		else{
			beginUserInitiatedSignIn();
		}
	}
  
	
	
  	@Override
	public void homeStatsButtonClicked() {
	    // Insert the fragment by replacing any existing fragment
	    switchToFragment(statsFragment);
	}

	@Override
	public void homeWorldButtonClicked() {
		if (serviceConnected){
			List<Pri0nNetwork> networks = service.getNetworks();
			worldFragment.setNetworks(networks);
		}
	    switchToFragment(worldFragment);
	}
	
	private boolean isMyServiceRunning() {
		    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		        if (Pri0nService.class.getName().equals(service.service.getClassName())) {
		            return true;
		        }
		    }
		    return false;
		}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    LoggingManager.logLn(this,  "On Create!");
	    
	    splashFragment = new SplashFragment();
	    worldFragment = new WorldFragment();
	    statsFragment = new StatsFragment();
	    homeFragment = new HomeFragment();
		multiplayerFragment = new MultiplayerFragment();

		loadingDialog = new ProgressDialog(this);
		loadingDialog.setTitle(getString(R.string.loading_dialog_title));
		loadingDialog.setMessage(getString(R.string.loading_dialog_message));
		
		playerIcons = new HashMap<String, Bitmap>();
		
	  //Handle Wifi
  		WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
  		if (!wifiManager.isWifiEnabled()){
  			showAlertWiFiDisabled();
  		}
  		
  		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
  		//Handle GPS
  		if ( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
  			showAlertGPSDisabled();
  	    }
  
	    
	    
	    startPri0nService();

	    // Insert the fragment by replacing any existing fragment
	    switchToFragment(splashFragment);
  	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}


	@Override
	protected void onStart(){
	    LoggingManager.logLn(this,  "On Start!");
	    //Bind Service
	    bindService(new Intent(this, Pri0nService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	    
	    //Register receiver
	    if (serviceReceiver == null) {
	    	serviceReceiver = new Pri0nServiceReceiver();
	    	serviceReceiver.registerListener(getClass().getName(), this);
	    }
	    try{
		    IntentFilter intentFilter = new IntentFilter(Pri0nService.ACTION_NEW_NETWORKS);
		    registerReceiver(serviceReceiver, intentFilter);
	    }
	    catch(Exception e){
	    	LoggingManager.logErr(this, "Service Receiver already registered.");
	    }
	    super.onStart();
	}

	@Override
	protected void onStop(){
	    LoggingManager.logLn(this,  "On Stop!");
	    if (serviceConnected){
	    	unbindService(serviceConnection);
	    	serviceConnected = false;
	    }
	    if (serviceReceiver != null) 
	    	unregisterReceiver(serviceReceiver);
	    super.onStop();
	}
	
	@Override
	protected void onPause() {
	    LoggingManager.logLn(this,  "On Pause!");
		super.onPause();
	}

	protected void onRestart(){
	    LoggingManager.logLn(this,  "On Restart!");
		super.onRestart();
	}



	@Override
	protected void onResume() {
	    LoggingManager.logLn(this,  "On Resume!");
		super.onResume();
	}


	@Override
	public void onSignInFailed() {
		LoggingManager.logLn(this, "Sign In Failed");
		
	}
	
	// Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
	
	// Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
    	LoggingManager.logLn(this, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        loadingDialog.show();
        keepScreenOn();
        resetGameVars();
        getGamesClient().joinRoom(roomConfigBuilder.build());

        loadingDialog.dismiss();
    }

    
    // Reset game variables in preparation for a new game.
    void resetGameVars() {
    	//RESET ANY GAME VARIABLES
    }
	
	@Override
	public void onSignInSucceeded() {
		LoggingManager.logLn(this, "Sign In Succeeded");
	    
	 // install invitation listener so we get notified if we receive an
        // invitation to play
        // a game.
        getGamesClient().registerInvitationListener(this);
	    
     // if we received an invite via notification, accept it; otherwise, go
        // to main screen
        if (getInvitationId() != null) {
            acceptInviteToRoom(getInvitationId());
            return;
        }
	    switchToFragment(homeFragment);
	}

	private void showAlertGPSDisabled() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(getString(R.string.alert_gps_disabled_title))
	    	.setMessage(getString(R.string.alert_gps_disabled_message))
	    	.setCancelable(false)
    		.setPositiveButton(getString(R.string.alert_gps_disabled_positive), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					 startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
	           })
	           .setNegativeButton(getString(R.string.alert_gps_disabled_negative), new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	
	
	@Override
	public void onBackPressed(){
		 LoggingManager.logLn(this, "Number of backstack entries:" + getSupportFragmentManager().getBackStackEntryCount());
		 //Temporary Fix. Backstack is complicated, handle later.
		 if (getSupportFragmentManager().getBackStackEntryCount() > 2)
			 getSupportFragmentManager().popBackStack();
	}

	private void showAlertWiFiDisabled() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(getString(R.string.alert_wifi_disabled_title))
	    	.setMessage(getString(R.string.alert_wifi_disabled_message))
	    	.setCancelable(false)
    		.setPositiveButton(getString(R.string.alert_wifi_disabled_positive), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
					wifiManager.setWifiEnabled(true);
					
				}
	           })
	           .setNegativeButton(getString(R.string.alert_gps_disabled_negative), new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}

	@Override
	public void splashOfflineButtonClicked() {
		LoggingManager.logLn(this, "Playing Offline");
	}

	@Override
	public void splashSettingsButtonClicked() {
		LoggingManager.logLn(this, "Go to Settings");
	}

	
	@Override
	public void splashSignInButtonClicked() {
		beginUserInitiatedSignIn();
	}

	private void startPri0nService(){
		  if (!isMyServiceRunning()){
			  LoggingManager.logLn(this, "Starting Service...");
			  Intent intent = new Intent(this, Pri0nService.class);
			  /*
			  // Create a new Messenger for the communication back
			  Messenger messenger = new Messenger(handler);
			  intent.putExtra(Pri0nService.MESSENGER, messenger); 
			  */
			  startService(intent);
		  }
		  else{
			  LoggingManager.logErr(this, "Service is already running.");
		  }
	  }

	@Override
	public void statsAchievementButtonPressed() {
		if (isSignedIn()) {
            startActivityForResult(getGamesClient().getAchievementsIntent(), RC_UNUSED);
        } else {
            showAlert(getString(R.string.unaavailable_achievements));
        }
	}

	@Override
	public void statsLeaderboardButtonPressed() {
		if (isSignedIn()) {
            startActivityForResult(getGamesClient().getAllLeaderboardsIntent(), RC_UNUSED);
        } else {
            showAlert(getString(R.string.unavailable_leaderboards));
        }
	}

    void switchToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
        	.replace(R.id.fragment_container, fragment)
        	.addToBackStack(null)
            .commit();
        loadingDialog.dismiss();
    }
	
	@Override
	public void worldEnableWifiScanButtonPressed(boolean enabled) {
	}

	@Override
	public void pri0nNetworksReceived(ArrayList<Pri0nNetwork> totalNetworks, ArrayList<Pri0nNetwork> newNetworks) {
		broadcastNetworks();
		broadcastLocation();
		if (totalNetworks == null){
			return;
		}
		if (newNetworks == null){
			return;
		}
		if (newNetworks.size() > 0){
			LoggingManager.logLn(this,"New Networks Received");
		}
	}

	@Override
	public void pri0nNetworksRequested(ArrayList<Pri0nNetwork> networks) {
		LoggingManager.logLn(this,"Requested Networks Received");
	}

	// Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        mWaitRoomDismissedFromCode = false;

        // minimum number of players required for our game
        final int MIN_PLAYERS = 2;
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }
	
	// Show error message about game being cancelled and return to main screen.
    void showGameError() {
        showAlert(getString(R.string.error), getString(R.string.game_problem));
        switchToFragment(homeFragment);
    }
	
	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		LoggingManager.logLn(this, "onJoinedRoom(" + statusCode + ", " + room + ")");
		if (statusCode != GamesClient.STATUS_OK) {
            LoggingManager.logErr(this, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
	}
	
	// Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
	@Override
	public void onLeftRoom(int statusCode, String roomId) {
		LoggingManager.logLn(this, "onLeftRoom, code " + statusCode);
		switchToFragment(homeFragment);
	}
	

	void updateRoom(Room room) {
        mParticipants = room.getParticipants();
        //KEEP ROOM UPDATED
    }

	// Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        LoggingManager.logLn(this, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesClient.STATUS_OK) {
        	LoggingManager.logErr(this, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

 // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        LoggingManager.logLn(this, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesClient.STATUS_OK) {
        	LoggingManager.logErr(this, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }
    
    
    // Forcibly dismiss the waiting room UI (this is useful, for example, if we realize the
    // game needs to start because someone else is starting to play).
    void dismissWaitingRoom() {
        mWaitRoomDismissedFromCode = true;
        finishActivity(RC_WAITING_ROOM);
    }

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
    	//SET UP REAL TIME MESSAGES
    	
        byte[] buf = rtm.getMessageData();
        Pri0nPacket packet;
        try {
			packet = (Pri0nPacket) Serializer.deserialize(buf);
		} catch (IOException e) {			
			LoggingManager.logErr(this, "Error deserializing real time message.");
			return;
		} catch (ClassNotFoundException e) {
			LoggingManager.logErr(this, "Deserialized class does not exist.");
			return;
		}
        
       // String sender = rtm.getSenderParticipantId();
        if (packet.type == Pri0nPacket.TYPE_START) {
            // someone else started to play -- so dismiss the waiting room and
            // get right to it!
        	LoggingManager.logLn(this, "Starting game because we got a start message.");
            dismissWaitingRoom();
            startGame(true);
        } 
        else if (packet.type == Pri0nPacket.TYPE_LOCATION) {
            LoggingManager.logLn(this, "Location Character Received!!!!");
            
         // Send to every other participant.
            for (Participant p : mParticipants) {
            	p.
                if (p.getParticipantId().equals(mMyId))
                    continue;
                if (p.getStatus() != Participant.STATUS_JOINED)
                    continue;
                if (p.getParticipantId().equals(rtm.getSenderParticipantId())){
                	Bitmap icon = playerIcons.get(p.getParticipantId());
                	worldFragment.updatePlayer(p.getParticipantId(), p.getDisplayName(), packet.latitude, packet.longitude, icon);
                }
            }
        } 
        else if (packet.type == Pri0nPacket.TYPE_NETWORK) {
            LoggingManager.logLn(this, "Networks Character Received!!!!");
        } 
        else{
            LoggingManager.logLn(this, "Realtime Message Received...?");
        }
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        LoggingManager.logLn(this, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(getGamesClient().getCurrentPlayerId());

        // print out the list of participants (for debug purposes)
        LoggingManager.logLn(this, "Room ID: " + mRoomId);
        LoggingManager.logLn(this, "My ID " + mMyId);
        LoggingManager.logLn(this, "<< CONNECTED TO ROOM>>");
    }


	// Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

	// We treat most of the room update callbacks in the same way: we update our list of
    // participants and update the display. In a real game we would also have to check if that
    // change requires some action like removing the corresponding player avatar from the screen,
    // etc.
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

/*
	private RoomConfig.Builder makeBasicRoomConfigBuilder() {
	    return RoomConfig.builder(this)
	            .setMessageReceivedListener(this)
	            .setRoomStatusUpdateListener(this);
	}
	*/

	@Override
	public void multiplayerInviteButtonClicked() {
		Intent intent = getGamesClient().getSelectPlayersIntent(1, 3);
        loadingDialog.show();
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    //    loadingDialog.dismiss();
		
	}

	@Override
	public void multiplayerQuickGameClicked() {
		final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        //Display a loading part here...
        loadingDialog.show();
        keepScreenOn();
        resetGameVars();
        getGamesClient().createRoom(rtmConfigBuilder.build());
      //  loadingDialog.dismiss();
	}

	@Override
	public void multiplayerCheckInvitesClicked() {
		Intent intent = getGamesClient().getInvitationInboxIntent();
        //switchToScreen(R.id.screen_wait);
        loadingDialog.show();
        startActivityForResult(intent, RC_INVITATION_INBOX);
     //   loadingDialog.dismiss();
	}

	@Override
	public void onInvitationReceived(Invitation invitation) {
		// We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
      //  mIncomingInvitationId = invitation.getInvitationId();
        /*
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(R.string.is_inviting_you));
                        */
       // switchToScreen(mCurScreen); // This will show the invitation popup
	}

} 