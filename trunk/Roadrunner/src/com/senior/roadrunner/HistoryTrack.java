package com.senior.roadrunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.senior.roadrunner.data.LatLngTimeData;

@SuppressLint("SimpleDateFormat")
public class HistoryTrack implements Runnable {

	private List<LatLngTimeData> latLngTimeData;
	private GoogleMap map;
	private Marker marker;
	private SimpleDateFormat sdf;
	private LatLng point;
	private Activity mapsPage;

	public HistoryTrack(GoogleMap map, Activity mapsPage) {
		this.map = map;
		this.mapsPage = mapsPage;
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}

	public List<LatLngTimeData> getLatLngTimeData() {
		return latLngTimeData;
	}

	public void setLatLngTimeData(List<LatLngTimeData> latLngTimeData) {
		this.latLngTimeData = latLngTimeData;
	}

	@Override
	public void run() {

		 int i;
		 sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//		 List<LatLngTimeData> data = trackDataBase.loadXmlFile();
		 long waitingTime;


			if (latLngTimeData == null) {
				return;
			}

			for (i = 0; i < latLngTimeData.size(); i++) {
				try {
					Date recentDate = sdf.parse(latLngTimeData.get(i)
							.getWhen());

					if (i == latLngTimeData.size() - 1) {
						break;
					}
					
					Date futureDate = sdf.parse(latLngTimeData.get(i + 1)
								.getWhen());

					waitingTime = futureDate.getTime()
							- recentDate.getTime();
					System.out.println("wait : " + waitingTime);
					
					double lat = latLngTimeData.get(i).getCoordinate()
							.getLat();
					double lng = latLngTimeData.get(i).getCoordinate()
							.getLng();
					point = new LatLng(lng, lat);
					
					mapsPage.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							
							

							if (marker != null) {
								marker.remove();
							}

							marker = map.addMarker(new MarkerOptions()
									.position(point).icon(
											BitmapDescriptorFactory
													.defaultMarker()));
							map.animateCamera(CameraUpdateFactory
									.newLatLngZoom(point, 15.0f));
							
							
						}
					});
					Thread.sleep(waitingTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		
	}
}
