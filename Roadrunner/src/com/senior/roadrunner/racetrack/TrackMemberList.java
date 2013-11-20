package com.senior.roadrunner.racetrack;

import java.io.Serializable;
import java.util.List;

import com.senior.roadrunner.data.LatLngTimeData;

public class TrackMemberList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String rId = "";
	private String fId = "";
	private int rank;
	private String trackerDir = "";
	private List<LatLngTimeData> trackData;

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
}
