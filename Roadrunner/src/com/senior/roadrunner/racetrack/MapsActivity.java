package com.senior.roadrunner.racetrack;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import android.R.drawable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.senior.roadrunner.MainActivity;
import com.senior.roadrunner.R;
import com.senior.roadrunner.data.Coordinate;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.finish.FinishActivity;
import com.senior.roadrunner.server.ConnectServer;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.tools.Distance;
import com.senior.roadrunner.tools.PathArea;
import com.senior.roadrunner.tools.Point;
import com.senior.roadrunner.tools.Polygon;
import com.senior.roadrunner.tools.RoadrunnerTools;
import com.senior.roadrunner.trackchooser.TrackMemberList;

@SuppressLint("NewApi")
public class MapsActivity extends Activity implements View.OnClickListener,
		LocationListener
// OnLocationChangedListener
{
	public static String rId = "";
	public static String fId = "";
	public static String savePath = RoadRunnerSetting.SDPATH + fId + ".xml";

	GoogleMap map;
	LocationManager myLocationManager;
	// MyLocationListener myLocationListener;
	private View btn_stop_track;
	private Button btn_track;
	private ArrayList<LatLngTimeData> latLngTimeData;
	// private Button btn_load_track;
	// private HistoryTrack historyTrack;
	// public static final String URLServer =
	// "http://roadrunner-5313180.dx.am/";// "http://192.168.1.111/";

	private Vector<Polygon> polygonsTrack = new Vector<Polygon>();
	private Polygon polygonStart;
	private boolean startCheck = false;
	private boolean pathCheck = false;

	private Marker marker;
	private Polyline poly;
	private PolylineOptions track;
	private boolean recordCheck = false;
	private Polygon polygonFinish;
	private static ArrayList<TrackMemberList> trackMemberList;
	private String trackPathData;
	private TextView txt_current_distace;
	private TextView txt_current_speed;
	private TextView txt_current_time;

	private Handler myHandler = new Handler();;
	// private ArrayList<LatLngTimeData> latLngTimeData;
	// private static final String SDCARD_TRACKER_XML = "/sdcard/tracker.xml";
	private long startTime = 0L;
	protected long timeInMillies = 0L;
	protected long timeSwap = 0L;
	protected long finalTime = 0L;
	private double totalDistance = 0;
	private RoadRunnerSetting roadRunnerSetting;
	private boolean countOut = false;
	private long startOutTime = 0L;
	private static ArrayList<TrackMemberList> trackMemberListTemp;
	private long timeOutInMillies;
	public static String mapcapPath = "";
	private ProgressBar progress_out_time;
	// Timer Thread
	private Runnable updateTimerMethod = new Runnable() {

		public void run() {
			timeInMillies = SystemClock.uptimeMillis() - startTime;
			finalTime = timeSwap + timeInMillies;

			int seconds = (int) (finalTime / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			// int milliseconds = (int) (finalTime % 1000);
			txt_current_time.setText("" + minutes + ":"
					+ String.format("%02d", seconds));
			if (countOut) {
				timeOutInMillies = SystemClock.uptimeMillis() - startOutTime;
				int sec = (int) (timeOutInMillies / 1000);
				if (sec > 5 && sec <= 15) {
					// Toast.makeText(
					// MapsActivity.this,
					// "Out of track  count "
					// + (sec-5) + " s",
					// 50).show();

					progress_out_time.setProgress((sec - 5) * 10);
				} else if (sec > 15) {
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								timeSwap += timeInMillies;
								myHandler.removeCallbacks(updateTimerMethod);
								Intent intent = new Intent(MapsActivity.this,
										MainActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								// No button clicked
								break;
							}
						}
					};
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MapsActivity.this);
					builder.setMessage("Leave race. you running out of track !")
							.setPositiveButton("Yes", dialogClickListener)
							.show();

					countOut = false;
				}

			}
			myHandler.postDelayed(this, 0);
		}
	};
	private String trackName;
	private View customMarker;
	private ImageView imageView;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);

		// get setting instance
		roadRunnerSetting = RoadRunnerSetting.getInstance();
		mapcapPath = Environment.getExternalStorageDirectory() + "/"
				+ "roadrunner/" + roadRunnerSetting.getFacebookId() + ".png";// set
																				// mapcap
																				// path
		fId = roadRunnerSetting.getFacebookId();

		Intent intent = getIntent();
		trackName = intent.getStringExtra("TrackName");
		trackMemberList = (ArrayList<TrackMemberList>) intent
				.getSerializableExtra("TrackMemberList");
		// trackMemberListTemp = new ArrayList<TrackMemberList>();
		// if(trackMemberList!=null)
		// trackMemberListTemp.addAll(trackMemberList);
		// set current Rid
		rId = trackMemberList.get(0).getrId();
		trackPathData = intent.getStringExtra("TrackPathData");
		initwidget();
		loadFile();

		track = new PolylineOptions();
		latLngTimeData = new ArrayList<LatLngTimeData>();
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.maps))
				.getMap();
		map.getUiSettings().setZoomControlsEnabled(false);
		// map.setMyLocationEnabled(true);
		// SupportMapFragment supportMapFragment = (SupportMapFragment)
		// getSupportFragmentManager()
		// .findFragmentById(R.id.maps);

		// map = supportMapFragment.getMap();
		// map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

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
		customMarker = ((LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.custom_marker_layout, null);
		imageView = (ImageView) customMarker.findViewById(R.id.profileIcon);
	}

	private void loadFile() {

		for (int i = 0; i < trackMemberList.size(); i++) {
			// System.out.println("TrackerDir : "
			// + trackMemberList.get(i).getTrackerDir());
			ConnectServer connectServerTrackMemberData = new ConnectServer(
					this, RoadRunnerSetting.URLServer + "/getTrackPath.php");
			connectServerTrackMemberData.addValue("Rdir",
					RoadRunnerSetting.URLServer
							+ trackMemberList.get(i).getTrackerDir());
			// setIndex when xml return from server
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
			if (i == 0) {
				MarkerOptions startMarker = new MarkerOptions()
						.position(new LatLng(lat, lng))
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.play)).title("Start");
				map.addMarker(startMarker);
			}
			if (i == data.size() - 1) {
				MarkerOptions endMarker = new MarkerOptions()
						.position(new LatLng(lat, lng))
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.flag)).title("End");
				map.addMarker(endMarker);
			}
		}

		Polyline p = map.addPolyline(options);
		p.setWidth(30);
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

		// for (int i = 0; i < polygonsTrack.size(); i++) {
		// // System.out.println("getside : "+polygons.get(i).getSides());
		// Polygon polygon = polygonsTrack.get(i);
		// PolygonOptions polygonOptions = new PolygonOptions();
		// for (int j = 0; j < polygon.getSides().size(); j++) {
		// polygonOptions.add(new LatLng(polygon.getSides().get(j)
		// .getStart().x, polygon.getSides().get(j).getStart().y));
		// if (i % 3 == 0)
		// polygonOptions.fillColor(Color.BLUE);
		// if (i % 3 == 1)
		// polygonOptions.fillColor(Color.GREEN);
		// if (i % 3 == 2)
		// polygonOptions.fillColor(Color.RED);
		//
		// }
		// // polygonOptions.fillColor(Color.BLUE);
		// map.addPolygon(polygonOptions.strokeWidth(2));
		// }

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
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(points.get(0).x, points.get(0).y), 15.0f));

	}

	// Check out/in path
	private void checkisInPath(Point point) {
		if (polygonStart != null && startCheck) {

			if (polygonStart.contains(point)) {
				Toast.makeText(this, "Start point : IN TRACK",
						Toast.LENGTH_SHORT).show();
				btn_track.setEnabled(true);
			} else {
				Toast.makeText(this, "Start point : OUT TRACK",
						Toast.LENGTH_SHORT).show();
				btn_track.setEnabled(false);
			}
		}
		if (polygonsTrack != null && pathCheck) {
			for (int i = 0; i < polygonsTrack.size(); i++) {
				Polygon polygon = polygonsTrack.get(i);
				if (polygon.contains(point)) {
					// Toast.makeText(this, "IN TRACK",
					// Toast.LENGTH_SHORT).show();
					progress_out_time.setProgress(0);
					countOut = false;
					break;
				}
				if (i == polygonsTrack.size() - 1) {
					Toast.makeText(this, "Path : OUT TRACK", Toast.LENGTH_SHORT)
							.show();
					if (!countOut) {
						startOutTime = SystemClock.uptimeMillis();
					}
					countOut = true;
				}
			}
		}
		if (polygonFinish != null && pathCheck) {
			if (polygonFinish.contains(point)) {
				try {
					takeSnapshot();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Toast.makeText(this, "Finish", Toast.LENGTH_SHORT).show();
				// btn_stop_track.setEnabled(true);
				// finish race
				recordCheck = false;
				myLocationManager.removeUpdates(this);
				// Save Track to file and set to trackMemberlist and sort by
				// duration time.
				saveTrackData();

				// stop timer
				timeSwap += timeInMillies;
				myHandler.removeCallbacks(updateTimerMethod);

				Intent intent = new Intent(this, FinishActivity.class);
				intent.putExtra("ClassName", "MapsActivity");
				intent.putExtra("TrackName", trackName);
				intent.putExtra("TrackMemberList", trackMemberList);
				startActivity(intent);
			} else {
				// btn_stop_track.setEnabled(false);
			}
		}
	}

	private void initwidget() {

		btn_track = (Button) findViewById(R.id.btn_track);
		btn_track.setOnClickListener(this);
		btn_stop_track = (Button) findViewById(R.id.btn_stop_track);
		btn_stop_track.setOnClickListener(this);
		// set Enable false for start
		// btn_stop_track.setEnabled(false);
		txt_current_distace = (TextView) findViewById(R.id.txt_curent_distance);
		txt_current_speed = (TextView) findViewById(R.id.txt_curent_speed);
		txt_current_time = (TextView) findViewById(R.id.txt_curent_time);
		txt_current_speed.setText("0");
		txt_current_distace.setText("0");
		txt_current_time.setText("00:00");

		progress_out_time = (ProgressBar) findViewById(R.id.progressBar);
		progress_out_time.setMax(100);
		progress_out_time.setBackgroundResource(drawable.alert_dark_frame);
		progress_out_time.setDrawingCacheBackgroundColor(Color.YELLOW);
	}

	public boolean isGpsEnable() {
		boolean isgpsenable = false;
		// String provider = Settings.Secure.getString(getContentResolver(),
		// Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		// if (!provider.equals("")) { // GPS is Enabled
		// isgpsenable = true;
		// }
		if (myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			isgpsenable = true;
		} else {
			isgpsenable = false;
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
			// startTimer
			startTime = SystemClock.uptimeMillis();
			myHandler.postDelayed(updateTimerMethod, 0);

			break;
		case R.id.btn_stop_track:
			// leave race
			recordCheck = false;
			myLocationManager.removeUpdates(this);
			timeSwap += timeInMillies;
			myHandler.removeCallbacks(updateTimerMethod);
			Intent intent = new Intent(MapsActivity.this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);

			// exitActivity();
			break;

		}

	}

	private void takeSnapshot() {
		if (map == null) {
			return;
		}

		final SnapshotReadyCallback callback = new SnapshotReadyCallback() {
			@Override
			public void onSnapshotReady(Bitmap snapshot) {
				// Callback is called from the main thread, so we can modify the
				// ImageView safely.
				try {

					FileOutputStream out = new FileOutputStream(mapcapPath);

					// above "/mnt ..... png" => is a storage path (where image
					// will be stored) + name of image you can customize as per
					// your Requirement

					snapshot.compress(Bitmap.CompressFormat.PNG, 90, out);
					roadRunnerSetting.setMapScreen(snapshot);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		map.snapshot(callback);
		// map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
		// @Override
		// public void onMapLoaded() {
		// map.snapshot(callback);
		// }
		// });

	}

	@SuppressLint("UseValueOf")
	private void saveTrackData() {
		// save file to sdcard
		savePath = RoadRunnerSetting.SDPATH + fId + ".xml";
		TrackDataBase.saveXmlFile(latLngTimeData, savePath);
		Toast.makeText(getApplicationContext(), "Save data", Toast.LENGTH_SHORT)
				.show();
		TrackMemberList myTrack = new TrackMemberList();
		myTrack.setCalories(0);
		myTrack.setDuration(finalTime);
		myTrack.setfId(fId);
		myTrack.setDistance(totalDistance);
		myTrack.setAVGSpeed((totalDistance * 3600) / (finalTime / 1000));
		myTrack.setfName(roadRunnerSetting.getFacebookName());
		// myTrack.setProfileImg(RoadRunnerSetting.getProfileIcon());
		// myTrack.setRank(rank);
		myTrack.setrId(rId);
		// myTrack.setTrackData(latLngTimeData);
		myTrack.setTrackerDir("tracker/" + rId + "/"
				+ roadRunnerSetting.getFacebookId() + ".xml");

		/*
		 * replace a new record
		 */
		for (int i = 0; i < trackMemberList.size(); i++) {
			if (trackMemberList.get(i).getfId()
					.equals(roadRunnerSetting.getFacebookId())) {
				trackMemberList.remove(i);
			}
		}
		trackMemberList.add(myTrack);

		// Sorting by duration Time.
		Collections.sort(trackMemberList, new Comparator<TrackMemberList>() {
			@Override
			public int compare(TrackMemberList c1, TrackMemberList c2) {
				return new Double(c1.getDuration()).compareTo(new Double(c2
						.getDuration()));
			}
		});
		// Set rank
		for (int i = 0; i < trackMemberList.size(); i++) {
			System.out.println(trackMemberList.get(i).getDuration());
			trackMemberList.get(i).setRank(i + 1);
		}
	}

	private void raceThread() {
		// List<LatLngTimeData> data = TrackDataBase
		// .loadXmlFile(SDCARD_TRACKER_XML);
		for (int i = 0; i < trackMemberList.size(); i++) {
			// List<LatLngTimeData> data =
			// trackMemberList.get(i).getTrackData();
			new RaceThread(trackMemberList.get(i), map, this);
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
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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
						10, this);
			} else {
				final List<String> providers = myLocationManager
						.getProviders(true);

				for (final String provider : providers) {
					myLocationManager.requestLocationUpdates(provider, 1000,
							10, this);
				}
			}

			myLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 10, this);
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
		System.out.println("Acuracy : " + loc.hasAccuracy());
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		Point point = new Point(latitude, longitude);
		LatLng coord = new LatLng(loc.getLatitude(), loc.getLongitude());
		if (marker != null) {
			marker.remove();
		}

		if (roadRunnerSetting.getProfileIcon() != null) {
			imageView.setImageBitmap(roadRunnerSetting.getProfileIcon());
		}
		marker = map
				.addMarker(new MarkerOptions()
						.position(coord)
						.icon(BitmapDescriptorFactory
								.fromBitmap(createDrawableFromView(this,
										customMarker))).title("Me"));
		map.animateCamera(CameraUpdateFactory.newLatLng(coord));

		checkisInPath(point);
		if (recordCheck) {
			recordTrack(loc);
		} else {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 15.0f));
		}
	}

	private Bitmap modifyCanvas(Bitmap bitmap) {
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmp = Bitmap.createBitmap(65, 65, conf);
		Canvas canvas1 = new Canvas(bmp);

		// paint defines the text color,
		// stroke width, size
		Paint color = new Paint();
		color.setTextSize(35);
		color.setColor(Color.BLACK);

		// modify canvas
		canvas1.drawBitmap(bitmap, 0, 0, color);
		// canvas1.drawText(listTracker.getfName(), 30, 40, color);
		return bmp;
	}

	// Convert a view to bitmap
	public static Bitmap createDrawableFromView(Context context, View view) {
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

		return bitmap;
	}

	@SuppressLint("SimpleDateFormat")
	private void recordTrack(Location loc) {
		DecimalFormat df = new DecimalFormat("0.00");
		if (loc.hasSpeed()) {
			// Toast.makeText(this, "Speed : " + loc.getSpeed() + " KPH",
			// Toast.LENGTH_SHORT).show();

			String gpsSpeed = df.format(loc.getSpeed() * 3.6);
			txt_current_speed.setText(gpsSpeed);
		}

		LatLng coord = new LatLng(loc.getLatitude(), loc.getLongitude());

		track.add(coord);
		if (poly != null) {
			poly.remove();
		}
		poly = map.addPolyline(track);
		poly.setColor(Color.RED);
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
	}

	// Return track.xml from dataBase server
	public synchronized void setMemberTrack(String result, int index) {
		String path = RoadRunnerSetting.SDPATH
				+ trackMemberList.get(index).getTrackerDir();
		RoadrunnerTools.writeStringToFile(path, result);
		// try {
		// String s = RoadRunnerSetting.SDPATH
		// + trackMemberList.get(index).getTrackerDir();
		// File f = new File(s);
		// File pf = f.getParentFile();
		// if (pf != null) {
		// pf.mkdirs();
		// }
		// if ((pf.exists()) && (pf.isDirectory())) {
		// if ((!f.exists()) || (!f.isFile())) {
		// f.createNewFile();
		// }
		// if ((f.exists()) || (f.isFile())) {
		// FileOutputStream os = null;
		// os = new FileOutputStream(s, false);
		// if (os != null) {
		// OutputStreamWriter myOutWriter = new OutputStreamWriter(
		// os);
		// myOutWriter.write(result);//
		// myOutWriter.close();
		// }
		// os.flush();
		// os.close();
		// }
		// }
		// } catch (IOException e) {
		// String s = e.toString();
		// System.out.println(s);
		// }

		// trackMemberList.get(index).setTrackData(trackData);

	}

	public static ArrayList<TrackMemberList> getTrackMemberList() {
		return trackMemberList;

	}

	private void exitActivity() {
		finish();
		super.onDestroy();

	}
}
