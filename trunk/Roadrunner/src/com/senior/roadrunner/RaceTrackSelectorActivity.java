package com.senior.roadrunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.model.GraphUser;
import com.facebook.SessionState;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.racetrack.TrackList;
import com.senior.roadrunner.racetrack.TrackListAdapter;
import com.senior.roadrunner.racetrack.TrackMemberList;
import com.senior.roadrunner.server.ConnectServer;
import com.senior.roadrunner.setting.RoadRunnerSetting;

@SuppressLint("NewApi")
public class RaceTrackSelectorActivity extends Activity implements
		SearchView.OnQueryTextListener, SearchView.OnCloseListener,
		OnClickListener, DrawerListener, StatusCallback {

	public static String mapcapPath = Environment
			.getExternalStorageDirectory() + "/" + "roadrunner/"
			+ RoadRunnerSetting.getFacebookId() +".png";
	private static final String URLServer = "http://roadrunner-5313180.dx.am/";// "http://192.168.1.111/";//
	private static final String FACEBOOK_INFORMATION_URL = "http://graph.facebook.com/";
	// 192.168.1.173//http://192.168.1.117/
	ListView list;
	TrackListAdapter adapter;
	public Activity CustomListView = null;
	public ArrayList<TrackList> trackList = new ArrayList<TrackList>();
	public ArrayList<TrackMemberList> trackMemberList;
	private ConnectServer connectServer;
	private SearchView mSearchView;
	private GoogleMap map;
	private Resources res;
	private Button raceBtn;
	private TextView trackDataTxtView;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	protected CharSequence mTitle;
	protected CharSequence mDrawerTitle;

	private Animation animAlpha;
	public ArrayList<LatLngTimeData> trackPathData = null;
	private int listPosition;
	private Session sessions;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.racetrack_layout);

		CustomListView = this;
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
		raceBtn.setVisibility(View.INVISIBLE);
		raceBtn.setOnClickListener(this);
		// raceBtn.setAlpha(0.0f);
		trackDataTxtView = (TextView) findViewById(R.id.track_data_txtview);
		// facebookGetData();s

	}

	private void facebookGetData() {
		// session = new Session(this);
		sessions = Session.getActiveSession();
		StatusCallback callback = new StatusCallback() {
			public void call(Session session, SessionState state,
					Exception exception) {
				// System.out.println("ERROR"+state +exception);
				// if (exception != null) {
				//
				// new AlertDialog.Builder(getApplicationContext())
				// .setTitle("Login Fail")
				// .setMessage(exception.getMessage())
				// .setPositiveButton("OK", null)
				// .show();
				// RaceTrackSelectorActivity.this.sessions = createSession();
				// return;
				// }
				Request.newMeRequest(session, new Request.GraphUserCallback() {

					// callback after Graph API response with user object
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// System.out.println("USER :"+ user);
						if (user != null) {
							// lblEmail.setText(user.getName());
							System.out.println(user.getName() + user.getId());
						}
					}
				}).executeAsync();

			}
		};
		// pendingRequest = true;
		sessions.openForRead(new Session.OpenRequest(this)
				.setCallback(callback));
		// System.out.println(Session.getActiveSession().getApplicationId());

	}

	private Session createSession() {
		Session activeSession = Session.getActiveSession();
		if (activeSession == null || activeSession.getState().isClosed()) {
			activeSession = new Session.Builder(this).setApplicationId(
					"523495727673676").build();
			Session.setActiveSession(activeSession);
		}
		return activeSession;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		MenuItem searchItem = menu.findItem(R.id.action_websearch);
		mSearchView = (SearchView) searchItem.getActionView();
		setupSearchView(searchItem);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			System.out.println("BACKPRESS");
			// NavUtils.navigateUpFromSameTask(this);
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

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
		connectServer = new ConnectServer(this, URLServer
				+ "/racetracklist.php");
		connectServer.setRequestTag(ConnectServer.TRACK_LIST);
		connectServer.execute();

	}

	public void onItemClick(int mPosition) {
		this.listPosition = mPosition;
		TrackList tempValues = (TrackList) trackList.get(mPosition);
		trackDataTxtView.setText("" + tempValues.getRaceTrackName() + " \nRid:"
				+ tempValues.getrId() + " \nLatLon:"
				+ tempValues.getDoubleLat() + "\t" + tempValues.getDoubleLon()
				+ " \nRdir:" + tempValues.getRdir());

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				tempValues.getDoubleLat(), tempValues.getDoubleLon()), 15.0f));

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(mPosition, true);
		// mDrawerList.setSelection(mPosition);
		setTitle(tempValues.getRaceTrackName());
		mDrawerLayout.closeDrawer(mDrawerList);

		// get trackpath from server.
		if (trackList.get(listPosition).getTrackData() != null
				|| trackList.get(listPosition).getTrackMemberList() != null) {
			setTrackPath(trackList.get(listPosition).getTrackData());
			trackMemberList = trackList.get(listPosition).getTrackMemberList();
			printTrackData();
			drawTrackPath();
			return;
		}
		connectServer = new ConnectServer(this, URLServer + "/getTrackPath.php");
		connectServer.addValue("Rdir",
				URLServer + "/racetrack/" + tempValues.getrId() + ".xml");
		connectServer.setRequestTag(ConnectServer.TRACK_PATH);
		connectServer.execute();

		// getMember of mPosition Race Track.
		connectServer = new ConnectServer(this, URLServer
				+ "/getTrackMember.php");
		connectServer.addValue("Rid", tempValues.getrId());
		connectServer.setRequestTag(ConnectServer.TRACK_MEMBER);
		connectServer.execute();
		raceBtn.setVisibility(View.VISIBLE);
		raceBtn.startAnimation(animAlpha);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
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
		System.out.println("QText : " + arg0);
		return false;
	}

	public void setTrackList(String result) {
		try {
			// get race track data from database server to set ListAdapter.
			JSONArray jsonArr = new JSONArray(result);

			for (int i = 0; i < jsonArr.length(); i++) {
				final TrackList sched = new TrackList();
				JSONObject jsonObject = new JSONObject(jsonArr.getString(i));

				/******* Firstly take data in model object ******/
				sched.setRaceTrackName(jsonObject.getString("Rname"));
				sched.setrId(jsonObject.getString("Rid"));
				String[] location = jsonObject.getString("Location").split(",");
				sched.setDoubleLat(Double.parseDouble(location[0]));
				sched.setDoubleLon(Double.parseDouble(location[1]));
				sched.setRdir(jsonObject.getString("Rdir"));
				/******** Take Model Object in ArrayList **********/
				trackList.add(sched);
				/**************** Create Custom Adapter *********/
				adapter = new TrackListAdapter(CustomListView, trackList, res);

				// set up the drawer's list view with items and click listener
				mDrawerList.setAdapter(adapter);
			}

		} catch (JSONException e) {
			// Draw track on map from xml string.

		}
	}

	public void setTrackPath(String result) {

		// Draw track on map from xml string.
		String xmlTrackData = result;
		trackPathData = (ArrayList<LatLngTimeData>) TrackDataBase
				.loadXmlString(result);
		drawTrackPath();
		trackList.get(listPosition).setTrackData(xmlTrackData);
	}

	// Drawing on map function.
	public void drawTrackPath() {
		
		map.clear();
		PolylineOptions options = new PolylineOptions();
		MarkerOptions startMarker;
		MarkerOptions endMarker;
		
		for (int i = 0; i < trackPathData.size(); i++) {
			LatLng point = new LatLng(trackPathData.get(i).getCoordinate()
					.getLat(), trackPathData.get(i).getCoordinate().getLng());
			if(i==0){
				startMarker=new MarkerOptions()
				.position(point)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.play))
				.title("Start");
				map.addMarker(startMarker);
			}if(i==trackPathData.size()-1){
				endMarker=new MarkerOptions()
				.position(point)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.flag))
				.title("End");
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

	public void setTeackMember(String result) {

		trackMemberList = new ArrayList<TrackMemberList>();
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
				sched.setRank(Integer.parseInt(jsonObject.getString("Rank")));
				sched.setTrackerDir(jsonObject.getString("Trackerdir"));
				sched.setDuration(Integer.parseInt(jsonObject.getString("Time")));
				/******** Take Model Object in ArrayList **********/
				trackMemberList.add(sched);

			}

		} catch (JSONException e) {

		}

		printTrackData();
	}

	private void printTrackData() {
		String trackMemberString = "";
		for (int i = 0; i < trackMemberList.size(); i++) {
			trackMemberString = trackMemberString
					+ trackMemberList.get(i).getRank()+" "+trackMemberList.get(i).getfName() + "\n";

		}
		Toast.makeText(CustomListView, trackMemberString, 3000)
				.show();

		trackList.get(listPosition).setTrackMemberList(trackMemberList);
	}

	public void cannotConnectToServer() {
		Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_LONG)
				.show();

	}

	@Override
	public void onClick(View v) {
		if (v.equals(raceBtn)) {
			if (trackMemberList == null || trackPathData == null) {
				return;
			}

			try {
				CaptureMapScreen();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			Intent intent = new Intent(this, MapsActivity.class);
			intent.putExtra("TrackMemberList", trackList.get(listPosition)
					.getTrackMemberList());
			intent.putExtra("TrackPathData", trackList.get(listPosition)
					.getTrackData());
			startActivity(intent);
		}

	}

	@Override
	public void onDrawerClosed(View arg0) {
		// System.out.println("Drawer close"+ arg0);

	}

	@Override
	public void onDrawerOpened(View arg0) {
		if (trackList.size() > 0) {
			return;
		}
		setListData();
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

	public void CaptureMapScreen() {
		SnapshotReadyCallback callback = new SnapshotReadyCallback() {
			Bitmap bitmap;

			@Override
			public void onSnapshotReady(Bitmap snapshot) {
				// TODO Auto-generated method stub
				bitmap = snapshot;
				try {

					FileOutputStream out = new FileOutputStream(mapcapPath);

					// above "/mnt ..... png" => is a storage path (where image
					// will be stored) + name of image you can customize as per
					// your Requirement

					bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
					RoadRunnerSetting.setMapScreen(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		map.snapshot(callback);

		// myMap is object of GoogleMap +> GoogleMap myMap;
		// which is initialized in onCreate() =>
		// myMap = ((SupportMapFragment)
		// getSupportFragmentManager().findFragmentById(R.id.map_pass_home_call)).getMap();
	}
}
