package com.senior.roadrunner.server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.senior.roadrunner.finish.FinishActivity;
import com.senior.roadrunner.racetrack.MapsActivity;
import com.senior.roadrunner.setting.RoadRunnerSetting;

@SuppressWarnings("deprecation")
public class UploadTrack extends AsyncTask<String, Integer, String> {

	private Context context;
	int serverResponseCode = 0;
	// DialogUpload dialog;
	String upLoadServerUri = RoadRunnerSetting.URLServer + "uploadTrack.php";
	// final String uploadFilePath = "/mnt/sdcard/";
	// final String uploadFileName = "tracker.xml";
	String rid = MapsActivity.rId;
	HttpURLConnection conn = null;
	DataOutputStream dos = null;
	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary = "*****";
	int bytesRead, bytesAvailable, bufferSize;
	byte[] buffer;
	int maxBufferSize = 1 * 1024 * 1024;
	private String trackName;
	private double lng;
	private double lat;
	private double distance;

	public UploadTrack(Context context, double lat, double lng, String trackName,double distance) {

		this.lat = lat;
		this.lng = lng;
		this.trackName = trackName;
		this.distance = distance;
		this.context = context;
		// dialog = new DialogUpload(this.context, this);
		// dialog.setTitle(this.context.getString(R.string.app_name));
		// dialog.setMessage("Upload result to server");
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		// dialog.show();
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		// dialog.dismiss();
		((FinishActivity) context).uploadTrackResponse(result);
	}

	@Override
	protected String doInBackground(String... params) {
		// take CPU lock to prevent CPU from going off if the user
		// presses the power button during download
		String fileUri = params[0];
		File sourceFile = new File(fileUri);
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wl.acquire();
		String response = null;
		try {
			// Url of the server
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(upLoadServerUri);
			// MultipartEntityBuilder mpEntity =
			// MultipartEntityBuilder.create();
			// mpEntity.setCharset(Charset.forName("UTF-8"));

			MultipartEntity mpEntity = new MultipartEntity();
			// Path of the file to be uploaded
			ContentBody cbFile = new FileBody(sourceFile);

			// Add the data to the multipart entity
			// mpEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			// mpEntity.addTextBody("uploaded_path",
			// "racetrack/",ContentType.DEFAULT_TEXT);
			// mpEntity.addTextBody("Latitude", lat +
			// "",ContentType.DEFAULT_TEXT);
			// mpEntity.addTextBody("Longitude", lng +
			// "",ContentType.DEFAULT_TEXT);
			// mpEntity.addTextBody("Rname",
			// trackName,ContentType.DEFAULT_TEXT);
			// mpEntity.addPart("uploaded_file", cbFile);
			//
			mpEntity.addPart("uploaded_path", new StringBody("racetrack/",
					Charset.forName("UTF-8")));
			mpEntity.addPart("Latitude",
					new StringBody(lat + "", Charset.forName("UTF-8")));
			mpEntity.addPart("Longitude",
					new StringBody(lng + "", Charset.forName("UTF-8")));
			mpEntity.addPart("Rname",
					new StringBody(trackName, Charset.forName("UTF-8")));
			mpEntity.addPart("Distance",
					new StringBody(distance+"", Charset.forName("UTF-8")));
			// mpEntity.addPart("data", new StringBody("This is test report",
			// Charset.forName("UTF-8")));
			mpEntity.addPart("uploaded_file", cbFile);

			// HttpEntity mp = mpEntity.build();

			post.setEntity(mpEntity);
			// Execute the post request
			HttpResponse response1 = client.execute(post);
			// Get the response from the server
			HttpEntity resEntity = response1.getEntity();
			response = EntityUtils.toString(resEntity);
			Log.d("Response Uploadtask : ", response);

			// Generate the array from the response
			// JSONArray jsonarray = new JSONArray(Response);
			// JSONObject jsonobject = jsonarray.getJSONObject(0);
			// //Get the result variables from response
			// String result = (jsonobject.getString("result"));
			// String msg = (jsonobject.getString("msg"));
			// Close the connection
			client.getConnectionManager().shutdown();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {
			wl.release();
		}
		// dialog.dismiss();

		return response;
	}
}
