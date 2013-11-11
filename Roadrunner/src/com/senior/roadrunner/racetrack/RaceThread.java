package com.senior.roadrunner.racetrack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.tools.GPSSpeed;
import com.senior.roadrunner.tools.MarkerAnimation;
import com.senior.roadrunner.tools.LatLngInterpolator.Spherical;

public class RaceThread extends Thread {
	private static final String SDCARD_TRACKER_XML = "/sdcard/tracker.xml";
	private int i;
	private Marker marker = null;
	private SimpleDateFormat sdf;
	private List<LatLngTimeData> data;
	private long waitingTime;
	private LatLng point;
	private LatLng end;
	private GoogleMap map;
	private Activity activity;
	private ListTracker listTracker;

	public RaceThread(ListTracker listTracker, GoogleMap map, Activity activity) {
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
		data = listTracker.getTrackData();
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

						marker = map.addMarker(new MarkerOptions()
								.position(point)
								.icon(BitmapDescriptorFactory.defaultMarker())
								.title(listTracker.getfId())
								.snippet("Speed : " + speed + "m/s"));
						marker.showInfoWindow();
						Spherical latLngInterpolator = new Spherical();
						latLngInterpolator.interpolate(5.0f, point, end);
						MarkerAnimation.animateMarkerToICS(marker, end,
								latLngInterpolator);
						map.animateCamera(CameraUpdateFactory.newLatLngZoom(
								end, 17.0f));

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
