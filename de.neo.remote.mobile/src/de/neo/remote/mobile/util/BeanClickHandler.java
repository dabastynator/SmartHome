package de.neo.remote.mobile.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;
import colorpickerview.dialog.ColorPickerDialogFragment;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.WebAPIActivity;
import de.neo.smarthome.api.IControlCenter.BeanWeb;
import de.neo.smarthome.api.IWebAction.BeanAction;
import de.neo.smarthome.api.IWebLEDStrip.BeanLEDStrips;
import de.neo.smarthome.api.IWebLEDStrip.LEDMode;
import de.neo.smarthome.api.IWebMediaServer.BeanMediaServer;
import de.remote.mobile.R;

public class BeanClickHandler {

	private Handler mHandler;
	private WebAPIActivity mContext;

	public BeanClickHandler(WebAPIActivity context, Handler handler) {
		mHandler = handler;
		mContext = context;
	}

	private BeanWeb mBean;

	public BeanWeb getBean() {
		return mBean;
	}

	public void selectBean(BeanWeb bean) {
		mBean = bean;
		if (mBean instanceof BeanMediaServer) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(mContext, MediaServerActivity.class);
					intent.putExtra(MediaServerActivity.EXTRA_MEDIA_ID, mBean.getID());
					mContext.startActivity(intent);
				}
			});
		}
		if (mBean instanceof BeanAction) {
			AsyncTask<BeanWeb, Integer, Exception> task = new AsyncTask<BeanWeb, Integer, Exception>() {

				@Override
				protected Exception doInBackground(BeanWeb... params) {
					try {
						BeanAction action = (BeanAction) mBean;
						mContext.getWebAction().startAction(action.getID());
					} catch (Exception e) {
						return e;
					}
					return null;
				}

				@Override
				protected void onPostExecute(Exception result) {
					BeanAction action = (BeanAction) mBean;
					if (result != null)
						mContext.showException(result);
					else {
						Toast.makeText(mContext, "Execute: " + action.getDescription(), Toast.LENGTH_SHORT).show();
					}
					if (action.getClientAction() != null && action.getClientAction().length() > 0) {
						Uri uri = Uri.parse(action.getClientAction());
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						mContext.startActivity(intent);

					}
				}
			};
			task.execute(mBean);
		}

		if (mBean instanceof BeanLEDStrips) {
			BeanLEDStrips led = (BeanLEDStrips) mBean;
			ColorPickerDialogFragment dialog = ColorPickerDialogFragment.newInstance(led.getID().hashCode(),
					"Pick color for " + led.getName(), mContext.getString(android.R.string.ok),
					led.getColor() | 0xFF000000, false);
			dialog.show(mContext.getFragmentManager(), "colorpicker");
		}
	}

	public void selectLongClickBean(BeanWeb bean) {
		mBean = bean;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (mBean instanceof BeanAction) {
					new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.command_stop))
							.setMessage(mContext.getString(R.string.command_stop_long) + " " + mBean.getName() + "?")
							.setPositiveButton(mContext.getString(android.R.string.yes), new KillAction())
							.setNegativeButton(mContext.getString(android.R.string.no), null).create().show();
				}
				if (mBean instanceof BeanLEDStrips) {
					new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.led_change_mode))
							.setMessage(mContext.getString(R.string.led_change_mode) + " " + mBean.getName() + "?")
							.setPositiveButton(mContext.getString(R.string.led_mode_normal),
									new SetLEDMode(LEDMode.NormalMode))
							.setNegativeButton(mContext.getString(R.string.led_mode_party),
									new SetLEDMode(LEDMode.PartyMode))
							.create().show();
				}
			}
		});
	}

	class SetLEDMode implements OnClickListener {

		private LEDMode mMode;

		public SetLEDMode(LEDMode mode) {
			mMode = mode;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			AsyncTask<BeanWeb, Integer, Exception> task = new AsyncTask<BeanWeb, Integer, Exception>() {

				@Override
				protected Exception doInBackground(BeanWeb... params) {
					try {
						mContext.getWebLED().setMode(mBean.getID(), mMode);
					} catch (Exception e) {
						return e;
					}
					return null;
				}

				@Override
				protected void onPostExecute(Exception result) {
					if (result != null)
						mContext.showException(result);
					else {
						Toast.makeText(mContext, "Set mode: " + mMode, Toast.LENGTH_SHORT).show();
					}
				}
			};
			task.execute(mBean);
		}

	}

	class KillAction implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mBean instanceof BeanAction) {
				AsyncTask<BeanWeb, Integer, Exception> task = new AsyncTask<BeanWeb, Integer, Exception>() {

					@Override
					protected Exception doInBackground(BeanWeb... params) {
						try {
							BeanAction action = (BeanAction) mBean;
							mContext.getWebAction().stopAction(action.getID());
						} catch (Exception e) {
							return e;
						}
						return null;
					}

					@Override
					protected void onPostExecute(Exception result) {
						if (result != null)
							mContext.showException(result);
						else {
							Toast.makeText(mContext, "Stop: " + mBean.getDescription(), Toast.LENGTH_SHORT).show();
						}
					}
				};
				task.execute(mBean);
			}
		}
	}

}
