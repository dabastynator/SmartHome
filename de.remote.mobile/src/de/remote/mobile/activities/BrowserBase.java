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
import de.remote.mobile.R;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    requestWindowFeature(Window.FEATURE_PROGRESS);
		
		setContentView(R.layout.main);
		
		findComponents();
		listView.setBackgroundResource(R.drawable.idefix_dark);
		listView.setScrollingCacheEnabled(false);
		listView.setCacheColorHint(0);
		registerForContextMenu(listView);
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
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(VIEWER_STATE, viewerState.ordinal());
		outState.putString(PLAYLIST, currentPlayList);
		outState.putInt(LISTVIEW_POSITION, listView.getFirstVisiblePosition());
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		viewerState = ViewerState.values()[bundle.getInt(VIEWER_STATE)];
		currentPlayList = bundle.getString(PLAYLIST);
		selectedPosition = bundle.getInt(LISTVIEW_POSITION);

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
		binder.addRemoteActionListener(remoteListener);
		remoteListener = new BrowserRemoteListener(downloadLayout,
				downloadProgress, playButton, binder);
		remoteListener.newPlayingFile(binder.getPlayingFile());
	}

}
