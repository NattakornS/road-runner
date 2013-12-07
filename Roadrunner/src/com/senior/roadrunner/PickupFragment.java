package com.senior.roadrunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.tools.GPSSpeed;
import com.senior.roadrunner.tools.LatLngInterpolator.Spherical;
import com.senior.roadrunner.tools.MarkerAnimation;

@SuppressLint("NewApi")
public class PickupFragment extends Fragment implements OnClickListener {
	
	LocationManager myLocationManager;
	MyLocationListener myLocationListener;
	private View btn_stop_track;
	private Button btn_track;
	private List<LatLngTimeData> latLngTimeData;
//	private Button btn_load_track;
	private HistoryTrack historyTrack;
	private static final String SDCARD_TRACKER_XML = "/sdcard/tracker.xml";
	
    public PickupFragment() {
        // Empty constructor required for fragment subclasses
    }

    static GoogleMap map;
    Context context;
    View rootView;
    MapFragment pickupMapFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.map_layout, container, false);
       
        return rootView;
    }
    @Override
    public void onResume(){
        super.onResume();
        setupMap();
    }

    private void setupMap() {
      if (map != null)
        return;
      map = pickupMapFragment.getMap();
      if (map == null)
        return;
    }
    private void killOldMap() {
    	FragmentManager fm = getFragmentManager();
        pickupMapFragment 
           = (MapFragment)fm.findFragmentById(R.id.maps);

        if(pickupMapFragment != null) {
            FragmentManager fM = getFragmentManager();
            fM.beginTransaction().remove(pickupMapFragment).commit();
        }

    }
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	killOldMap();
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	initwidget();
    	 FragmentManager fm = getFragmentManager();
         pickupMapFragment 
            = (MapFragment)fm.findFragmentById(R.id.maps);
         if (pickupMapFragment == null) {
             pickupMapFragment = MapFragment.newInstance();
             fm.beginTransaction().replace(R.id.maps, pickupMapFragment).commit();
         }     
         map = pickupMapFragment.getMap();
         context = getActivity();

         map.setMyLocationEnabled(true);
         map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
 				13.793888, 100.324146), 15.0f));
         historyTrack = new HistoryTrack(map, getActivity());
      // ////////////////////// GPS tracker //////////////////////

 		myLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
 		Context context = getActivity().getApplicationContext();
 		myLocationListener = new MyLocationListener(context, map);
    }
    private void initwidget() {
		btn_track = (Button) getActivity().findViewById(R.id.btn_track);
		btn_track.setOnClickListener(this);
		btn_stop_track = (Button) getActivity().findViewById(R.id.btn_stop_track);
		btn_stop_track.setOnClickListener(this);
//		btn_load_track = (Button) getActivity().findViewById(R.id.btn_load_track);
//		btn_load_track.setOnClickListener(this);

	}

	public boolean isGpsEnable() {
		boolean isgpsenable = false;
		String provider = Settings.Secure.getString(getActivity().getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.equals("")) { // GPS is Enabled
			isgpsenable = true;
		}
		return isgpsenable;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_track:
			if (!isGpsEnable()) {
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							Intent intent = new Intent(
									Settings.ACTION_SECURITY_SETTINGS);
							startActivity(intent);
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							// No button clicked
							break;
						}
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Setting GPS?")
						.setPositiveButton("Yes", dialogClickListener)
						.setNegativeButton("No", dialogClickListener).show();
			} else {

				final Criteria criteria = new Criteria();

				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				criteria.setSpeedRequired(true);
				criteria.setAltitudeRequired(false);
				criteria.setBearingRequired(false);
				criteria.setCostAllowed(true);
				criteria.setPowerRequirement(Criteria.POWER_LOW);

				final String bestProvider = myLocationManager
						.getBestProvider(criteria, true);

				if (bestProvider != null && bestProvider.length() > 0) {
					myLocationManager.requestLocationUpdates(
							bestProvider, 1000,
							15,
							myLocationListener);
				} else {
					final List<String> providers = myLocationManager
							.getProviders(true);

					for (final String provider : providers) {
						myLocationManager.requestLocationUpdates(
								provider, 1000,
								15,
								myLocationListener);
					}
				}

				myLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 1000, 15,
						myLocationListener);
				Toast.makeText(getActivity().getApplicationContext(), "Track data",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_stop_track:
			myLocationManager.removeUpdates(myLocationListener);
			latLngTimeData = myLocationListener.getLatLngTimeData();
			if (latLngTimeData.isEmpty()) {
				break;
			}
			String fId = "1234455";
			String savePath = Environment.getExternalStorageDirectory() + "/" + "roadrunner/"+fId+".xml";
			TrackDataBase.saveXmlFile(latLngTimeData,savePath);
			Toast.makeText(getActivity().getApplicationContext(), "Save data",
					Toast.LENGTH_SHORT).show();
			break;

//		case R.id.btn_load_track:
//			// myLocationListener.setLatLngTimeData(latLngTimeData);
//			Toast.makeText(getActivity().getApplicationContext(), "Load data",
//					Toast.LENGTH_SHORT).show();
//			// historyTrack.setLatLngTimeData(trackDataBase.loadXmlFile());
//			// this.runOnUiThread(historyTrack);
//			// Thread t = new Thread(historyTrack);
//			// t.start();
//			runThread();
//			break;
		}

	}

	private void runThread() {

		new Thread() {
			private int i;
			private Marker marker = null;
			private SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			private List<LatLngTimeData> data = TrackDataBase
					.loadXmlFile(SDCARD_TRACKER_XML);
			private long waitingTime;
			private LatLng point;
			private LatLng end;

			public void run() {
				if (data == null) {
					return;
				}

				for (i = 0; i < data.size(); i++) {
					try {
						Date recentDate = sdf.parse(data.get(i).getWhen());

						double lat = data.get(i).getCoordinate().getLat();
						double lng = data.get(i).getCoordinate().getLng();

						point = new LatLng(lat, lng);

						if (i == data.size() - 1) {
							break;
						}
						Date futureDate = sdf.parse(data.get(i + 1).getWhen());

						waitingTime = futureDate.getTime()
								- recentDate.getTime();
						System.out.println("wait : " + waitingTime);

						double elat = data.get(i + 1).getCoordinate().getLat();
						double elng = data.get(i + 1).getCoordinate().getLng();
						end = new LatLng(elat, elng);
						// System.out.println("Speed m/s : "+GPSSpeed.SpeedFrom2PointTime(point,
						// end, futureDate.getTime(), recentDate.getTime()));
						final long speed = GPSSpeed
								.SpeedFrom2PointTime(point, end,
										futureDate.getTime(),
										recentDate.getTime());
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {

								if (marker != null) {
									marker.remove();
								}

								marker = map.addMarker(new MarkerOptions()
										.position(point)
										.icon(BitmapDescriptorFactory
												.defaultMarker())
										.title("History")
										.snippet("Speed : " + speed + "m/s"));
								marker.showInfoWindow();
								Spherical latLngInterpolator = new Spherical();
								latLngInterpolator
										.interpolate(5.0f, point, end);
								MarkerAnimation.animateMarkerToICS(marker, end,
										latLngInterpolator);
								map.animateCamera(CameraUpdateFactory
										.newLatLngZoom(end, 17.0f));

							}
						});
						Thread.sleep(waitingTime);

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}

			}
		}.start();
	}
}
