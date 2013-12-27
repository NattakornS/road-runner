package com.senior.roadrunner.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.senior.roadrunner.setting.RoadRunnerSetting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GetMyProfulePicture extends Thread {
	private RoadRunnerSetting roadRunnerSetting;

	public GetMyProfulePicture() {
		roadRunnerSetting = RoadRunnerSetting.getInstance();
	}

	@Override
	public void run() {
		URL url_value;
		Bitmap profileIcon;
		String gurl = "https://graph.facebook.com/"
				+ roadRunnerSetting.getFacebookId()
				+ "/picture?width=75&height=75";
		try {
			url_value = new URL(gurl);
			profileIcon = BitmapFactory.decodeStream(url_value.openConnection()
					.getInputStream());
			roadRunnerSetting.setProfileImg(profileIcon);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
