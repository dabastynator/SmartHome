package de.remote.mobile.activies;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.PlayerException;
import de.remote.mobile.R;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.services.RemoteService;
import de.remote.mobile.services.RemoteService.PlayerBinder;
import de.remote.mobile.util.BrowserAdapter;
import de.remote.mobile.util.BufferBrowser;

/**
 * the browser activity shows the current directory with all folders and files.
 * It provides icons to control the music player.
 * 
 * @author sebastian
 */
public class BrowserActivity extends Activity {

	/**
	 * name for extra data for server name
	 */
	public static final String EXTRA_SERVER_ID = "serverId";

	/**
	 * name of the viewer state field to store and restore the value
	 */
	private static final String VIEWER_STATE = "viewerstate";

	/**
	 * name of the playlist field to store and restore the value
	 */
	private static final String PLAYLIST = "playlist";

	/**
	 * name of the position in the list view
	 */
	private static final String LISTVIEW_POSITION = "listviewPosition";

	/**
	 * viewer states of the browser
	 * 
	 * @author sebastian
	 */
	public enum ViewerState {
		DIRECTORIES, PLAYLISTS, PLS_ITEMS
	}

	/**
	 * current directories
	 */
	private String[] directories;

	/**
	 * current files
	 */
	private String[] files;

	/**
	 * list view
	 */
	private ListView listView;

	/**
	 * search input field
	 */
	private EditText searchText;

	/**
	 * map to get full path string from a file name of a playlist item
	 */
	private Map<String, String> plsFileMap = new HashMap<String, String>();

	/**
	 * area that contains the search field and button
	 */
	private LinearLayout searchLayout;

	/**
	 * binder object
	 */
	private PlayerBinder binder;

	/**
	 * current viewer state
	 */
	private ViewerState viewerState = ViewerState.DIRECTORIES;

	/**
	 * current selected item of the list view
	 */
	public String selectedItem;

	/**
	 * current shown playlist
	 */
	private String currentPlayList;

	/**
	 * database object
	 */
	private ServerDatabase serverDB;

	/**
	 * id of connected server
	 */
	private int serverID = -1;

	/**
	 * current selected item
	 */
	private int selectedPosition;

	/**
	 * connection to the service
	 */
	private ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			disableScreen();
			// if there is a server in extra -> connect with this server
			if (getIntent().getExtras() != null
					&& getIntent().getExtras().containsKey(EXTRA_SERVER_ID))
				serverID = getIntent().getExtras().getInt(EXTRA_SERVER_ID);
			// else just connect if there is no connection
			else if (!binder.isConnected()) {
				serverID = serverDB.getFavoriteServer();
				if (serverID == -1)
					Toast.makeText(BrowserActivity.this,
							"no server configurated", Toast.LENGTH_SHORT)
							.show();
			}

			// if there is a server name to connect -> connect
			if (serverID >= 0)
				binder.connectToServer(serverID, new ShowFolderRunnable());
			// else if there is still a connection update the gui
			else if (binder.isConnected())
				new ShowFolderRunnable().run();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		serverDB = new ServerDatabase(this);
		findComponents();
		listView.setBackgroundResource(R.drawable.idefix_dark);
		listView.setScrollingCacheEnabled(false);
		listView.setCacheColorHint(0);
		listView.setOnItemClickListener(new MyClickListener());
		listView.setOnItemLongClickListener(new MyLongClickListener());
		registerForContextMenu(listView);

		searchText.addTextChangedListener(new MyTextWatcher());

		// bind service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * find components by their id
	 */
	private void findComponents() {
		listView = (ListView) findViewById(R.id.fileList);
		searchText = (EditText) findViewById(R.id.txt_search);
		searchLayout = (LinearLayout) findViewById(R.id.layout_search);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = new MenuInflater(getApplication());
		if (viewerState == ViewerState.DIRECTORIES)
			mi.inflate(R.menu.item_pref, menu);
		if (viewerState == ViewerState.PLAYLISTS)
			mi.inflate(R.menu.pls_pref, menu);
		if (viewerState == ViewerState.PLS_ITEMS)
			mi.inflate(R.menu.pls_item_pref, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.browser_pref, menu);
		return true;
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

	/**
	 * update gui elements, show current directory or playlist
	 */
	private void showUpdateUI() {
		if (binder == null || binder.getBrowser() == null) {
			disableScreen();
			return;
		}
		try {
			if (viewerState == ViewerState.DIRECTORIES) {
				directories = binder.getBrowser().getDirectories();
				files = binder.getBrowser().getFiles();
				String[] all = new String[directories.length + files.length];
				System.arraycopy(directories, 0, all, 0, directories.length);
				System.arraycopy(files, 0, all, directories.length,
						files.length);
				listView.setAdapter(new BrowserAdapter(this, binder
						.getBrowser(), all, viewerState));
				setTitle(binder.getBrowser().getLocation() + "@"
						+ binder.getServerName());
			}
			if (viewerState == ViewerState.PLAYLISTS) {
				listView.setAdapter(new BrowserAdapter(this, binder
						.getBrowser(), binder.getPlayList().getPlayLists(),
						viewerState));
				setTitle("Playlists@" + binder.getServerName());
			}
			if (viewerState == ViewerState.PLS_ITEMS) {
				plsFileMap.clear();
				for (String item : binder.getPlayList().listContent(
						currentPlayList))
					if (item.indexOf("/") >= 0)
						plsFileMap
								.put(item.substring(item.lastIndexOf("/") + 1),
										item);
					else
						plsFileMap.put(item, item);
				listView.setAdapter(new BrowserAdapter(this, binder
						.getBrowser(), plsFileMap.keySet().toArray(
						new String[] {}), viewerState));
				setTitle("Playlist: " + currentPlayList + "@"
						+ binder.getServerName());
			}
			listView.setSelection(selectedPosition);
		} catch (RemoteException e) {
			disableScreen();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (PlayerException e) {
			disableScreen();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if (binder == null)
				return super.onKeyDown(keyCode, event);
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (searchLayout.getVisibility() == View.VISIBLE) {
					searchLayout.setVisibility(View.GONE);
					return true;
				}
				selectedPosition = 0;
				if (viewerState == ViewerState.DIRECTORIES)
					if (binder.getBrowser().goBack()) {
						showUpdateUI();
						return true;
					}
				if (viewerState == ViewerState.PLAYLISTS) {
					viewerState = ViewerState.DIRECTORIES;
					showUpdateUI();
					return true;
				}
				if (viewerState == ViewerState.PLS_ITEMS) {
					viewerState = ViewerState.PLAYLISTS;
					showUpdateUI();
					return true;
				}
			}
			if (keyCode == KeyEvent.KEYCODE_SEARCH) {
				searchLayout.setVisibility(View.VISIBLE);
				searchText.requestFocus();
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT);
			}
			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				binder.getPlayer().volDown();
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				binder.getPlayer().volUp();
				return true;
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * toggle playing and pausing status on player.
	 * 
	 * @param v
	 */
	public void playPause(View v) {
		try {
			binder.getPlayer().playPause();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * quit player
	 * 
	 * @param v
	 */
	public void stopPlayer(View v) {
		try {
			binder.getPlayer().quit();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * play next file
	 * 
	 * @param v
	 */
	public void next(View v) {
		try {
			binder.getPlayer().next();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * play previous file
	 * 
	 * @param v
	 */
	public void prev(View v) {
		try {
			binder.getPlayer().previous();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * seek backward
	 * 
	 * @param v
	 */
	public void seekBwd(View v) {
		try {
			binder.getPlayer().seekBackwards();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * seek forward
	 * 
	 * @param v
	 */
	public void seekFwd(View v) {
		try {
			binder.getPlayer().seekForwards();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * volume up
	 * 
	 * @param v
	 */
	public void volUp(View v) {
		try {
			binder.getPlayer().volUp();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * volume down
	 * 
	 * @param v
	 */
	public void volDown(View v) {
		try {
			binder.getPlayer().volDown();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * switch fullscreen status
	 * 
	 * @param v
	 */
	public void fullScreen(View v) {
		try {
			binder.getPlayer().fullScreen();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * perform search in the list view
	 * 
	 * @param v
	 */
	public void search(View v) {
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView
				.getAdapter();
		if (adapter != null) {
			String s = searchText.getText().toString();
			adapter.getFilter().filter(s);
			adapter.notifyDataSetChanged();
		}
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
		searchLayout.setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy() {
		unbindService(playerConnection);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.opt_mplayer:
				binder.useMPlayer();
				break;
			case R.id.opt_totem:
				binder.useTotemPlayer();
				break;
			case R.id.opt_light_off:
				binder.getControl().displayDark();
				break;
			case R.id.opt_light_on:
				binder.getControl().displayBride();
				break;
			case R.id.opt_shutdown:
				binder.getControl().shutdown();
				break;
			case R.id.opt_audiotrack:
				binder.getPlayer().nextAudio();
				break;
			case R.id.opt_left:
				binder.getPlayer().moveLeft();
				break;
			case R.id.opt_right:
				binder.getPlayer().moveRight();
				break;
			case R.id.opt_exit:
				binder.disconnect(null);
				Intent intent = new Intent(this, RemoteService.class);
				stopService(intent);
				finish();
				break;
			case R.id.opt_playlist:
				viewerState = ViewerState.PLAYLISTS;
				showUpdateUI();
				break;
			case R.id.opt_create_playlist:
				intent = new Intent(this, GetTextActivity.class);
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
			case R.id.opt_shuffle_on:
				binder.getPlayer().useShuffle(true);
				break;
			case R.id.opt_shuffle_off:
				binder.getPlayer().useShuffle(false);
				break;
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.opt_item_play:
				binder.getPlayer().play(
						binder.getBrowser().getFullLocation() + selectedItem);
				Toast.makeText(BrowserActivity.this, "Ordner abspielen",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.opt_item_addplaylist:
				Intent i = new Intent(this, SelectPlaylistActivity.class);
				i.putExtra(SelectPlaylistActivity.PLS_LIST, binder
						.getPlayList().getPlayLists());
				startActivityForResult(i,
						SelectPlaylistActivity.SELECT_PLS_CODE);
				break;
			case R.id.opt_item_delete:
				selectedPosition = listView.getFirstVisiblePosition();
				binder.getBrowser().delete(
						binder.getBrowser().getFullLocation() + selectedItem);
				((BufferBrowser) binder.getBrowser()).setDirty();
				showUpdateUI();
				break;
			case R.id.opt_pls_delete:
				binder.getPlayList().removePlayList(selectedItem);
				showUpdateUI();
				Toast.makeText(BrowserActivity.this,
						"Playlist '" + selectedItem + "' deleted",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.opt_pls_show:
				viewerState = ViewerState.PLS_ITEMS;
				currentPlayList = selectedItem;
				showUpdateUI();
				break;
			case R.id.opt_pls_item_delete:
				binder.getPlayList().removeItem(currentPlayList, selectedItem);
				showUpdateUI();
				Toast.makeText(BrowserActivity.this,
						"Entry '" + selectedItem + "' deleted",
						Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (RemoteException e) {
			Toast.makeText(BrowserActivity.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (PlayerException e) {
			Toast.makeText(BrowserActivity.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if (requestCode == SelectPlaylistActivity.SELECT_PLS_CODE) {
				if (data.getExtras() == null)
					return;
				String pls = data.getExtras().getString(
						SelectPlaylistActivity.RESULT);
				binder.getPlayList().extendPlayList(pls,
						binder.getBrowser().getFullLocation() + selectedItem);
				Toast.makeText(BrowserActivity.this, selectedItem + " added",
						Toast.LENGTH_SHORT).show();
			}
			if (requestCode == GetTextActivity.RESULT_CODE && data != null
					&& data.getExtras() != null) {
				String pls = data.getExtras().getString(GetTextActivity.RESULT);
				binder.getPlayList().addPlayList(pls);
				showUpdateUI();
				Toast.makeText(BrowserActivity.this,
						"playlist '" + pls + "' added", Toast.LENGTH_SHORT)
						.show();
			}
			if (requestCode == SelectServerActivity.RESULT_CODE) {
				if (data == null || data.getExtras() == null)
					return;
				serverID = data.getExtras().getInt(
						SelectServerActivity.SERVER_ID);
				disableScreen();
				binder.connectToServer(serverID, new ShowFolderRunnable());
			}
		} catch (RemoteException e) {
			Toast.makeText(BrowserActivity.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (PlayerException e) {
			Toast.makeText(BrowserActivity.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * disable the gui elements. inform user about connecting status.
	 */
	private void disableScreen() {
		setTitle("connecting...");
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new String[] {}));
	}

	/**
	 * callback for connecting to the remote server. update the list view and
	 * show the directory.
	 * 
	 * @author sebastian
	 */
	public class ShowFolderRunnable implements Runnable {
		@Override
		public void run() {
			showUpdateUI();
		}
	}

	/**
	 * listener for clicks on the list view. play the selected item.
	 * 
	 * @author sebastian
	 */
	public class MyClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			String item = ((TextView) view.findViewById(R.id.lbl_item_name))
					.getText().toString();
			try {
				if (viewerState == ViewerState.PLAYLISTS) {
					binder.getPlayer().playPlayList(binder.getPlayList().getPlaylistFullpath(item));
					Toast.makeText(BrowserActivity.this, "play playlist",
							Toast.LENGTH_SHORT).show();
				}
				if (viewerState == ViewerState.PLS_ITEMS){
					binder.getPlayer().play(plsFileMap.get(item));
				}
				if (viewerState == ViewerState.DIRECTORIES) {
					if (position < binder.getBrowser().getDirectories().length) {
						binder.getBrowser().goTo(item);
						selectedPosition = 0;
						showUpdateUI();
						return;
					}
					String file = binder.getBrowser().getFullLocation() + item;

					binder.getPlayer().play(file);
				}
			} catch (RemoteException e) {
				Toast.makeText(BrowserActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
			} catch (PlayerException e) {
				Toast.makeText(BrowserActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * listener for long clicks on the list view. store the selected item in the
	 * selecteditem field.
	 * 
	 * @author sebastian
	 */
	public class MyLongClickListener implements OnItemLongClickListener {

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
	 * the text watcher gets the new input of the search field and informs the
	 * adapter to filder the input.
	 * 
	 * @author sebastian
	 */
	public class MyTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView
					.getAdapter();
			if (adapter != null) {
				adapter.getFilter().filter(s);
				adapter.notifyDataSetChanged();
			}
		}
	}
}