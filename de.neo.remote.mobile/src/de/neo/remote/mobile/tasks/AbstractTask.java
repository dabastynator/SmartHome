package de.neo.remote.mobile.tasks;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;

public abstract class AbstractTask extends
		AsyncTask<String, Integer, Exception> {

	public enum TaskMode {
		DialogTask, ToastTask
	};

	protected AbstractConnectionActivity mActivity;
	protected boolean mCanceld = false;
	protected TaskMode mMode;
	protected Exception mException;

	public AbstractTask(AbstractConnectionActivity activity, TaskMode mode) {
		mActivity = activity;
		mMode = mode;
		mCanceld = false;
	}

	@Override
	protected void onPreExecute() {
		if (mMode == TaskMode.DialogTask) {
			ProgressDialog progress = mActivity.createProgress();
			progress.setTitle(getDialogTitle());
			progress.setMessage(getDialogMsg());
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setButton(DialogInterface.BUTTON_NEGATIVE,
					mActivity.getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							mCanceld = true;
						}
					});
			progress.setCancelable(false);
			progress.show();
		}
		if (mMode == TaskMode.ToastTask) {
			Toast.makeText(mActivity, getDialogTitle(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (mMode == TaskMode.DialogTask) {
			int progress = values[0];
			ProgressDialog dialog = null;
			if (progress == 0) {
				mActivity.dismissProgress();
				dialog = mActivity.createProgress();
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setTitle(getDialogTitle());
				dialog.setMessage(getDialogMsg());
				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								mCanceld = true;
							}
						});
				dialog.setMax(getProgressMaximum());
				dialog.setCancelable(false);
				dialog.show();
			}
			if (dialog == null)
				dialog = mActivity.createProgress();
			dialog.setProgress(progress);
		}
	}

	@Override
	protected Exception doInBackground(String... params) {
		try {
			onExecute();
		} catch (Exception e) {
			mException = e;
			e.printStackTrace();
			return e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Exception result) {
		if (mMode == TaskMode.DialogTask) {
			mActivity.dismissProgress();
		}
		if (result == null && !mCanceld)
			mActivity.progressFinished(getResult());
		if (result != null) {
			if (!mActivity.isFinishing())
				new ErrorDialog(mActivity, result).show();
		}
	}

	public void execute() {
		execute(new String[] {});
	}

	protected abstract String getDialogTitle();

	protected String getDialogMsg() {
		return "";
	};

	protected abstract void onExecute() throws Exception;

	protected int getProgressMaximum() {
		return 0;
	}

	protected Object getResult() {
		return null;
	}

	public static class ErrorDialog {
		private Exception mError;
		private Context mContext;

		public ErrorDialog(Context context, Exception e) {
			mError = e;
			mContext = context;
		}

		public void show() {
			Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(mError.getClass().getSimpleName());
			if (mError.getMessage() == null
					|| mError.getMessage().length() == 0) {
				StringWriter errors = new StringWriter();
				mError.printStackTrace(new PrintWriter(errors));
				builder.setMessage(errors.toString());
			} else {
				builder.setMessage(mError.getMessage());
			}
			builder.create().show();
		}
	}

}
