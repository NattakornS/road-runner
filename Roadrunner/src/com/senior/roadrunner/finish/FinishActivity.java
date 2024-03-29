package com.senior.roadrunner.finish;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.ShareDialogBuilder;
import com.senior.roadrunner.MainActivity;
import com.senior.roadrunner.R;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.racetrack.MapsActivity;
import com.senior.roadrunner.server.ConnectServer;
import com.senior.roadrunner.server.UploadTask;
import com.senior.roadrunner.server.UploadTrack;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.tools.RoadrunnerTools;
import com.senior.roadrunner.trackchooser.TrackMemberList;
import com.senior.roadrunner.trackchooser.UploadActionBarView;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class FinishActivity extends FragmentActivity implements OnClickListener {

	private ArrayList<TrackMemberList> trackMemberList;

	private com.senior.roadrunner.finish.TabsAdapter mTabsAdapter;

	private UiLifecycleHelper uiHelper;
	private static final String PERMISSION = "publish_actions";

	private static final String tag = "FinishActivity";

	private final String PENDING_ACTION_BUNDLE_KEY = "com.senior.roadrunner.FinishActivity:PendingAction";

	private PendingAction pendingAction = PendingAction.NONE;
	private GraphUser user;
	private GraphPlace place;
	private List<GraphUser> tags;
	private boolean canPresentShareDialog;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onError(FacebookDialog.PendingCall pendingCall,
				Exception error, Bundle data) {
			Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.d("HelloFacebook", "Success!");
		}
	};

	private TrackMemberList myTrack;

	private RoadRunnerSetting roadRunnerSetting;

	private String trackName;

	private String parentName;

	private double lng;

	private double lat;

	// private UploadActionBarView mUploadActionView;

	private UploadActionBarView mUploadFBActionView;

	private SmoothProgressBar mProgressBar;

	private ListView finishListView;

	private TextView finishPlaceTxtView;

	private TextView finishAvgkphTxtView;

	private TextView finishNameTxtView;

	private TextView finishDurationTxtView;

	private TextView finishCaloriesTxtView;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish_layout);
		// get setting instance
		roadRunnerSetting = RoadRunnerSetting.getInstance();
		// set Uihelper to shared content
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		Intent intent = getIntent();
		parentName = intent.getStringExtra("ClassName");
		trackName = intent.getStringExtra("TrackName");
		trackMemberList = (ArrayList<TrackMemberList>) intent
				.getSerializableExtra("TrackMemberList");
		List<LatLngTimeData> trackFile = TrackDataBase
				.loadXmlFile(RoadRunnerSetting.SDPATH
						+ roadRunnerSetting.getFacebookId() + ".xml");
		if (trackFile != null) {
			if (trackFile.size() > 0) {
				lat = trackFile.get(0).getCoordinate().getLat();
				lng = trackFile.get(0).getCoordinate().getLng();
			} else {
				Log.e(tag,
						"track file size is less than zero. can't get first position for send to server.");
			}
		}

		for (int i = 0; i < trackMemberList.size(); i++) {
			if (trackMemberList.get(i).getfId()
					.equals(roadRunnerSetting.getFacebookId())) {
				myTrack = trackMemberList.get(i);
				break;
			}
		}
		initLayout();
		// initialisePaging();
		canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
		// upload result
		mProgressBar.setVisibility(SmoothProgressBar.VISIBLE);
		submitRaceResult();
		// Delete file in tracker directory
		RoadrunnerTools.deleteDirectory(new File(RoadRunnerSetting.SDPATH
				+ "tracker"));

	}

	private void initLayout() {
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		invalidateOptionsMenu();
		if (trackMemberList != null) {
			// FinishAdaptor aa = new FinishAdaptor(this, trackMemberList);
			finishListView = (ListView) findViewById(R.id.finishListView);
			// finishListView.setAdapter(aa);
		}
		mProgressBar = (SmoothProgressBar) findViewById(R.id.progressbar);
		mProgressBar.setVisibility(SmoothProgressBar.GONE);

		finishPlaceTxtView = (TextView) findViewById(R.id.finish_my_place_txt);

		finishAvgkphTxtView = (TextView) findViewById(R.id.finish_my_avgkph_txt);

		finishNameTxtView = (TextView) findViewById(R.id.finish_my_name_txt);

		finishDurationTxtView = (TextView) findViewById(R.id.finish_my_duration_txt);
		finishCaloriesTxtView = (TextView) findViewById(R.id.finish_my_calories_txt);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();

		// Call the 'activateApp' method to log an app event for use in
		// analytics and advertising reporting. Do so in
		// the onResume methods of the primary Activities that an app may be
		// launched into.
		AppEventsLogger.activateApp(this);

		updateUI();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		backToHome();
		uiHelper.onDestroy();
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(FinishActivity.this)
					.setTitle(R.string.cancelled)
					.setMessage(R.string.permission_not_granted)
					.setPositiveButton(R.string.ok, null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}
		updateUI();
	}

	@SuppressWarnings("unused")
	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());
	}

	@SuppressWarnings("incomplete-switch")
	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		// These actions may re-set pendingAction if they are still pending, but
		// we assume they
		// will succeed.
		pendingAction = PendingAction.NONE;

		switch (previouslyPendingAction) {
		case POST_PHOTO:
			postPhoto();
			break;
		case POST_STATUS_UPDATE:
			postStatusUpdate();
			break;
		}
	}

	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}

	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		String title = null;
		String alertMessage = null;
		if (error == null) {
			title = getString(R.string.success);
			String id = result.cast(GraphObjectWithId.class).getId();
			alertMessage = getString(R.string.successfully_posted_post,
					message, id);
		} else {
			title = getString(R.string.error);
			alertMessage = error.getErrorMessage();
		}

		new AlertDialog.Builder(this).setTitle(title).setMessage(alertMessage)
				.setPositiveButton(R.string.ok, null).show();
	}

	@SuppressWarnings("unused")
	private void onClickPostStatusUpdate() {
		performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
	}

	private ShareDialogBuilder createShareDialogBuilder() {

		List<String> friends = new ArrayList<String>();
		for (int i = 0; i < trackMemberList.size(); i++) {
			friends.add(trackMemberList.get(i).getfId());
		}
		return new FacebookDialog.ShareDialogBuilder(this)
				.setName("Roadrunner")
				.setDescription(
						"The 'Road Runner' Running and race application")
				.setPicture(
						RoadRunnerSetting.URLServer + "tracker/"
								+ MapsActivity.rId + "/"
								+ roadRunnerSetting.getFacebookId() + ".png")
				.setFriends(friends)
				.setLink("https://www.facebook.com/roadrunner5313180");
	}

	private void postStatusUpdate() {
		if (canPresentShareDialog) {
			FacebookDialog shareDialog = createShareDialogBuilder().build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		} else if (user != null && hasPublishPermission()) {
			final String message = getString(R.string.status_update,
					user.getFirstName(), (new Date().toString()));
			Request request = Request.newStatusUpdateRequest(
					Session.getActiveSession(), message, place, tags,
					new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult(message,
									response.getGraphObject(),
									response.getError());
						}
					});
			request.executeAsync();
		} else {
			pendingAction = PendingAction.POST_STATUS_UPDATE;
		}
	}

	private void onClickPostPhoto() {
		performPublish(PendingAction.POST_PHOTO, canPresentShareDialog);
	}

	private void postPhoto() {
		if (hasPublishPermission()) {
			Bitmap image = roadRunnerSetting.getMapScreen();
			String stringPost = "";
			for (int i = 0; i < trackMemberList.size(); i++) {
				stringPost = stringPost + (i + 1) + " "
						+ trackMemberList.get(i).getfName() + "\n";
			}
			Request request = Request.newMyUploadPhotoRequest(
					Session.getActiveSession(), image, "Roadrunner @ "
							+ trackName + " : \n" + stringPost, "Description",
					new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult(getString(R.string.photo_post),
									response.getGraphObject(),
									response.getError());
							mProgressBar.setVisibility(SmoothProgressBar.GONE);
							if (mUploadFBActionView != null)
								mUploadFBActionView.stopAnimatingBackground();
						}
					});
			Request.executeBatchAsync(request);

//		       Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
//	                @Override
//	                public void onCompleted(Response response) {
//	                	showPublishResult(getString(R.string.photo_post),
//								response.getGraphObject(),
//								response.getError());
//						mProgressBar.setVisibility(SmoothProgressBar.GONE);
//						if (mUploadFBActionView != null)
//							mUploadFBActionView.stopAnimatingBackground();
//					}
//	      
//	            });
	            
//	            Bundle params = request.getParameters();
//	            // Add the parameters you want, the caption in this case
//	            params.putString("name", "Roadrunner @ "+ trackName + " : \n" + stringPost);
//	            // Update the request parameters
//	            request.setParameters(params);          
//	            
//	            request.executeAsync();
	            
			// request=Request.newStatusUpdateRequest(Session.getActiveSession(),
			// "Roadrunner : \n"+stringPost, null, null, new Request.Callback(){
			//
			// @Override
			// public void onCompleted(Response response) {
			//
			// }
			//
			// });
			// request.executeAsync();
			// OpenGraphObject rdnr =
			// OpenGraphObject.Factory.createForPost("road_runner:Course");
			// rdnr.setProperty("title", "Roadrunner");
			// // rdnr.setProperty("image",
			// "https://example.com/cooking-app/meal/Shrimp-Curry.html");
			// // rdnr.setProperty("description", "...recipe text...");
			//
			// Bitmap bitmap1 = RoadRunnerSetting.getMapScreen();
			// List<Bitmap> images = new ArrayList<Bitmap>();
			// images.add(bitmap1);
			//
			// OpenGraphAction action =
			// GraphObject.Factory.create(OpenGraphAction.class);
			// action.setProperty("Run", rdnr);
			// action.setType("road_runner:Run");
			//
			// FacebookDialog shareDialog = new
			// FacebookDialog.OpenGraphActionDialogBuilder(this, action, "Run")
			// // .setImageAttachmentsForAction(images, true)
			// .build();
			// uiHelper.trackPendingDialogCall(shareDialog.present());
		} else {
			pendingAction = PendingAction.POST_PHOTO;
		}
	}

	@SuppressWarnings("unused")
	private void showAlert(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
				.setPositiveButton(R.string.ok, null).show();
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null
				&& session.getPermissions().contains("publish_actions");
	}

	private void performPublish(PendingAction action, boolean allowNoSession) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction();
				return;
			} else if (session.isOpened()) {
				// We need to get new permissions, then complete the action when
				// we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
						this, PERMISSION));
				return;
			}
		}

		if (allowNoSession) {
			pendingAction = action;
			handlePendingAction();
		}
	}

	// private void initialisePaging() {
	//
	// ViewPager pager = (ViewPager) super.findViewById(R.id.finishPager);
	// ActionBar bar = getActionBar();
	// bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	// bar.setTitle("Roadrunner");
	// bar.setSubtitle("Race Result");
	//
	//
	//
	// List<LatLngTimeData> trackFile = TrackDataBase
	// .loadXmlFile(RoadRunnerSetting.SDPATH
	// + roadRunnerSetting.getFacebookId() + ".xml");
	// if (trackFile != null) {
	// if (trackFile.size() > 0) {
	// lat = trackFile.get(0).getCoordinate().getLat();
	// lng = trackFile.get(0).getCoordinate().getLng();
	// } else {
	// Log.e(tag,
	// "track file size is less than zero. can't get first position for send to server.");
	// }
	// }
	//
	// mTabsAdapter = new TabsAdapter(this, pager, myTrack);
	// mTabsAdapter.addTab(bar.newTab().setText("Result"),
	// FinishMyListViewFragment.class, null);
	// mTabsAdapter.addTab(bar.newTab().setText("Maps"),
	// FinishMapFragment.class, null);
	// bar.setDisplayHomeAsUpEnabled(true);
	// invalidateOptionsMenu();
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.finish_menu, menu);

		// MenuItem item = menu.findItem(R.id.save_finish_menu_btn);
		// mUploadActionView = (UploadActionBarView) item.getActionView();
		// mUploadActionView.getAnimateImageView().setImageResource(
		// R.drawable.upload_t);

		MenuItem item = menu.findItem(R.id.share_facebook_finish_menu_btn);
		mUploadFBActionView = (UploadActionBarView) item.getActionView();
		mUploadFBActionView.getAnimateImageView().setImageResource(
				R.drawable.facebookshare);
		// onOptionsItemSelected will NOT be called for a custom View,
		// so set a OnClickListener and handle it ourselves.
		// mUploadActionView.setOnClickListener(this);
		mUploadFBActionView.setOnClickListener(this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.save_finish_menu_btn:
			// submitRaceResult();
			// backToHome();
			return true;
		case android.R.id.home:
			backToHome();
			return true;
		case R.id.share_facebook_finish_menu_btn:
			// onClickPostPhoto();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void backToHome() {

		Intent intent = new Intent(FinishActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.save_finish_menu_btn).setVisible(true);
		return super.onPrepareOptionsMenu(menu);
	}

	private void submitRaceResult() {
		System.out.println("Race result submited");
		//
		// OpenGraphAction action = GraphObject.Factory
		// .create(OpenGraphAction.class);
		// action.setProperty("Roadrunner",
		// "https://www.facebook.com/roadrunner5313180");
		//
		// Bitmap bitmap = RoadRunnerFacebookSetting.getMapScreen();
		// System.out.println(bitmap.getHeight());
		// List<Bitmap> images = new ArrayList<Bitmap>();
		// images.add(bitmap);
		// FacebookDialog shareDialog = new
		// FacebookDialog.OpenGraphActionDialogBuilder(
		// this, action, "Roadrunner")
		// .setImageAttachmentsForAction(images, true).build();
		// uiHelper.trackPendingDialogCall(shareDialog.present());
		//

		// OpenGraphObject recipe =
		// OpenGraphObject.Factory.createForPost("running-app:Roadrunner");
		// recipe.setProperty("title", "Roadrunner");
		// recipe.setProperty("description", "Mahidol Track");
		//
		// Bitmap bitmap1 = RoadRunnerFacebookSetting.getMapScreen();
		// List<Bitmap> images = new ArrayList<Bitmap>();
		// images.add(bitmap1);
		//
		// OpenGraphAction action =
		// GraphObject.Factory.create(OpenGraphAction.class);
		// action.setProperty("recipe", recipe);
		//
		// FacebookDialog shareDialog = new
		// FacebookDialog.OpenGraphActionDialogBuilder(this,
		// action,"Roadrunner")
		// .setImageAttachmentsForObject("running", images, true)
		// .build();
		// uiHelper.trackPendingDialogCall(shareDialog.present());
		uploadFile();
		// onClickPostStatusUpdate(); // Post to facebook

		// initFragment();
	}

	private void updateDataBase() {
		// Update DataBase Server when finish racing

		ConnectServer connectServer = new ConnectServer(this,
				RoadRunnerSetting.URLServer + "/connect_server.php");
		connectServer.addValue("Fid", myTrack.getfId());
		connectServer.addValue("Rid", myTrack.getrId());
		connectServer.addValue("Trackerdir", "tracker/" + myTrack.getrId()
				+ "/" + roadRunnerSetting.getFacebookId() + ".xml");
		connectServer.addValue("Rank", myTrack.getRank() + "");
		connectServer.addValue("Time", myTrack.getDuration() + "");
		connectServer.addValue("fName", myTrack.getfName());
		connectServer.execute();

	}

	public void onItemClick(int mPosition) {
		// TODO Auto-generated method stub

	}

	private void uploadFile() {
		if (parentName.equals("CreateTrackActivity")) {
			if (myTrack == null) {
				return;
			}
			UploadTrack uploadTrack = new UploadTrack(this, lat, lng,
					trackName, myTrack.getDistance());
			String exString[] = { RoadRunnerSetting.SDPATH
					+ roadRunnerSetting.getFacebookId() + ".xml" };
			uploadTrack.execute(exString);

		} else {
			updateDataBase();

			File sourceFile = new File(RoadRunnerSetting.SDPATH
					+ roadRunnerSetting.getFacebookId() + ".xml");
			File imgFile = new File(Environment.getExternalStorageDirectory()
					+ "/" + "roadrunner/" + roadRunnerSetting.getFacebookId()
					+ ".png");
			UploadTask uploadTask = new UploadTask(this);

			uploadTask.addMultipartValue("uploaded_path", new StringBody(
					"tracker/" + myTrack.getrId() + "/",
					ContentType.DEFAULT_TEXT));
			uploadTask.addMultipartValue("uploaded_file", new FileBody(
					sourceFile));
			uploadTask.addMultipartValue("uploaded_img", new FileBody(imgFile));

			uploadTask.execute();

			// UploadTask uploadTask = new UploadTask(this);
			// String exString[] = { MapsActivity.savePath,
			// MapsActivity.mapcapPath, MapsActivity.rId };
			// uploadTask.execute(exString);
			//
			// // onClickPostPhoto();
		}
	}

	@SuppressLint("ShowToast")
	public void setDataBaseServerResponse(String result) {
		// DataBase response result
//		System.out.println("Response DataBase : " + result);
//		Toast.makeText(this, "Database : " + result, 2000).show();
		mProgressBar.setVisibility(SmoothProgressBar.GONE);
		// clear Track memberlist for add a new rerank data from server.
		trackMemberList.clear();
		trackMemberList = new ArrayList<TrackMemberList>();
		try {
			// get race track data from database server to set ListAdapter.
			JSONArray jsonArr = new JSONArray(result);

			for (int i = 0; i < jsonArr.length(); i++) {
				final TrackMemberList sched = new TrackMemberList();
				JSONObject jsonObject = new JSONObject(jsonArr.getString(i));

				/******* Firstly take data in model object ******/
				sched.setfId(jsonObject.getString("Fid"));
				sched.setfName(jsonObject.getString("fName"));
				sched.setrId(jsonObject.getString("Rid"));
				sched.setRank(jsonObject.getInt("Rank"));
				sched.setTrackerDir(jsonObject.getString("Trackerdir"));
				sched.setDuration(Integer.parseInt(jsonObject.getString("Time")));
				/******** Take Model Object in ArrayList **********/
				trackMemberList.add(sched);
				if (roadRunnerSetting.getFacebookId().equals(sched.getfId())) {
					// FinishMyListViewFragment fin = FinishMyListViewFragment
					// .getInstance(sched);
//					myTrack.setRank(rank)
					long mils = sched.getDuration();
					int seconds = (int) (mils / 1000);
					int minutes = seconds / 60;
					seconds = seconds % 60;
					// fin.setPlace(sched.getRank() + "");
					// fin.setAvgKph(sched.getAVGSpeed() + "");
					// fin.setCalories("300");
					// fin.setName(sched.getfName());
					// fin.setDuration("" + minutes + ":"
					// + String.format("%02d", seconds));
					finishPlaceTxtView.setText(sched.getRank() + "");
					finishAvgkphTxtView.setText(String.format("%.2f", myTrack.getAVGSpeed()));
					finishNameTxtView.setText(sched.getfName());
					finishDurationTxtView.setText("" + minutes + ":"
							+ String.format("%02d", seconds));
					finishCaloriesTxtView.setText("300");
				}
			}

			// load profile picture to sd card
			// RaceTrackBitmapProfile getBitmapProfile = new
			// RaceTrackBitmapProfile(
			// trackMemberList);
			// getBitmapProfile.start();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		FinishAdaptor aa = new FinishAdaptor(this, trackMemberList);
		finishListView.setAdapter(aa);
		// if (mUploadActionView != null)
		// mUploadActionView.stopAnimatingBackground();

	}

	public void uploadTrackResponse(String response) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
			String rid = jsonObject.getString("NumRow");
			myTrack.setrId(rid);
			// String string[] = { CreateTrackActivity.savePath,
			// CreateTrackActivity.mapcapPath, rid };
			// Upload racetrack
			File sourceFile = new File(RoadRunnerSetting.SDPATH
					+ roadRunnerSetting.getFacebookId() + ".xml");
			File imgFile = new File(Environment.getExternalStorageDirectory()
					+ "/" + "roadrunner/" + roadRunnerSetting.getFacebookId()
					+ ".png");
			UploadTask uploadTask = new UploadTask(this);

			uploadTask.addMultipartValue("uploaded_path", new StringBody(
					"tracker/" + rid + "/", ContentType.DEFAULT_TEXT));
			uploadTask.addMultipartValue("uploaded_file", new FileBody(
					sourceFile));
			uploadTask.addMultipartValue("uploaded_img", new FileBody(imgFile));

			uploadTask.execute();

			updateDataBase();
			// onClickPostPhoto();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// updateDataBase();
	}

	@Override
	public void onClick(View v) {
		// actionbar button click
		// if (v == mUploadActionView) {
		// // Action Bar item has been clicked, do something...
		// // When you later want to animate the background, or stop the
		// // animate, just call:
		// if (null != mUploadActionView) {
		// // To start the animation
		// mUploadActionView.animateBackground();
		// submitRaceResult();
		// }
		// }
		if (v == mUploadFBActionView) {
			mProgressBar.setVisibility(SmoothProgressBar.VISIBLE);
			if (null != mUploadFBActionView) {
				// To start the animation
				mUploadFBActionView.animateBackground();
				onClickPostPhoto();
			}
		}

	}
}
