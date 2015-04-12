package de.neo.remote.mobile.fragments;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.IThumbnailListener;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.activities.ControlSceneActivity;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.MediaState;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.BrowserLoadTask;
import de.neo.remote.mobile.tasks.PlayItemTask;
import de.neo.remote.mobile.tasks.PlayListTask;
import de.neo.remote.mobile.util.BrowserAdapter;
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

	/**
	 * map to get full path string from a file name of a playlist item
	 */
	public Map<String, String> plsFileMap = new HashMap<String, String>();

	/**
	 * current viewer state
	 */
	public ViewerState viewerState = ViewerState.DIRECTORIES;

	/**
	 * current remote media state
	 */
	public MediaState mediaState = MediaState.MUSIC_VIDEO;

	/**
	 * current selected item of the list view
	 */
	protected String selectedItem;

	/**
	 * current shown playlist
	 */
	public String currentPlayList;

	/**
	 * current selected item
	 */
	public int selectedPosition;

	public ListView browserContentView;
	private LinearLayout downloadLayout;
	private ProgressBar downloadProgress;
	private TextView downloadText;
	// private LinearLayout dvdLayout;
	private long maxDonwloadSize = 0;

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
		registerForContextMenu(browserContentView);

		browserContentView.setOnItemClickListener(new ListClickListener());
		browserContentView
				.setOnItemLongClickListener(new ListLongClickListener());
	}

	private void findComponents() {
		browserContentView = (ListView) getActivity().findViewById(
				R.id.fileList);
		downloadLayout = (LinearLayout) getActivity().findViewById(
				R.id.layout_download);
		downloadProgress = (ProgressBar) getActivity().findViewById(
				R.id.prg_donwload);
		downloadText = (TextView) getActivity().findViewById(R.id.lbl_download);
	}

	public void onLoadedItems(String[] itemArray, boolean goBack) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		if (goBack) {
			Intent intent = new Intent(activity, ControlSceneActivity.class);
			activity.startActivity(intent);
		}
		final StationStuff mediaServer = activity.mBinder
				.getLatestMediaServer();
		if (mediaServer == null) {
			activity.setTitle(getActivity().getResources().getString(
					R.string.mediaserver_no_server)
					+ "@" + activity.mBinder.getLatestMediaServer().name);
			browserContentView.setAdapter(new BrowserAdapter(activity, null,
					new String[] {}, viewerState, activity.playingBean));
			return;
		}
		browserContentView.setAdapter(new BrowserAdapter(activity,
				mediaServer.browser, itemArray, viewerState,
				activity.playingBean));
		browserContentView.setSelection(selectedPosition);
		if (mediaServer.browser != null)
			new Thread() {
				public void run() {
					try {
						mediaServer.browser.fireThumbnails(
								BrowserFragment.this,
								BrowserAdapter.PREVIEW_SIZE,
								BrowserAdapter.PREVIEW_SIZE);
					} catch (Exception e) {
					}
				}
			}.start();
		switch (viewerState) {
		case DIRECTORIES:
			activity.setTitle(mediaServer.browser.getLocationInt() + "@"
					+ mediaServer.name);
			activity.filesystemButton
					.setBackgroundResource(R.drawable.image_border);
			activity.playlistButton.setBackgroundResource(0);
			break;
		case PLAYLISTS:
			activity.setTitle("Playlists@" + mediaServer.name);
			activity.playlistButton
					.setBackgroundResource(R.drawable.image_border);
			activity.filesystemButton.setBackgroundResource(0);
			break;
		case PLS_ITEMS:
			activity.playlistButton
					.setBackgroundResource(R.drawable.image_border);
			activity.filesystemButton.setBackgroundResource(0);
			activity.setTitle("Playlist: " + currentPlayList + "@"
					+ mediaServer.name);
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(VIEWER_STATE, viewerState.ordinal());
		outState.putInt(MEDIA_STATE, mediaState.ordinal());
		outState.putString(PLAYLIST, currentPlayList);
		outState.putInt(LISTVIEW_POSITION,
				browserContentView.getFirstVisiblePosition());
	}

	public void onRestoreInstanceState(Bundle bundle) {
		viewerState = ViewerState.values()[bundle.getInt(VIEWER_STATE)];
		mediaState = MediaState.values()[bundle.getInt(MEDIA_STATE)];
		currentPlayList = bundle.getString(PLAYLIST);
		selectedPosition = bundle.getInt(LISTVIEW_POSITION);
	}

	public boolean onContextItemSelected(MenuItem item) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		final StationStuff mediaServer = activity.mBinder
				.getLatestMediaServer();
		switch (item.getItemId()) {
		case R.id.opt_item_play:
			new PlayItemTask(activity, selectedItem, activity.mBinder)
					.execute(new String[] {});
			Toast.makeText(
					activity,
					getActivity().getResources().getString(
							R.string.player_play_directory), Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.opt_item_addplaylist:
			new PlayListTask(activity, mediaServer).addItem(selectedItem);
			break;
		case R.id.opt_item_download:
			if (viewerState == ViewerState.PLAYLISTS) {
				activity.mBinder.downloadPlaylist(
						mediaServer.browser,
						plsFileMap.values().toArray(
								new String[plsFileMap.size()]), selectedItem);
			} else if (selectedPosition < mediaServer.directoryCount)
				activity.mBinder.downloadDirectory(activity,
						mediaServer.browser, selectedItem);
			else
				activity.mBinder.downloadFile(activity, mediaServer.browser,
						selectedItem);
			break;
		case R.id.opt_pls_delete:
			new PlayListTask(activity, mediaServer)
					.deletePlaylist(selectedItem);
			break;
		case R.id.opt_pls_show:
			viewerState = ViewerState.PLS_ITEMS;
			currentPlayList = selectedItem;
			new BrowserLoadTask(activity, null, false).execute();
			break;
		case R.id.opt_pls_item_delete:
			new PlayListTask(activity, mediaServer).deleteItemFromPlaylist(
					currentPlayList, selectedItem);
			break;
		}
		return super.onContextItemSelected(item);
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
		MenuInflater mi = new MenuInflater(getActivity().getApplication());
		if (viewerState == ViewerState.DIRECTORIES)
			mi.inflate(R.menu.item_pref, menu);
		if (viewerState == ViewerState.PLAYLISTS)
			mi.inflate(R.menu.pls_pref, menu);
		if (viewerState == ViewerState.PLS_ITEMS)
			mi.inflate(R.menu.pls_item_pref, menu);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		if (activity.mBinder == null)
			return false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			selectedPosition = 0;
			if (viewerState == ViewerState.DIRECTORIES) {
				new BrowserLoadTask(activity, null, true).execute();
				return true;
			}
			if (viewerState == ViewerState.PLAYLISTS) {
				viewerState = ViewerState.DIRECTORIES;
				new BrowserLoadTask(activity, null, false).execute();
				return true;
			}
			if (viewerState == ViewerState.PLS_ITEMS) {
				viewerState = ViewerState.PLAYLISTS;
				new BrowserLoadTask(activity, null, false).execute();
				return true;
			}
		}
		return false;
	}

	/**
	 * listener for long clicks on the list view. store the selected item in the
	 * selecteditem field.
	 * 
	 * @author sebastian
	 */
	public class ListLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				int position, long arg3) {
			selectedItem = ((TextView) view.findViewById(R.id.lbl_item_name))
					.getText().toString();
			if (viewerState == ViewerState.PLS_ITEMS)
				selectedItem = plsFileMap.get(selectedItem);
			selectedPosition = position;
			return false;
		}
	}

	/**
	 * listener for clicks on the list view. play the selected item.
	 * 
	 * @author sebastian
	 */
	public class ListClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			MediaServerActivity activity = (MediaServerActivity) getActivity();
			StationStuff mediaServer = activity.mBinder.getLatestMediaServer();
			String item = ((TextView) view.findViewById(R.id.lbl_item_name))
					.getText().toString();
			if (viewerState == ViewerState.DIRECTORIES) {
				if (position < mediaServer.browser.getDirectoriesInt().length) {
					selectedPosition = 0;
					new BrowserLoadTask(activity, item, false).execute();
					return;
				}
			}
			if (viewerState == ViewerState.PLS_ITEMS)
				item = plsFileMap.get(item);
			PlayItemTask task = new PlayItemTask(
					(MediaServerActivity) getActivity(), item, activity.mBinder);
			task.execute(new String[] {});
		}
	}

	@Override
	public void startReceive(long size, String file) {
		maxDonwloadSize = size;
		downloadLayout.setVisibility(View.VISIBLE);
		downloadProgress.setProgress(0);
		if (file != null)
			downloadText.setText(file);
		else
			downloadText.setText(getActivity().getResources().getString(
					R.string.str_download));
	}

	@Override
	public void startSending(long size) {
		maxDonwloadSize = size;
		downloadLayout.setVisibility(View.VISIBLE);
		downloadProgress.setProgress(0);
		downloadText.setText(getActivity().getResources().getString(
				R.string.str_upload));
	}

	@Override
	public void progressReceive(long size, String file) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		downloadLayout.setVisibility(View.VISIBLE);
		if (maxDonwloadSize == 0)
			maxDonwloadSize = activity.mBinder.getReceiver().getFullSize();
		downloadProgress.setProgress((int) ((100d * size) / maxDonwloadSize));
		if (file != null)
			downloadText.setText(file);
	}

	@Override
	public void progressSending(long size) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		downloadLayout.setVisibility(View.VISIBLE);
		if (maxDonwloadSize == 0)
			maxDonwloadSize = activity.mBinder.getReceiver().getFullSize();
		downloadProgress.setProgress((int) ((100d * size) / maxDonwloadSize));
	}

	@Override
	public void endReceive(long size) {
		downloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void endSending(long size) {
		downloadLayout.setVisibility(View.GONE);
	}

	public void cancelDownload(View v) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		AbstractReceiver receiver = activity.mBinder.getReceiver();
		if (receiver != null) {
			receiver.cancel();
		} else {
			Toast.makeText(activity, "no receiver available",
					Toast.LENGTH_SHORT).show();
			downloadLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void sendingCanceled() {
	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		if (browserContentView != null
				&& browserContentView.getAdapter() instanceof BrowserAdapter) {
			BrowserAdapter adapter = (BrowserAdapter) browserContentView
					.getAdapter();
			adapter.setPlayingFile(bean);
		}
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
	}

	@Override
	public void onStopService() {
	}

	@Override
	public void onPowerSwitchChange(String _switch, State state) {
	}

	@Override
	public void exceptionOccurred(Exception e) {
		downloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void downloadCanceled() {
		downloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void setThumbnail(String file, int width, int height, int[] thumbnail)
			throws RemoteException {
		BrowserAdapter adapter = (BrowserAdapter) browserContentView
				.getAdapter();
		adapter.setThumbnail(file, width, height, thumbnail);
	}

}
