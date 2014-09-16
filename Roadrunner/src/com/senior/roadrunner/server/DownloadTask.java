package com.senior.roadrunner.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.senior.roadrunner.racetrack.MapsActivity;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.trackchooser.RaceTrackSelectorActivity;

@SuppressLint({ "SdCardPath", "Wakelock" })
public class DownloadTask extends AsyncTask<String, Integer, String> {

	private Context context;
	private String path;
	private int index;
	private int requestTag;
	public static int DATA_UPDATE = 4;
	public static int TRACK_LIST = 0;
	public static int TRACK_PATH = 1;
	public static int TRACK_MEMBER = 2;
	public static int TRACK_MEMBER_PATH = 3;

	public DownloadTask(Context context) {
		this.context = context;
	}

	@SuppressWarnings("resource")
	@Override
	protected String doInBackground(String... sUrl) {
		// take CPU lock to prevent CPU from going off if the user
		// presses the power button during download
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wl.acquire();

		try {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(sUrl[0] + sUrl[1]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				// expect HTTP 200 OK, so we don't mistakenly save error report
				// instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
					return "Server returned HTTP "
							+ connection.getResponseCode() + " "
							+ connection.getResponseMessage();

				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();

				// download the file
				input = connection.getInputStream();

				path = RoadRunnerSetting.SDPATH + sUrl[1];

				File f = new File(path);
				File pf = f.getParentFile();
				if (pf != null) {
					pf.mkdirs();
				}
				if ((pf.exists()) && (pf.isDirectory())) {
					if ((!f.exists()) || (!f.isFile())) {
						f.createNewFile();
					}
					if ((f.exists()) || (f.isFile())) {
						output = new FileOutputStream(path, false);
						byte data[] = new byte[4096];
						long total = 0;
						int count;
						while ((count = input.read(data)) != -1) {
							// allow canceling with back button
							if (isCancelled())
								return "Cancle";
							total += count;
							// publishing the progress....
							if (fileLength > 0) // only if total length is known
								publishProgress((int) (total * 100 / fileLength));
							output.write(data, 0, count);
						}

					}
				}
			} catch (Exception e) {
				return e.toString();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
				}

				if (connection != null)
					connection.disconnect();
			}
		} finally {
			wl.release();
		}
		return path;
	}

	@Override
	protected void onPostExecute(String path) {
		//
		super.onPostExecute(path);
		if (path != null) {
			if (context instanceof MapsActivity) {
				switch (requestTag) {
				case 3:
					((MapsActivity) context).setMemberTrack(path, index);
					break;
				}
			}else if (context instanceof RaceTrackSelectorActivity) {
				((RaceTrackSelectorActivity) context).setTrackPath(path, index);
			}
		}
	}

	public void setIndex(int index) {
		this.index = index;

	}

	public void setRequestTag(int requestTag ) {
		this.requestTag = requestTag;

	}
}
