package com.senior.roadrunner.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.senior.roadrunner.finish.FinishActivity;
import com.senior.roadrunner.racetrack.MapsActivity;
import com.senior.roadrunner.trackchooser.RaceTrackSelectorActivity;

public class ConnectServer extends AsyncTask<String, Integer, String> {
	public static int MY_ACTIVITY = 5;
	public static int DATA_UPDATE = 4;
	public static int TRACK_LIST = 0;
	public static int TRACK_PATH = 1;
	public static int TRACK_MEMBER = 2;
	public static int TRACK_MEMBER_PATH = 3;

	private HttpPost httppost;
	private HttpClient httpclient;
	private List<NameValuePair> nameValuePairs;
	// private DialogConnect dialogConnect;
	private Context context;
	private int requestTag;
	private int index;
	private OnServerResponseListener responseCallback;

	public interface OnServerResponseListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onResponse(String result);
	}

	public ConnectServer(Context context, String URL) {
		this.context = context;

		// à¸ªà¸£à¹?à¸²à¸?à¸ªà¹?à¸§à¸?à¸?à¸£à¸°à¸?à¸­à¸?à¸—à¸µà¹?à¸?à¸³à¹€à¸?à¹?à¸?à¹?à¸?à¸?à¸²à¸£à¹€à¸?à¸·à¹?à¸­à¸¡à¸?à¸±à¸?
		// Server
		this.httpclient = new DefaultHttpClient();
		this.httppost = new HttpPost(URL);
		this.nameValuePairs = new ArrayList<NameValuePair>();

		// à¸ªà¸£à¹?à¸²à¸? Dialog à¸•à¸­à¸?à¹€à¸?à¸·à¹?à¸­à¸¡à¸•à¹?à¸­à¸?à¸±à¸?
		// Server
		// à¸¡à¸µà¸?à¸²à¸£à¸ªà¹?à¸? ConnectServer à¹?à¸«à¹?à¸?à¸±à¸? Dialog
		// à¹€à¸?à¸·à¹?à¸­à¹?à¸?à¹?à¹?à¸?à¸?à¸²à¸£à¸¢à¸?à¹€à¸¥à¸´à¸?
		// dialogConnect = new DialogConnect(this.context, this);
		// dialogConnect.setTitle(this.context.getString(R.string.app_name));
		// dialogConnect.setMessage("Connect to server");
	}

	// Function
	// à¸ªà¸³à¸«à¸£à¸±à¸?à¹€à¸?à¸´à¹?à¸¡à¸•à¸±à¸§à¹?à¸?à¸£à¹?à¸?à¸?à¸²à¸£à¸ªà¹?à¸?à¸?à¹?à¸²à¹?à¸?à¸?
	// Post
	public void addValue(String key, String value) {
		nameValuePairs.add(new BasicNameValuePair(key, value));
	}

	public void setRequestTag(int requestTag) {
		this.requestTag = requestTag;
	}

	// à¸?à¹?à¸­à¸?à¸—à¸µà¹?à¸?à¸°à¸—à¸³ doInBackground
	// à¸?à¸°à¸—à¸³à¸?à¸²à¸?à¸—à¸µà¹? Function à¸?à¸µà¹?à¸?à¹?à¸­à¸?
	protected void onPreExecute() {
		// dialogConnect.show();

	}

	// à¹€à¸£à¸´à¹?à¸¡à¸—à¸³à¸?à¸²à¸?à¹?à¸?à¸? Background
	protected String doInBackground(String... params) {
		InputStream is = null;
		String result = null;

		// à¹€à¸£à¸´à¹?à¸¡à¸?à¸²à¸£à¹€à¸?à¸·à¹?à¸­à¸¡à¸•à¹?à¸?à¸±à¸? Server
		try {
			// à¸—à¸³à¸?à¸²à¸£à¸ªà¹?à¸?à¸•à¸±à¸§à¹?à¸?à¸£à¸•à¹?à¸²à¸?à¹?
			// à¹?à¸?à¸£à¸¹à¸?à¹?à¸?à¸?à¸?à¸­à¸? UTF-8
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

			// à¸­à¹?à¸²à¸?à¸?à¸¥à¸¥à¸±à¸?à¸?à¹?à¹?à¸?à¸£à¸¹à¸?à¹?à¸?à¸?à¸?à¸­à¸?
			// UTF-8
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

			is.close();
			result = sb.toString();

			// à¸–à¹?à¸²à¸?à¸“à¸°à¹€à¸?à¸·à¹?à¸­à¸¡à¸•à¹?à¸­à¸?à¸±à¸? Server
			// à¸¡à¸µà¸?à¸±à¸?à¸«à¸² à¸?à¸°à¹?à¸ªà¸”à¸? Log Error
		} catch (ClientProtocolException e) {
			Log.e("ConnectServer", e.toString());
		} catch (IOException e) {
			Log.e("ConnectServer", e.toString());
		}
		if (requestTag == MY_ACTIVITY) {
			try {
				responseCallback = (OnServerResponseListener) context;
				responseCallback.onResponse(result);
			} catch (ClassCastException e) {
				throw new ClassCastException(context.toString()
						+ " must implement OnServerResponseListener");
			}
			
		}

		return result;
	}

	// à¸–à¹?à¸²à¸—à¸³à¸?à¸²à¸?à¸—à¸µà¹? doInBackground
	// à¹€à¸ªà¸£à¹?à¸?à¹?à¸¥à¹?à¸§ à¸?à¸°à¸¡à¸²à¸—à¸³à¸?à¸²à¸?à¸—à¸µà¹? Function
	// à¸?à¸µà¹?
	protected void onPostExecute(String result) {
		// list à¸—à¸µà¹?à¹?à¸?à¹?à¹€à¸?à¹?à¸?à¸?à¹?à¸­à¸¡à¸¹à¸¥

		// à¸–à¹?à¸² result à¹€à¸?à¹?à¸? null à¸?à¸·à¸­
		// à¹?à¸¡à¹?à¸ªà¸²à¸¡à¸²à¸£à¸–à¹€à¸?à¸·à¹?à¸­à¸¡à¸•à¹?à¸­à¸?à¸±à¸?
		// server à¹?à¸”à¹?
		// à¸–à¹?à¸²à¹€à¸?à¸·à¹?à¸­à¸¡à¸•à¹?à¸?à¸±à¸? server à¹?à¸”à¹?
		// à¸?à¸°à¸—à¸³à¸?à¸²à¸?à¸•à¹?à¸­à¹?à¸?à¸?à¸µà¹?
		if (result != null) {
			// à¹€à¸£à¸´à¹?à¸¡à¸?à¸²à¸£à¹?à¸?à¸¥à¸? JSON
			// à¹€à¸?à¹?à¸?à¸?à¹?à¸­à¸¡à¸¹à¸¥
			if (context instanceof MapsActivity) {

				switch (requestTag) {
				case 4:
					((MapsActivity) context).setList(result);
					break;
				case 3:
					((MapsActivity) context).setMemberTrack(result, index);
					break;
				}
			} else if (context instanceof RaceTrackSelectorActivity) {
				switch (requestTag) {
				case 0:
					((RaceTrackSelectorActivity) context).setTrackList(result);
					break;
				case 1:
					((RaceTrackSelectorActivity) context).setTrackPath(result,
							index);
					break;
				case 2:
					((RaceTrackSelectorActivity) context).setTeackMember(
							result, index);
					break;
				// default : ((RaceTrackSelectorActivity)
				// context).setTeackMember(result);
				}
			} else if (context instanceof FinishActivity) {
				((FinishActivity) context).setDataBaseServerResponse(result);
			} else if (context instanceof MapsActivity) {
				((MapsActivity) context).cannotConnectToServer();
			} else if (context instanceof RaceTrackSelectorActivity) {
				((RaceTrackSelectorActivity) context).cannotConnectToServer();
			}
			// à¸–à¹?à¸²à¹€à¸?à¸·à¹?à¸­à¸¡à¸•à¹?à¸­à¸?à¸±à¸? server
			// à¹?à¸¡à¹?à¹?à¸”à¹?à¸?à¸°à¸—à¸³à¸?à¸²à¸?à¸•à¹?à¸­à¹?à¸?à¸?à¸µà¹?
		}

		// dialogConnect.dismiss();
	}

	public void setIndex(int index) {
		this.index = index;

	}
}
