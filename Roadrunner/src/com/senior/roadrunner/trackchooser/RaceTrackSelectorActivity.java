package com.senior.roadrunner.trackchooser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.senior.roadrunner.R;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.racetrack.MapsActivity;
import com.senior.roadrunner.server.ConnectServer;
import com.senior.roadrunner.server.GetMyProfulePicture;
import com.senior.roadrunner.setting.RoadRunnerSetting;

@SuppressLint("NewApi")
public class RaceTrackSelectorActivity extends Activity implements
		SearchView.OnQueryTextListener, SearchView.OnCloseListener,
		OnClickListener, DrawerListener, StatusCallback, LocationListener {

	// private static final String URLServer =
	// "http://roadrunner-5313180.dx.am/";// "http://192.168.1.111/";//
	// 192.168.1.173//http://192.168.1.117/
	ListView list;
	TrackListAdapter adapter;
	public Activity activity = null;
	public ArrayList<TrackList> trackList;
	private ConnectServer connectServer;
	private SearchView mSearchView;
	private GoogleMap map;
	private Resources res;
	private Button raceBtn;
	private TextView trackDataTxtView;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	protected CharSequence mTitle;
	protected CharSequence mDrawerTitle;

	private Animation animAlpha;
	private int listPosition;
	private Location currentLoc;

	private LocationManager mLocationManager;
	private MarkerOptions startMarker;
	private MarkerOptions endMarker;
	private RoadRunnerSetting roadRunnerSetting;
	private Animation animTranslate;
	private Menu menu;
	private Marker marker;
	private LatLng currentcoord;
	private Animation animRotate;
	private ImageView openCloseImageView;
	private UploadActionBarView mUploadActionView;
	private ImageView imageView;
	private View customMarker;
	private ListView listView;
	private static final String TAG = "RaceTrackSelectorActivity";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.racetrack_layout);

		roadRunnerSetting = RoadRunnerSetting.getInstance();
		activity = this;
		invalidateOptionsMenu();
		/******** Take some data in Arraylist ( CustomListViewValuesArr ) ***********/

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_trackselector);
		mDrawerList = (ListView) findViewById(R.id.right_drawer);
		mDrawerLayout.setDrawerListener(this);
		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		res = getResources();
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.maps))
				.getMap();
		raceBtn = (Button) findViewById(R.id.race_btn);
		animAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
		animTranslate = AnimationUtils.loadAnimation(this,
				R.anim.anim_translate);
		animRotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
		raceBtn.setVisibility(View.INVISIBLE);
		raceBtn.setOnClickListener(this);
		// raceBtn.setAlpha(0.0f);
		trackDataTxtView = (TextView) findViewById(R.id.track_data_txtview);
		// facebookGetData();s
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		currentLoc = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (currentLoc == null) {
			currentLoc = mLocationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		trackList = new ArrayList<TrackList>();
		setListData();
		listView = (ListView) findViewById(R.id.infoListView);
		customMarker = ((LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.custom_marker_layout, null);
		imageView = (ImageView) customMarker.findViewById(R.id.profileIcon);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLocationManager != null)
			mLocationManager.removeUpdates(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mLocationManager != null)
			mLocationManager.removeUpdates(this);
	}

	private void setListenCurrentLocation() {
		// set request location to refresh track list
		trackList = new ArrayList<TrackList>();

		final Criteria criteria = new Criteria();

		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setSpeedRequired(true);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		final String bestProvider = mLocationManager.getBestProvider(criteria,
				true);

		if (bestProvider != null && bestProvider.length() > 0) {
			mLocationManager.requestLocationUpdates(bestProvider, 1000, 15,
					this);
		} else {
			final List<String> providers = mLocationManager.getProviders(true);

			for (final String provider : providers) {
				mLocationManager.requestLocationUpdates(provider, 1000, 15,
						this);
			}
		}

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

	public boolean isGpsEnable() {
		boolean isgpsenable = false;
		// String provider =
		// Settings.Secure.getString(this.getContentResolver(),
		// Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		// if (provider.equals("network,gps")) { // GPS is Enabled
		// isgpsenable = true;
		// }
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			isgpsenable = true;
		} else {
			isgpsenable = false;
		}
		return isgpsenable;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.race_sellector_menu, menu);
		// MenuItem searchItem = menu.findItem(R.id.action_websearch);
		// mSearchView = (SearchView) searchItem.getActionView();
		// setupSearchView(searchItem);

		MenuItem item = menu.findItem(R.id.action_open_close_drawer);
		mUploadActionView = (UploadActionBarView) item.getActionView();
		mUploadActionView.getAnimateImageView().setImageResource(R.drawable.al);
		// onOptionsItemSelected will NOT be called for a custom View,
		// so set a OnClickListener and handle it ourselves.
		mUploadActionView.setOnClickListener(this);

		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			onBackPressed();
			return true;
		case R.id.action_current_location:
			setListenCurrentLocation();
			return true;
		case R.id.action_open_close_drawer:
			// if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			// mDrawerLayout.closeDrawers();
			// // ImageView imageView = new ImageView(this);
			// openCloseImageView.setImageResource(R.drawable.al);
			// // item.setActionView(imageView);
			// openCloseImageView.startAnimation(animRotate);
			// // item.setIcon(R.drawable.al);
			// } else {
			// mDrawerLayout.openDrawer(mDrawerList);
			// // ImageView imageView = new ImageView(this);
			// openCloseImageView.setImageResource(R.drawable.ar);
			// // item.setActionView(imageView);
			// openCloseImageView.startAnimation(animRotate);
			// // item.setIcon(R.drawable.ar);
			// }
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("static-access")
	@SuppressLint({ "NewApi", "NewApi" })
	private void setupSearchView(MenuItem searchItem) {
		if (isAlwaysExpanded()) {
			mSearchView.setIconifiedByDefault(false);
		} else {
			searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
					| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		}
		SearchManager searchManager = (SearchManager) getSystemService(this
				.getApplicationContext().SEARCH_SERVICE);
		if (searchManager != null) {
			List<SearchableInfo> searchables = searchManager
					.getSearchablesInGlobalSearch();
			SearchableInfo info = searchManager
					.getSearchableInfo(getComponentName());
			for (SearchableInfo inf : searchables) {
				if (inf.getSuggestAuthority() != null
						&& inf.getSuggestAuthority().startsWith("applications")) {
					info = inf;
				}
			}
			mSearchView.setSearchableInfo(info);
		}
		mSearchView.setOnQueryTextListener(this);
	}

	protected boolean isAlwaysExpanded() {
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_websearch).setVisible(true);
		return super.onPrepareOptionsMenu(menu);
	}

	/****** Function to set data in ArrayList *************/
	public void setListData() {
		if (currentLoc == null)
			return;
		connectServer = new ConnectServer(this, RoadRunnerSetting.URLServer
				+ "/racetracklist.php");
		connectServer.addValue("Latitude", currentLoc.getLatitude() + "");
		connectServer.addValue("Longitude", currentLoc.getLongitude() + "");
		connectServer.setRequestTag(ConnectServer.TRACK_LIST);
		connectServer.execute();
		if (null != mUploadActionView) {
			mUploadActionView.animateBackground();
		}

	}

	public void onItemClick(int mPosition) {
		this.listPosition = mPosition;
		// set listview hiding when user choose tracklist
		listView.setVisibility(View.GONE);
		raceBtn.setVisibility(View.GONE);
		TrackList tempValues = (TrackList) trackList.get(mPosition);
		Toast.makeText(
				this,
				String.format("Total Distance : %.2f km",
						tempValues.getDistance()), 3000).show();
		// trackDataTxtView.setText("" + tempValues.getRaceTrackName() +
		// " \nRid:"
		// + tempValues.getrId() + " \nLatLon:"
		// + tempValues.getDoubleLat() + "\t" + tempValues.getDoubleLon()
		// + " \nRdir:" + tempValues.getRdir());

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				tempValues.getDoubleLat(), tempValues.getDoubleLon()), 15.0f));

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(mPosition, true);
		// mDrawerList.setSelection(mPosition);
		setTitle(tempValues.getRaceTrackName());
		mDrawerLayout.closeDrawer(mDrawerList);
		// set track name in Roadrunner setting.
		roadRunnerSetting.setRaceTrackName(tempValues.getRaceTrackName());
		// get trackpath from server.
		if (trackList.get(listPosition).getTrackData() != null
				|| trackList.get(listPosition).getTrackMemberList() != null) {
			ArrayList<LatLngTimeData> trackPathData = (ArrayList<LatLngTimeData>) TrackDataBase
					.loadXmlString(trackList.get(listPosition).getTrackData());
			drawTrackPath(trackPathData);
			printTrackData(trackList.get(listPosition).getTrackMemberList());
			return;
		}

		connectServer = new ConnectServer(this, RoadRunnerSetting.URLServer
				+ "/getTrackPath.php");
		connectServer.addValue("Rdir", RoadRunnerSetting.URLServer
				+ "/racetrack/" + tempValues.getrId() + ".xml");
		connectServer.setRequestTag(ConnectServer.TRACK_PATH);
		connectServer.setIndex(mPosition);
		connectServer.execute();

		// getMember of mPosition Race Track.
		connectServer = new ConnectServer(this, RoadRunnerSetting.URLServer
				+ "/getTrackMember.php");
		connectServer.addValue("Rid", tempValues.getrId());
		connectServer.setRequestTag(ConnectServer.TRACK_MEMBER);
		connectServer.setIndex(mPosition);
		connectServer.execute();
		if (null != mUploadActionView) {
			mUploadActionView.animateBackground();
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle("Track Name");
		getActionBar().setSubtitle(mTitle);
	}

	@Override
	public boolean onClose() {

		return false;
	}

	@Override
	public boolean onQueryTextChange(String arg0) {

		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		return false;
	}

	public synchronized void setTrackList(String result) {
		try {
			// get race track data from database server to set ListAdapter.
			JSONArray jsonArr = new JSONArray(result);

			for (int i = 0; i < jsonArr.length(); i++) {
				final TrackList sched = new TrackList();
				JSONObject jsonObject = new JSONObject(jsonArr.getString(i));

				/******* Firstly take data in model object ******/
				sched.setRaceTrackName(jsonObject.getString("Rname"));
				sched.setrId(jsonObject.getString("Rid"));
				// String[] location =
				// jsonObject.getString("Location").split(",");
				// sched.setDoubleLat(Double.parseDouble(location[0]));
				// sched.setDoubleLon(Double.parseDouble(location[1]));
				sched.setDoubleLat(jsonObject.getDouble("Latitude"));
				sched.setDoubleLon(jsonObject.getDouble("Longitude"));
				sched.setRdir(jsonObject.getString("Rdir"));
				sched.setDistance(jsonObject.getDouble("Distance"));
				/******** Take Model Object in ArrayList **********/
				trackList.add(sched);
				/**************** Create Custom Adapter *********/
				adapter = new TrackListAdapter(activity, trackList, res);

				// set up the drawer's list view with items and click listener
				mDrawerList.setAdapter(adapter);
				if (null != mUploadActionView) {
					mUploadActionView.stopAnimatingBackground();
				}
			}

		} catch (JSONException e) {
			// Draw track on map from xml string.

		}
	}

	public synchronized void setTrackPath(String result, int index) {

		// Draw track on map from xml string.
		String xmlTrackData = result;
		ArrayList<LatLngTimeData> trackPathData = (ArrayList<LatLngTimeData>) TrackDataBase
				.loadXmlString(result);
		drawTrackPath(trackPathData);
		trackList.get(index).setTrackData(xmlTrackData);
		if (null != mUploadActionView) {
			mUploadActionView.stopAnimatingBackground();
		}
	}

	// Drawing on map function.
	public void drawTrackPath(ArrayList<LatLngTimeData> trackPathData) {

		map.clear();
		setCurrentMarker();
		PolylineOptions options = new PolylineOptions();
		if (trackPathData == null) {
			Log.e(TAG, "trackPathData is null");
			return;
		}
		for (int i = 0; i < trackPathData.size(); i++) {
			LatLng point = new LatLng(trackPathData.get(i).getCoordinate()
					.getLat(), trackPathData.get(i).getCoordinate().getLng());
			if (i == 0) {
				startMarker = new MarkerOptions()
						.position(point)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.play)).title("Start");
				map.addMarker(startMarker);
			}
			if (i == trackPathData.size() - 1) {
				endMarker = new MarkerOptions()
						.position(point)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.flag)).title("End");
				map.addMarker(endMarker);
			}
			options.add(point);
		}
		options.color(Color.RED);
		options.width(8);
		map.addPolyline(options);

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				trackPathData.get(0).getCoordinate().getLat(), trackPathData
						.get(0).getCoordinate().getLng()), 15.0f));
	}

	public synchronized void setTeackMember(String result, int index) {

		ArrayList<TrackMemberList> trackMemberList = new ArrayList<TrackMemberList>();
		try {
			// get race track data from database server to set ListAdapter.
			JSONArray jsonArr = new JSONArray(result);

			for (int i = 0; i < jsonArr.length(); i++) {
				final TrackMemberList sched = new TrackMemberList();
				JSONObject jsonObject = new JSONObject(jsonArr.getString(i));

				/******* Firstly take data in model object ******/
				sched.setfId(jsonObject.getString("Fid"));
				sched.setfName(jsonObject.getString("fName"));
				sched.setrId(jsonObject.getString("Rid"));
				sched.setRank(i + 1);// rank has been query by duration.
				sched.setTrackerDir(jsonObject.getString("Trackerdir"));
				sched.setDuration(Integer.parseInt(jsonObject.getString("Time")));
				/******** Take Model Object in ArrayList **********/
				trackMemberList.add(sched);

			}
			// load profile picture to sd card
			RaceTrackBitmapProfile getBitmapProfile = new RaceTrackBitmapProfile(
					trackMemberList);
			getBitmapProfile.start();
		} catch (JSONException e) {

		}
		trackList.get(index).setTrackMemberList(trackMemberList);
		printTrackData(trackMemberList);
	}

	@SuppressLint("ShowToast")
	private synchronized void printTrackData(
			ArrayList<TrackMemberList> trackMemberList) {
		String trackMemberString = "";
		if (trackMemberList == null) {
			return;
		}
		// for (int i = 0; i < trackMemberList.size(); i++) {
		// trackMemberString = trackMemberString
		// + trackMemberList.get(i).getRank() + " "
		// + trackMemberList.get(i).getfName() + " "
		// + trackMemberList.get(i).getDuration() + "\n";
		//
		// }
		//
		// Toast.makeText(activity, trackMemberString, 3000).show();
		// View v = getLayoutInflater().inflate(R.layout.info_layout,
		// null);

		if (trackMemberList != null) {
			InfoAdaptor aa = new InfoAdaptor(activity, trackMemberList);
			listView.setVisibility(View.VISIBLE);
			listView.setAdapter(aa);
			listView.startAnimation(animTranslate);
			// go button enable
			raceBtn.setVisibility(View.VISIBLE);
			raceBtn.startAnimation(animAlpha);
		}
		// map.setInfoWindowAdapter(new InfoWindowAdapter() {
		//
		// @Override
		// public View getInfoWindow(Marker marker) {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		// @Override
		// public View getInfoContents(Marker marker) {
		// // info view
		// System.out.println(marker.getTitle());
		// if (marker.getTitle().equals("Start")) {
		// View v = getLayoutInflater().inflate(R.layout.info_layout,
		// null);
		//
		// if (trackMemberList != null) {
		// InfoAdaptor aa = new InfoAdaptor(activity,
		// trackMemberList);
		// final ListView ll = (ListView) v
		// .findViewById(R.id.finishListView);
		// ll.setAdapter(aa);
		// }
		//
		// return v;
		// }
		// return null;
		//
		// }
		// });

	}

	public void cannotConnectToServer() {
		Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_LONG)
				.show();

	}

	@Override
	public void onClick(View v) {
		if (v.equals(raceBtn)) {

			// if (trackMemberList == null || trackPathData == null) {
			// return;
			// }
			boolean isGPSEnabled = mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (!isGPSEnabled) {
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
						.setPositiveButton("Yes", dialogClickListener).show();
			} else {
				Intent intent = new Intent(this, MapsActivity.class);
				intent.putExtra("TrackName", trackList.get(listPosition)
						.getRaceTrackName());
				intent.putExtra("TrackMemberList", trackList.get(listPosition)
						.getTrackMemberList());
				intent.putExtra("TrackPathData", trackList.get(listPosition)
						.getTrackData());
				startActivity(intent);
			}
		}
		if (v == mUploadActionView) {
			// Action Bar item has been clicked, do something...
			// When you later want to animate the background, or stop the
			// animate, just call:
			if (null != mUploadActionView) {
				// To start the animation

				if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
					// mUploadActionView.stopAnimatingBackground();
					mDrawerLayout.closeDrawers();
					// mUploadActionView.animateArrowLeft();
					// mUploadActionView.animateBackground();
				} else {
					// mUploadActionView.stopAnimatingBackground();
					mDrawerLayout.openDrawer(mDrawerList);
					// mUploadActionView.animateArrowRight();
					// mUploadActionView.animateBackground();
				}
				// Or to stop it
				//
			}
		}

	}

	@Override
	public void onDrawerClosed(View arg0) {
		// for (int i = 0; i < menu.size(); i++) {
		// MenuItem array_element = menu.getItem(i);
		// if (array_element.getItemId() == R.id.action_open_close_drawer) {
		// ImageView imageView = new ImageView(this);
		// imageView.setImageResource(R.drawable.ar);
		// array_element.setActionView(imageView);
		// array_element.getActionView().startAnimation(animRotate);
		// // array_element.setIcon(R.drawable.ar);
		//
		// }
		//
		// }
		if (null != mUploadActionView) {
			mUploadActionView.animateArrowLeft();
		}
	}

	@Override
	public void onDrawerOpened(View arg0) {
		// for (int i = 0; i < menu.size(); i++) {
		// MenuItem array_element = menu.getItem(i);
		// if (array_element.getItemId() == R.id.action_open_close_drawer) {
		// ImageView imageView = new ImageView(this);
		// imageView.setImageResource(R.drawable.al);
		// array_element.setActionView(imageView);
		// array_element.getActionView().startAnimation(animRotate);
		// // array_element.setIcon(R.drawable.al);
		//
		// }
		//
		// }
		if (null != mUploadActionView) {
			mUploadActionView.animateArrowRight();
		}
	}

	@Override
	public void onDrawerSlide(View arg0, float arg1) {
		// System.out.println("Drawer slide"+ arg0);

	}

	@Override
	public void onDrawerStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {

	}

	@Override
	public void onLocationChanged(Location location) {

		this.currentLoc = location;
		currentcoord = new LatLng(location.getLatitude(),
				location.getLongitude());
		if (trackList.size() > 0) {
			return;
		}
		setListData();
		setCurrentMarker();

		mLocationManager.removeUpdates(this);

	}

	private void setCurrentMarker() {
		if (currentcoord == null) {
			return;
		}
		if (marker != null) {
			marker.remove();
		}
		if (roadRunnerSetting.getProfileIcon() != null) {
			imageView.setImageBitmap(roadRunnerSetting.getProfileIcon());
		} else {
			new GetMyProfulePicture();
		}
		marker = map
				.addMarker(new MarkerOptions()
						.position(currentcoord)
						.icon(BitmapDescriptorFactory
								.fromBitmap(createDrawableFromView(this,
										customMarker))).title("Me"));
		map.animateCamera(CameraUpdateFactory
				.newLatLngZoom(currentcoord, 15.0f));
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
}
