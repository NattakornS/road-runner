package com.senior.roadrunner;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.ProfilePictureView;
import com.facebook.widget.UserSettingsFragment;
import com.senior.roadrunner.finish.FinishActivity;
import com.senior.roadrunner.setting.RoadRunnerFacebookSetting;


@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mPlanetTitles;
	private long lastPressedTime;
	private static final int PERIOD = 2000;
	
	private UserSettingsFragment userSettingsFragment;
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
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("HelloFacebook", "Success!");
        }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//set Uihelper to shared content
		uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
		
		// Navigation Drawer
		mTitle = mDrawerTitle = getTitle();
		mPlanetTitles = getResources()
				.getStringArray(R.array.menu_drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mPlanetTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		

		
		
		if (savedInstanceState == null) {
			selectItem(0);
		}

		//
		// Button btn_maps = (Button)findViewById(R.id.btn_maps);
		// btn_maps.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// startActivity(new Intent(MainActivity.this,MapsPage.class));
		//
		// }
		// });
		
        canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
		//On click post status
		
	}
	
	 @Override
	    protected void onResume() {
	        super.onResume();
	        uiHelper.onResume();

	        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
	        // the onResume methods of the primary Activities that an app may be launched into.
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
	    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	        if (pendingAction != PendingAction.NONE &&
	                (exception instanceof FacebookOperationCanceledException ||
	                exception instanceof FacebookAuthorizationException)) {
	                new AlertDialog.Builder(MainActivity.this)
	                    .setTitle(R.string.cancelled)
	                    .setMessage(R.string.permission_not_granted)
	                    .setPositiveButton(R.string.ok, null)
	                    .show();
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
	            case POST_STATUS_UPDATE:
	                postStatusUpdate();
	                break;
	        }
	    }
	    private interface GraphObjectWithId extends GraphObject {
	        String getId();
	    }
	    private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
	        String title = null;
	        String alertMessage = null;
	        if (error == null) {
	            title = getString(R.string.success);
	            String id = result.cast(GraphObjectWithId.class).getId();
	            alertMessage = getString(R.string.successfully_posted_post, message, id);
	        } else {
	            title = getString(R.string.error);
	            alertMessage = error.getErrorMessage();
	        }

	        new AlertDialog.Builder(this)
	                .setTitle(title)
	                .setMessage(alertMessage)
	                .setPositiveButton(R.string.ok, null)
	                .show();
	    }

	    private void onClickPostStatusUpdate() {
	        performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
	    }
	    
	    private FacebookDialog.ShareDialogBuilder createShareDialogBuilder() {
	        return new FacebookDialog.ShareDialogBuilder(this)
	                .setName("Roadrunner")
	                .setDescription("The 'Road Runner' Running and race application")
	                .setLink("https://www.facebook.com/roadrunner5313180");
	    }

	    private void postStatusUpdate() {
	        if (canPresentShareDialog) {
	            FacebookDialog shareDialog = createShareDialogBuilder().build();
	            uiHelper.trackPendingDialogCall(shareDialog.present());
	        } else if (user != null && hasPublishPermission()) {
	            final String message = getString(R.string.status_update, user.getFirstName(), (new Date().toString()));
	            Request request = Request
	                    .newStatusUpdateRequest(Session.getActiveSession(), message, place, tags, new Request.Callback() {
	                        @Override
	                        public void onCompleted(Response response) {
	                            showPublishResult(message, response.getGraphObject(), response.getError());
	                        }
	                    });
	            request.executeAsync();
	        } else {
	            pendingAction = PendingAction.POST_STATUS_UPDATE;
	        }
	    }
	    private void showAlert(String title, String message) {
	        new AlertDialog.Builder(this)
	                .setTitle(title)
	                .setMessage(message)
	                .setPositiveButton(R.string.ok, null)
	                .show();
	    }

	    private boolean hasPublishPermission() {
	        Session session = Session.getActiveSession();
	        return session != null && session.getPermissions().contains("publish_actions");
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
	                // We need to get new permissions, then complete the action when we get called back.
	                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSION));
	                return;
	            }
	        }

	        if (allowNoSession) {
	            pendingAction = action;
	            handlePendingAction();
	        }
	    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_websearch:
			// create intent to perform web search for this planet
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			// catch event that there's no activity to handle intent
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.app_not_available,
						Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	@SuppressLint("NewApi")
	private void selectItem(int position) {
		// update the main content by replacing fragments
		Fragment fragment = new PlanetFragment();
//		Fragment pf = new PickupFragment();
		// RaceTrackFragment raceTrackFragment = RaceTrackFragment
		// .createInstacnce();
//		SettingFragment settingFragment = SettingFragment.createInstacnce();

		Bundle args = new Bundle();
		args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		fragment.setArguments(args);

//		FragmentManager fragmentManager = getFragmentManager();
		
		if (position == 0) {
//			fragmentManager.beginTransaction().replace(R.id.content_frame, pf)
//					.commit();
//			 fragmentManager.beginTransaction()
//			 .replace(R.id.content_frame, settingFragment).commit();
//			Intent i = new Intent(this, LoginActivity.class);
//			startActivity(i);
			settingFragment();
		} else if (position == 1) {
			// fragmentManager.beginTransaction()
			// .replace(R.id.content_frame, raceTrackFragment).commit();
			
			Intent i = new Intent(this, RaceTrackSelectorActivity.class);
//			i.putExtra(name, value)
			startActivity(i);
		} else if (position == 2) {
			onClickPostStatusUpdate();
		}else if (position == 3) {
			Intent i = new Intent(this, FinishActivity.class);
			startActivity(i);
		}
		else if (position == 4) {
			exitApp();
		} else {
//			fragmentManager.beginTransaction()
//					.replace(R.id.content_frame, fragment).commit();
		}

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private void settingFragment() {
		// Facebook Setting Frahment

		 FragmentManager fragmentManager = getSupportFragmentManager();
        userSettingsFragment = (UserSettingsFragment) fragmentManager.findFragmentById(R.id.login_fragment);
        userSettingsFragment.setSessionStatusCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
            	System.out.println(session.isOpened());
            	if(session.isOpened()){

            		 Request.newMeRequest(session, new Request.GraphUserCallback() {

      				   // callback after Graph API response with user object
      				   @Override
      				   public void onCompleted(GraphUser user, Response response) {
//      					   System.out.println("USER :"+ user);
      				     if (user != null) {
//      				    	 lblEmail.setText(user.getName());
      				    	 System.out.println(user.getName());
      				    	 RoadRunnerFacebookSetting.setFacebookId(user.getId());
      				    	 RoadRunnerFacebookSetting.setFacebookName(user.getName());
      				     }
      				   }
      				 }).executeAsync();
//            		 Request.newMyFriendsRequest(session, new GraphUserListCallback() {
//
//            	            @Override
//            	            public void onCompleted(List<GraphUser> users, Response response) {
//            	                // TODO Auto-generated method stub
//            	                if(response.getError()==null)
//            	                {
//            	                    for (int i = 0; i < users.size(); i++) {
////            	                    	System.out.println("users "+users.get(i).getName());
//            	                        Log.e("users", "users "+users.get(i).getName());
//            	                    }
//            	                }
//            	                else
//            	                {
////            	                    Toast.makeText(MainActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
//            	                }
//            	            }
//            	        }).executeAsync();
            	}
//                Log.d("LoginUsingLoginFragmentActivity", String.format("New session state: %s", state.toString()));
            }
        });
	}


	private void exitApp() {
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();

	}

	@SuppressLint("NewApi")
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Fragment that appears in the "content_frame", shows a planet
	 */
	@SuppressLint("NewApi")
	public static class PlanetFragment extends Fragment {
		public static final String ARG_PLANET_NUMBER = "planet_number";

		public PlanetFragment() {
			// Empty constructor required for fragment subclasses
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_planet,
					container, false);
			int i = getArguments().getInt(ARG_PLANET_NUMBER);
			String planet = getResources().getStringArray(
					R.array.menu_drawer_array)[i];

			int imageId = getResources().getIdentifier(
					planet.toLowerCase(Locale.getDefault()), "drawable",
					getActivity().getPackageName());
			((ImageView) rootView.findViewById(R.id.image))
					.setImageResource(imageId);
			getActivity().setTitle(planet);
			return rootView;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			switch (event.getAction()) {
			case KeyEvent.ACTION_DOWN:
				if (event.getDownTime() - lastPressedTime < PERIOD) {
					exitApp();
				} else {
					Toast.makeText(getApplicationContext(),
							"Press again to exit.", Toast.LENGTH_SHORT).show();
					lastPressedTime = event.getEventTime();
				}
				return true;
			}
		}
		return false;
	}
}
