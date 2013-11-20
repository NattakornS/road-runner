package com.senior.roadrunner;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.senior.roadrunner.data.Coordinate;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.racetrack.TrackMemberList;
import com.senior.roadrunner.racetrack.RaceThread;
import com.senior.roadrunner.racetrack.TrackList;
import com.senior.roadrunner.server.ConnectServer;
import com.senior.roadrunner.server.DownloadTask;
import com.senior.roadrunner.server.UploadTask;
import com.senior.roadrunner.tools.Distance;
import com.senior.roadrunner.tools.GPSSpeed;
import com.senior.roadrunner.tools.PathArea;
import com.senior.roadrunner.tools.Point;
import com.senior.roadrunner.tools.Polygon;

@SuppressLint("NewApi")
public class MapsActivity extends Activity implements View.OnClickListener,
		LocationListener
// OnLocationChangedListener
{
	private static final String rId = "3";
	private static final String fId = "1234456";
	private static final String savePath = Environment
			.getExternalStorageDirectory() + "/" + "roadrunner/" + fId + ".xml";

	private static final LatLng LAT_LNG = new LatLng(12, 102);
	GoogleMap map;
	LocationManager myLocationManager;
	// MyLocationListener myLocationListener;
	private View btn_stop_track;
	private Button btn_track;
	private ArrayList<LatLngTimeData> latLngTimeData;
	private Button btn_load_track;
	// private HistoryTrack historyTrack;
	private static final String SDCARD_TRACKER_XML = "/sdcard/tracker.xml";
	private static final String URLServer = "http://roadrunner-5313180.dx.am/";// "http://192.168.1.111/";

	private Vector<Polygon> polygonsTrack = new Vector<Polygon>();
	private Polygon polygonStart;
	private boolean startCheck = false;
	private boolean pathCheck = false;

	private Marker marker;
	private Polyline poly;
	private PolylineOptions track;
	private boolean recordCheck = false;
	private ConnectServer connectServer;
	private Polygon polygonFinish;
	private ArrayList<TrackMemberList> trackMemberList;
	private String trackPathData;
	private TextView txt_current_distace;
	private TextView txt_current_speed;
	private TextView txt_current_time;

	private Handler myHandler;
	private Runnable updateTimerMethod;
	// private ArrayList<LatLngTimeData> latLngTimeData;
	// private static final String SDCARD_TRACKER_XML = "/sdcard/tracker.xml";
	private long startTime = 0L;
	protected long timeInMillies = 0L;
	protected long timeSwap = 0L;
	protected long finalTime = 0L;
	private double totalDistance = 0;
	private TrackList trackList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);
		Intent intent = getIntent();
		trackMemberList = (ArrayList<TrackMemberList>) intent
				.getSerializableExtra("TrackMemberList");
		trackPathData = intent.getStringExtra("TrackPathData");
		initwidget();
		loadFile();

		track = new PolylineOptions();
		latLngTimeData = new ArrayList<LatLngTimeData>();
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.maps))
				.getMap();
		// SupportMapFragment supportMapFragment = (SupportMapFragment)
		// getSupportFragmentManager()
		// .findFragmentById(R.id.maps);

		// map = supportMapFragment.getMap();
		// map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				13.793888, 100.324146), 15.0f));

		// map.setMyLocationEnabled(true);
		// CircleOptions options = new CircleOptions();
		// options.center(LAT_LNG);
		// options.radius(1000000);
		// options.fillColor(Color.TRANSPARENT);
		// options.strokeColor(Color.BLACK);
		// options.strokeWidth(10);
		// map.addCircle(options);
		//
		// map.animateCamera(CameraUpdateFactory
		// .newLatLngZoom(LAT_LNG, 15.0f));

		// historyTrack = new HistoryTrack(map, this);

		// ////////////////////// GPS tracker //////////////////////

		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Context context = getApplicationContext();
		// myLocationListener = new MyLocationListener(this, map);
		enableGPSListener();
		createRunningPath();
		setStartPointTracking(true);
	}

	@SuppressWarnings("unchecked")
	private void loadFile() {

		for (int i = 0; i < trackMemberList.size(); i++) {
			System.out.println("TrackerDir : "
					+ trackMemberList.get(i).getTrackerDir());
			ConnectServer connectServerTrackMemberData = new ConnectServer(
					this, URLServer + "/getTrackPath.php");
			connectServerTrackMemberData.addValue("Rdir", URLServer
					+ trackMemberList.get(i).getTrackerDir());
			connectServerTrackMemberData.setIndex(i);
			connectServerTrackMemberData
					.setRequestTag(ConnectServer.TRACK_MEMBER_PATH);
			connectServerTrackMemberData.execute();
		}

		// DownloadTask downloadTask = new DownloadTask(this);
		// downloadTask
		// .execute("http://192.168.1.121/uploads/mahidol/tracker.xml");

	}

	public void createRunningPath() {
		PolylineOptions options = new PolylineOptions();
		Vector<Point> points = new Vector<Point>();
		List<LatLngTimeData> data = TrackDataBase.loadXmlString(trackPathData);

		for (int i = 0; i < data.size(); i++) {
			double lat = data.get(i).getCoordinate().getLat();
			double lng = data.get(i).getCoordinate().getLng();
			options.add(new LatLng(lat, lng));
			points.add(new Point(lat, lng));
		}

		Polyline p = map.addPolyline(options);
		p.setWidth(5);
		p.setGeodesic(true);
		p.setVisible(true);

		// Start polygon
		polygonStart = PathArea.circleBuffer(points.get(0));
		PolygonOptions polygonStartOptions = new PolygonOptions();
		for (int i = 0; i < polygonStart.getSides().size(); i++) {
			// System.out.println(polygonStart.getSides().get(i).getStart().x
			// + "    " + polygonStart.getSides().get(i).getStart().y);
			polygonStartOptions
					.add(new LatLng(
							polygonStart.getSides().get(i).getStart().x,
							polygonStart.getSides().get(i).getStart().y));
		}
		polygonStartOptions.fillColor(Color.BLACK);
		map.addPolygon(polygonStartOptions);

		// Path polygon
		polygonsTrack = PathArea.createPathArea(points);
		for (int i = 0; i < polygonsTrack.size(); i++) {
			// System.out.println("getside : "+polygons.get(i).getSides());
			Polygon polygon = polygonsTrack.get(i);
			PolygonOptions polygonOptions = new PolygonOptions();
			for (int j = 0; j < polygon.getSides().size(); j++) {
				polygonOptions.add(new LatLng(polygon.getSides().get(j)
						.getStart().x, polygon.getSides().get(j).getStart().y));
				if (i % 3 == 0)
					polygonOptions.fillColor(Color.BLUE);
				if (i % 3 == 1)
					polygonOptions.fillColor(Color.GREEN);
				if (i % 3 == 2)
					polygonOptions.fillColor(Color.RED);

			}
			// polygonOptions.fillColor(Color.BLUE);
			map.addPolygon(polygonOptions.strokeWidth(2));
		}

		// Finish polygon
		polygonFinish = PathArea.circleBuffer(points.lastElement());
		PolygonOptions polygonFinishOptions = new PolygonOptions();
		for (int i = 0; i < polygonFinish.getSides().size(); i++) {
			polygonFinishOptions
					.add(new LatLng(
							polygonFinish.getSides().get(i).getStart().x,
							polygonFinish.getSides().get(i).getStart().y));
		}
		polygonFinishOptions.fillColor(Color.BLACK);
		map.addPolygon(polygonFinishOptions);

	}

	private void checkisInPath(Point point) {
		if (polygonStart != null && startCheck) {

			if (polygonStart.contains(point)) {
				Toast.makeText(this, "IN TRACK", Toast.LENGTH_SHORT).show();
				btn_track.setEnabled(true);
			} else {
				Toast.makeText(this, "OUT TRACK", Toast.LENGTH_SHORT).show();
				btn_track.setEnabled(false);
			}
		}
		if (polygonsTrack != null && pathCheck) {
			for (int i = 0; i < polygonsTrack.size(); i++) {
				Polygon polygon = polygonsTrack.get(i);
				if (polygon.contains(point)) {
					Toast.makeText(this, "IN TRACK", Toast.LENGTH_SHORT).show();
					break;
				}
				if (i == polygonsTrack.size() - 1) {
					Toast.makeText(this, "OUT TRACK", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
		if (polygonFinish != null && pathCheck) {
			if (polygonFinish.contains(point)) {
				Toast.makeText(this, "Finish", Toast.LENGTH_SHORT).show();
				btn_stop_track.setEnabled(true);
			} else {
				btn_stop_track.setEnabled(false);
			}
		}
	}

	private void initwidget() {
		myHandler = new Handler();
		btn_track = (Button) findViewById(R.id.btn_track);
		btn_track.setOnClickListener(this);
		btn_stop_track = (Button) findViewById(R.id.btn_stop_track);
		btn_stop_track.setOnClickListener(this);
		btn_load_track = (Button) findViewById(R.id.btn_load_track);
		btn_load_track.setOnClickListener(this);
		txt_current_distace = (TextView) findViewById(R.id.txt_curent_distance);
		txt_current_speed = (TextView) findViewById(R.id.txt_curent_speed);
		txt_current_time = (TextView) findViewById(R.id.txt_curent_time);
		txt_current_speed.setText("0");
		txt_current_distace.setText("0");
		txt_current_time.setText("00:00");
	}

	public boolean isGpsEnable() {
		boolean isgpsenable = false;
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.equals("")) { // GPS is Enabled
			isgpsenable = true;
		}
		return isgpsenable;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_track:
			setTrackingPath(true);
			setStartPointTracking(false);
			recordCheck = true;
			btn_track.setEnabled(false);
			raceThread();
			timer();
			break;
		case R.id.btn_stop_track:
			updateDataBase();
			recordCheck = false;
			myLocationManager.removeUpdates(this);
			if (latLngTimeData.isEmpty()) {
				break;
			}
			saveTrackData();
			uploadFile();
			myHandler.removeCallbacks(updateTimerMethod);
			break;

		case R.id.btn_load_track:
			Toast.makeText(getApplicationContext(), "Load data",
					Toast.LENGTH_SHORT).show();
			raceThread();
			break;
		}

	}

	private void timer() {
		updateTimerMethod = new Runnable() {

			public void run() {
				timeInMillies = SystemClock.uptimeMillis() - startTime;
				finalTime = timeSwap + timeInMillies;

				int seconds = (int) (finalTime / 1000);
				int minutes = seconds / 60;
				seconds = seconds % 60;
				// int milliseconds = (int) (finalTime % 1000);
				txt_current_time.setText("" + minutes + ":"
						+ String.format("%02d", seconds));
				myHandler.postDelayed(this, 0);
			}

		};
		startTime = SystemClock.uptimeMillis();
		myHandler.postDelayed(updateTimerMethod, 0);

	}

	private void saveTrackData() {
		// latLngTimeData = myLocationListener.getLatLngTimeData();
		System.out.println(savePath);
		TrackDataBase.saveXmlFile(latLngTimeData, savePath);
		Toast.makeText(getApplicationContext(), "Save data", Toast.LENGTH_SHORT)
				.show();

	}

	private void updateDataBase() {
		connectServer = new ConnectServer(this,
				"http://192.168.1.105/connect_server.php");

		connectServer.addValue("Fid", fId);
		connectServer.addValue("Rid", rId);
		connectServer.addValue("Trackerdir", "tracker/" + rId + "/" + fId
				+ ".xml");
		connectServer.addValue("Rank", "5");
		connectServer.setRequestTag(ConnectServer.DATA_UPDATE);
		connectServer.execute();

	}

	private void uploadFile() {
		UploadTask uploadTask = new UploadTask(this);
		uploadTask.execute(savePath);
	}

	private void raceThread() {
		// List<LatLngTimeData> data = TrackDataBase
		// .loadXmlFile(SDCARD_TRACKER_XML);
		for (int i = 0; i < trackMemberList.size(); i++) {
			// List<LatLngTimeData> data =
			// trackMemberList.get(i).getTrackData();
			RaceThread raceThread = new RaceThread(trackMemberList.get(i), map,
					this);
			raceThread.start();
		}

	}

	private void enableGPSListener() {
		if (!isGpsEnable()) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						Intent intent = new Intent(
								Settings.ACTION_SECURITY_SETTINGS);
						startActivity(intent);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						break;
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Setting GPS?")
					.setPositiveButton("Yes", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();
		} else {

			final Criteria criteria = new Criteria();

			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setSpeedRequired(true);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW);

			final String bestProvider = myLocationManager.getBestProvider(
					criteria, true);

			if (bestProvider != null && bestProvider.length() > 0) {
				myLocationManager.requestLocationUpdates(bestProvider, 1000,
						15, this);
			} else {
				final List<String> providers = myLocationManager
						.getProviders(true);

				for (final String provider : providers) {
					myLocationManager.requestLocationUpdates(provider, 1000,
							15, this);
				}
			}

			myLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 15, this);
			Toast.makeText(getApplicationContext(), "Track data",
					Toast.LENGTH_SHORT).show();
		}

	}

	// ONlocation change
	// - draw map
	// - record location
	// - update distance
	@Override
	public void onLocationChanged(Location loc) {
		// check if gps not accuracy.
		if (!loc.hasAccuracy()) {
			return;
		}
		System.out.println(loc.hasAccuracy());
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		System.out.println("INCOMMING : " + latitude + "    " + longitude);
		Point point = new Point(latitude, longitude);
		LatLng coord = new LatLng(loc.getLatitude(), loc.getLongitude());
		if (marker != null) {
			marker.remove();
		}

		marker = map.addMarker(new MarkerOptions().position(coord)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.king))
				.title("Nattakorn Sanpabopit"));

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 15.0f));

		checkisInPath(point);
		if (recordCheck)
			recordTrack(loc);

	}

	private void recordTrack(Location loc) {
		DecimalFormat df = new DecimalFormat("0.00");
		if (loc.hasSpeed()) {
			Toast.makeText(this, "Speed : " + loc.getSpeed() + " KPH",
					Toast.LENGTH_SHORT).show();

			String gpsSpeed = df.format(loc.getSpeed() * 1000 / 3600);
			txt_current_speed.setText(gpsSpeed);
		}

		LatLng coord = new LatLng(loc.getLatitude(), loc.getLongitude());

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
		String timeStamp = sdf.format(new Date(loc.getTime()));
		Coordinate coordinate = new Coordinate(coord.latitude, coord.longitude);
		latLngTimeData.add(new LatLngTimeData(coordinate, timeStamp));

		// dstace update
		if (latLngTimeData.size() >= 2) {
			Coordinate startCoord = latLngTimeData.get(
					latLngTimeData.size() - 2).getCoordinate();
			Coordinate recentCoord = latLngTimeData.get(
					latLngTimeData.size() - 1).getCoordinate();
			double distance = Distance.calculateDistance(startCoord,
					recentCoord, Distance.KILOMETERS);
			totalDistance += distance;
			txt_current_distace.setText(df.format(totalDistance));
		}
	}

	public void setTrackingPath(boolean b) {
		this.pathCheck = b;

	}

	public void setStartPointTracking(boolean b) {
		this.startCheck = b;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
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

	public void cannotConnectToServer() {
		Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_LONG)
				.show();
	}

	public void errorConnectToServer() {
		Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
	}

	public void setList(String result) {
		System.out.println("Result : " + result);
	}

	public synchronized void setMemberTrack(String result, int index) {
		// System.out.println("Index : "+ index);
		List<LatLngTimeData> trackData = TrackDataBase.loadXmlString(result);
		trackMemberList.get(index).setTrackData(trackData);
	}
}
