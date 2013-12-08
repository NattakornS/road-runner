package com.senior.roadrunner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FacebookFragment extends Fragment {
	private static FacebookFragment fragment;
	public static FacebookFragment createInstacnce(){
		if (fragment == null)
			return fragment = new FacebookFragment();
		else {
			return fragment;
		}
	}
	private View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(savedInstanceState==null){
			rootView = inflater.inflate(R.layout.facebook_fragment, container, false);
		}else{}
		 
		 
		 return rootView;
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putDouble("FID", 192168);
		outState.putString("FNAME", "Nattakorn Sanpabopit");
	}
}
