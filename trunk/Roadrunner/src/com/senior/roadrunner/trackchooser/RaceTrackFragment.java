package com.senior.roadrunner.trackchooser;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.senior.roadrunner.R;

@SuppressLint("NewApi")
public class RaceTrackFragment extends Fragment {
	private static RaceTrackFragment fragment;
	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 rootView = inflater.inflate(R.layout.racetrack_layout, container, false);
		return rootView;
	}
	public static RaceTrackFragment createInstacnce(){
		if (fragment == null)
			return fragment = new RaceTrackFragment();
		else {
			return fragment;
		}
	}

}
