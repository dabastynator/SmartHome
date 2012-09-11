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
import de.remote.api.PlayerException;
import de.remote.mobile.R;
import de.remote.mobile.util.BrowserAdapter;
import de.remote.mobile.util.BufferBrowser;

/**
 * the browser activity shows the current directory with all folders and files.
 * It provides icons to control the music player.
 * 
 * @author sebastian
 */
public class BrowserActivity extends BrowserBase {

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

	private String[] loadItems() throws RemoteException, PlayerException {
		if (binder == null || !binder.isConnected()) {
			disableScreen();
			return new String[] {};
		}
		switch (viewerState) {
		case DIRECTORIES:
			String[] directories = binder.getBrowser().getDirectories();
			String[] files = binder.getBrowser().getFiles();
			String[] all = new String[directories.length + files.length];
			System.arraycopy(directories, 0, all, 0, directories.length);
			System.arraycopy(files, 0, all, directories.length, files.length);
			return all;
		case PLAYLISTS:
			return binder.getPlayList().getPlayLists();
		case PLS_ITEMS:
			plsFileMap.clear();
			for (String item : binder.getPlayList()
					.listContent(currentPlayList))
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

	/**
	 * update gui elements, show current directory or playlist
	 */
	private void showUpdateUI() {
		if (binder == null || !binder.isConnected()) {
			disableScreen();
			return;
		}
		new AsyncTask<Integer, Integer, String[]>() {

			Exception exeption = null;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				setProgressBarVisibility(true);
				setTitle("loading...");
			}

			@Override
			protected String[] doInBackground(Integer... params) {
				try {
					exeption = null;
					return loadItems();
				} catch (Exception e) {
					exeption = e;
					return new String[] {};
				}
			}

			@Override
			protected void onPostExecute(String[] result) {
				super.onPostExecute(result);
				setProgressBarVisibility(false);
				listView.setAdapter(new BrowserAdapter(BrowserActivity.this,
						binder.getBrowser(), result, viewerState));
				listView.setSelection(selectedPosition);
				switch (viewerState) {
				case DIRECTORIES:
					try {
						setTitle(binder.getBrowser().getLocation() + "@"
								+ binder.getServerName());
					} catch (RemoteException e) {
						setTitle("no connection");
					}
					break;
				case PLAYLISTS:
					setTitle("Playlists@" + binder.getServerName());
					break;
				case PLS_ITEMS:
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

		}.execute(new Integer[] {});
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
			case R.id.opt_playlist:
				viewerState = ViewerState.PLAYLISTS;
				showUpdateUI();
				break;
			case R.id.opt_upload:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(
						Intent.createChooser(intent, "File Chooser"),
						FILE_REQUEST);
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
		return super.onMenuItemSelected(featureId, item);
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
			case R.id.opt_item_download:
				if (selectedPosition < binder.getBrowser().getDirectories().length)
					binder.downloadDirectory(selectedItem);
				else
					binder.downloadFile(selectedItem);
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
		if (data == null)
			return;
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
			if (requestCode == FILE_REQUEST) {
				Uri uri = data.getData();
				binder.uploadFile(new File(getFilePathByUri(uri)));
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
			fileName = fileName + "_"
					+ filePathUri.getLastPathSegment().toString();
		}
		return fileName;
	}

	@Override
	protected void disableScreen() {
		setTitle("connecting...");
		setProgressBarVisibility(true);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new String[] {}));
	}

	@Override
	void remoteConnected() {
		if (binder.isConnected())
			showUpdateUI();
		else
			disableScreen();
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
					binder.getPlayer().playPlayList(
							binder.getPlayList().getPlaylistFullpath(item));
					Toast.makeText(BrowserActivity.this, "play playlist",
							Toast.LENGTH_SHORT).show();
				}
				if (viewerState == ViewerState.PLS_ITEMS) {
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
}