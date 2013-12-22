package com.senior.roadrunner.trackchooser;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class TrackList implements Parcelable {

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	private String rId = "";
	private String raceTrackName = "";
	private String Image = "";
	private String placeName = "";
	private double lon;
	private double lat;
	private String rDir;
	private String trackData;
	private ArrayList<TrackMemberList> trackMemberList;

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

	public ArrayList<TrackMemberList> getTrackMemberList() {
		return trackMemberList;
	}

	public void setTrackMemberList(ArrayList<TrackMemberList> trackMemberList) {
		this.trackMemberList = trackMemberList;
	}

	public String getTrackData() {
		return trackData;
	}

	public void setTrackData(String trackData) {
		this.trackData = trackData;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
