package com.senior.roadrunner.trackchooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.senior.roadrunner.R;
import com.senior.roadrunner.setting.RoadRunnerSetting;

public class InfoAdaptor extends BaseAdapter {

	private Activity activity;
	private ArrayList<TrackMemberList> data;
	private LayoutInflater inflater;
	private TrackMemberList tempValues;


	public InfoAdaptor(Activity activity,
			ArrayList<TrackMemberList> trackMemberList) {
		this.activity = activity;
		data = trackMemberList;

		/*********** Layout inflator to call external xml layout () **********************/
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if (data.size() <= 0)
			return 1;
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {

		public TextView nameTxt;
		public TextView durationTxt;
		public TextView placeTxt;
		public ImageView image;

	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;

		if (convertView == null) {

			/********** Inflate tabitem.xml file for each row ( Defined below ) ************/
			vi = inflater.inflate(R.layout.finish_list_item, null);
			vi.setBackgroundColor(Color.alpha(R.color.blue_gradientE));
			// vi.setBackgroundResource(R.drawable.rounded_corners);
			/******** View Holder Object to contain tabitem.xml file elements ************/
			holder = new ViewHolder();
			holder.nameTxt = (TextView) vi
					.findViewById(R.id.finish_name_list_txt);
			holder.durationTxt = (TextView) vi
					.findViewById(R.id.finish_duration_list_txt);
			holder.placeTxt = (TextView) vi
					.findViewById(R.id.finish_place_list_txt);
			holder.image = (ImageView) vi
					.findViewById(R.id.finish_pic_list_img);

			/************ Set holder with LayoutInflater ************/
			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		if (data.size() <= 0) {
			holder.nameTxt.setText("No Data");

		} else {
			/***** Get each Model object from Arraylist ********/
			tempValues = null;
			tempValues = data.get(position);
			// String name =
			// "https://graph.facebook.com/"+tempValues.getfId()+"/picture?75=&height=75";
			// URL url_value;
			// try {
			// url_value = new URL(name);
			// profileIcon =
			// BitmapFactory.decodeStream(url_value.openConnection().getInputStream());
			// } catch (MalformedURLException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			Bitmap bitmap = null;
			File f = new File(RoadRunnerSetting.SDPATH+"img/"+tempValues.getfId()+".png");

			try {
				bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			int seconds = (int) (tempValues.getDuration() / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			/************ Set Model values in Holder elements ***********/
			holder.nameTxt.setText(tempValues.getfName());
			holder.placeTxt.setText(tempValues.getRank() + "");
			holder.durationTxt.setText("" + minutes + ":"
					+ String.format("%02d", seconds));
			holder.image.setImageBitmap(bitmap);

		}
		return vi;
	}
	
}
