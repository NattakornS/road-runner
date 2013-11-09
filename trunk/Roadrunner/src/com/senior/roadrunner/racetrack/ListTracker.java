package com.senior.roadrunner.racetrack;

public class ListTracker {
	private String rId = "";
	private String fId = "";
	private int rank;
	private String trackerDir = "";
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

}
