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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.tools.GPSSpeed;
import com.senior.roadrunner.tools.LatLngInterpolator.Spherical;
import com.senior.roadrunner.tools.MarkerAnimation;

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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("THREAD : "+RoadRunnerSetting.SDPATH
				+ listTracker.getTrackerDir());
		data = TrackDataBase.loadXmlFile(RoadRunnerSetting.SDPATH
				+ listTracker.getTrackerDir());
		if(data == null){
//			Toast.makeText(activity, "Data is error while loading tracker file from server.", 1000).show();
			System.out.println("Data is error while loading tracker file from server");
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
						if(profileIcon==null){
							marker = map.addMarker(new MarkerOptions()
							.position(point)
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
							.title(listTracker.getfName())
							.snippet("Speed : " + speed + "m/s"));
						}else{
						marker = map.addMarker(new MarkerOptions()
								.position(point)
								.icon(BitmapDescriptorFactory
										.fromBitmap(profileIcon))
								.title(listTracker.getfName())
								.snippet("Speed : " + speed + "m/s"));
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

}
