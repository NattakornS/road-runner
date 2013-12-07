package com.senior.roadrunner.finish;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
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
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.ShareDialogBuilder;
import com.facebook.widget.ProfilePictureView;
import com.senior.roadrunner.MapsActivity;
import com.senior.roadrunner.R;
import com.senior.roadrunner.RaceTrackSelectorActivity;
import com.senior.roadrunner.racetrack.TrackMemberList;
import com.senior.roadrunner.server.ConnectServer;
import com.senior.roadrunner.server.UploadTask;
import com.senior.roadrunner.setting.RoadRunnerSetting;

public class FinishActivity extends FragmentActivity {
	private int imageArra[] = { R.drawable.action_search,
			R.drawable.ic_launcher, R.drawable.bar_bg, R.drawable.blue_menu_btn };

	private String text[] = new String[] { "Nattakorn Sanpabopit",
			"Jetarin Samuel", "Pathompong Kornkaseam",
			"Thanathan Choysongkroi", "Fernando Tortoa", "Max Ratthapol",
			"Mapraw Naja", "Mai Ramita", "NNNNNNNNNNNNN NNNNNNNNNNN",
			"Jaturong Panipak" };

	private ArrayList<TrackMemberList> trackMemberList;
	private ViewPagerAdapter mPagerAdapter;

	private com.senior.roadrunner.finish.TabsAdapter mTabsAdapter;

	private ViewPager pager;

	private FinishMyListViewFragment finishMyListViewFragment;

	private UiLifecycleHelper uiHelper;
	private static final String PERMISSION = "publish_actions";
	private static final Location SEATTLE_LOCATION = new Location("") {
		{
			setLatitude(47.6097);
			setLongitude(-122.3331);
		}
	};

	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

	private ProfilePictureView profilePictureView;
	private TextView greeting;
	private PendingAction pendingAction = PendingAction.NONE;
	private ViewGroup controlsContainer;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish_layout);
		// set Uihelper to shared content
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		 Intent intent = getIntent();
		 trackMemberList = (ArrayList<TrackMemberList>) intent
		 .getSerializableExtra("TrackMemberList");
//		trackMemberList = MapsActivity.getTrackMemberList();
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

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());
	}

    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
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

	private void onClickPostStatusUpdate() {
		performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
	}

	private ShareDialogBuilder createShareDialogBuilder() {

		
		 List<String> friends = new ArrayList<String>();
		 for (int i = 0; i < trackMemberList.size(); i++) {
			friends.add(trackMemberList.get(i).getfName());
		}
		return new FacebookDialog.ShareDialogBuilder(this)
		 .setName("Roadrunner")
		 .setDescription("The 'Road Runner' Running and race application")
		 .setPicture(MapsActivity.URLServer+"tracker/" + MapsActivity.rId + "/"+RoadRunnerSetting.getFacebookId() +".png")
		 .setFriends(friends )
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
//	            Bitmap image = RoadRunnerFacebookSetting.getMapScreen();
//	            Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
//	                @Override
//	                public void onCompleted(Response response) {
//	                    showPublishResult(getString(R.string.photo_post), response.getGraphObject(), response.getError());
//	                }
//	            });
//	            request.executeAsync();
	        	OpenGraphObject rdnr = OpenGraphObject.Factory.createForPost("road_runner:Course");
	        	rdnr.setProperty("title", "Roadrunner");
//	        	rdnr.setProperty("image", "https://example.com/cooking-app/meal/Shrimp-Curry.html");
//	        	rdnr.setProperty("description", "...recipe text...");

	        	Bitmap bitmap1 = RoadRunnerSetting.getMapScreen();
	        	List<Bitmap> images = new ArrayList<Bitmap>();
	        	images.add(bitmap1);

	        	OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
	        	action.setProperty("Run", rdnr);
	        	action.setType("road_runner:Run");

	        	FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, action, "Run")
//	        	.setImageAttachmentsForAction(images, true)
	        	    .build();
	        	uiHelper.trackPendingDialogCall(shareDialog.present());
	        } else {
	            pendingAction = PendingAction.POST_PHOTO;
	        }
	    }
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

	private void initFragment() {
		// Set data in page fragment
		// if(finishMyListViewFragment == null){
		// finishMyListViewFragment = new FinishMyListViewFragment();
		// }else{
		// // finishMyListViewFragment.set
		// }
		// FinishMyListViewFragment finishMylistViewFragment =
		// (FinishMyListViewFragment) mTabsAdapter
		// .getItem(0);
		// finishMylistViewFragment.setDuration("30.00");
		// finishMylistViewFragment.setName("Nattakorn_king");
		// finishMylistViewFragment.setAvgKph("15.00");
		// finishMylistViewFragment.setCalories("450");
		// finishMylistViewFragment.setPlace("2nd");
	}

	private void initialisePaging() {

		// List<Fragment> fragments = new Vector<Fragment>();
		// fragments.add(Fragment.instantiate(this, List_View.class.getName()));
		// fragments.add(Fragment.instantiate(this, List_View.class.getName()));
		// fragments.add(Fragment.instantiate(this,
		// FinishMapFragment.class.getName()));
		// this.mPagerAdapter = new
		// ViewPagerAdapter(super.getSupportFragmentManager(), fragments);
		//
		ViewPager pager = (ViewPager) super.findViewById(R.id.finishPager);
		// pager = new ViewPager(this);
		// pager.setId(R.id.finishPager);
		ActionBar bar = getActionBar();
		// setContentView(pager);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setTitle("Roadrunner");
		bar.setSubtitle("Race Result");
		if (trackMemberList == null) {
			trackMemberList = new ArrayList<TrackMemberList>();
			TrackMemberList tt = new TrackMemberList();
			tt.setRank(2);
			trackMemberList.add(tt);
		}
		for (int i = 0; i < trackMemberList.size()	; i++) {
			if(trackMemberList.get(i).getfName().equals(RoadRunnerSetting.getFacebookName())){
				myTrack = trackMemberList.get(i);
			}
		}
		mTabsAdapter = new TabsAdapter(this, pager, myTrack);
		// mTabsAdapter.addTab(bar.newTab().setText("List Fragment 1"),
		// List_View.class, null);
		mTabsAdapter.addTab(bar.newTab().setText("Result"),
				FinishMyListViewFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setText("Maps"),
				FinishMapFragment.class, null);
		// pager.setAdapter(this.mPagerAdapter);
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
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.save_finish_menu_btn).setVisible(true);
		return super.onPrepareOptionsMenu(menu);
	}

	private void submitRaceResult() {
		System.out.println("Race result submited");
//		
//		OpenGraphAction action = GraphObject.Factory
//				.create(OpenGraphAction.class);
//		action.setProperty("Roadrunner",
//				"https://www.facebook.com/roadrunner5313180");
//
//		Bitmap bitmap = RoadRunnerFacebookSetting.getMapScreen();
//		System.out.println(bitmap.getHeight());
//		List<Bitmap> images = new ArrayList<Bitmap>();
//		images.add(bitmap);
//		 FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(
//				this, action, "Roadrunner")
//				.setImageAttachmentsForAction(images, true).build();
//				uiHelper.trackPendingDialogCall(shareDialog.present());
//				
				
//				OpenGraphObject recipe = OpenGraphObject.Factory.createForPost("running-app:Roadrunner");
//				recipe.setProperty("title", "Roadrunner");
//				recipe.setProperty("description", "Mahidol Track");
//
//				Bitmap bitmap1 = RoadRunnerFacebookSetting.getMapScreen();
//				List<Bitmap> images = new ArrayList<Bitmap>();
//				images.add(bitmap1);
//
//				OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
//				action.setProperty("recipe", recipe);
//
//				FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, action,"Roadrunner")
//				    .setImageAttachmentsForObject("running", images, true)
//				    .build();
//				uiHelper.trackPendingDialogCall(shareDialog.present());
		uploadFile();
		updateDataBase();
		onClickPostStatusUpdate(); //Post to facebook
//		onClickPostPhoto();
		// initFragment();
	}

	private void updateDataBase() {
		// Update DataBase Server when finish racing

			ConnectServer connectServer = new ConnectServer(this, MapsActivity.URLServer
					+ "/connect_server.php");
			connectServer.addValue("Fid", myTrack.getfId());
			connectServer.addValue("Rid", myTrack.getrId());
			connectServer.addValue("Trackerdir", myTrack.getTrackerDir());
			connectServer.addValue("Rank", myTrack.getRank()+"");
			connectServer.addValue("Time", myTrack.getDuration()+"");
			connectServer.addValue("fName", myTrack.getfName());
			connectServer.execute();
		
	}

	public void onItemClick(int mPosition) {
		// TODO Auto-generated method stub

	}
	private void uploadFile() {
		UploadTask uploadTask = new UploadTask(this);
		String exString[] = {MapsActivity.savePath,RaceTrackSelectorActivity.mapcapPath};
		uploadTask.execute(exString);
	}

	public void setDataBaseServerResponse(String result) {
		// DataBase response result
		Log.d("Response DataBase : ", result);
		Toast.makeText(this, "Database : "+result, 500);
		
	}
}
