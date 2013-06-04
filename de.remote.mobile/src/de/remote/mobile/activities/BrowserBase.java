package de.remote.mobile.activities;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;
import de.remote.mobile.R;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.remote.mobile.util.AI;

public abstract class BrowserBase extends BindedActivity {

	/**
	 * name of the viewer state field to store and restore the value
	 */
	public static final String VIEWER_STATE = "viewerstate";

	/**
	 * name of the playlist field to store and restore the value
	 */
	public static final String PLAYLIST = "playlist";

	/**
	 * request code for file search
	 */
	public static final int FILE_REQUEST = 3;

	/**
	 * name of the position in the list view
	 */
	public static final String LISTVIEW_POSITION = "listviewPosition";

	public static final String SPINNER_POSITION = "spinnerPosition";

	/**
	 * viewer states of the browser
	 * 
	 * @author sebastian
	 */
	public enum ViewerState {
		DIRECTORIES, PLAYLISTS, PLS_ITEMS
	}

	/**
	 * list view
	 */
	protected ListView listView;

	/**
	 * listener for remote actions
	 */
	protected IRemoteActionListener remoteListener;

	/**
	 * search input field
	 */
	protected EditText searchText;

	/**
	 * spinner for all music stations of the server.
	 */
	protected Spinner musicstationSpinner;

	/**
	 * map to get full path string from a file name of a playlist item
	 */
	protected Map<String, String> plsFileMap = new HashMap<String, String>();

	/**
	 * area that contains the search field and button
	 */
	protected LinearLayout searchLayout;

	/**
	 * current viewer state
	 */
	protected ViewerState viewerState = ViewerState.DIRECTORIES;

	/**
	 * current selected item of the list view
	 */
	protected String selectedItem;

	/**
	 * current shown playlist
	 */
	protected String currentPlayList;

	/**
	 * current selected item
	 */
	protected int selectedPosition;

	/**
	 * play / pause button
	 */
	protected ImageView playButton;

	/**
	 * progress bar for download progress
	 */
	protected ProgressBar downloadProgress;

	/**
	 * The artificial intelligence recognize speech
	 */
	protected AI ai;

	protected ImageView mplayerButton;

	protected ImageView totemButton;

	protected ImageView filesystemButton;

	protected ImageView playlistButton;

	protected int spinnerPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.main);

		findComponents();
		// listView.setBackgroundResource(R.drawable.idefix_dark);
		// listView.setScrollingCacheEnabled(false);
		// listView.setCacheColorHint(0);
		registerForContextMenu(listView);

		ai = new AI(this);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_create_playlist:
			Intent intent = new Intent(this, GetTextActivity.class);
			startActivityForResult(intent, GetTextActivity.RESULT_CODE);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * find components by their id
	 */
	private void findComponents() {
		listView = (ListView) findViewById(R.id.fileList);
		searchText = (EditText) findViewById(R.id.txt_search);
		searchLayout = (LinearLayout) findViewById(R.id.layout_search);
		downloadLayout = (LinearLayout) findViewById(R.id.layout_download);
		downloadProgress = (ProgressBar) findViewById(R.id.prg_donwload);
		playButton = (ImageView) findViewById(R.id.button_play);
		musicstationSpinner = (Spinner) findViewById(R.id.spinner_music_station);
		mplayerButton = (ImageView) findViewById(R.id.button_mplayer);
		totemButton = (ImageView) findViewById(R.id.button_totem);
		filesystemButton = (ImageView) findViewById(R.id.button_filesystem);
		playlistButton = (ImageView) findViewById(R.id.button_playlist);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(VIEWER_STATE, viewerState.ordinal());
		outState.putString(PLAYLIST, currentPlayList);
		outState.putInt(LISTVIEW_POSITION, listView.getFirstVisiblePosition());
		outState.putInt(SPINNER_POSITION,
				musicstationSpinner.getSelectedItemPosition());
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		viewerState = ViewerState.values()[bundle.getInt(VIEWER_STATE)];
		currentPlayList = bundle.getString(PLAYLIST);
		selectedPosition = bundle.getInt(LISTVIEW_POSITION);
		spinnerPosition = bundle.getInt(SPINNER_POSITION);
	}

	@Override
	protected void onResume() {
		super.onResume();
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView
				.getAdapter();
		String s = searchText.getText().toString();
		if (adapter != null && s != null) {
			adapter.getFilter().filter(s);
			adapter.notifyDataSetChanged();
			if (s.length() > 0)
				searchLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	void binderConnected() {
		onPlayingBeanChanged(binder.getPlayingFile());
	}

	private long max = 0;

	@Override
	public void onPlayingBeanChanged(PlayingBean bean) {
		if (bean == null || bean.getState() == STATE.PLAY)
			playButton.setImageResource(R.drawable.pause);
		else if (bean.getState() == STATE.PAUSE)
			playButton.setImageResource(R.drawable.play);
	}

	@Override
	public void onServerConnectionChanged(String serverName) {

	}

	@Override
	public void startReceive(long size) {
		max = size;
		downloadLayout.setVisibility(View.VISIBLE);
		downloadProgress.setProgress(0);
	}

	@Override
	public void progressReceive(long size) {
		if (max == 0)
			max = binder.getReceiver().getFullSize();
		downloadProgress.setProgress((int) ((100d * size) / max));
	}

	@Override
	public void endReceive(long size) {
		downloadLayout.setVisibility(View.GONE);
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
	public void onStopService() {

	}

}
