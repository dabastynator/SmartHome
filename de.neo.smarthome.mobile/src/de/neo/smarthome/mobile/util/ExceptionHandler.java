package de.neo.smarthome.mobile.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	private Context mContext;

	public ExceptionHandler(Context context) {
		mContext = context;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("error", ex.toString());

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { "pichlmeier.sebastian@gmail.com" });
		i.putExtra(Intent.EXTRA_SUBJECT, "Crash in RemoteControl");
		i.putExtra(Intent.EXTRA_TEXT, createTextForCrash(thread, ex));
		try {
			mContext.startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException e) {
			Toast.makeText(mContext, "Can't send crash-log email, there are no email clients installed.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private String createTextForCrash(Thread thread, Throwable ex) {
		StringWriter errors = new StringWriter();

		errors.append("There was a crash in the RemoteControl app.\n\n");

		errors.append("Error type:\n");
		errors.append(ex.getClass().getSimpleName() + "\n\n");

		errors.append("Error message:\n");
		errors.append(ex.getMessage() + "\n\n");

		errors.append("StackTrace:\n");
		ex.printStackTrace(new PrintWriter(errors));

		errors.append("\nCause:\n");
		if (ex.getCause() != null) {
			errors.append("Error type:\n");
			errors.append(ex.getCause().getClass().getSimpleName() + "\n\n");

			errors.append("Error message:\n");
			errors.append(ex.getCause().getMessage() + "\n\n");

			errors.append("StackTrace:\n");
			ex.getCause().printStackTrace(new PrintWriter(errors));
		} else {
			errors.append("-no cause-\n");
		}
		return errors.toString();
	}

}
