package com.senior.roadrunner.racetrack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.senior.roadrunner.R;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.tools.GPSSpeed;
import com.senior.roadrunner.tools.LatLngInterpolator.Spherical;
import com.senior.roadrunner.tools.MarkerAnimation;
import com.senior.roadrunner.trackchooser.TrackMemberList;

public class RaceThread implements Runnable {
	private static final String TAG = "RaceThread";
	private int i;
	private Marker marker = null;
	private SimpleDateFormat sdf;
	private List<LatLngTimeData> data;
	private long waitingTime;
	private LatLng point;
	private LatLng end;
	private GoogleMap map;
	private Activity activity;
	private TrackMemberList listTracker;
	private Bitmap profileIcon;
	private Bitmap bmp;
	private ImageView imageView;
	private View customMarker;
	private long timeSpeek;
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener;
	private ImageLoader imageLoader;

	@SuppressLint("SimpleDateFormat")
	public RaceThread(TrackMemberList listTracker, GoogleMap map,
			Activity activity) {
		this.listTracker = listTracker;
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		this.map = map;
		this.activity = activity;
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(
						R.drawable.com_facebook_profile_picture_blank_square)
				.showImageForEmptyUri(
						R.drawable.com_facebook_profile_picture_blank_square)
				.showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisc(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		// File cacheDir = StorageUtils.getCacheDirectory(activity);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				activity)
				.memoryCacheExtraOptions(480, 800)
				// default = device screen dimensions
				.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
				.memoryCacheSize(2 * 1024 * 1024)
				.discCacheSize(50 * 1024 * 1024).discCacheFileCount(100)
				.writeDebugLogs()
				// .discCache(new UnlimitedDiscCache(cacheDir)) // default
				.build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
		customMarker = ((LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.custom_marker_layout, null);
		imageView = (ImageView) customMarker.findViewById(R.id.profileIcon);

		Thread thread = new Thread(this);
		thread.start();

	}

	@Override
	public void run() {

		if (listTracker == null) {
			return;
		}
		String name = "https://graph.facebook.com/" + listTracker.getfId()
				+ "/picture?75=&height=75";
		;
		// File f = new File(RoadRunnerSetting.SDPATH + "img/"
		// + listTracker.getfId() + ".png");
		//
		// try {
		// profileIcon = BitmapFactory.decodeStream(new FileInputStream(f));
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }
		// Universal image loader
		// imageView.setImageBitmap(imageLoader.loadImageSync(name));
		// profileIcon = createDrawableFromView(activity,customMarker);
		imageLoader.loadImage(name, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				// Do whatever you want with Bitmap
				imageView.setImageBitmap(loadedImage);
				profileIcon = createDrawableFromView(activity, customMarker);
			}
		});

		data = TrackDataBase.loadXmlFile(RoadRunnerSetting.SDPATH
				+ listTracker.getTrackerDir());
		if (data == null) {
			Log.e(TAG, "Data is error while loading tracker file from server");
			return;
		}
		try {
			Date startDate = sdf.parse(data.get(0).getWhen());

			for (i = 0; i < data.size(); i++) {

				Date recentDate = sdf.parse(data.get(i).getWhen());

				double lat = data.get(i).getCoordinate().getLat();
				double lng = data.get(i).getCoordinate().getLng();

				point = new LatLng(lat, lng);

				if (i == data.size() - 1) {
					break;
				}
				Date futureDate = sdf.parse(data.get(i + 1).getWhen());
				timeSpeek = futureDate.getTime() - startDate.getTime();
				waitingTime = futureDate.getTime() - recentDate.getTime();
				// System.out.println("wait : " + waitingTime);

				double elat = data.get(i + 1).getCoordinate().getLat();
				double elng = data.get(i + 1).getCoordinate().getLng();
				end = new LatLng(elat, elng);
				if ((timeSpeek % 300000) < 1000) {
					if (activity instanceof MapsActivity) {
						((MapsActivity) activity).setThreadLocation(
								listTracker.getfName(), end);
					}
				}
				// System.out.println("Speed m/s : "+GPSSpeed.SpeedFrom2PointTime(point,
				// end, futureDate.getTime(), recentDate.getTime()));
				final long speed = GPSSpeed.SpeedFrom2PointTime(point, end,
						futureDate.getTime(), recentDate.getTime());
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {

						if (marker != null) {
							marker.remove();
						}
						if (profileIcon == null) {
							marker = map.addMarker(new MarkerOptions()
									.position(point)
									.icon(BitmapDescriptorFactory
											.fromBitmap(createDrawableFromView(activity, customMarker)))
									.title(listTracker.getfName())
									.snippet("Speed : " + speed + "KPH"));
							marker.showInfoWindow();

							Spherical latLngInterpolator = new Spherical();
							latLngInterpolator.interpolate(5.0f, point, end);
							MarkerAnimation.animateMarkerToICS(marker, end,
									latLngInterpolator, waitingTime);
						} else {
							marker = map.addMarker(new MarkerOptions()
									.position(point)
									.icon(BitmapDescriptorFactory
											.fromBitmap(profileIcon))
									.title(listTracker.getfName())
									.snippet("Speed : " + speed + "KPH"));
							marker.showInfoWindow();

							Spherical latLngInterpolator = new Spherical();
							latLngInterpolator.interpolate(5.0f, point, end);
							MarkerAnimation.animateMarkerToICS(marker, end,
									latLngInterpolator, waitingTime);

						}

						// map.animateCamera(CameraUpdateFactory.newLatLngZoom(
						// end, 17.0f));

					}
				});
				Thread.sleep(waitingTime);

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	// Convert a view to bitmap
	public Bitmap createDrawableFromView(Context context, View view) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
		view.layout(0, 0, displayMetrics.widthPixels,
				displayMetrics.heightPixels);
		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
				view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);

		return adjustOpacity(bitmap, 200);
	}

	public Bitmap adjustOpacity(Bitmap bitmap, int opacity) {
		Bitmap mutableBitmap = bitmap.isMutable() ? bitmap : bitmap.copy(
				Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(mutableBitmap);
		int colour = (opacity & 0xFF) << 24;
		canvas.drawColor(colour, PorterDuff.Mode.DST_IN);
		return mutableBitmap;
	}
	// private static class AnimateFirstDisplayListener extends
	// SimpleImageLoadingListener {
	//
	// static final List<String> displayedImages =
	// Collections.synchronizedList(new LinkedList<String>());
	//
	// @Override
	// public void onLoadingComplete(String imageUri, View view, Bitmap
	// loadedImage) {
	// if (loadedImage != null) {
	// ImageView imageView = (ImageView) view;
	// boolean firstDisplay = !displayedImages.contains(imageUri);
	// if (firstDisplay) {
	// FadeInBitmapDisplayer.animate(imageView, 500);
	// displayedImages.add(imageUri);
	//
	// }
	// }
	// }
	// }
}
