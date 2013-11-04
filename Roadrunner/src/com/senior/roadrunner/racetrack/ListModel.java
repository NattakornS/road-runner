package com.senior.roadrunner.racetrack;

public class ListModel {
	private String rId = "";
	private String raceTrackName = "";
	private String Image = "";
	private String placeName = "";
	private double lon;
	private double lat;
	private String rDir;

	/*********** Set Methods ******************/
	public void setRaceTrackName(String raceTrackName) {
		this.raceTrackName = raceTrackName;
	}

	public void setImage(String image) {
		Image = image;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	/*********** Get Methods ****************/
	public String getRaceTrackName() {
		return raceTrackName;
	}

	public String getPlaceName() {
		return placeName;
	}

	public String getImage() {
		return Image;
	}

	public String getrId() {
		return rId;
	}

	public void setrId(String rId) {
		this.rId = rId;
	}

	public void setDoubleLat(double lat) {
		this.lat=lat;
		
	}

	public void setDoubleLon(double lon) {
		this.lon=lon;
		
	}
	public double getDoubleLat() {
		return lat;
	}
	public double getDoubleLon() {
		return lon;
	}

	public void setRdir(String rDir) {
		this.rDir = rDir;
		
	}
	public String getRdir() {
		return rDir;
	}
}
