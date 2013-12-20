package com.senior.roadrunner.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class DialogConnect extends ProgressDialog{
	
	@SuppressWarnings("rawtypes")
	private AsyncTask task;
	
	@SuppressWarnings("rawtypes")
	public DialogConnect(Context context,AsyncTask task) {
		//à¸ªà¸£à¹?à¸²à¸? Dialog à¸?à¸²à¸? super class
		super(context);
		
		//à¹€à¸?à¸·à¹?à¸­à¸¡ AsyncTask (ConnectServer) à¹€à¸?à¹?à¸²à¸?à¸±à¸? Dialog
		this.task = task;
	}

	//à¸–à¹?à¸²à¸¡à¸µà¸?à¸²à¸£à¸¢à¸?à¹€à¸¥à¸´à¸?à¸•à¸­à¸?à¸—à¸µà¹? Dialog à¸?à¸¶à¹?à¸?à¸¡à¸²à¸?à¸°à¸¡à¸²à¸—à¸³à¸?à¸²à¸? Function à¸?à¸µà¹?
	public void cancel() {
		//à¸¢à¸?à¹€à¸¥à¸´à¸?à¹€à¸?à¸·à¹?à¸­à¸¡à¸•à¹?à¸­à¸?à¸±à¸? Server
		task.cancel(true);
		super.cancel();
	}
	
}
