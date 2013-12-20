package com.senior.roadrunner;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

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

	@SuppressWarnings("unused")
	private FragmentManager fragmentManager;

	private static final String USER_SKIPPED_LOGIN_KEY = "user_skipped_login";

	private static final int SPLASH = 0;
	private static final int SETTING = 1;
	private static final int USERSETTINGS = 2;
	private static final int FRAGMENT_COUNT = USERSETTINGS + 1;

	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	private MenuItem settings;
	private boolean isResumed = false;
	private boolean userSkippedLogin = false;
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState != null) {
			userSkippedLogin = savedInstanceState
					.getBoolean(USER_SKIPPED_LOGIN_KEY);
		}
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
			FragmentManager fm = getSupportFragmentManager();
			SplashFragment splashFragment = (SplashFragment) fm
					.findFragmentById(R.id.splashFragment);
			fragments[SPLASH] = splashFragment;
			fragments[SETTING] = fm.findFragmentById(R.id.settingFragment);
			fragments[USERSETTINGS] = fm
					.findFragmentById(R.id.userSettingsFragment);

			FragmentTransaction transaction = fm.beginTransaction();
			for (int i = 0; i < fragments.length; i++) {
				transaction.hide(fragments[i]);
			}
			transaction.commit();

			splashFragment
					.setSkipLoginCallback(new SplashFragment.SkipLoginCallback() {
						@Override
						public void onSkipLoginPressed() {
							userSkippedLogin = true;
							showFragment(SETTING, false);
						}
					});
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

	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
		isResumed = true;

		// Call the 'activateApp' method to log an app event for use in
		// analytics and advertising reporting. Do so in
		// the onResume methods of the primary Activities that an app may be
		// launched into.
		AppEventsLogger.activateApp(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
		isResumed = false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putBoolean(USER_SKIPPED_LOGIN_KEY, userSkippedLogin);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Session session = Session.getActiveSession();

		if (session != null && session.isOpened()) {
			// if the session is already open, try to show the selection
			// fragment
			showFragment(SETTING, false);
			userSkippedLogin = false;
		} else if (userSkippedLogin) {
			showFragment(SETTING, false);
		} else {
			// otherwise present the splash screen and ask the user to login,
			// unless the user explicitly skipped.
			showFragment(SPLASH, false);
		}
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (isResumed) {
			FragmentManager manager = getSupportFragmentManager();
			int backStackSize = manager.getBackStackEntryCount();
			for (int i = 0; i < backStackSize; i++) {
				manager.popBackStack();
			}
			// check for the OPENED state instead of session.isOpened() since
			// for the
			// OPENED_TOKEN_UPDATED state, the selection fragment should already
			// be showing.
			if (state.equals(SessionState.OPENED)) {
				showFragment(SETTING, false);
			} else if (state.isClosed()) {
				showFragment(SPLASH, false);
			}
		}
	}

	private void showFragment(int fragmentIndex, boolean addToBackStack) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			if (i == fragmentIndex) {
				transaction.show(fragments[i]);
			} else {
				transaction.hide(fragments[i]);
			}
		}
		if (addToBackStack) {
			transaction.addToBackStack(null);
		}
		transaction.commit();
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
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		// return super.onPrepareOptionsMenu(menu);
		// only add the menu when the selection fragment is showing
		if (fragments[SETTING].isVisible()) {
			if (menu.size() == 0) {
				settings = menu.add(R.string.settings);
			}
			return true;
		} else {
			menu.clear();
			settings = null;
		}
		return false;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (item.equals(settings)) {
			showSettingsFragment();
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

	public void showSettingsFragment() {
		showFragment(USERSETTINGS, true);
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
	public boolean isGpsEnable() {
		boolean isgpsenable = false;
		String provider = Settings.Secure.getString(this.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (provider.equals("network,gps")) { // GPS is Enabled
			isgpsenable = true;
		}
		return isgpsenable;
	}
	@SuppressLint("NewApi")
	private void selectItem(int position) {
		// update the main content by replacing fragments
		Fragment fragment = new PlanetFragment();
		// Fragment pf = new PickupFragment();
		// RaceTrackFragment raceTrackFragment = RaceTrackFragment
		// .createInstacnce();
		// SettingFragment settingFragment = SettingFragment.createInstacnce();

		Bundle args = new Bundle();
		args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		fragment.setArguments(args);

		// FragmentManager fragmentManager = getFragmentManager();
		fragmentManager = getSupportFragmentManager();
		if (position == 0) {
			// fragmentManager.beginTransaction().replace(R.id.content_frame,
			// pf)
			// .commit();
			// fragmentManager.beginTransaction()
			// .replace(R.id.content_frame, settingFragment).commit();
			// Intent i = new Intent(this, LoginActivity.class);
			// startActivity(i);
			// settingFragment();
			// fragmentManager
			// .beginTransaction()
			// .replace(R.id.content_frame,
			// FacebookFragment.createInstacnce()).commit();
			showFragment(USERSETTINGS, false);
		} else if (position == 1) {
			// fragmentManager.beginTransaction()
			// .replace(R.id.content_frame, raceTrackFragment).commit();
//			if (!isGpsEnable()) {
//				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						switch (which) {
//						case DialogInterface.BUTTON_POSITIVE:
//							Intent intent = new Intent(
//									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//							startActivity(intent);
//							break;
//						case DialogInterface.BUTTON_NEGATIVE:
//							// No button clicked
//							break;
//						}
//					}
//				};
//				AlertDialog.Builder builder = new AlertDialog.Builder(this);
//				builder.setMessage("Setting GPS?")
//						.setPositiveButton("Yes", dialogClickListener).show();
//			} else {
				Intent i = new Intent(this, RaceTrackSelectorActivity.class);
				// i.putExtra(name, value)
				startActivity(i);
//			}
			
		} else if (position == 2) {
		} else if (position == 3) {
			// Intent i = new Intent(this, FinishActivity.class);
			// startActivity(i);
			// fragmentManager
			// .beginTransaction()
			// .replace(R.id.content_frame,
			// SettingFragment.createInstacnce()).commit();
			showFragment(SETTING, false);

		} else if (position == 4) {
			exitApp();
		} else {
			// fragmentManager.beginTransaction()
			// .replace(R.id.content_frame, fragment).commit();
		}

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	// private void settingFragment() {
	// // Facebook Setting Frahment
	// userSettingsFragment = (UserSettingsFragment)
	// fragmentManager.findFragmentById(R.id.login_fragment);
	// userSettingsFragment.setSessionStatusCallback(new
	// Session.StatusCallback() {
	// @Override
	// public void call(Session session, SessionState state, Exception
	// exception) {
	// System.out.println(session.isOpened());
	// if(session.isOpened()){
	//
	// Request.newMeRequest(session, new Request.GraphUserCallback() {
	//
	// // callback after Graph API response with user object
	// @Override
	// public void onCompleted(GraphUser user, Response response) {
	// // System.out.println("USER :"+ user);
	// if (user != null) {
	// // lblEmail.setText(user.getName());
	// System.out.println(user.getName());
	// RoadRunnerSetting.setFacebookId(user.getId());
	// RoadRunnerSetting.setFacebookName(user.getName());
	// }
	// }
	// }).executeAsync();
	// // Request.newMyFriendsRequest(session, new GraphUserListCallback() {
	// //
	// // @Override
	// // public void onCompleted(List<GraphUser> users, Response response) {
	// // if(response.getError()==null)
	// // {
	// // for (int i = 0; i < users.size(); i++) {
	// //// System.out.println("users "+users.get(i).getName());
	// // Log.e("users", "users "+users.get(i).getName());
	// // }
	// // }
	// // else
	// // {
	// //// Toast.makeText(MainActivity.this,
	// response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
	// // }
	// // }
	// // }).executeAsync();
	// }
	// // Log.d("LoginUsingLoginFragmentActivity",
	// String.format("New session state: %s", state.toString()));
	// }
	// });
	// }

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
