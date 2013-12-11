package com.senior.roadrunner.tools;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;
import com.senior.roadrunner.tools.LocationResolver.LocationResult;

public class LocationGetter {
    private final Context context;
    private Location location = null;
    private final Object gotLocationLock = new Object();
    private final LocationResult locationResult = new LocationResult() {            
        @Override
        public void gotLocation(Location location) {
            synchronized (gotLocationLock) {
                LocationGetter.this.location = location;
                gotLocationLock.notifyAll();
                Looper.myLooper().quit();
            }
        }
    };
	private LatLng coordinates;

    public LocationGetter(Context context) {
        if (context == null)
            throw new IllegalArgumentException("context == null");

        this.context = context;
    }

    public synchronized LatLng getLocation(int maxWaitingTime, int updateTimeout) {
        try {
            final int updateTimeoutPar = updateTimeout;
            synchronized (gotLocationLock) {            
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        LocationResolver locationResolver = new LocationResolver();
                        locationResolver.prepare();
                        locationResolver.getLocation(context, locationResult, updateTimeoutPar);
                        Looper.loop();
                    }
                }.start();

                gotLocationLock.wait(maxWaitingTime);
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        if (location != null){
            coordinates = new LatLng(location.getLatitude(), location.getLongitude());
        System.out.println("Location : "+location.getLatitude()+" "+location.getLongitude());
        }
        else
            coordinates = null;
        return coordinates; 
    }
}
