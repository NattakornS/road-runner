package com.senior.roadrunner.data;

import java.util.ArrayList;
import java.util.List;

public class TrackData {
	// private ArrayList<LatLngTimeData> track;
	private List<LatLngTimeData> tracks = new ArrayList<LatLngTimeData>();

	public TrackData() {

	}

	public void add(LatLngTimeData track) {
		tracks.add(track);
	}

	public List<LatLngTimeData> getTracks() {
		return tracks;
	}
}
