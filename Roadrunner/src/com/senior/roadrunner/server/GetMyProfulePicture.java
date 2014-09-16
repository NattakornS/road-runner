package com.senior.roadrunner.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.senior.roadrunner.setting.RoadRunnerSetting;

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
			
			String imgPath = RoadRunnerSetting.SDPATH + "img/";
			File dir = new File(imgPath);

			url_value = new URL(gurl);
			profileIcon = BitmapFactory.decodeStream(url_value
					.openConnection().getInputStream());

			if (!dir.exists())
				dir.mkdirs();
			File file = new File(dir, roadRunnerSetting.getFacebookId()
					+ ".png");
			if (!file.exists()) {
				FileOutputStream fOut = new FileOutputStream(file);
				profileIcon.compress(Bitmap.CompressFormat.PNG, 85, fOut);
				fOut.flush();
				fOut.close();
			}
//			url_value = new URL(gurl);
//			profileIcon = BitmapFactory.decodeStream(url_value.openConnection()
//					.getInputStream());
//			
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
