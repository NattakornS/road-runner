package com.senior.roadrunner.data;



public class Coordinate {
	private double lng;
	private double lat;

	public Coordinate(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}

	public Coordinate() {
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}
}
