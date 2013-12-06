package com.senior.roadrunner.setting;

import android.graphics.Bitmap;

public class RoadRunnerFacebookSetting {
	private static String facebookName = "";
	private static String facebookId = "";
	private static Bitmap mapScreen;
	private static Bitmap profileIcon;
	public static String getFacebookName() {
		return facebookName;
	}

	public static void setFacebookName(String facebookName) {
		RoadRunnerFacebookSetting.facebookName = facebookName;
	}

	public static String getFacebookId() {
		return facebookId;
	}

	public static void setFacebookId(String facebookId) {
		RoadRunnerFacebookSetting.facebookId = facebookId;
	}

	public static Bitmap getMapScreen() {
		return mapScreen;
	}public static void setMapScreen(Bitmap mapScreen) {
		RoadRunnerFacebookSetting.mapScreen = mapScreen;
	}

	public static void setProfileImg(Bitmap profileIcon) {
		RoadRunnerFacebookSetting.profileIcon = profileIcon;
		
	}
	public static Bitmap getProfileIcon() {
		return profileIcon;
	}
}
