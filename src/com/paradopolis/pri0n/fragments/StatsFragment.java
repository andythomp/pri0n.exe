package com.paradopolis.pri0n.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.paradopolis.pri0n.R;

public class StatsFragment extends Fragment implements OnClickListener{

	private Listener listener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.fragment_stats, container, false);
	    
	    final int[] CLICKABLES = new int[] {
	    		R.id.stats_achievement_button,
	    		R.id.stats_leaderboards_button
        };
	    
        for (int i : CLICKABLES) {
        	view.findViewById(i).setOnClickListener(this);
        }
		
		return view;
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
	
	
	
	@Override
	public void onClick(View view) {
		if (view == null || listener == null){
			return;
		}
		switch (view.getId()) {
			case R.id.stats_achievement_button:
				listener.statsAchievementButtonPressed();
	            break;
	        case R.id.stats_leaderboards_button:
				listener.statsLeaderboardButtonPressed();
	            break;
			}
       
		}

	public interface Listener{
		public void statsAchievementButtonPressed();
		public void statsLeaderboardButtonPressed();
	}
}
