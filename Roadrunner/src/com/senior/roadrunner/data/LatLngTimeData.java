package com.senior.roadrunner.data;

import java.io.Serializable;



public class LatLngTimeData{
	private Coordinate coordinate;
	private String when;

	public LatLngTimeData(Coordinate coordinate2, String when) {
		this.coordinate = coordinate2;
		this.when = when;
	}

	public LatLngTimeData() {
		
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public void setWhen(String when) {
		this.when = when;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public String getWhen() {
		return when;
	}
}
