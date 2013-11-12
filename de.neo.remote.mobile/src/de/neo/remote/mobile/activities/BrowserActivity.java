package de.neo.remote.mobile.activities;

import java.io.File;

import android.app.ProgressDialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mediaserver.api.IDVDPlayer;
import de.neo.remote.mediaserver.api.PlayerException;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.util.BrowserAdapter;
import de.neo.remote.mobile.util.BufferBrowser;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.transceiver.AbstractReceiver;
import de.neo.rmi.transceiver.AbstractReceiver.ReceiverState;
import de.remote.mobile.R;

/**
 * the browser activity shows the current directory with all folders and files.
 * It provides icons to control the music player.
 * 
 * @author sebastian
 */
public class BrowserActivity extends BrowserBase {

	private boolean isFullscreen;

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
		StationStuff mediaServer = binder.getLatestMediaServer();
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
		if (binder.getLatestMediaServer() == null) {
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
				StationStuff mediaServer = binder.getLatestMediaServer();
				setProgressBarVisibility(false);
				if (mediaServer == null) {
					setTitle("No media server@"
							+ binder.getLatestMediaServer().name);
					listView.setAdapter(new BrowserAdapter(
							BrowserActivity.this, null, new String[] {},
							viewerState, playingBean));
					return;
				}
				listView.setAdapter(new BrowserAdapter(BrowserActivity.this,
						mediaServer.browser, result, viewerState, playingBean));
				listView.setSelection(selectedPosition);
				switch (viewerState) {
				case DIRECTORIES:
					try {
						setTitle(mediaServer.browser.getLocation() + "@"
								+ mediaServer.name);
					} catch (RemoteException e) {
						setTitle("no connection");
					}
					filesystemButton
							.setBackgroundResource(R.drawable.image_border);
					playlistButton.setBackgroundDrawable(null);
					break;
				case PLAYLISTS:
					setTitle("Playlists@" + mediaServer.name);
					playlistButton
							.setBackgroundResource(R.drawable.image_border);
					filesystemButton.setBackgroundDrawable(null);
					break;
				case PLS_ITEMS:
					playlistButton
							.setBackgroundResource(R.drawable.image_border);
					filesystemButton.setBackgroundDrawable(null);
					setTitle("Playlist: " + currentPlayList + "@"
							+ mediaServer.name);
				}
				if (exeption != null) {
					if (exeption instanceof NullPointerException)
						Toast.makeText(
								BrowserActivity.this,
								"NullPointerException: Mediaserver might incorrectly configured",
								Toast.LENGTH_SHORT).show();
					else if (exeption.getMessage() != null
							&& exeption.getMessage().length() > 0)
						Toast.makeText(BrowserActivity.this,
								exeption.getMessage(), Toast.LENGTH_SHORT)
								.show();
					else
						Toast.makeText(BrowserActivity.this,
								exeption.getClass().getSimpleName(),
								Toast.LENGTH_SHORT).show();
				}
				IPlayer player = mediaServer.player;
				totemButton.setBackgroundDrawable(null);
				mplayerButton.setBackgroundDrawable(null);
				if (omxButton != null)
					omxButton.setBackgroundDrawable(null);
				if (player == binder.getLatestMediaServer().mplayer)
					mplayerButton
							.setBackgroundResource(R.drawable.image_border);
				if (player == binder.getLatestMediaServer().totem)
					totemButton.setBackgroundResource(R.drawable.image_border);
				if (player == binder.getLatestMediaServer().omxplayer
						&& omxButton != null)
					omxButton.setBackgroundResource(R.drawable.image_border);
			}

		}.execute(new String[] { gotoPath });
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		StationStuff mediaServer = binder.getLatestMediaServer();
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

	public void showDVDBar(View v) {
		IPlayer player = binder.getLatestMediaServer().player;
		if (dvdLayout.getVisibility() == View.VISIBLE) {
			dvdLayout.setVisibility(View.GONE);
			dvdButton.setBackgroundDrawable(null);
		} else {
			if (player instanceof IDVDPlayer) {
				try {
					((IDVDPlayer) player).playDVD();
					dvdLayout.setVisibility(View.VISIBLE);
					dvdButton.setBackgroundResource(R.drawable.image_border);
				} catch (Exception e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT)
							.show();
				}
			} else
				Toast.makeText(this, "player does not support dvds",
						Toast.LENGTH_SHORT).show();
		}
	}

	public void playerAction(View v) {
		if (binder == null || binder.getLatestMediaServer() == null)
			return;
		IPlayer player = binder.getLatestMediaServer().player;
		try {
			switch (v.getId()) {
			case R.id.button_play:
				player.playPause();
				break;
			case R.id.button_next:
				player.next();
				break;
			case R.id.button_pref:
				player.previous();
				break;
			case R.id.button_vol_up:
				player.volUp();
				break;
			case R.id.button_vol_down:
				player.volDown();
				break;
			case R.id.button_seek_bwd:
				player.seekBackwards();
				break;
			case R.id.button_seek_fwd:
				player.seekForwards();
				break;
			case R.id.button_full:
				player.fullScreen(isFullscreen = !isFullscreen);
				break;
			case R.id.button_quit:
				player.quit();
				break;
			case R.id.button_left:
				if (player instanceof IDVDPlayer)
					((IDVDPlayer) player).menuLeft();
				break;
			case R.id.button_right:
				if (player instanceof IDVDPlayer)
					((IDVDPlayer) player).menuRight();
				break;
			case R.id.button_up:
				if (player instanceof IDVDPlayer)
					((IDVDPlayer) player).menuUp();
				break;
			case R.id.button_down:
				if (player instanceof IDVDPlayer)
					((IDVDPlayer) player).menuDown();
				;
				break;
			case R.id.button_enter:
				if (player instanceof IDVDPlayer)
					((IDVDPlayer) player).menuEnter();
				break;
			}
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
		StationStuff mediaServer = binder.getLatestMediaServer();
		try {
			switch (item.getItemId()) {
			case R.id.opt_mplayer:
				setMPlayer(null);
				break;
			case R.id.opt_totem:
				setTotem(null);
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
			case R.id.opt_dvd_eject:
				if (mediaServer.player instanceof IDVDPlayer)
					((IDVDPlayer) mediaServer.player).ejectDVD();
				break;
			case R.id.opt_dvd_no_subtitle:
				if (mediaServer.player instanceof IDVDPlayer)
					((IDVDPlayer) mediaServer.player).subtitleRemove();
				break;
			case R.id.opt_dvd_next_subtitle:
				if (mediaServer.player instanceof IDVDPlayer)
					((IDVDPlayer) mediaServer.player).subtitleNext();
				break;
			case R.id.opt_dvd_menu:
				if (mediaServer.player instanceof IDVDPlayer)
					((IDVDPlayer) mediaServer.player).showMenu();
				break;
			case R.id.opt_playlist:
				viewerState = ViewerState.PLAYLISTS;
				updateGUI(null);
				break;
			case R.id.opt_refresh:
				binder.getMediaServerByName(mediaServerName);
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
		StationStuff mediaServer = binder.getLatestMediaServer();
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
		StationStuff mediaServer = binder.getLatestMediaServer();
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
	protected void onStartConnecting() {
		setTitle("connecting...");
		setProgressBarVisibility(true);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new String[] {}));
	};

	@Override
	void onBinderConnected() {
		super.onBinderConnected();
		if (binder.getLatestMediaServer() != null
				&& binder.getLatestMediaServer().name.equals(mediaServerName)) {
			updateGUI(null);
		}
		Bundle extras = getIntent().getExtras();
		String youtubeURL = extras.getString(Intent.EXTRA_TEXT);
		if (youtubeURL != null) {
			playYoutubeStream(youtubeURL);
			getIntent().removeExtra(Intent.EXTRA_TEXT);
		}
	}

	@Override
	protected void onPause() {
		if (progress != null) {
			progress.dismiss();
			progress = null;
		}
		super.onPause();
	}

	private void playYoutubeStream(String youtubeURL) {
		AsyncTask<String, Integer, String> asyncTask = new AsyncTask<String, Integer, String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progress = new ProgressDialog(BrowserActivity.this);
				progress.setTitle("Stream Youtube");
				progress.setMessage("Stream Youtube");
				progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progress.show();
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if (progress != null)
					progress.dismiss();
				if (result != null)
					Toast.makeText(BrowserActivity.this,
							"Error streaming youtuge: " + result,
							Toast.LENGTH_SHORT).show();
			}

			@Override
			protected String doInBackground(String... youtubeURL) {
				try {
					if (binder == null)
						throw new Exception("not bindet");
					if (binder.getLatestMediaServer() == null)
						throw new Exception("no mediaserver selected");
					IPlayer player = binder.getLatestMediaServer().player;
					if (player != null) {
						player.playFromYoutube(youtubeURL[0]);
					} else
						throw new Exception("no player selected");
				} catch (Exception e) {
					return e.getMessage();
				}
				return null;
			}

		};
		asyncTask.execute(youtubeURL);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		// now getIntent() should always return the last received intent
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
					binder.getMediaServerByName(mediaServerName);
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
				if (binder.getLatestMediaServer() != null) {
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
			StationStuff mediaServer = binder.getLatestMediaServer();
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
		StationStuff mediaServer = binder.getLatestMediaServer();
		mediaServer.player = mediaServer.totem;
		totemButton.setBackgroundResource(R.drawable.image_border);
		mplayerButton.setBackgroundDrawable(null);
		if (omxButton != null)
			omxButton.setBackgroundDrawable(null);
	}

	public void setMPlayer(View view) {
		StationStuff mediaServer = binder.getLatestMediaServer();
		mediaServer.player = mediaServer.mplayer;
		mplayerButton.setBackgroundResource(R.drawable.image_border);
		totemButton.setBackgroundDrawable(null);
		if (omxButton != null)
			omxButton.setBackgroundDrawable(null);
	}

	public void setOMXPlayer(View view) {
		StationStuff server = binder.getLatestMediaServer();
		server.player = server.omxplayer;
		omxButton.setBackgroundResource(R.drawable.image_border);
		totemButton.setBackgroundDrawable(null);
		mplayerButton.setBackgroundDrawable(null);
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