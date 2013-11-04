package com.senior.roadrunner.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class DialogUpload extends ProgressDialog {

	private AsyncTask task;

	public DialogUpload(Context context, AsyncTask task) {
		super(context);

		this.task = task;
	}

	public void cancel() {
		// à¸¢à¸?à¹€à¸¥à¸´à¸?à¹€à¸?à¸·à¹?à¸­à¸¡à¸•à¹?à¸­à¸?à¸±à¸? Server
		task.cancel(true);
		super.cancel();
	}
}
