package com.senior.roadrunner.finish;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.senior.roadrunner.R;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.trackchooser.TrackMemberList;

public class FinishMyListViewFragment extends Fragment {

	private static FinishMyListViewFragment finishMyListViewFragment;

	public static FinishMyListViewFragment getInstance(
			TrackMemberList trackMemberList) {
		if (finishMyListViewFragment == null) {
			finishMyListViewFragment = new FinishMyListViewFragment();
		}

		return finishMyListViewFragment;
	}

	private TextView finishCaloriesTxtView;
	private TextView finishNameTxtView;
	private TextView finishAvgkphTxtView;
	private TextView finishPlaceTxtView;
	private TextView finishDurationTxtView;
	private RoadRunnerSetting roadRunnerSetting;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.finish_my_listview,
				container, false);
		
		return rootView;
	}

	public void setPlace(String place) {
		finishPlaceTxtView = (TextView) getView().findViewById(
				R.id.finish_my_place_txt);
		finishPlaceTxtView.setText(place);
	}

	public void setAvgKph(String speed) {
		finishAvgkphTxtView = (TextView) getView().findViewById(
				R.id.finish_my_avgkph_txt);
		finishAvgkphTxtView.setText(speed);
	}

	public void setName(String name) {
		finishNameTxtView = (TextView) getView().findViewById(
				R.id.finish_my_name_txt);
		finishNameTxtView.setText(name);
	}

	public void setDuration(String duration) {
		finishDurationTxtView = (TextView) getView().findViewById(
				R.id.finish_my_duration_txt);
		finishDurationTxtView.setText(duration);
	}

	public void setCalories(String calories) {
		finishCaloriesTxtView = (TextView) getView().findViewById(
				R.id.finish_my_calories_txt);
		finishCaloriesTxtView.setText(calories);
	}

}
