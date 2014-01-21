package com.senior.roadrunner.setting;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.senior.roadrunner.tools.RoadrunnerTools;

import android.graphics.Bitmap;
import android.os.Environment;

public class RoadRunnerSetting {
	private String facebookName;
	private String facebookId;
	private Bitmap mapScreen;
	private Bitmap profileIcon;
	public static String SDPATH = Environment.getExternalStorageDirectory()
			+ File.separator + "roadrunner" + File.separator;
	private String city;
	private String raceTrackName;
	public static String URLServer = "http://roadrunner-5313180.dx.am/";
	private static RoadRunnerSetting instance;
	private static final String WEIGHT = "weight";
	private static final String LANGUAGE = "language";
	private static final String FNAME = "fname";
	private static final String FID = "fid";
	public static synchronized RoadRunnerSetting getInstance() {
		if (instance == null) {
			instance = new RoadRunnerSetting();
			
		}
		return instance;
	}
	private void readSetting() {
		String str = RoadrunnerTools
				.readStringFromFile(RoadRunnerSetting.SDPATH + "setting.ini");
		if (str == null || str == "") {
			RoadrunnerTools.writeStringToFile(RoadRunnerSetting.SDPATH
					+ "setting.ini", "");
		} else {
			try {
				JSONObject settingjsonObject = new JSONObject(str);

				instance.setFacebookId(settingjsonObject.getString(FID));
				instance.setFacebookName(settingjsonObject.getString(FNAME));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	public String getFacebookName() {
		return facebookName;
	}

	public void setFacebookName(String facebookName) {
		this.facebookName = facebookName;
	}

	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String facebookId) {
		System.out.println("setFacebookId : "+facebookId);
		this.facebookId = facebookId;
	}

	public Bitmap getMapScreen() {
		return mapScreen;
	}

	public void setMapScreen(Bitmap mapScreen) {
		this.mapScreen = mapScreen;
	}

	public void setProfileImg(Bitmap profileIcon) {
		this.profileIcon = profileIcon;

	}

	public Bitmap getProfileIcon() {
		return profileIcon;
	}

	public void setCity(String city) {
		this.city = city;

	}

	public String getCity() {
		return city;
	}

	public void setRaceTrackName(String raceTrackName) {
		this.raceTrackName = raceTrackName;

	}

	public String getRaceTrackName() {
		return raceTrackName;
	}
}
