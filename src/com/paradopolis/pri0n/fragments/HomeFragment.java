package com.paradopolis.pri0n.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.paradopolis.pri0n.R;

public class HomeFragment extends Fragment implements OnClickListener{
	
	private Listener listener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.fragment_home, container, false);
	    
	    final int[] CLICKABLES = new int[] {
	    		R.id.home_button_world,
	    		R.id.home_button_authentication,
	    		R.id.home_button_build,
	    		R.id.home_button_multiplayer,
	    		R.id.home_button_settings,
	    		R.id.home_button_stats
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
			case R.id.home_button_world:
				listener.homeWorldButtonClicked();
	            break;
			case R.id.home_button_authentication:
				listener.homeSignInButtonClicked();
	            break;
			
			case R.id.home_button_build:
				listener.homeBuildButtonClicked();
	            break;
			
			case R.id.home_button_multiplayer:
				listener.homeMultiplayerButtonClicked();
	            break;
			
			case R.id.home_button_stats:
				listener.homeStatsButtonClicked();
	            break;
			
			case R.id.home_button_settings:
				listener.homeSettingsButtonClicked();
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
		void homeWorldButtonClicked();
		void homeBuildButtonClicked();
		void homeMultiplayerButtonClicked();
		void homeStatsButtonClicked();
		void homeSettingsButtonClicked();
		void homeSignInButtonClicked();
	}
	
}
