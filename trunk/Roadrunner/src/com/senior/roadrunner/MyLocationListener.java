package com.senior.roadrunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.senior.roadrunner.data.Coordinate;
import com.senior.roadrunner.data.LatLngTimeData;

@SuppressLint("SimpleDateFormat")
public class MyLocationListener implements LocationListener {

	public static double latitude;
	public static double longitude;
	private Location location;
	private GoogleMap map;
	Polyline poly;
	private PolylineOptions track;
	private Context context;
	private Marker marker;
	private ArrayList<LatLngTimeData> latLngTimeData;
	// private Polygon polygon = null;

	private OnLocationChangedListener locationChangedListener;

	public MyLocationListener(Context context, GoogleMap googleMap) {
		this.context = context;
		map = googleMap;
		track = new PolylineOptions();
		latLngTimeData = new ArrayList<LatLngTimeData>();
	}

	

	public interface OnLocationChangedListener {
		public void onLocationChanged(Location loc);
	}

	@Override
	public void onLocationChanged(Location loc) {
		location = loc;
		latitude = loc.getLatitude();
		longitude = loc.getLongitude();
		if (location.hasSpeed()) {
			Toast.makeText(context, "Speed : " + location.getSpeed() + " m/s",
					Toast.LENGTH_SHORT).show();
		}

		LatLng coord = new LatLng(latitude, longitude); // from LocationListener
		if (context instanceof OnLocationChangedListener) {
			locationChangedListener = (OnLocationChangedListener) context;
		} else {
			throw new ClassCastException(context.toString()
					+ " must implemenet MyListFragment.OnItemSelectedListener");
		}
		locationChangedListener.onLocationChanged(loc);


		// String pathName = "resources/king.bmp";

		// File imgFile = new File(pathName);
		// if(imgFile.exists()){
		// Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		// MarkerOptions markerOptions = new MarkerOptions();
		// markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.king));
		// map.addMarker(markerOptions);
		// }

		// bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageUrl
		// ).getContent());
		if (marker != null) {
			marker.remove();
		}
		if (context != null) {
			Bitmap.Config conf = Bitmap.Config.ARGB_8888;
			Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
			Canvas canvas1 = new Canvas(bmp);

			// paint defines the text color,
			// stroke width, size
			Paint color = new Paint();
			color.setTextSize(35);
			color.setColor(Color.BLACK);

			// modify canvas
			canvas1.drawBitmap(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.king), 0, 0, color);
			canvas1.drawText("Nattakorn S.", 30, 40, color);

			// add marker to Map
			// marker=map.addMarker(new MarkerOptions().position(coord)
			// .icon(BitmapDescriptorFactory.fromBitmap(bmp))
			// // Specifies the anchor to be at a particular point in the
			// // marker image.
			// .anchor(0.5f, 1));
			marker = map
					.addMarker(new MarkerOptions()
							.position(coord)
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.king))
							.title("Nattakorn Sanpabopit"));
		} else {
			marker = map
					.addMarker(new MarkerOptions()
							.position(coord)
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.king))
							.title("Nattakorn Sanpabopit"));

		}

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 15.0f));

		track.add(coord);
		if (poly != null) {
			poly.remove();
		}
		poly = map.addPolyline(track);

		poly.setWidth(5);
		poly.setGeodesic(true);

		// String timeStamp = DateUtils.fromDate(date );
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String timeStamp = sdf.format(new Date(location.getTime()));
		Coordinate coordinate = new Coordinate(coord.latitude, coord.longitude);
		latLngTimeData.add(new LatLngTimeData(coordinate, timeStamp));

	}

	

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}


	public ArrayList<LatLngTimeData> getLatLngTimeData() {
		return latLngTimeData;
	}

	public void setLatLngTimeData(List<LatLngTimeData> latLngTimeData2) {
		PolylineOptions polylineOptions = new PolylineOptions();
		for (Iterator<LatLngTimeData> iterator = latLngTimeData2.iterator(); iterator
				.hasNext();) {
			LatLngTimeData latLngTimeData = (LatLngTimeData) iterator.next();
			double lat = latLngTimeData.getCoordinate().getLat();
			double lng = latLngTimeData.getCoordinate().getLng();
			LatLng point = new LatLng(lng, lat);
			polylineOptions.add(point);
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0f));
		}
		polylineOptions.color(Color.YELLOW);
		polylineOptions.width(5);
		map.addPolyline(polylineOptions);

	}

}
