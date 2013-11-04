package com.senior.roadrunner.tools;

import com.google.android.gms.maps.model.LatLng;

public class GPSSpeed {
/*
 * time : time between two point
 * 
 * 
 * */
	public static long SpeedFrom2PointTime(LatLng start ,LatLng end,long startTime,long endTime){
		long speed = 0;
		double distance = Distance.calclateArc(start.latitude, start.longitude, end.latitude, end.longitude, Distance.KILOMETERS);
		System.out.println("Distance km : "+ distance);
		long sec =(startTime-endTime)/(1000);
		System.out.println("Time : "+sec);
		speed=(long) (distance*1000/sec);
		return speed;
		
	}
}
