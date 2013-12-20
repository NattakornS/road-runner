package com.senior.roadrunner.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class DialogConnect extends ProgressDialog{
	
	@SuppressWarnings("rawtypes")
	private AsyncTask task;
	
	@SuppressWarnings("rawtypes")
	public DialogConnect(Context context,AsyncTask task) {
		//สร�?า�? Dialog �?า�? super class
		super(context);
		
		//เ�?ื�?อม AsyncTask (ConnectServer) เ�?�?า�?ั�? Dialog
		this.task = task;
	}

	//ถ�?ามี�?ารย�?เลิ�?ตอ�?ที�? Dialog �?ึ�?�?มา�?ะมาทำ�?า�? Function �?ี�?
	public void cancel() {
		//ย�?เลิ�?เ�?ื�?อมต�?อ�?ั�? Server
		task.cancel(true);
		super.cancel();
	}
	
}
