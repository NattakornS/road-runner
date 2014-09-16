package com.senior.roadrunner.finish;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.senior.roadrunner.R;
import com.senior.roadrunner.trackchooser.TrackMemberList;

public class FinishAdaptor extends BaseAdapter implements OnClickListener {

	private Activity activity;
	private ArrayList<TrackMemberList> data;
	private LayoutInflater inflater;
	private TrackMemberList tempValues;
	@SuppressWarnings("unused")
	private Bitmap profileIcon;
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener;
	private ImageLoader imageLoader;
	
	public FinishAdaptor(Activity finishActivity,
			ArrayList<TrackMemberList> trackMemberList) {
		activity = finishActivity;
		data = trackMemberList;
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.com_facebook_profile_picture_blank_square)
		.showImageForEmptyUri(R.drawable.com_facebook_profile_picture_blank_square)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.displayer(new RoundedBitmapDisplayer(20))
		.build();
		animateFirstListener = new AnimateFirstDisplayListener();
		File cacheDir = StorageUtils.getCacheDirectory(activity);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity)
		        .memoryCacheExtraOptions(75, 75) // default = device screen dimensions
		        .discCacheExtraOptions(75, 75, CompressFormat.PNG, 100, null)
		        .denyCacheImageMultipleSizesInMemory()
		        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
		        .memoryCacheSize(2 * 1024 * 1024)
		        .discCacheSize(50 * 1024 * 1024)
		        .discCacheFileCount(100)
		        .discCache(new UnlimitedDiscCache(cacheDir)) // default
		        .writeDebugLogs()
		        .build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
		/*********** Layout inflator to call external xml layout () **********************/
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/******** What is the size of Passed Arraylist Size ************/
	public int getCount() {

		if (data.size() <= 0)
			return 1;
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {

		public TextView nameTxt;
		public TextView durationTxt;
		public TextView placeTxt;
		public ImageView image;
		public ImageView placeImage;
		public TextView rank;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;

		if (convertView == null) {

			/********** Inflate tabitem.xml file for each row ( Defined below ) ************/
			vi = inflater.inflate(R.layout.finish_list_item, null);
			vi.setBackgroundColor(Color.alpha(R.color.blue_gradientE));
			// vi.setBackgroundResource(R.drawable.rounded_corners);
			/******** View Holder Object to contain tabitem.xml file elements ************/
			holder = new ViewHolder();
			holder.rank = (TextView) vi
					.findViewById(R.id.finish_rank);
			holder.nameTxt = (TextView) vi
					.findViewById(R.id.finish_name_list_txt);
			holder.durationTxt = (TextView) vi
					.findViewById(R.id.finish_duration_list_txt);
			holder.placeTxt = (TextView) vi
					.findViewById(R.id.finish_place_list_txt);
			holder.image = (ImageView) vi
					.findViewById(R.id.finish_pic_list_img);
			holder.placeImage = (ImageView) vi
					.findViewById(R.id.place_image);
			/************ Set holder with LayoutInflater ************/
			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		if (data.size() <= 0) {
			holder.nameTxt.setText("No Data");

		} else {
			/***** Get each Model object from Arraylist ********/
			tempValues = null;
			tempValues = data.get(position);
			 String name =
			 "https://graph.facebook.com/"+tempValues.getfId()+"/picture?width=75&height=75";
			// URL url_value;
			// try {
			// url_value = new URL(name);
			// profileIcon =
			// BitmapFactory.decodeStream(url_value.openConnection().getInputStream());
			// } catch (MalformedURLException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
//			Bitmap bitmap = null;
//			File f = new File(RoadRunnerSetting.SDPATH+"img/"+tempValues.getfId()+".png");
//
//			try {
//				bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
			int seconds = (int) (tempValues.getDuration() / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			/************ Set Model values in Holder elements ***********/
			holder.nameTxt.setText(tempValues.getfName());
			holder.placeTxt.setText(tempValues.getRank() + "");
			holder.durationTxt.setText("" + minutes + ":"
					+ String.format("%02d", seconds));
			//Universal image loader
			imageLoader.displayImage(name, holder.image, options, animateFirstListener);
//			holder.image.setImageBitmap(bitmap);
			switch (tempValues.getRank()) {
			case 1:
				holder.placeImage.setImageResource(R.drawable.first);
				break;
			case 2:
				holder.placeImage.setImageResource(R.drawable.second);
				break;
			case 3:
				holder.placeImage.setImageResource(R.drawable.thirds);
				break;
			case 4:
				holder.placeImage.setImageResource(R.drawable.fourth);
				break;
			case 5:
				holder.placeImage.setImageResource(R.drawable.fifth);
				break;
			default:
				holder.placeImage.setImageResource(R.drawable.award);
				holder.rank.setText(tempValues.getRank()+"");
				break;
			}
			/******** Set Item Click Listner for LayoutInflater for each row ***********/
			vi.setOnClickListener(new OnItemClickListener(position));
		}
		return vi;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	/********* Called when Item click in ListView ************/
	@SuppressLint("ShowToast")
	private class OnItemClickListener implements OnClickListener {
		private int mPosition;

		OnItemClickListener(int position) {
			mPosition = position;

		}

		@Override
		public void onClick(View arg0) {
			FinishActivity sct = (FinishActivity) activity;
			sct.onItemClick(mPosition);
			Toast.makeText(activity, "click : " + mPosition, 1000).show();
		}
	}
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
}
