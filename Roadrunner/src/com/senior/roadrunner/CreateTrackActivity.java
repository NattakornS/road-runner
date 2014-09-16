package com.senior.roadrunner;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import android.R.drawable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.senior.roadrunner.data.Coordinate;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.finish.FinishActivity;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.tools.Distance;
import com.senior.roadrunner.tools.Polygon;
import com.senior.roadrunner.trackchooser.TrackMemberList;

@SuppressLint("NewApi")
public class CreateTrackActivity extends Activity implements
		View.OnClickListener, LocationListener
// OnLocationChangedListener
{
	public static String rId = "";
	public static String fId = "";
	public static String savePath = RoadRunnerSetting.SDPATH + fId + ".xml";

	GoogleMap map;
	LocationManager myLocationManager;
	// MyLocationListener myLocationListener;
	private Button btn_stop_track;
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
			myHandler.postDelayed(this, 0);
		}
	};
	private EditText nameInput;
	protected String trackName;
	private View customMarker;
	private ImageView imageView;
	private TextView txt_acuracy;
	private Bitmap profileBitmap;
	private String myProfilePath;
	protected Bitmap profileIcon;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);
		setTitle("Create Track");
		trackMemberList = new ArrayList<TrackMemberList>();
		// get setting instance
		roadRunnerSetting = RoadRunnerSetting.getInstance();
		mapcapPath = Environment.getExternalStorageDirectory() + "/"
				+ "roadrunner/" + roadRunnerSetting.getFacebookId() + ".png";// set
		myProfilePath = Environment.getExternalStorageDirectory() + "/"
				+ "roadrunner/img/" + roadRunnerSetting.getFacebookId() + ".png";																		// mapcap
																				// path
		fId = roadRunnerSetting.getFacebookId();
		initwidget();

		track = new PolylineOptions();
		latLngTimeData = new ArrayList<LatLngTimeData>();
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.maps))
				.getMap();
		map.getUiSettings().setZoomControlsEnabled(false);
		// ////////////////////// GPS tracker //////////////////////

		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		enableGPSListener();

		customMarker = ((LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.custom_marker_layout, null);
		imageView = (ImageView) customMarker.findViewById(R.id.profileIcon);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this)
				.memoryCacheExtraOptions(480, 800)
				// default = device screen dimensions
				.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
				.memoryCacheSize(2 * 1024 * 1024)
				.discCacheSize(50 * 1024 * 1024).discCacheFileCount(100)
				.writeDebugLogs()
				// .discCache(new UnlimitedDiscCache(cacheDir)) // default
				.build();
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
		String name = "https://graph.facebook.com/" + roadRunnerSetting.getFacebookId()
				+ "/picture?width=75&height=75";
		imageLoader.loadImage(name, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				// Do whatever you want with Bitmap
				imageView.setImageBitmap(loadedImage);
				profileIcon = createDrawableFromView(CreateTrackActivity.this, customMarker);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isGpsEnable())
			enableGPSListener();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (recordCheck) {
			recordCheck = false;
			myLocationManager.removeUpdates(this);
			timeSwap += timeInMillies;
			myHandler.removeCallbacks(updateTimerMethod);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (recordCheck) {
			recordCheck = false;
			myLocationManager.removeUpdates(this);
			timeSwap += timeInMillies;
			myHandler.removeCallbacks(updateTimerMethod);
		}
	}

	private void initwidget() {
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		btn_track = (Button) findViewById(R.id.btn_track);
		btn_track.setText("Start");
		btn_track.setOnClickListener(this);
		btn_stop_track = (Button) findViewById(R.id.btn_stop_track);
		btn_stop_track.setText("Finish");
		btn_stop_track.setOnClickListener(this);
		btn_stop_track.setEnabled(false);
		// set Enable false for start
		// btn_stop_track.setEnabled(false);
		txt_current_distace = (TextView) findViewById(R.id.txt_curent_distance);
		txt_current_speed = (TextView) findViewById(R.id.txt_curent_speed);
		txt_current_time = (TextView) findViewById(R.id.txt_curent_time);
		txt_current_speed.setText("0");
		txt_current_distace.setText("0");
		txt_current_time.setText("00:00");

		txt_acuracy = (TextView) findViewById(R.id.txt_acuracy);

		progress_out_time = (ProgressBar) findViewById(R.id.progressBar);
		progress_out_time.setMax(100);
		progress_out_time.setBackgroundResource(drawable.alert_dark_frame);
		progress_out_time.setDrawingCacheBackgroundColor(Color.YELLOW);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.race_sellector_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			if (recordCheck) {
				recordCheck = false;
				myLocationManager.removeUpdates(this);
				timeSwap += timeInMillies;
				myHandler.removeCallbacks(updateTimerMethod);
			}
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean isGpsEnable() {
		boolean isgpsenable = false;
		// String provider = Settings.Secure.getString(getContentResolver(),
		// Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		// Toast.makeText(this,
		// "GPS Provider : "+provider+"  "+LocationManager.GPS_PROVIDER,
		// 2000).show();
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
			btn_stop_track.setEnabled(true);
			// startTimer
			startTime = SystemClock.uptimeMillis();
			myHandler.postDelayed(updateTimerMethod, 0);

			break;
		case R.id.btn_stop_track:
			if (latLngTimeData == null) {
				return;
			}
			try {
				takeSnapshot();
			} catch (Exception e) {
				e.printStackTrace();
			}
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						if (nameInput.getText().length() > 20) {
							trackName = nameInput.getText().toString()
									.substring(0, 20);
						} else {
							trackName = nameInput.getText().toString();
						}
						Toast.makeText(CreateTrackActivity.this,
								"Track name : " + trackName, Toast.LENGTH_SHORT)
								.show();
						// btn_stop_track.setEnabled(true);
						// finish race
						recordCheck = false;
						myLocationManager
								.removeUpdates(CreateTrackActivity.this);
						// Save Track to file and set to trackMemberlist and
						// sort by
						// duration time.
						saveTrackData();

						// stop timer
						timeSwap += timeInMillies;
						myHandler.removeCallbacks(updateTimerMethod);

						Intent intent = new Intent(CreateTrackActivity.this,
								FinishActivity.class);
						intent.putExtra("ClassName", "CreateTrackActivity");
						intent.putExtra("TrackName", trackName.toString());
						intent.putExtra("TrackMemberList", trackMemberList);
						startActivity(intent);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						break;
					}
				}
			};
			if (latLngTimeData.size() < 5) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						CreateTrackActivity.this);
				builder.setMessage(
						"Your track is too short or gps point is less than 5 points. Please continue create track.")
						.show();
				return;
			} else {
				nameInput = new EditText(this);
				nameInput.setSingleLine(true);
				nameInput.setSaveEnabled(true);
				nameInput.setHint("Name not longer than 20 charecters");
				AlertDialog.Builder builder = new AlertDialog.Builder(
						CreateTrackActivity.this);
				builder.setMessage("Name your track !")
						.setPositiveButton("OK", dialogClickListener)
						.setView(nameInput).show();
			}
			// leave race
			recordCheck = false;
			myLocationManager.removeUpdates(this);
			timeSwap += timeInMillies;
			myHandler.removeCallbacks(updateTimerMethod);
			// Intent intent = new Intent(CreateTrackActivity.this,
			// MainActivity.class);
			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// startActivity(intent);

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
		myTrack.setRank(1);
		myTrack.setrId(rId);
		// myTrack.setTrackData(latLngTimeData);
		myTrack.setTrackerDir("tracker/" + rId + "/"
				+ roadRunnerSetting.getFacebookId() + ".xml");
		trackMemberList.add(myTrack);

	}

	private void enableGPSListener() {
//		final Criteria criteria = new Criteria();
//
//		criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		criteria.setSpeedRequired(true);
//		criteria.setAltitudeRequired(false);
//		criteria.setBearingRequired(false);
//		criteria.setCostAllowed(true);
//		criteria.setPowerRequirement(Criteria.POWER_LOW);
//
//		final String bestProvider = myLocationManager.getBestProvider(criteria,
//				true);
//
//		if (bestProvider != null && bestProvider.length() > 0) {
//			myLocationManager.requestLocationUpdates(bestProvider, 1000, 10,
//					this);
//		} else {
//			final List<String> providers = myLocationManager.getProviders(true);
//
//			for (final String provider : providers) {
//				myLocationManager.requestLocationUpdates(provider, 1000, 10,
//						this);
//			}
//		}

		myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1500, 10, this);
		Toast.makeText(getApplicationContext(), "Track data",
				Toast.LENGTH_SHORT).show();

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
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Setting GPS?")
					.setPositiveButton("Yes", dialogClickListener).show();
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
		txt_acuracy.setText("Acuracy : " + loc.getAccuracy()+" m");
		LatLng coord = new LatLng(loc.getLatitude(), loc.getLongitude());
		if (marker != null) {
			marker.remove();
		}
		if(profileIcon == null){
			marker = map.addMarker(new MarkerOptions()
			.position(coord)
			.icon(BitmapDescriptorFactory
					.fromBitmap(createDrawableFromView(this,
							customMarker))).title("Me"));
		}else{
			marker = map.addMarker(new MarkerOptions()
			.position(coord)
			.icon(BitmapDescriptorFactory
					.fromBitmap(profileIcon)).title("Me"));
		}
		
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 15.0f));
		if (loc.getAccuracy() < 40.0) {
			if (recordCheck) {
				recordTrack(loc);
			} else {
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(coord,
						15.0f));
			}
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
		DecimalFormat df = new DecimalFormat("0.0");
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

		poly.setWidth(5);
		poly.setGeodesic(true);

		// String timeStamp = DateUtils.fromDate(date );
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String timeStamp = sdf.format(new Date(loc.getTime()));
		Coordinate coordinate = new Coordinate(coord.latitude, coord.longitude);
		latLngTimeData.add(new LatLngTimeData(coordinate, timeStamp));

		// dstance update
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

	public void cannotConnectToServer() {
		Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_LONG)
				.show();
	}

	public void errorConnectToServer() {
		Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
	}

	public void setList(String result) {

	}

	public static ArrayList<TrackMemberList> getTrackMemberList() {
		return trackMemberList;

	}

	private void exitActivity() {
		finish();
		super.onDestroy();

	}
}
