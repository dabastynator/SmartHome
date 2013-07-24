package de.remote.mobile.activities;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.Toast;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.transceiver.AbstractReceiver;
import de.newsystem.rmi.transceiver.AbstractReceiver.ReceiverState;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mediaserver.api.PlayerException;
import de.remote.mobile.R;
import de.remote.mobile.services.RemoteService.StationStuff;
import de.remote.mobile.util.BrowserAdapter;
import de.remote.mobile.util.BufferBrowser;

/**
 * the browser activity shows the current directory with all folders and files.
 * It provides icons to control the music player.
 * 
 * @author sebastian
 */
public class BrowserActivity extends BrowserBase {

	private StationStuff mediaServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);

		// set listener
		searchText.addTextChangedListener(new SearchTextWatcher());
		listView.setOnItemClickListener(new ListClickListener());
		listView.setOnItemLongClickListener(new ListLongClickListener());
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

	private String[] loadItems(String[] gotoPath) throws RemoteException,
			PlayerException {
		if (mediaServer == null) {
			disableScreen();
			return new String[] {};
		}
		switch (viewerState) {
		case DIRECTORIES:
			if (gotoPath != null && gotoPath.length > 0 && gotoPath[0] != null)
				mediaServer.browser.goTo(gotoPath[0]);
			String[] directories = mediaServer.browser.getDirectories();
			String[] files = mediaServer.browser.getFiles();
			String[] all = new String[directories.length + files.length];
			System.arraycopy(directories, 0, all, 0, directories.length);
			System.arraycopy(files, 0, all, directories.length, files.length);
			return all;
		case PLAYLISTS:
			return mediaServer.pls.getPlayLists();
		case PLS_ITEMS:
			plsFileMap.clear();
			for (String item : mediaServer.pls.listContent(currentPlayList))
				if (item.indexOf("/") >= 0)
					plsFileMap.put(item.substring(item.lastIndexOf("/") + 1),
							item);
				else
					plsFileMap.put(item, item);
			return plsFileMap.keySet().toArray(new String[] {});
		default:
			return new String[] {};
		}
	}

	private void disableScreen() {
		setTitle("No connection");
		setProgressBarVisibility(false);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new String[] {}));
	}

	/**
	 * update gui elements, show current directory or playlist
	 * 
	 * @param gotoPath
	 */
	private void updateGUI(final String gotoPath) {
		if (mediaServer == null) {
			disableScreen();
			return;
		}
		new AsyncTask<String, Integer, String[]>() {

			Exception exeption = null;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				setProgressBarVisibility(true);
				setTitle("loading...");
			}

			@Override
			protected String[] doInBackground(String... params) {
				try {
					exeption = null;
					return loadItems(params);
				} catch (Exception e) {
					exeption = e;
					return new String[] {};
				}
			}

			@Override
			protected void onPostExecute(String[] result) {
				super.onPostExecute(result);
				setProgressBarVisibility(false);
				if (mediaServer.browser == null) {
					setTitle("No media server@" + binder.getServerName());
					listView.setAdapter(new BrowserAdapter(
							BrowserActivity.this, mediaServer.browser,
							new String[] {}, viewerState, playingBean));
					return;
				}
				listView.setAdapter(new BrowserAdapter(BrowserActivity.this,
						mediaServer.browser, result, viewerState, playingBean));
				listView.setSelection(selectedPosition);
				switch (viewerState) {
				case DIRECTORIES:
					try {
						setTitle(mediaServer.browser.getLocation() + "@"
								+ binder.getServerName());
					} catch (RemoteException e) {
						setTitle("no connection");
					}
					filesystemButton
							.setBackgroundResource(R.drawable.image_border);
					playlistButton.setBackgroundDrawable(null);
					break;
				case PLAYLISTS:
					setTitle("Playlists@" + binder.getServerName());
					playlistButton
							.setBackgroundResource(R.drawable.image_border);
					filesystemButton.setBackgroundDrawable(null);
					break;
				case PLS_ITEMS:
					playlistButton
							.setBackgroundResource(R.drawable.image_border);
					filesystemButton.setBackgroundDrawable(null);
					setTitle("Playlist: " + currentPlayList + "@"
							+ binder.getServerName());
				}
				if (exeption != null) {
					if (exeption.getMessage() != null
							&& exeption.getMessage().length() > 0)
						Toast.makeText(BrowserActivity.this,
								exeption.getMessage(), Toast.LENGTH_SHORT)
								.show();
					else
						Toast.makeText(BrowserActivity.this,
								exeption.getClass().getSimpleName(),
								Toast.LENGTH_SHORT).show();
				}
			}

		}.execute(new String[] { gotoPath });
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if (binder == null)
				return super.onKeyDown(keyCode, event);
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (searchLayout.getVisibility() == View.VISIBLE) {
					searchLayout.setVisibility(View.GONE);
					searchText.setText("");
					return true;
				}
				selectedPosition = 0;
				if (viewerState == ViewerState.DIRECTORIES)
					if (mediaServer.browser.goBack()) {
						updateGUI(null);
						return true;
					}
				if (viewerState == ViewerState.PLAYLISTS) {
					viewerState = ViewerState.DIRECTORIES;
					updateGUI(null);
					return true;
				}
				if (viewerState == ViewerState.PLS_ITEMS) {
					viewerState = ViewerState.PLAYLISTS;
					updateGUI(null);
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
				mediaServer.player.volDown();
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				mediaServer.player.volUp();
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
			mediaServer.player.playPause();
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
			mediaServer.player.quit();
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
			mediaServer.player.next();
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
			mediaServer.player.previous();
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
			mediaServer.player.seekBackwards();
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
			mediaServer.player.seekForwards();
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
			mediaServer.player.volUp();
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
			mediaServer.player.volDown();
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
			mediaServer.player.fullScreen();
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

	/**
	 * cancel the current download
	 * 
	 * @param v
	 */
	public void cancelDownload(View v) {
		AbstractReceiver receiver = binder.getReceiver();
		if (receiver != null) {
			receiver.cancel();
		} else {
			Toast.makeText(this, "no receiver available", Toast.LENGTH_SHORT)
					.show();
			downloadLayout.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		unbindService(playerConnection);
		super.onDestroy();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.opt_mplayer:
				mediaServer.player = mediaServer.mplayer;
				break;
			case R.id.opt_totem:
				mediaServer.player = mediaServer.totem;
				break;
			case R.id.opt_light_off:
				mediaServer.control.displayDark();
				break;
			case R.id.opt_light_on:
				mediaServer.control.displayBride();
				break;
			case R.id.opt_shutdown:
				mediaServer.control.shutdown();
				break;
			case R.id.opt_audiotrack:
				mediaServer.player.nextAudio();
				break;
			case R.id.opt_left:
				mediaServer.player.moveLeft();
				break;
			case R.id.opt_right:
				mediaServer.player.moveRight();
				break;
			case R.id.opt_playlist:
				viewerState = ViewerState.PLAYLISTS;
				updateGUI(null);
				break;
			case R.id.opt_record:
				ai.record();
				break;
			case R.id.opt_upload:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(
						Intent.createChooser(intent, "File Chooser"),
						FILE_REQUEST);
				break;
			// case R.id.opt_shuffle_on:
			// binder.getPlayer().useShuffle(true);
			// break;
			// case R.id.opt_shuffle_off:
			// binder.getPlayer().useShuffle(false);
			// break;
			case R.id.opt_mousepad:
				intent = new Intent(this, MouseActivity.class);
				startActivity(intent);
				break;
			case R.id.opt_power:
				intent = new Intent(this, PowerActivity.class);
				startActivity(intent);
				break;
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.opt_item_play:
				mediaServer.player.play(mediaServer.browser.getFullLocation()
						+ selectedItem);
				Toast.makeText(BrowserActivity.this, "Ordner abspielen",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.opt_item_addplaylist:
				Intent i = new Intent(this, SelectPlaylistActivity.class);
				i.putExtra(SelectPlaylistActivity.PLS_LIST,
						mediaServer.pls.getPlayLists());
				startActivityForResult(i,
						SelectPlaylistActivity.SELECT_PLS_CODE);
				break;
			case R.id.opt_item_download:
				if (selectedPosition < mediaServer.browser.getDirectories().length)
					binder.downloadDirectory(mediaServer.browser, selectedItem);
				else
					binder.downloadFile(mediaServer.browser, selectedItem);
				break;
			case R.id.opt_item_delete:
				selectedPosition = listView.getFirstVisiblePosition();
				mediaServer.browser.delete(mediaServer.browser
						.getFullLocation() + selectedItem);
				((BufferBrowser) mediaServer.browser).setDirty();
				updateGUI(null);
				break;
			case R.id.opt_pls_delete:
				mediaServer.pls.removePlayList(selectedItem);
				updateGUI(null);
				Toast.makeText(BrowserActivity.this,
						"Playlist '" + selectedItem + "' deleted",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.opt_pls_show:
				viewerState = ViewerState.PLS_ITEMS;
				currentPlayList = selectedItem;
				updateGUI(null);
				break;
			case R.id.opt_pls_item_delete:
				mediaServer.pls.removeItem(currentPlayList, selectedItem);
				updateGUI(null);
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
		if (data == null)
			return;
		try {
			if (requestCode == SelectPlaylistActivity.SELECT_PLS_CODE) {
				if (data.getExtras() == null)
					return;
				String pls = data.getExtras().getString(
						SelectPlaylistActivity.RESULT);
				mediaServer.pls.extendPlayList(pls,
						mediaServer.browser.getFullLocation() + selectedItem);
				Toast.makeText(BrowserActivity.this, selectedItem + " added",
						Toast.LENGTH_SHORT).show();
			}
			if (requestCode == GetTextActivity.RESULT_CODE && data != null
					&& data.getExtras() != null) {
				String pls = data.getExtras().getString(GetTextActivity.RESULT);
				mediaServer.pls.addPlayList(pls);
				updateGUI(null);
				Toast.makeText(BrowserActivity.this,
						"playlist '" + pls + "' added", Toast.LENGTH_SHORT)
						.show();
			}
			if (requestCode == FILE_REQUEST) {
				Uri uri = data.getData();
				binder.uploadFile(mediaServer.browser, new File(
						getFilePathByUri(uri)));
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
	 * convert given uri to file path.
	 * 
	 * @param uri
	 * @return file path
	 */
	public String getFilePathByUri(Uri uri) {
		String fileName = "unknown";// default fileName
		Uri filePathUri = uri;
		if (uri.getScheme().toString().compareTo("content") == 0) {
			Cursor cursor = getContentResolver().query(uri, null, null, null,
					null);
			if (cursor.moveToFirst()) {
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				filePathUri = Uri.parse(cursor.getString(column_index));
				fileName = filePathUri.getPath();
			}
		} else if (uri.getScheme().compareTo("file") == 0) {
			fileName = filePathUri.getPath();
		} else {
			fileName = filePathUri.getPath();
		}
		return fileName;
	}

	@Override
	protected void startConnecting() {
		setTitle("connecting...");
		setProgressBarVisibility(true);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new String[] {}));
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {

		new AsyncTask<String, Integer, String[]>() {

			Exception exeption = null;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				setProgressBarVisibility(true);
				setTitle("loading media objects...");
			}

			@Override
			protected String[] doInBackground(String... params) {
				try {
					mediaServer = binder.getMediaServerByName(mediaServerName);
					return new String[] {};
				} catch (Exception e) {
					exeption = e;
					return new String[] {};
				}
			}

			@Override
			protected void onPostExecute(String[] result) {
				if (exeption != null)
					Toast.makeText(BrowserActivity.this, exeption.getMessage(),
							Toast.LENGTH_SHORT).show();
				if (mediaServer != null) {
					updateGUI(null);
				} else {
					disableScreen();
				}
				ai.setPlayerBinder(binder);
			}

		}.execute(new String[] {});
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
			updateGUI(null);
			if (binder.getReceiver() != null
					&& binder.getReceiver().getState() == ReceiverState.LOADING) {
				downloadLayout.setVisibility(View.VISIBLE);
				downloadProgress.setProgress((int) ((100d * binder
						.getReceiver().getDownloadProgress()) / binder
						.getReceiver().getFullSize()));
			}
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
			String item = ((TextView) view.findViewById(R.id.lbl_item_name))
					.getText().toString();
			try {
				if (viewerState == ViewerState.PLAYLISTS) {
					mediaServer.player.playPlayList(mediaServer.pls
							.getPlaylistFullpath(item));
					Toast.makeText(BrowserActivity.this, "play playlist",
							Toast.LENGTH_SHORT).show();
				}
				if (viewerState == ViewerState.PLS_ITEMS) {
					mediaServer.player.play(plsFileMap.get(item));
				}
				if (viewerState == ViewerState.DIRECTORIES) {
					if (position < mediaServer.browser.getDirectories().length) {
						selectedPosition = 0;
						updateGUI(item);
						return;
					}
					String file = mediaServer.browser.getFullLocation() + item;

					mediaServer.player.play(file);
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

	public void showFileSystem(View view) {
		viewerState = ViewerState.DIRECTORIES;
		updateGUI(null);
	}

	public void showPlaylist(View view) {
		viewerState = ViewerState.PLAYLISTS;
		updateGUI(null);
	}

	public void setTotem(View view) {
		mediaServer.player = mediaServer.totem;
		totemButton.setBackgroundResource(R.drawable.image_border);
		mplayerButton.setBackgroundDrawable(null);
	}

	public void setMPlayer(View view) {
		mediaServer.player = mediaServer.totem;
		mplayerButton.setBackgroundResource(R.drawable.image_border);
		totemButton.setBackgroundDrawable(null);
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
	 * the text watcher gets the new input of the search field and informs the
	 * adapter to filder the input.
	 * 
	 * @author sebastian
	 */
	public class SearchTextWatcher implements TextWatcher {

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

	@Override
	public void onPowerSwitchChange(String _switch, State state) {
		// TODO Auto-generated method stub

	}

}