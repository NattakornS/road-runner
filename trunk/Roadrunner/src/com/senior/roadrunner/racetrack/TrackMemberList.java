package com.senior.roadrunner.racetrack;

import java.io.Serializable;
import java.util.List;

import android.graphics.Bitmap;

import com.senior.roadrunner.data.LatLngTimeData;

public class TrackMemberList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String rId = "";
	private String fId = "";
	private String fName ="";
	private int duration;
	private int calories;
	private int rank;
	private String trackerDir = "";
	private List<LatLngTimeData> trackData;
	private Bitmap profileImg;

	public String getrId() {
		return rId;
	}

	public void setrId(String rId) {
		this.rId = rId;
	}

	public String getfId() {
		return fId;
	}

	public void setfId(String fId) {
		this.fId = fId;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getTrackerDir() {
		return trackerDir;
	}

	public void setTrackerDir(String trackerDir) {
		this.trackerDir = trackerDir;
	}

	public void setTrackData(List<LatLngTimeData> trackData) {
		this.trackData = trackData;

	}

	public List<LatLngTimeData> getTrackData() {
		return trackData;
	}

	public String getfName() {
		return fName;
	}

	public void setfName(String fName) {
		this.fName = fName;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getCalories() {
		return calories;
	}

	public void setCalories(int calories) {
		this.calories = calories;
	}

	public Bitmap getProfileImg() {
		return profileImg;
	}

	public void setProfileImg(Bitmap profileImg) {
		this.profileImg = profileImg;
	}
}
