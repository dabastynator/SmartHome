package de.neo.remote.mobile.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;

public class SimpleTask extends AsyncTask<String, Integer, Exception> {

	private AbstractConnectionActivity mActivity;
	private String mSuccess;
	private BackgroundAction mAction;
	private String mMessage;
	private String mMessageTitle;

	public SimpleTask(AbstractConnectionActivity activity) {
		mActivity = activity;
	}

	public SimpleTask setAction(BackgroundAction action) {
		mAction = action;
		return this;
	}

	public SimpleTask setSuccess(String success) {
		mSuccess = success;
		return this;
	}

	public SimpleTask setDialogMessage(String message) {
		mMessage = message;
		return this;
	}

	public SimpleTask setDialogtitle(String title) {
		mMessageTitle = title;
		return this;
	}

	@Override
	protected void onPreExecute() {
		if (mMessage != null && mActivity.isActive()) {
			ProgressDialog progress = mActivity.createProgress();
			progress.setTitle(mMessageTitle);
			progress.setMessage(mMessage);
			progress.show();
		}
	}

	public void execute() {
		execute("");
	}

	@Override
	protected Exception doInBackground(String... params) {
		try {
			mAction.run();
		} catch (Exception e) {
			return e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Exception result) {
		mActivity.dismissProgress();
		if (result != null && mActivity.isActive()) {
			new AbstractTask.ErrorDialog(mActivity, result).show();
		} else if (result == null && mActivity.isActive() && mSuccess != null
				&& mSuccess.length() > 0) {
			Toast.makeText(mActivity, mSuccess, Toast.LENGTH_SHORT).show();
		}
	}

	public static interface BackgroundAction {
		public void run() throws Exception;
	}

}
