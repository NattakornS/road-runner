package com.senior.roadrunner;

import java.util.Arrays;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;


public class SettingFragment extends Fragment {
	private static SettingFragment fragment;
	private String TAG = "SettingActivity";
	private View rootView;
	private TextView lblEmail;

	public static SettingFragment createInstacnce(){
		if (fragment == null)
			return fragment = new SettingFragment();
		else {
			return fragment;
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 rootView = inflater.inflate(R.layout.setting, container, false);
		  lblEmail = (TextView) rootView.findViewById(R.id.lblEmail);
		  
		  LoginButton authButton = (LoginButton) rootView.findViewById(R.id.authButton);
		  authButton.setOnErrorListener(new OnErrorListener() {
		   
		   @Override
		   public void onError(FacebookException error) {
		    Log.i(TAG, "Error " + error.getMessage());
		   }
		  });
		  // set permission list, Don't foeget to add email
		  authButton.setReadPermissions(Arrays.asList("basic_info","email"));
//		  authButton.setFragment(rootView);
		  // session state call back event
		  authButton.setSessionStatusCallback(new Session.StatusCallback() {
		   
		   @Override
		   public void call(Session session, SessionState state, Exception exception) {
			   System.out.println("Session : "+ session.isOpened());
			   Request.newMeRequest(session, new Request.GraphUserCallback() {

				   // callback after Graph API response with user object
				   @Override
				   public void onCompleted(GraphUser user, Response response) {
					   System.out.println("USER :"+ user);
				     if (user != null) {
//				    	 lblEmail.setText(user.getName());
				    	 System.out.println(user.getName());
				     }
				   }
				 }).executeAsync();
//		    if (session.isOpened()) {
//		              Log.i(TAG,"Access Token"+ session.getAccessToken());
//		              Request.executeMeRequestAsync(session,
//		                      new Request.GraphUserCallback() {
//		                          @Override
//		                          public void onCompleted(GraphUser user,Response response) {
//		                              if (user != null) { 
//		                               Log.i(TAG,"User ID "+ user.getId());
//		                               Log.i(TAG,"Email "+ user.asMap().get("email"));
//		                               lblEmail.setText(user.asMap().get("email").toString());
//		                              }
//		                          }
//		                      });
//		          }
//		    
		   }
		  });
		 
		return rootView;
	}
	 @Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	     Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
	 }
}
