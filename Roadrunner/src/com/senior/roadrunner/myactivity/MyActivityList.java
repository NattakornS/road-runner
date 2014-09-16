package com.senior.roadrunner.myactivity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.senior.roadrunner.R;
import com.senior.roadrunner.trackchooser.TrackMemberList;

public class MyActivityList extends BaseAdapter implements OnClickListener {

	private Activity activity;
	private ArrayList<MyActivityListData> data;
	private LayoutInflater inflater;
	private TrackMemberList tempValues;
	@SuppressWarnings("unused")
	private Bitmap profileIcon;

	public MyActivityList(Activity finishActivity,
			ArrayList<MyActivityListData> activityListDatas) {
		activity = finishActivity;
		data = activityListDatas;

		/*********** Layout inflator to call external xml layout () **********************/
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/******** What is the size of Passed Arraylist Size ************/
	public int getCount() {

		if (data.size() <= 0)
			return 1;
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {

		public TextView rank;
		public TextView duration;
		public TextView trackName;
		public TextView date;
		public TextView detail;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;

		if (convertView == null) {

			/********** Inflate tabitem.xml file for each row ( Defined below ) ************/
			vi = inflater.inflate(R.layout.myactivity_listview, null);
			// vi.setBackgroundColor(Color.alpha(R.color.blue_gradientE));
			// vi.setBackgroundResource(R.drawable.rounded_corners);
			/******** View Holder Object to contain tabitem.xml file elements ************/
			holder = new ViewHolder();
			holder.rank = (TextView) vi.findViewById(R.id.rank);
			holder.duration = (TextView) vi.findViewById(R.id.duration);
			holder.trackName = (TextView) vi.findViewById(R.id.trackName);
			holder.date = (TextView) vi.findViewById(R.id.date);
			holder.detail = (TextView) vi.findViewById(R.id.detail);
			/************ Set holder with LayoutInflater ************/
			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		if (data.size() <= 0) {
			// holder.nameTxt.setText("No Data");

		} else {
			MyActivityListData temp = data.get(position);
			if (temp != null) {
				holder.date.setText(temp.getDate());
				holder.detail.setText(String.format("%.2f km",
						data.get(position).getDistance()));
				int seconds = (int) (temp.getDuration() / 1000);
				int minutes = seconds / 60;
				seconds = seconds % 60;

				holder.duration.setText("" + minutes + ":"
						+ String.format("%02d", seconds));
				holder.rank.setText(temp.getRank());
				holder.trackName.setText(temp.getTrackName());
				// System.out.println(temp.getDate());
				// System.out.println(temp.getDetail());
				// System.out.println(temp.getDuration());
				// System.out.println(temp.getRank());
				// System.out.println(temp.getTrackName());
			}
		}
		/******** Set Item Click Listner for LayoutInflater for each row ***********/
		vi.setOnClickListener(new OnItemClickListener(position));
		return vi;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	/********* Called when Item click in ListView ************/
	@SuppressLint("ShowToast")
	private class OnItemClickListener implements OnClickListener {
		private int mPosition;

		OnItemClickListener(int position) {
			mPosition = position;

		}

		@Override
		public void onClick(View arg0) {
			// MyActivityList sct = (MyActivityList) activity;
			// sct.onItemClick(mPosition);
			Toast.makeText(activity, "click : " + mPosition, 1000).show();
		}
	}

}
