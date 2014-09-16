package com.senior.roadrunner.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.senior.roadrunner.racetrack.MapsActivity;
import com.senior.roadrunner.setting.RoadRunnerSetting;

@SuppressWarnings("deprecation")
public class UploadTask extends AsyncTask<String, Integer, String> {

	private Context context;
	int serverResponseCode = 0;
//	DialogUpload dialog;
	String upLoadServerUri = RoadRunnerSetting.URLServer + "UploadToServer.php";
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
	private MultipartEntity mpEntity;

	public UploadTask(Context context) {
		this.context = context;
//		dialog = new DialogUpload(this.context, this);
//		dialog.setTitle(this.context.getString(R.string.app_name));
//		dialog.setMessage("Upload result to server");
		mpEntity = new MultipartEntity();
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
//		dialog.show();
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
//		dialog.dismiss();
	}

	@Override
	protected String doInBackground(String... params) {
		// take CPU lock to prevent CPU from going off if the user
		// presses the power button during download
//		String fileUri = params[0];
//		String imgUri = params[1];
//		this.rid = params[2];
//		File sourceFile = new File(fileUri);
//		File imgFile = new File(imgUri);
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wl.acquire();

		// InputStream inputStream;
		// InputStream is = null;
		// String result = null;
		// try {
		// inputStream = new FileInputStream(sourceFile);
		// byte[] data;
		// try {
		// data = IOUtils.toByteArray(inputStream);
		//
		// HttpClient httpClient = new DefaultHttpClient();
		// HttpPost httpPost = new HttpPost(upLoadServerUri);
		// // List<NameValuePair> setPostEntity = new
		// ArrayList<NameValuePair>();
		// // setPostEntity.add(new BasicNameValuePair("uploaded_path",
		// // "tracker/" + rid + "/"));
		// // httpPost.setEntity(new UrlEncodedFormEntity(setPostEntity,
		// // HTTP.UTF_8));
		//
		// InputStreamBody inputStreamBody = new InputStreamBody(
		// new ByteArrayInputStream(data),
		// RoadRunnerFacebookSetting.getFacebookId() + ".xml");
		// MultipartEntity multipartEntity = new MultipartEntity();
		// multipartEntity.addPart("uploaded_file", inputStreamBody);
		// multipartEntity.addPart("uploaded_path", new StringBody("tracker/" +
		// rid + "/"));
		// httpPost.setEntity(multipartEntity);
		//
		// HttpResponse httpResponse = httpClient.execute(httpPost);
		//
		// // Handle response back from script.
		// if (httpResponse != null) {
		// System.out.println(httpResponse.getStatusLine());
		// HttpEntity entity = httpResponse.getEntity();
		// is = entity.getContent();
		//
		// // à¸­à¹?à¸²à¸?à¸?à¸¥à¸¥à¸±à¸?à¸?à¹?à¹?à¸?à¸£à¸¹à¸?à¹?à¸?à¸?à¸?à¸­à¸?
		// // UTF-8
		// BufferedReader reader = new BufferedReader(new InputStreamReader(
		// is, "UTF-8"), 8);
		// StringBuilder sb = new StringBuilder();
		// String line = null;
		// while ((line = reader.readLine()) != null) {
		// sb.append(line + "\n");
		// }
		//
		// is.close();
		// result = sb.toString();
		// System.out.println(result);
		// } else { // Error, no response.
		//
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// } catch (FileNotFoundException e1) {
		// e1.printStackTrace();
		// }

		// System.out.println("Connection ");
		// try {
		// // //Upload task
		// StringBuilder str = new StringBuilder();
		// HttpClient client = new DefaultHttpClient();
		// HttpPost httpPost = new HttpPost(upLoadServerUri);
		// List<NameValuePair> setPostEntity = new ArrayList<NameValuePair>();
		// setPostEntity.add(new BasicNameValuePair("uploaded_path",
		// "tracker/" + rid + "/"));
		//
		// try {
		// httpPost.setEntity(new UrlEncodedFormEntity(setPostEntity,
		// HTTP.UTF_8));
		// HttpResponse response = client.execute(httpPost);
		// StatusLine statusLine = response.getStatusLine();
		// int statusCode = statusLine.getStatusCode();
		// if (statusCode == 200) { // Status OK
		// HttpEntity entity = response.getEntity();
		// InputStream content = entity.getContent();
		// BufferedReader reader = new BufferedReader(
		// new InputStreamReader(content));
		// String line;
		// while ((line = reader.readLine()) != null) {
		// str.append(line);
		// }
		// } else {
		// Log.e("Log", "Failed to download result..");
		// }
		// } catch (ClientProtocolException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// // open a URL connection to the Servlet
		// FileInputStream fileInputStream = new FileInputStream(sourceFile);
		// URL url = new URL(upLoadServerUri);
		//
		// // Open a HTTP connection to the URL
		// conn = (HttpURLConnection) url.openConnection();
		// conn.setDoInput(true); // Allow Inputs
		// conn.setDoOutput(true); // Allow Outputs
		// conn.setUseCaches(false); // Don't use a Cached Copy
		// conn.setRequestMethod("POST");
		// conn.setRequestProperty("Connection", "Keep-Alive");
		// conn.setRequestProperty("ENCTYPE", "multipart/form-data");
		// conn.setRequestProperty("Content-Type",
		// "multipart/form-data;boundary=" + boundary);
		// conn.setRequestProperty("uploaded_path", "tracker/" + rid + "/");
		// conn.setRequestProperty("uploaded_file", fileUri);
		// conn.connect();
		//
		// dos = new DataOutputStream(conn.getOutputStream());
		// dos.writeBytes(twoHyphens + boundary + lineEnd);
		// //
		// //
		// dos.writeBytes("Content-Disposition: form-data; name="uploaded_file";filename=""
		// // + fileName + """ + lineEnd);
		// dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
		// + fileUri + "\"" + lineEnd);
		// dos.writeBytes(lineEnd);
		//
		// // create a buffer of maximum size
		// bytesAvailable = fileInputStream.available();
		//
		// bufferSize = Math.min(bytesAvailable, maxBufferSize);
		// buffer = new byte[bufferSize];
		//
		// // read file and write it into form...
		// bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		//
		// while (bytesRead > 0) {
		//
		// dos.write(buffer, 0, bufferSize);
		// bytesAvailable = fileInputStream.available();
		// bufferSize = Math.min(bytesAvailable, maxBufferSize);
		// bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		//
		// }
		//
		// // send multipart form data necesssary after file data...
		// dos.writeBytes(lineEnd);
		// dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
		//
		// // Responses from the server (code and message)
		// serverResponseCode = conn.getResponseCode();
		// String serverResponseMessage = conn.getResponseMessage();
		// System.out.println(serverResponseMessage);
		// Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage
		// + ": " + serverResponseCode);
		//
		// if (serverResponseCode == 200) {
		//
		// // String msg =
		// // "File Upload Completed.\n\n See uploaded file here : \n\n"
		// // +" http://www.androidexample.com/media/uploads/"
		// // +uploadFileName;
		// }
		//
		// // close the streams //
		// fileInputStream.close();
		// dos.flush();
		// dos.close();
		// conn.disconnect();
		//
		//
		// } catch (MalformedURLException ex) {
		//
		// dialog.dismiss();
		// ex.printStackTrace();
		//
		// } catch (Exception e) {
		//
		// dialog.dismiss();
		// e.printStackTrace();
		//
		// }

		try {
			// Url of the server
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(upLoadServerUri);
			
			// mpEntity.addPart("data", new StringBody("This is test report",
						// Charset.forName("UTF-8")));
			// Add the data to the multipart entity
//			mpEntity.addPart("uploaded_path", new StringBody("tracker/" + rid
//					+ "/", ContentType.DEFAULT_TEXT));
//			mpEntity.addPart("uploaded_file", new FileBody(sourceFile));
//			mpEntity.addPart("uploaded_img", new FileBody(imgFile));
			
			post.setEntity(mpEntity);
			// Execute the post request
			HttpResponse response1 = client.execute(post);
			// Get the response from the server
			HttpEntity resEntity = response1.getEntity();
			String Response = EntityUtils.toString(resEntity);
			Log.d("Response Uploadtask : ", Response);
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
//		dialog.dismiss();
		return null;
	}

	public void addMultipartValue(String string, ContentBody contentBody) {
		mpEntity.addPart(string, contentBody);
	}
}
