package de.neo.remote.mobile.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.rmi.protokol.RemoteException;

public abstract class AbstractTask extends
		AsyncTask<String, Integer, Exception> {

	public enum TaskMode {
		DialogTask, ActionBarTask
	};

	protected AbstractConnectionActivity activity;
	protected boolean canceld = false;
	protected TaskMode mode;
	protected Exception exception;

	public AbstractTask(AbstractConnectionActivity activity, TaskMode mode) {
		this.activity = activity;
		this.mode = mode;
		canceld = false;
	}

	@Override
	protected void onPreExecute() {
		if (mode == TaskMode.DialogTask) {
			ProgressDialog progress = activity.createProgress();
			progress.setTitle(getDialogTitle());
			progress.setMessage(getDialogMsg());
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							canceld = true;
						}
					});
			progress.setCancelable(false);
			progress.show();
		}
		if (mode == TaskMode.ActionBarTask) {
			activity.setProgressBarVisibility(true);
			activity.setTitle(getDialogTitle());
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (mode == TaskMode.DialogTask) {
			int progress = values[0];
			ProgressDialog dialog = null;
			if (progress == 0) {
				activity.dismissProgress();
				dialog = activity.createProgress();
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setTitle(getDialogTitle());
				dialog.setMessage(getDialogMsg());
				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								canceld = true;
							}
						});
				dialog.setMax(getProgressMaximum());
				dialog.setCancelable(false);
				dialog.show();
			}
			if (dialog == null)
				dialog = activity.createProgress();
			dialog.setProgress(progress);
		}
	}

	@Override
	protected Exception doInBackground(String... params) {
		try {
			onExecute();
		} catch (Exception e) {
			exception = e;
			e.printStackTrace();
			return e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Exception result) {
		if (mode == TaskMode.DialogTask) {
			activity.dismissProgress();
		}
		if (mode == TaskMode.ActionBarTask) {
			activity.setProgressBarVisibility(false);
		}
		if (result == null && !canceld)
			activity.progressFinished(getResult());
		if (result != null) {
			result.printStackTrace();
			AlertDialog.Builder errorDialog = new AlertDialog.Builder(activity);
			if (result instanceof RemoteException)
				errorDialog.setTitle("Remote error");
			else
				errorDialog.setTitle(getDialogTitle());
			errorDialog.setMessage(result.getClass().getSimpleName() + ": "
					+ result.getMessage());
			errorDialog.setPositiveButton(
					activity.getResources().getString(android.R.string.ok),
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			if (!activity.isFinishing())
				errorDialog.show();
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

}
