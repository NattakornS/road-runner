package com.senior.roadrunner.trackchooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.senior.roadrunner.setting.RoadRunnerSetting;

public class RaceTrackBitmapProfile extends Thread {
	private ArrayList<TrackMemberList> trackMemberList;
	private RoadRunnerSetting roadRunnerSetting;

	public RaceTrackBitmapProfile(ArrayList<TrackMemberList> trackMemberList) {
		this.trackMemberList = trackMemberList;
		roadRunnerSetting = RoadRunnerSetting.getInstance();
	}

	@Override
	public void run() {
		try {
			URL url_value;
			Bitmap profileIcon;
			String gurl = "https://graph.facebook.com/"
					+ roadRunnerSetting.getFacebookId()
					+ "/picture?width=75&height=75";
			url_value = new URL(gurl);
			profileIcon = BitmapFactory.decodeStream(url_value.openConnection()
					.getInputStream());
			roadRunnerSetting.setProfileImg(profileIcon);
			for (int i = 0; i < trackMemberList.size(); i++) {

				String name = "https://graph.facebook.com/"
						+ trackMemberList.get(i).getfId()
						+ "/picture?width=75&height=75";

				String imgPath = RoadRunnerSetting.SDPATH + "img/";
				File dir = new File(imgPath);

				url_value = new URL(name);
				profileIcon = BitmapFactory.decodeStream(url_value
						.openConnection().getInputStream());

				if (!dir.exists())
					dir.mkdirs();
				File file = new File(dir, trackMemberList.get(i).getfId()
						+ ".png");
				if (!file.exists()) {
					FileOutputStream fOut = new FileOutputStream(file);

					profileIcon.compress(Bitmap.CompressFormat.PNG, 85, fOut);
					fOut.flush();
					fOut.close();
				}

			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
