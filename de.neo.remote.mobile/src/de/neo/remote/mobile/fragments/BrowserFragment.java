package de.neo.remote.mobile.fragments;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.IThumbnailListener;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.util.BrowserPageAdapter;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.transceiver.AbstractReceiver;
import de.remote.mobile.R;

public class BrowserFragment extends Fragment implements IRemoteActionListener,
		IThumbnailListener {

	public static final String LISTVIEW_POSITION = "listviewPosition";
	public static final String SPINNER_POSITION = "spinnerPosition";
	public static final String VIEWER_STATE = "viewerstate";
	public static final String MEDIA_STATE = "mediastate";
	public static final String PLAYLIST = "playlist";

	private ViewPager mListPager;
	private LinearLayout mDownloadLayout;
	private ProgressBar mDownloadProgress;
	private TextView mDownloadText;
	private long mMaxDonwloadSize = 0;
	private BrowserPageAdapter mBrowserPageAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.mediaserver_browser, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		findComponents();
		// listView.setBackgroundResource(R.drawable.idefix_dark);
		// listView.setScrollingCacheEnabled(false);
		// listView.setCacheColorHint(0);
		registerForContextMenu(mListPager);
	}

	private void findComponents() {
		mListPager = (ViewPager) getActivity().findViewById(R.id.media_pager);
		AbstractConnectionActivity activity = (AbstractConnectionActivity) getActivity();
		if (activity.mBinder == null)
			mBrowserPageAdapter = new BrowserPageAdapter(getFragmentManager(),
					activity, null);
		else
			mBrowserPageAdapter = new BrowserPageAdapter(getFragmentManager(),
					activity, activity.mBinder.getLatestMediaServer());
		mListPager.setAdapter(mBrowserPageAdapter);
		mDownloadLayout = (LinearLayout) getActivity().findViewById(
				R.id.layout_download);
		mDownloadProgress = (ProgressBar) getActivity().findViewById(
				R.id.prg_donwload);
		mDownloadText = (TextView) getActivity()
				.findViewById(R.id.lbl_download);
	}

	public boolean onContextItemSelected(MenuItem item) {
		return mBrowserPageAdapter.onContextItemSelected(item,
				mListPager.getCurrentItem());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		final StationStuff mediaServer = activity.mBinder
				.getLatestMediaServer();
		if (data == null)
			return;
		if (requestCode == MediaServerActivity.FILE_REQUEST) {
			Uri uri = data.getData();
			activity.mBinder.uploadFile(mediaServer.browser,
					new File(activity.getFilePathByUri(uri)));
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Fragment fragment = mBrowserPageAdapter.getItem(mListPager
				.getCurrentItem());
		fragment.onCreateContextMenu(menu, v, menuInfo);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		mBrowserPageAdapter.onKeyDown(keyCode, event,
				mListPager.getCurrentItem());
		return false;
	}

	@Override
	public void startReceive(long size, String file) {
		mMaxDonwloadSize = size;
		mDownloadLayout.setVisibility(View.VISIBLE);
		mDownloadProgress.setProgress(0);
		if (file != null)
			mDownloadText.setText(file);
		else
			mDownloadText.setText(getActivity().getResources().getString(
					R.string.str_download));
	}

	@Override
	public void startSending(long size) {
		mMaxDonwloadSize = size;
		mDownloadLayout.setVisibility(View.VISIBLE);
		mDownloadProgress.setProgress(0);
		mDownloadText.setText(getActivity().getResources().getString(
				R.string.str_upload));
	}

	@Override
	public void progressReceive(long size, String file) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		mDownloadLayout.setVisibility(View.VISIBLE);
		if (mMaxDonwloadSize == 0)
			mMaxDonwloadSize = activity.mBinder.getReceiver().getFullSize();
		mDownloadProgress.setProgress((int) ((100d * size) / mMaxDonwloadSize));
		if (file != null)
			mDownloadText.setText(file);
	}

	@Override
	public void progressSending(long size) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		mDownloadLayout.setVisibility(View.VISIBLE);
		if (mMaxDonwloadSize == 0)
			mMaxDonwloadSize = activity.mBinder.getReceiver().getFullSize();
		mDownloadProgress.setProgress((int) ((100d * size) / mMaxDonwloadSize));
	}

	@Override
	public void endReceive(long size) {
		mDownloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void endSending(long size) {
		mDownloadLayout.setVisibility(View.GONE);
	}

	public void cancelDownload(View v) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		AbstractReceiver receiver = activity.mBinder.getReceiver();
		if (receiver != null) {
			receiver.cancel();
		} else {
			Toast.makeText(activity, "no receiver available",
					Toast.LENGTH_SHORT).show();
			mDownloadLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void sendingCanceled() {
	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		mBrowserPageAdapter.onPlayingBeanChanged(mediaserver, bean);
	}

	@Override
	public void onServerConnectionChanged(RemoteServer server) {
	}

	@Override
	public void onStopService() {
	}

	@Override
	public void onPowerSwitchChange(String _switch, State state) {
	}

	@Override
	public void exceptionOccurred(Exception e) {
		mDownloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void downloadCanceled() {
		mDownloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void setThumbnail(String file, int width, int height, int[] thumbnail)
			throws RemoteException {
		mBrowserPageAdapter.setThumbnail(file, width, height, thumbnail);
	}

	public void showView(ViewerState state) {
		if (state == ViewerState.DIRECTORIES)
			mListPager.setCurrentItem(0);
		if (state == ViewerState.PLAYLISTS)
			mListPager.setCurrentItem(1);
	}

	public void setStation(StationStuff station) {
		AbstractConnectionActivity activity = (AbstractConnectionActivity) getActivity();
		mBrowserPageAdapter = new BrowserPageAdapter(getFragmentManager(),
				activity, activity.mBinder.getLatestMediaServer());
		mListPager.setAdapter(mBrowserPageAdapter);
	}

	public void onRemoteBinder(RemoteBinder mBinder) {
		if (mBrowserPageAdapter.mBinder != mBinder) {
			AbstractConnectionActivity activity = (AbstractConnectionActivity) getActivity();
			if (mBinder == null)
				mBrowserPageAdapter = new BrowserPageAdapter(
						getFragmentManager(), activity, null);
			else
				mBrowserPageAdapter = new BrowserPageAdapter(
						getFragmentManager(), activity,
						mBinder.getLatestMediaServer());
		}
	}

}
