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

		// สร�?า�?ส�?ว�?�?ระ�?อ�?ที�?�?ำเ�?�?�?�?�?�?ารเ�?ื�?อม�?ั�?
		// Server
		this.httpclient = new DefaultHttpClient();
		this.httppost = new HttpPost(URL);
		this.nameValuePairs = new ArrayList<NameValuePair>();

		// สร�?า�? Dialog ตอ�?เ�?ื�?อมต�?อ�?ั�?
		// Server
		// มี�?ารส�?�? ConnectServer �?ห�?�?ั�? Dialog
		// เ�?ื�?อ�?�?�?�?�?�?ารย�?เลิ�?
		// dialogConnect = new DialogConnect(this.context, this);
		// dialogConnect.setTitle(this.context.getString(R.string.app_name));
		// dialogConnect.setMessage("Connect to server");
	}

	// Function
	// สำหรั�?เ�?ิ�?มตัว�?�?ร�?�?�?ารส�?�?�?�?า�?�?�?
	// Post
	public void addValue(String key, String value) {
		nameValuePairs.add(new BasicNameValuePair(key, value));
	}

	public void setRequestTag(int requestTag) {
		this.requestTag = requestTag;
	}

	// �?�?อ�?ที�?�?ะทำ doInBackground
	// �?ะทำ�?า�?ที�? Function �?ี�?�?�?อ�?
	protected void onPreExecute() {
		// dialogConnect.show();

	}

	// เริ�?มทำ�?า�?�?�?�? Background
	protected String doInBackground(String... params) {
		InputStream is = null;
		String result = null;

		// เริ�?ม�?ารเ�?ื�?อมต�?�?ั�? Server
		try {
			// ทำ�?ารส�?�?ตัว�?�?รต�?า�?�?
			// �?�?รู�?�?�?�?�?อ�? UTF-8
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

			// อ�?า�?�?ลลั�?�?�?�?�?รู�?�?�?�?�?อ�?
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

			// ถ�?า�?ณะเ�?ื�?อมต�?อ�?ั�? Server
			// มี�?ั�?หา �?ะ�?สด�? Log Error
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

	// ถ�?าทำ�?า�?ที�? doInBackground
	// เสร�?�?�?ล�?ว �?ะมาทำ�?า�?ที�? Function
	// �?ี�?
	protected void onPostExecute(String result) {
		// list ที�?�?�?�?เ�?�?�?�?�?อมูล

		// ถ�?า result เ�?�?�? null �?ือ
		// �?ม�?สามารถเ�?ื�?อมต�?อ�?ั�?
		// server �?ด�?
		// ถ�?าเ�?ื�?อมต�?�?ั�? server �?ด�?
		// �?ะทำ�?า�?ต�?อ�?�?�?ี�?
		if (result != null) {
			// เริ�?ม�?าร�?�?ล�? JSON
			// เ�?�?�?�?�?อมูล
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
			// ถ�?าเ�?ื�?อมต�?อ�?ั�? server
			// �?ม�?�?ด�?�?ะทำ�?า�?ต�?อ�?�?�?ี�?
		}

		// dialogConnect.dismiss();
	}

	public void setIndex(int index) {
		this.index = index;

	}
}
