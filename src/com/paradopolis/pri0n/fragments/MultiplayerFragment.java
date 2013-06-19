package com.paradopolis.pri0n.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.paradopolis.global.LoggingManager;
import com.paradopolis.pri0n.R;

public class MultiplayerFragment extends Fragment implements OnClickListener{
	
	private Listener listener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.fragment_multiplayer, container, false);
	    
	    final int[] CLICKABLES = new int[] {
	    		R.id.multplayer_button_quick_game,
	    		R.id.multplayer_button_invite_friends,
	    		R.id.multplayer_button_check_invites
        };
        for (int i : CLICKABLES) {
        	view.findViewById(i).setOnClickListener(this);
        }
		
		return view;
	}
	
	
	@Override
	public void onClick(View view) {
		if (view == null || listener == null){
			return;
		}
		switch (view.getId()) {
			case R.id.multplayer_button_quick_game:
				listener.multiplayerQuickGameClicked();
	            break;
			case R.id.multplayer_button_invite_friends:
				listener.multiplayerInviteButtonClicked();
				
	            break;
			
			case R.id.multplayer_button_check_invites:
				listener.multiplayerCheckInvitesClicked();
	            break;
			default:
				LoggingManager.logLn(this, "Unknown Button Pressed.");
				break;
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

	public interface Listener{
		void multiplayerInviteButtonClicked();
		void multiplayerQuickGameClicked();
		void multiplayerCheckInvitesClicked();
	}
	
}
