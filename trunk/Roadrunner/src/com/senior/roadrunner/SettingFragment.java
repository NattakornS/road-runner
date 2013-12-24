package com.senior.roadrunner;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.senior.roadrunner.setting.RoadRunnerSetting;
import com.senior.roadrunner.tools.RoadrunnerTools;

public class SettingFragment extends Fragment implements OnClickListener {
	private static final String WEIGHT = "weight";
	private static final String LANGUAGE = "language";
	private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/profile?access_token=";
	private static Fragment fragment;
	// private String TAG = "SettingActivity";
	private View view;
	// private TextView lblFname;
	private TextView facebookName;
	private Button buttonLoginLogout;
	private StatusCallback statusCallback = new SessionStatusCallback();
	private ProfilePictureView profilePictureView;

	public static Fragment createInstacnce() {
		if (fragment == null)
			return fragment = new SettingFragment();
		else {
			return fragment;
		}
	}

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {
			onSessionStateChange(session, state, exception);
			updateView();
		}
	};
	private RoadRunnerSetting roadRunnerSetting;
	private TextView weight_txt;
	private TextView language_txt;
	private View aboutLayout;
	private TextView weightTxtView;
	private View weightLayout;
	private EditText nameInput;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// activity = (MainActivity) getActivity();
		uiHelper = new UiLifecycleHelper(getActivity(), sessionCallback);
		uiHelper.onCreate(savedInstanceState);
		roadRunnerSetting = RoadRunnerSetting.getInstance();

	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.setting_fragment, container, false);
		buttonLoginLogout = (Button) view.findViewById(R.id.loginBtn);
		facebookName = (TextView) view.findViewById(R.id.facebookNameTxt);
		profilePictureView = (ProfilePictureView) view
				.findViewById(R.id.profile_pic);
		aboutLayout=(View)view.findViewById(R.id.aboutLayout);
		aboutLayout.setOnClickListener(this);
		aboutLayout.setClickable(true);
		weightLayout = (View)view.findViewById(R.id.weightLayout);
		weightLayout.setOnClickListener(this);
		weightTxtView = (TextView)view.findViewById(R.id.weightTxt);
		language_txt = (TextView) view.findViewById(R.id.languageTxt);
		weight_txt = (TextView) view.findViewById(R.id.weightTxt);

		readSetting();

		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(getActivity(), null,
						statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(getActivity());
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback));
			}
		}

		updateView();
		return view;
	}

	private void readSetting() {
		String str = RoadrunnerTools
				.readStringFromFile(RoadRunnerSetting.SDPATH + "setting.ini");
		if (str == null || str == "") {
			RoadrunnerTools.writeStringToFile(RoadRunnerSetting.SDPATH
					+ "setting.ini", "");
		} else {
			try {
				JSONObject settingjsonObject = new JSONObject(str);

				weight_txt.setText(settingjsonObject.getString(WEIGHT));
				language_txt.setText(settingjsonObject.getString(LANGUAGE));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
		readSetting();
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
		saveSetting();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(getActivity(), requestCode,
				resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
		saveSetting();
	}

	// save setting to file
	private void saveSetting() {
		JSONObject settingJsonObject = new JSONObject();
		try {
			settingJsonObject.put(LANGUAGE, language_txt.getText());
			settingJsonObject.put(WEIGHT, weight_txt.getText());
			String jsonString = settingJsonObject.toString();
			RoadrunnerTools.writeStringToFile(RoadRunnerSetting.SDPATH
					+ "setting.ini", jsonString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println("Save setting");
	}

	private void updateView() {
		final Session session = Session.getActiveSession();
		if (session.isOpened()) {
			// set data when activity exit
			profilePictureView.setProfileId(roadRunnerSetting.getFacebookId());
			roadRunnerSetting.setProfileImg(profilePictureView.getDrawingCache());
			facebookName.setText(roadRunnerSetting.getFacebookName());

			buttonLoginLogout.setText(R.string.logout);
			buttonLoginLogout.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					onClickLogout();
				}
			});
		} else {
			facebookName.setText("name");
			buttonLoginLogout.setText(R.string.login);
			buttonLoginLogout.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					onClickLogin();
				}
			});
		}
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(statusCallback));
		} else {
			Session.openActiveSession(getActivity(), this, true, statusCallback);
			Request.newMeRequest(session, new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (user != null) {
						facebookName.setText(user.getName());
						roadRunnerSetting.setFacebookId(user.getId());
						roadRunnerSetting.setFacebookName(user.getName());
						roadRunnerSetting.setCity(user.getLocation().getCity());
					}
				}
			});
		}
	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			updateView();
		}
	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
				// tokenUpdated();
			} else {
				makeMeRequest(session);
			}
		} else {
			profilePictureView.setProfileId(null);
			facebookName.setText("");
		}
	}

	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (session == Session.getActiveSession()) {
							if (user != null) {
								profilePictureView.setProfileId(user.getId());
								roadRunnerSetting.setProfileImg(profilePictureView.getDrawingCache());
								facebookName.setText(user.getName());
								System.out.println("name : " + user.getName());
								roadRunnerSetting.setFacebookId(user.getId());
								roadRunnerSetting.setFacebookName(user
										.getName());
//								roadRunnerSetting.setCity(user.getLocation()
//										.getCity());

							}
						}
						if (response.getError() != null) {
							// handleError(response.getError());
						}
					}
				});
		request.executeAsync();

	}

	@Override
	public void onClick(View v) {
		if(v.equals(aboutLayout)){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Contact : Nattakorn Sanpabopit\nE-mail : nattakor.kingman@gmail.com")
					.show();
		}else if(v.equals(weightLayout)){
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						weight_txt.setText(nameInput.getText());
						break;
					}
				}
			};
			nameInput = new EditText(getActivity());
			nameInput.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					getActivity());
			builder.setMessage("Weight in KG")
					.setPositiveButton("OK", dialogClickListener)
					.setView(nameInput).show();
		}
		
	}
}
