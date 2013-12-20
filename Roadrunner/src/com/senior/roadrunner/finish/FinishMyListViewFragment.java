package com.senior.roadrunner.finish;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.senior.roadrunner.R;
import com.senior.roadrunner.racetrack.TrackMemberList;
import com.senior.roadrunner.setting.RoadRunnerSetting;

public class FinishMyListViewFragment extends Fragment {

	public static FinishMyListViewFragment newInstance(TrackMemberList trackMemberList) {

		FinishMyListViewFragment finishMyListViewFragment = new FinishMyListViewFragment();
		Bundle bundle = new Bundle();
		bundle.putString("rank", trackMemberList.getRank()+"");
//		bundle.putString("avgkph", trackMemberList.getRank()+"");
		bundle.putLong("duration", trackMemberList.getDuration());
//		bundle.putString("calories", trackMemberList.getRank()+"");
		finishMyListViewFragment.setArguments(bundle);
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
		roadRunnerSetting = RoadRunnerSetting.getInstance();
		finishDurationTxtView = (TextView) rootView
				.findViewById(R.id.finish_my_duration_txt);
		finishPlaceTxtView = (TextView) rootView
				.findViewById(R.id.finish_my_place_txt);
		finishAvgkphTxtView = (TextView) rootView
				.findViewById(R.id.finish_my_avgkph_txt);
		finishNameTxtView = (TextView) rootView
				.findViewById(R.id.finish_my_name_txt);
		finishCaloriesTxtView = (TextView) rootView
				.findViewById(R.id.finish_my_calories_txt);
		long mils = getArguments().getLong("duration");
        int seconds = (int) (mils / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		finishPlaceTxtView.setText(getArguments().getString("rank"));
		finishAvgkphTxtView.setText("8");
		finishCaloriesTxtView.setText("300");
		finishNameTxtView.setText(roadRunnerSetting.getFacebookName());
		finishDurationTxtView.setText("" + minutes + ":"
				+ String.format("%02d", seconds));

		return rootView;
	}

	public void setPlace(String place){
		finishPlaceTxtView.setText(place);
	}
	public void setAvgKph(String speed){
		finishAvgkphTxtView.setText(speed);
	}
	public void setName(String name){
		finishNameTxtView.setText(name);
	}
	public void setDuration(String duration){
		finishDurationTxtView.setText(duration);
	}
	public void setCalories(String calories){
		finishCaloriesTxtView.setText(calories);
	}
	
}
