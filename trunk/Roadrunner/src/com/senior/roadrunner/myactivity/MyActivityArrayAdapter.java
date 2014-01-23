package com.senior.roadrunner.myactivity;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.senior.roadrunner.R;

public class MyActivityArrayAdapter extends ArrayAdapter<MyActivityListData> {

	private ArrayList<MyActivityListData> values;
	private Context context;

	public MyActivityArrayAdapter(Context context, int resource,
			ArrayList<MyActivityListData> activityListDatas) {
		super(context, resource, activityListDatas);
		this.context = context;
		this.values = activityListDatas;

	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vi = inflater.inflate(R.layout.myactivity_listview, parent, false);
		TextView holderrank = (TextView) vi.findViewById(R.id.rank);
		TextView holderduration = (TextView) vi.findViewById(R.id.duration);
		TextView holdertrackName = (TextView) vi.findViewById(R.id.trackName);
		TextView holderdate = (TextView) vi.findViewById(R.id.date);
		TextView holderdetail = (TextView) vi.findViewById(R.id.detail);

		holderrank.setText(values.get(position).getRank());
		int seconds = (int) (values.get(position).getDuration() / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;

		holderduration.setText("" + minutes + ":"
				+ String.format("%02d", seconds));
		holdertrackName.setText(values.get(position).getTrackName());
		holderdate.setText(values.get(position).getDate());
		holderdetail.setText(values.get(position).getDetail());
		return vi;
	}

}
