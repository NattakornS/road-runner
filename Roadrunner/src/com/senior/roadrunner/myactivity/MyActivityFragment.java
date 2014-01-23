package com.senior.roadrunner.myactivity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.senior.roadrunner.server.ConnectServer;
import com.senior.roadrunner.setting.RoadRunnerSetting;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the
 * ListView with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MyActivityFragment extends ListFragment implements
		ListView.OnItemClickListener, OnRefreshListener {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	private static final String GetMyActivityURL = RoadRunnerSetting.URLServer
			+ "getMyActivity.php";

	/**
	 * The Adapter which will be used to populate the ListView/GridView with
	 * Views.
	 */
	private ListAdapter mAdapter;
	private RoadRunnerSetting roadRunnerSetting;
	private PullToRefreshLayout mPullToRefreshLayout;
	private MyActivityArrayAdapter myadapter;

	// TODO: Rename and change types of parameters
	public static MyActivityFragment newInstance(String param1, String param2) {
		MyActivityFragment fragment = new MyActivityFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MyActivityFragment() {
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ViewGroup viewGroup = (ViewGroup) view;

		// As we're using a ListFragment we create a PullToRefreshLayout
		// manually
		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

		// We can now setup the PullToRefreshLayout
		ActionBarPullToRefresh
				.from(getActivity())
				// We need to insert the PullToRefreshLayout into the Fragment's
				// ViewGroup
				.insertLayoutInto(viewGroup)
				// Here we mark just the ListView and it's Empty View as
				// pullable
				.theseChildrenArePullable(android.R.id.list, android.R.id.empty)
				.listener(this).setup(mPullToRefreshLayout);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setListShown(false);
		roadRunnerSetting = RoadRunnerSetting.getInstance();
		ConnectServer myActivityConnect = new ConnectServer(getActivity(),
				GetMyActivityURL);
		myActivityConnect.setRequestTag(ConnectServer.MY_ACTIVITY);
		// System.out.println();
		myActivityConnect.addValue("Fid", roadRunnerSetting.getFacebookId());
		myActivityConnect.execute();

	}

	@Override
	public void onRefreshStarted(View view) {
		// Hide the list
		setListShown(false);
		/**
		 * Simulate Refresh with 4 seconds sleep
		 */
		ConnectServer myActivityConnect = new ConnectServer(getActivity(),
				GetMyActivityURL);
		myActivityConnect.setRequestTag(ConnectServer.MY_ACTIVITY);
		myActivityConnect.addValue("Fid", roadRunnerSetting.getFacebookId());
		myActivityConnect.execute();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {

					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);

				// Notify PullToRefreshLayout that the refresh has finished
				mPullToRefreshLayout.setRefreshComplete();

				if (getView() != null) {
					// Show the list again
					setListShown(true);
				}
			}
		}.execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	public synchronized void setServerResponseResult(String result) {
		System.out.println(result);
		ArrayList<MyActivityListData> activityListDatas = new ArrayList<MyActivityListData>();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
				MyActivityListData activityListData = new MyActivityListData();
				activityListData.setDate(jsonObject.getString("TimeStamp"));
				activityListData.setDetail(jsonObject.getString("Distance"));
				activityListData.setDuration(jsonObject.getDouble("Time"));
				activityListData.setRank(jsonObject.getString("Rank"));
				activityListData.setTrackName(jsonObject.getString("Rname"));
				activityListDatas.add(activityListData);
			}
			if (getListView() != null) {

			}
			if (myadapter == null) {
				myadapter = new MyActivityArrayAdapter(getActivity(),
						android.R.id.list, activityListDatas);
				setListAdapter(myadapter);
			} else {
				myadapter.clear();
				myadapter.addAll(activityListDatas);

			}


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Do something with data
		super.onListItemClick(l, v, position, id);
		// Toast.makeText(getActivity(), position, 1000).show();
	}

}
