package com.senior.roadrunner.racetrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.senior.roadrunner.R;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.tools.GPSSpeed;
import com.senior.roadrunner.tools.LatLngInterpolator.Spherical;
import com.senior.roadrunner.tools.MarkerAnimation;
import com.senior.roadrunner.trackchooser.TrackMemberList;

public class RaceThread extends Thread {
	private int i;
	private Marker marker = null;
	private SimpleDateFormat sdf;
	private List<LatLngTimeData> data;
	private long waitingTime;
	private LatLng point;
	private LatLng end;
	private GoogleMap map;
	private Activity activity;
	private TrackMemberList listTracker;
	private Bitmap profileIcon;
	private Bitmap bmp;

	@SuppressLint("SimpleDateFormat")
	public RaceThread(TrackMemberList listTracker, GoogleMap map,
			Activity activity) {
		this.listTracker = listTracker;
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		this.map = map;
		this.activity = activity;

	}

	@Override
	public void run() {

		if (listTracker == null) {
			return;
		}
		File f = new File(RoadRunnerSetting.SDPATH + "img/"
				+ listTracker.getfId() + ".png");

		try {
			profileIcon = BitmapFactory.decodeStream(new FileInputStream(f));
			modifyCanvas();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("THREAD : " + RoadRunnerSetting.SDPATH
				+ listTracker.getTrackerDir());
		data = TrackDataBase.loadXmlFile(RoadRunnerSetting.SDPATH
				+ listTracker.getTrackerDir());
		if (data == null) {
			// Toast.makeText(activity,
			// "Data is error while loading tracker file from server.",
			// 1000).show();
			System.out
					.println("Data is error while loading tracker file from server");
			return;
		}
		for (i = 0; i < data.size(); i++) {
			try {
				Date recentDate = sdf.parse(data.get(i).getWhen());

				double lat = data.get(i).getCoordinate().getLat();
				double lng = data.get(i).getCoordinate().getLng();

				point = new LatLng(lat, lng);

				if (i == data.size() - 1) {
					break;
				}
				Date futureDate = sdf.parse(data.get(i + 1).getWhen());

				waitingTime = futureDate.getTime() - recentDate.getTime();
				System.out.println("wait : " + waitingTime);

				double elat = data.get(i + 1).getCoordinate().getLat();
				double elng = data.get(i + 1).getCoordinate().getLng();
				end = new LatLng(elat, elng);
				// System.out.println("Speed m/s : "+GPSSpeed.SpeedFrom2PointTime(point,
				// end, futureDate.getTime(), recentDate.getTime()));
				final long speed = GPSSpeed.SpeedFrom2PointTime(point, end,
						futureDate.getTime(), recentDate.getTime());
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {

						if (marker != null) {
							marker.remove();
						}
						if (profileIcon == null) {
							marker = map.addMarker(new MarkerOptions()
									.position(point)
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
									.title(listTracker.getfName())
									.snippet("Speed : " + speed + "KPH"));
						} else {
							View customMarker = ((LayoutInflater) activity
									.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
									.inflate(R.layout.custom_marker_layout,
											null);
							ImageView imageView = (ImageView) customMarker
									.findViewById(R.id.profileIcon);
							imageView.setImageBitmap(bmp);
							marker = map.addMarker(new MarkerOptions()
									.position(point)
									.icon(BitmapDescriptorFactory
											.fromBitmap(createDrawableFromView(
													activity, customMarker)))
									.title(listTracker.getfName())
									.snippet("Speed : " + speed + "KPH"));
						}
						marker.showInfoWindow();
						Spherical latLngInterpolator = new Spherical();
						latLngInterpolator.interpolate(5.0f, point, end);
						MarkerAnimation.animateMarkerToICS(marker, end,
								latLngInterpolator);
						// map.animateCamera(CameraUpdateFactory.newLatLngZoom(
						// end, 17.0f));

					}
				});
				Thread.sleep(waitingTime);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		super.run();
	}

	// Convert a view to bitmap
	public Bitmap createDrawableFromView(Context context, View view) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
		view.layout(0, 0, displayMetrics.widthPixels,
				displayMetrics.heightPixels);
		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
				view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		
		return adjustOpacity(bitmap,200);
	}
	public Bitmap adjustOpacity(Bitmap bitmap, int opacity)
	{
	    Bitmap mutableBitmap = bitmap.isMutable()
	                           ? bitmap
	                           : bitmap.copy(Bitmap.Config.ARGB_8888, true);
	    Canvas canvas = new Canvas(mutableBitmap);
	    int colour = (opacity & 0xFF) << 24;
	    canvas.drawColor(colour, PorterDuff.Mode.DST_IN);
	    return mutableBitmap;
	}
	private void modifyCanvas() {
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		bmp = Bitmap.createBitmap(65,65,conf);
		Canvas canvas1 = new Canvas(bmp);

		// paint defines the text color,
		// stroke width, size
		Paint color = new Paint();
		color.setTextSize(35);
		color.setColor(Color.BLACK);

		// modify canvas
		canvas1.drawBitmap(profileIcon, 0, 0, color);
		// canvas1.drawText(listTracker.getfName(), 30, 40, color);

	}
}
