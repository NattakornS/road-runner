package com.senior.roadrunner.finish;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.senior.roadrunner.R;

public class FinishMapFragment extends Fragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.activity_pickup, container, false);
		return rootView;
	}
}
