package com.senior.roadrunner.setting;

import android.graphics.Bitmap;
import android.os.Environment;

public class RoadRunnerSetting {
	private static String facebookName = "";
	private static String facebookId = "";
	private static Bitmap mapScreen;
	private static Bitmap profileIcon;
	public static String SDPATH = Environment.getExternalStorageDirectory()
			+ "/" + "roadrunner/";

	public static String getFacebookName() {
		return facebookName;
	}

	public static void setFacebookName(String facebookName) {
		RoadRunnerSetting.facebookName = facebookName;
	}

	public static String getFacebookId() {
		return facebookId;
	}

	public static void setFacebookId(String facebookId) {
		RoadRunnerSetting.facebookId = facebookId;
	}

	public static Bitmap getMapScreen() {
		return mapScreen;
	}

	public static void setMapScreen(Bitmap mapScreen) {
		RoadRunnerSetting.mapScreen = mapScreen;
	}

	public static void setProfileImg(Bitmap profileIcon) {
		RoadRunnerSetting.profileIcon = profileIcon;

	}

	public static Bitmap getProfileIcon() {
		return profileIcon;
	}
}
