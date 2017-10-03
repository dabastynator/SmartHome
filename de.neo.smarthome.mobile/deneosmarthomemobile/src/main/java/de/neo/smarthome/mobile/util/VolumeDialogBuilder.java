package de.neo.smarthome.mobile.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.mobile.api.PlayerException;
import de.neo.smarthome.mobile.api.PlayingBean;
import de.neo.smarthome.mobile.persistence.MediaServerState;
import de.neo.smarthome.mobile.tasks.AbstractTask;
import de.remote.mobile.R;

public class VolumeDialogBuilder implements OnSeekBarChangeListener, OnKeyListener {

	public static int ShowDuration = 1000 * 6;

	private Builder mBuilder;
	private SeekBar mVolume;
	private MediaServerState mMediaServer;
	private int mVolumeValue;
	private Exception mError;
	private Handler mHandler;
	private Context mContext;
	private int mChangeVolume = 0;
	private AlertDialog mDialog;
	private int mShowDuration;

	public VolumeDialogBuilder(Context context, MediaServerState mediaserver) {
		mHandler = new Handler();
		mContext = context;
		mBuilder = new AlertDialog.Builder(context);
		mMediaServer = mediaserver;
		LayoutInflater inflater = LayoutInflater.from(context);
		View dialogView = inflater.inflate(R.layout.volume, null);
		mBuilder.setView(dialogView);
		mBuilder.setTitle(context.getResources().getString(R.string.volume));
		mVolume = (SeekBar) dialogView.findViewById(R.id.volume_value);
		mVolume.setMax(100);
		mVolume.setOnSeekBarChangeListener(this);
		mShowDuration = ShowDuration;
		new Thread() {
			public void run() {
				getVolumeValue();
			}
		}.start();
	}

	private void getVolumeValue() {
		try {
			mVolumeValue = -1;
			mError = null;
			PlayingBean result = null;
			if (mChangeVolume < 0)
				result = mMediaServer.volDown();
			if (mChangeVolume > 0)
				result = mMediaServer.volUp();
			if (result == null)
				result = mMediaServer.getPlaying();
			if (result != null)
				mVolumeValue = result.getVolume();
			mChangeVolume = 0;
		} catch (RemoteException | PlayerException e) {
			mError = e;
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				setVolumeValue();
			}
		});
	};

	private void setVolumeValue() {
		if (mDialog != null && mDialog.isShowing()) {
			if (mError != null) {
				mDialog.dismiss();
				new AbstractTask.ErrorDialog(mContext, mError).show();
			} else {
				mVolume.setProgress(mVolumeValue);
			}
			mVolume.setOnSeekBarChangeListener(this);
		}
	}

	public AlertDialog show() {
		mDialog = mBuilder.create();
		mDialog.setOnKeyListener(this);
		mDialog.show();
		new Thread() {
			public void run() {
				countDownCloser();
			}
		}.start();
		return mDialog;
	}

	private void countDownCloser() {
		while (mShowDuration > 0) {
			mShowDuration -= 100;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (mDialog != null && mDialog.isShowing())
			mDialog.dismiss();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
		mShowDuration = ShowDuration;
		new Thread() {
			@Override
			public void run() {
				try {
					mMediaServer.setVolume(progress);
				} catch (RemoteException | PlayerException e) {
					// ignore
				}
			}
		}.start();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public void changeVolume(int i) {
		mChangeVolume = i;
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mVolume.setOnSeekBarChangeListener(null);
			mShowDuration = ShowDuration;
			changeVolume(-1);
			new Thread() {
				public void run() {
					getVolumeValue();
				}
			}.start();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			mVolume.setOnSeekBarChangeListener(null);
			mShowDuration = ShowDuration;
			changeVolume(1);
			new Thread() {
				public void run() {
					getVolumeValue();
				}
			}.start();
			return true;
		}
		return false;
	}

}
