package com.senior.roadrunner;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.racetrack.CustomAdapter;
import com.senior.roadrunner.racetrack.ListModel;
import com.senior.roadrunner.racetrack.ListTracker;
import com.senior.roadrunner.server.ConnectServer;

@SuppressLint("NewApi")
public class RaceTrackSelectorActivity extends Activity implements
		SearchView.OnQueryTextListener, SearchView.OnCloseListener ,OnClickListener{

	private static final String URLServer = "http://192.168.1.111/";// 192.168.1.173//http://192.168.1.117/
	ListView list;
	CustomAdapter adapter;
	public Activity CustomListView = null;
	public ArrayList<ListModel> CustomListViewValuesArr = new ArrayList<ListModel>();
	public ArrayList<ListTracker> TrackMemberList;
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
	private String xmlTrackData;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.racetrack_layout);

		CustomListView = this;
		invalidateOptionsMenu();
		/******** Take some data in Arraylist ( CustomListViewValuesArr ) ***********/
		setListData();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_trackselector);
		mDrawerList = (ListView) findViewById(R.id.right_drawer);

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
//		raceBtn.setAlpha(0.0f);
		trackDataTxtView = (TextView) findViewById(R.id.track_data_txtview);

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
		ListModel tempValues = (ListModel) CustomListViewValuesArr
				.get(mPosition);
		trackDataTxtView.setText("" + tempValues.getRaceTrackName() + " \nRid:"
				+ tempValues.getrId() + " \nLatLon:"
				+ tempValues.getDoubleLat() + "\t" + tempValues.getDoubleLon()
				+ " \nRdir:" + tempValues.getRdir());
		// Toast.makeText(
		// CustomListView,
		// "" + tempValues.getRaceTrackName() + " \nRid:"
		// + tempValues.getrId() + " \nLatLon:"
		// + tempValues.getDoubleLat() + "\t"
		// + tempValues.getDoubleLon() + " \nRdir:"
		// + tempValues.getRdir(), Toast.LENGTH_LONG).show();
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				tempValues.getDoubleLat(), tempValues.getDoubleLon()), 15.0f));

		connectServer = new ConnectServer(this, URLServer + "/getTrackPath.php");
		connectServer.addValue("Rdir",
				URLServer + "/racetrack/" + tempValues.getrId() + ".xml");
		connectServer.setRequestTag(ConnectServer.TRACK_PATH);
		connectServer.execute();

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(mPosition, true);
		// mDrawerList.setSelection(mPosition);
		setTitle(tempValues.getRaceTrackName());
		mDrawerLayout.closeDrawer(mDrawerList);

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
				final ListModel sched = new ListModel();
				JSONObject jsonObject = new JSONObject(jsonArr.getString(i));

				/******* Firstly take data in model object ******/
				sched.setRaceTrackName(jsonObject.getString("Rname"));
				sched.setrId(jsonObject.getString("Rid"));
				String[] location = jsonObject.getString("Location").split(",");
				sched.setDoubleLat(Double.parseDouble(location[0]));
				sched.setDoubleLon(Double.parseDouble(location[1]));
				sched.setRdir(jsonObject.getString("Rdir"));
				/******** Take Model Object in ArrayList **********/
				CustomListViewValuesArr.add(sched);
				/**************** Create Custom Adapter *********/
				adapter = new CustomAdapter(CustomListView,
						CustomListViewValuesArr, res);

				// set up the drawer's list view with items and click listener
				mDrawerList.setAdapter(adapter);
			}

		} catch (JSONException e) {
			// Draw track on map from xml string.

		}
	}

	public void setTrackPath(String result) {
		// Draw track on map from xml string.
		xmlTrackData = result;
		map.clear();
		trackPathData = (ArrayList<LatLngTimeData>) TrackDataBase.loadXmlString(result);
		PolylineOptions options = new PolylineOptions();
		for (int i = 0; i < trackPathData.size(); i++) {
			options.add(new LatLng(trackPathData.get(i).getCoordinate().getLat(),
					trackPathData.get(i).getCoordinate().getLng()));
		}
		options.color(Color.YELLOW);
		options.width(5);
		map.addPolyline(options);
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				trackPathData.get(0).getCoordinate().getLat(), trackPathData.get(0)
						.getCoordinate().getLng()), 15.0f));
	}

	public void setTeackMember(String result) {
		TrackMemberList = new ArrayList<ListTracker>();
		try {
			// get race track data from database server to set ListAdapter.
			JSONArray jsonArr = new JSONArray(result);

			for (int i = 0; i < jsonArr.length(); i++) {
				final ListTracker sched = new ListTracker();
				JSONObject jsonObject = new JSONObject(jsonArr.getString(i));

				/******* Firstly take data in model object ******/
				sched.setfId(jsonObject.getString("Fid"));
				sched.setrId(jsonObject.getString("Rid"));
				sched.setRank(Integer.parseInt(jsonObject.getString("Rank")));
				sched.setTrackerDir(jsonObject.getString("Trackerdir"));
				/******** Take Model Object in ArrayList **********/
				TrackMemberList.add(sched);
			}

		} catch (JSONException e) {


		}
		String trackMemberString = "";
		for (int i = 0; i < TrackMemberList.size(); i++) {
			trackMemberString = trackMemberString + TrackMemberList.get(i).getfId() + "\n";

		}
		Toast.makeText(CustomListView, trackMemberString, Toast.LENGTH_LONG)
				.show();
	}

	public void cannotConnectToServer() {
		Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_LONG)
				.show();

	}

	@Override
	public void onClick(View v) {
		if(v.equals(raceBtn)){
			if(TrackMemberList==null||trackPathData == null){
				return;
			}
			Intent intent = new Intent(this,MapsActivity.class);
			intent.putExtra("TrackMemberList",TrackMemberList);
			intent.putExtra("TrackPathData", xmlTrackData);
			startActivity(intent);
		}
		
	}

}
