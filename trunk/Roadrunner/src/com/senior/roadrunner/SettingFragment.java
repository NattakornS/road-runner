package com.senior.roadrunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class SettingFragment extends Fragment {
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
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
	private RoadRunnerSetting roadRunnerSetting;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        activity = (MainActivity) getActivity();
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
		profilePictureView = (ProfilePictureView) view.findViewById(R.id.profile_pic);
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

		// LoginButton authButton = (LoginButton)
		// rootView.findViewById(R.id.loginBtn);
		// authButton.setOnErrorListener(new OnErrorListener() {
		//
		// @Override
		// public void onError(FacebookException error) {
		// Log.i(TAG, "Error " + error.getMessage());
		// }
		// });
		// // set permission list, Don't foeget to add email
		// authButton.setReadPermissions(Arrays.asList("basic_info","name"));
		// // authButton.setFragment(rootView);
		// // session state call back event
		// authButton.setSessionStatusCallback(new Session.StatusCallback() {
		//
		// @Override
		// public void call(Session session, SessionState state, Exception
		// exception) {
		// System.out.println("Session : "+ session.isOpened());
		// Request.newMeRequest(session, new Request.GraphUserCallback() {
		//
		// // callback after Graph API response with user object
		// @Override
		// public void onCompleted(GraphUser user, Response response) {
		// System.out.println("USER :"+ user);
		// if (user != null) {
		// // lblEmail.setText(user.getName());
		// System.out.println(user.getName());
		// }
		// }
		// }).executeAsync();
		// // if (session.isOpened()) {
		// // Log.i(TAG,"Access Token"+ session.getAccessToken());
		// // Request.executeMeRequestAsync(session,
		// // new Request.GraphUserCallback() {
		// // @Override
		// // public void onCompleted(GraphUser user,Response response) {
		// // if (user != null) {
		// // Log.i(TAG,"User ID "+ user.getId());
		// // Log.i(TAG,"Email "+ user.asMap().get("email"));
		// // lblEmail.setText(user.asMap().get("email").toString());
		// // }
		// // }
		// // });
		// // }
		// //
		// }
		// });
		//
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
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
	}

	private void updateView() {
		final Session session = Session.getActiveSession();
		if (session.isOpened()) {
			
//			facebookName.setText(URL_PREFIX_FRIENDS + session.getAccessToken());
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					// get json object from token string
//					try {
//						URL url = new URL(URL_PREFIX_FRIENDS+session.getAccessToken());
//						InputStream jsonIs = url.openConnection().getInputStream();
//						BufferedReader reader = new BufferedReader(new InputStreamReader(
//								jsonIs, "UTF-8"), 8);
//						StringBuilder sb = new StringBuilder();
//						String line = null;
//						while ((line = reader.readLine()) != null) {
//							sb.append(line + "\n");
//						}
//
//						jsonIs.close();
//						String jsonString = sb.toString();
//						JSONObject jsonObject = new JSONObject(jsonString);
//						String id =jsonObject.getString("id");
//						String name =jsonObject.getString("name");
//						RoadRunnerSetting.setFacebookId(id);
//						RoadRunnerSetting.setFacebookName(name);
//						facebookName.setText(name);
//						updateView();
//					} catch (MalformedURLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//				}
//			});
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
//						facebookName.setText(user.getName());
//						RoadRunnerSetting.setFacebookId(user.getId());
//						RoadRunnerSetting.setFacebookName(user.getName());
//						RoadRunnerSetting.setCity(user.getLocation().getCity());
//						System.out.println("id :"+user.getId()+"\nname : "+user.getName());
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
    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
//                tokenUpdated();
            } else { 
                makeMeRequest(session);
            }
        } else {
            profilePictureView.setProfileId(null);
            facebookName.setText("");
        }
    }
	  private void makeMeRequest(final Session session) {
	        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
	            @Override
	            public void onCompleted(GraphUser user, Response response) {
	                if (session == Session.getActiveSession()) {
	                    if (user != null) {
	                        profilePictureView.setProfileId(user.getId());
	                        facebookName.setText(user.getName());
	                        System.out.println("name : "+ user.getName());
	                        roadRunnerSetting.setFacebookId(user.getId());
	                        roadRunnerSetting.setFacebookName(user.getName());
	                        
//	                        RoadRunnerSetting.setFacebookId(user.getId());
//							RoadRunnerSetting.setFacebookName(user.getName());
//							RoadRunnerSetting.setCity(user.getLocation().getCity());
	                    }
	                }
	                if (response.getError() != null) {
//	                    handleError(response.getError());
	                }
	            }
	        });
	        request.executeAsync();

	    }
}
