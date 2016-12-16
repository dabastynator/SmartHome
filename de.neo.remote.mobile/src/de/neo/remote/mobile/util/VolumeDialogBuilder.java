package de.neo.remote.mobile.util;

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
import de.neo.remote.api.PlayerException;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.persistence.MediaServerState;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

public class VolumeDialogBuilder implements OnSeekBarChangeListener, OnKeyListener {

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
		mShowDuration = 1000 * 3;
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
		if (mError != null) {
			if (mDialog != null && mDialog.isShowing())
				mDialog.dismiss();
			new AbstractTask.ErrorDialog(mContext, mError).show();
		} else {
			mVolume.setProgress(mVolumeValue);
		}
		mVolume.setOnSeekBarChangeListener(this);
	}

	public void show() {
		mDialog = mBuilder.create();
		mDialog.setOnKeyListener(this);
		mDialog.show();
		new Thread() {
			public void run() {
				countDownCloser();
			}
		}.start();
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
		mShowDuration = 1000 * 4;
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
			mShowDuration = 1000 * 3;
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
			mShowDuration = 1000 * 3;
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
