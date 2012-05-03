package de.remote.mobile.activies;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import de.remote.mobile.R;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.services.PlayerBinder;

public abstract class BrowserBase extends Activity {

	/**
	 * name for extra data for server name
	 */
	public static final String EXTRA_SERVER_ID = "serverId";

	/**
	 * name of the viewer state field to store and restore the value
	 */
	public static final String VIEWER_STATE = "viewerstate";

	/**
	 * name of the playlist field to store and restore the value
	 */
	public static final String PLAYLIST = "playlist";

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
	 * area that contains the download progress and cancel button
	 */
	protected LinearLayout downloadLayout;

	/**
	 * binder object
	 */
	protected PlayerBinder binder;

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
	 * database object
	 */
	protected ServerDatabase serverDB;

	/**
	 * id of connected server
	 */
	protected int serverID = -1;

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
		setContentView(R.layout.main);
		serverDB = new ServerDatabase(this);
		findComponents();
		listView.setBackgroundResource(R.drawable.idefix_dark);
		listView.setScrollingCacheEnabled(false);
		listView.setCacheColorHint(0);
		registerForContextMenu(listView);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_create_playlist:
			Intent intent = new Intent(this, GetTextActivity.class);
			startActivityForResult(intent, GetTextActivity.RESULT_CODE);
			break;
		case R.id.opt_chat:
			intent = new Intent(this, ChatActivity.class);
			startActivity(intent);
			break;
		case R.id.opt_server_select:
			intent = new Intent(this, SelectServerActivity.class);
			startActivityForResult(intent, SelectServerActivity.RESULT_CODE);
			break;
		}
		return super.onOptionsItemSelected(item);
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

}
