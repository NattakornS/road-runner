package com.senior.roadrunner.finish;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

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
import com.senior.roadrunner.CreateTrackActivity;
import com.senior.roadrunner.MainActivity;
import com.senior.roadrunner.R;
import com.senior.roadrunner.data.LatLngTimeData;
import com.senior.roadrunner.data.TrackDataBase;
import com.senior.roadrunner.racetrack.MapsActivity;
import com.senior.roadrunner.server.ConnectServer;
import com.senior.roadrunner.server.UploadTask;
import com.senior.roadrunner.server.UploadTrack;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.trackchooser.TrackMemberList;

public class FinishActivity extends FragmentActivity {

	private ArrayList<TrackMemberList> trackMemberList;

	private com.senior.roadrunner.finish.TabsAdapter mTabsAdapter;

	private UiLifecycleHelper uiHelper;
	private static final String PERMISSION = "publish_actions";

	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

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
		// trackMemberList = MapsActivity.getTrackMemberList();
		if (trackMemberList != null) {
			FinishAdaptor aa = new FinishAdaptor(this, trackMemberList);
			final ListView ll = (ListView) findViewById(R.id.finishListView);
			ll.setAdapter(aa);
		}

		initialisePaging();
		canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
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
							+ trackName + " : \n"
							+ stringPost, "", new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult(getString(R.string.photo_post),
									response.getGraphObject(),
									response.getError());
						}
					});
			Request.executeBatchAsync(request);

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

	private void initialisePaging() {

		ViewPager pager = (ViewPager) super.findViewById(R.id.finishPager);
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setTitle("Roadrunner");
		bar.setSubtitle("Race Result");

		System.out.println("Facebook Name : "
				+ roadRunnerSetting.getFacebookId());
		for (int i = 0; i < trackMemberList.size(); i++) {
			System.out.println(trackMemberList.get(i).getfName());
			if (trackMemberList.get(i).getfId()
					.equals(roadRunnerSetting.getFacebookId())) {
				myTrack = trackMemberList.get(i);
				break;
			}
		}

		List<LatLngTimeData> trackFile = TrackDataBase
				.loadXmlFile(roadRunnerSetting.SDPATH
						+ roadRunnerSetting.getFacebookId() + ".xml");
		if (trackFile != null) {
			lat = trackFile.get(0).getCoordinate().getLat();
			lng = trackFile.get(0).getCoordinate().getLng();
		}

		mTabsAdapter = new TabsAdapter(this, pager, myTrack);
		mTabsAdapter.addTab(bar.newTab().setText("Result"),
				FinishMyListViewFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setText("Maps"),
				FinishMapFragment.class, null);
		bar.setDisplayHomeAsUpEnabled(true);
		invalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.finish_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.save_finish_menu_btn:
			submitRaceResult();
			// backToHome();
			return true;
		case android.R.id.home:
			backToHome();
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
//		onClickPostStatusUpdate(); // Post to facebook
		
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
			System.out.println("Upload Track");
			UploadTrack uploadTrack = new UploadTrack(this, lat, lng, trackName);
			String exString[] = { CreateTrackActivity.savePath };
			uploadTrack.execute(exString);

		} else {
			updateDataBase();
			UploadTask uploadTask = new UploadTask(this);
			String exString[] = { MapsActivity.savePath,
					MapsActivity.mapcapPath, MapsActivity.rId };
			uploadTask.execute(exString);
			onClickPostPhoto();
		}
	}

	@SuppressLint("ShowToast")
	public void setDataBaseServerResponse(String result) {
		// DataBase response result
		Log.d("Response DataBase : ", result);
		Toast.makeText(this, "Database : " + result, 500).show();

	}

	public void uploadTrackResponse(String response) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
			String rid = jsonObject.getString("NumRow");
			System.out.println(rid);
			myTrack.setrId(rid);

			// Upload racetrack
			UploadTask uploadTask = new UploadTask(this);
			String string[] = { CreateTrackActivity.savePath,
					CreateTrackActivity.mapcapPath, rid };
			uploadTask.execute(string);

			updateDataBase();
			onClickPostPhoto();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// updateDataBase();
	}
}