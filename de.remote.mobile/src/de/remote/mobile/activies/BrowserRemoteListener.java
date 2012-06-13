package de.remote.mobile.activies;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;
import de.remote.mobile.R;
import de.remote.mobile.services.PlayerBinder;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;

public class BrowserRemoteListener implements IRemoteActionListener {

	private View downloadView;
	private ProgressBar progress;
	private ImageView playButton;
	private PlayerBinder binder;

	public BrowserRemoteListener(View downloadView, ProgressBar progress, ImageView playButton, PlayerBinder binder) {
		this.downloadView = downloadView;
		this.progress = progress;
		this.playButton = playButton;
		this.binder = binder;
	}
	
	private long max = 0;

	@Override
	public void newPlayingFile(PlayingBean bean) {
		if (bean == null || bean.getState() == STATE.PLAY)
			playButton.setImageResource(R.drawable.pause);
		else if (bean.getState() == STATE.PAUSE)
			playButton.setImageResource(R.drawable.play);
	}

	@Override
	public void serverConnectionChanged(String serverName) {

	}

	@Override
	public void startReceive(long size) {
		max = size;
		downloadView.setVisibility(View.VISIBLE);
		progress.setProgress(0);
	}

	@Override
	public void progressReceive(long size) {
		if (max == 0)
			max = binder.getReceiver().getFullSize();
		progress.setProgress((int) ((100d * size) / max));
	}

	@Override
	public void endReceive(long size) {
		downloadView.setVisibility(View.GONE);
	}

	@Override
	public void exceptionOccurred(Exception e) {
		downloadView.setVisibility(View.GONE);
	}

	@Override
	public void downloadCanceled() {
		downloadView.setVisibility(View.GONE);
	}

}