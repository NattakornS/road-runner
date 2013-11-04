package com.senior.roadrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.j;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.racetrack.CustomAdapter;
import com.senior.roadrunner.racetrack.ListModel;
import com.senior.roadrunner.server.ConnectServer;

@SuppressLint("NewApi")
public class RaceTrackSelectorActivity extends Activity implements
		SearchView.OnQueryTextListener, SearchView.OnCloseListener {

	ListView list;
	CustomAdapter adapter;
	public Activity CustomListView = null;
	public ArrayList<ListModel> CustomListViewValuesArr = new ArrayList<ListModel>();
	private ConnectServer connectServer;
	private SearchView mSearchView;
	private GoogleMap map;
	private Resources res;
	private Button raceBtn;
	private TextView trackDataTxtView;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.racetrack_layout);

		CustomListView = this;
		invalidateOptionsMenu();
		/******** Take some data in Arraylist ( CustomListViewValuesArr ) ***********/
		setListData();

		res = getResources();
		list = (ListView) findViewById(R.id.list);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.maps))
				.getMap();
		raceBtn = (Button)findViewById(R.id.race_btn);
		trackDataTxtView = (TextView)findViewById(R.id.track_data_txtview);

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
		connectServer = new ConnectServer(this,
				"http://192.168.1.173/racetracklist.php");
		connectServer.execute();

	}

	public void onItemClick(int mPosition) {
		ListModel tempValues = (ListModel) CustomListViewValuesArr
				.get(mPosition);
		trackDataTxtView.setText("" + tempValues.getRaceTrackName() + " \nRid:"
				+ tempValues.getrId() + " \nLatLon:"
				+ tempValues.getDoubleLat() + "\t"
				+ tempValues.getDoubleLon() + " \nRdir:"
				+ tempValues.getRdir());
//		Toast.makeText(
//				CustomListView,
//				"" + tempValues.getRaceTrackName() + " \nRid:"
//						+ tempValues.getrId() + " \nLatLon:"
//						+ tempValues.getDoubleLat() + "\t"
//						+ tempValues.getDoubleLon() + " \nRdir:"
//						+ tempValues.getRdir(), Toast.LENGTH_LONG).show();
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				tempValues.getDoubleLat(), tempValues.getDoubleLon()), 15.0f));
		
		connectServer = new ConnectServer(this,
				"http://192.168.1.173/getTrackPath.php");
		connectServer.addValue("Rdir", "http://192.168.1.173/racetrack/"+tempValues.getrId()+".xml");
		connectServer.execute();
		
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

	public void setJsonResult(String result) {
//		System.out.println(result);
		try {
			//get race track data from database server to set ListAdapter.
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
				adapter = new CustomAdapter(CustomListView, CustomListViewValuesArr,
						res);
				list.setAdapter(adapter);
			}
			
		} catch (JSONException e) {
			//Draw track on map from xml string.
			map.clear();
			List<LatLngTimeData> trackData = TrackDataBase.loadXmlString(result);
			PolylineOptions options = new PolylineOptions();
			for (int i = 0; i < trackData.size(); i++) {
				options.add(new LatLng(trackData.get(i).getCoordinate().getLat(), trackData.get(i).getCoordinate().getLng()));
			}
			options.color(Color.YELLOW);
			options.width(5);
			map.addPolyline(options);
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					trackData.get(0).getCoordinate().getLat(), trackData.get(0).getCoordinate().getLng()), 15.0f));
		}
		

	}

}
