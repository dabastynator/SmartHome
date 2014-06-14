package de.neo.remote.mobile.activities;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.neo.remote.mediaserver.api.IDVDPlayer;
import de.neo.remote.mediaserver.api.IImageViewer;
import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mediaserver.api.PlayerException;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mediaserver.api.PlayingBean.STATE;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.BrowserLoadTask;
import de.neo.remote.mobile.tasks.PlayItemTask;
import de.neo.remote.mobile.tasks.PlayTatortTask;
import de.neo.remote.mobile.tasks.PlayYoutubeTask;
import de.neo.remote.mobile.util.AI;
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
public class BrowserActivity extends BindedActivity {

	public static final String EXTRA_MEDIA_NAME = "mediaServerName";
	public static final String VIEWER_STATE = "viewerstate";
	public static final String MEDIA_STATE = "mediastate";
	public static final String PLAYLIST = "playlist";
	public static final int FILE_REQUEST = 3;
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
	 * remote media state, either playing music / video or showing images.
	 * 
	 * @author sebastian
	 */
	public enum MediaState {
		MUSIC_VIDEO, IMAGES
	}

	/**
	 * list view
	 */
	public ListView listView;

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
	public Map<String, String> plsFileMap = new HashMap<String, String>();

	/**
	 * area that contains the search field and button
	 */
	protected LinearLayout searchLayout;

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

	public ImageView mplayerButton;

	public ImageView totemButton;

	public ImageView filesystemButton;

	public ImageView playlistButton;

	/**
	 * Name of current music server
	 */
	protected String mediaServerName;

	protected LinearLayout dvdLayout;

	protected ImageView dvdButton;

	public ImageView omxButton;

	protected TextView downloadText;
	
	private boolean isFullscreen;
	
	private long maxDonwloadSize = 0;
	
	/**
	 * area that contains the download progress and cancel button
	 */
	protected LinearLayout downloadLayout;
	public PlayingBean playingBean;

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

		if (getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey(EXTRA_MEDIA_NAME)) {
			mediaServerName = getIntent().getExtras().getString(
					EXTRA_MEDIA_NAME);
		}

		ai = new AI(this);

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
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		if (binder == null || binder.getLatestMediaServer() == null
				|| !mediaserver.equals(binder.getLatestMediaServer().name))
			return;
		if (bean == null || bean.getState() == STATE.PLAY)
			playButton.setImageResource(R.drawable.pause);
		else if (bean.getState() == STATE.PAUSE)
			playButton.setImageResource(R.drawable.play);
		playingBean = bean;
		if (listView != null && listView.getAdapter() instanceof BrowserAdapter) {
			BrowserAdapter adapter = (BrowserAdapter) listView.getAdapter();
			adapter.setPlayingFile(bean);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.browser_pref, menu);
		return true;
	}

	public void disableScreen() {
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final StationStuff mediaServer = binder.getLatestMediaServer();
		if (binder == null)
			return super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (searchLayout.getVisibility() == View.VISIBLE) {
				searchLayout.setVisibility(View.GONE);
				searchText.setText("");
				return true;
			}
			selectedPosition = 0;
			if (viewerState == ViewerState.DIRECTORIES) {
				new BrowserLoadTask(this, null, true).execute();
				return true;
			}
			if (viewerState == ViewerState.PLAYLISTS) {
				viewerState = ViewerState.DIRECTORIES;
				new BrowserLoadTask(this, null, false).execute();
				return true;
			}
			if (viewerState == ViewerState.PLS_ITEMS) {
				viewerState = ViewerState.PLAYLISTS;
				new BrowserLoadTask(this, null, false).execute();
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
			new Thread() {
				public void run() {
					try {
						mediaServer.player.volDown();
					} catch (Exception e) {
					}
				};
			}.start();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			new Thread() {
				public void run() {
					try {
						mediaServer.player.volUp();
					} catch (Exception e) {
					}
				};
			}.start();
			return true;
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

	public void playerAction(final View v) {
		if (binder == null || binder.getLatestMediaServer() == null)
			return;
		final IPlayer player = binder.getLatestMediaServer().player;
		AsyncTask<String, Integer, String> task = new AsyncTask<String, Integer, String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				if (v != null)
					v.setBackgroundResource(R.drawable.image_border);
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if (v != null)
					v.setBackgroundDrawable(null);
				if (result != null)
					Toast.makeText(BrowserActivity.this, result,
							Toast.LENGTH_SHORT).show();
			}

			@Override
			protected String doInBackground(String... params) {
				try {
					switch (v.getId()) {
					case R.id.button_play:
						if (mediaState == MediaState.MUSIC_VIDEO)
							player.playPause();
						else
							binder.getLatestMediaServer().imageViewer
									.toggleDiashow(3 * 1000);
						break;
					case R.id.button_next:
						if (mediaState == MediaState.MUSIC_VIDEO)
							player.next();
						else
							binder.getLatestMediaServer().imageViewer.next();
						break;
					case R.id.button_pref:
						if (mediaState == MediaState.MUSIC_VIDEO)
							player.previous();
						else
							binder.getLatestMediaServer().imageViewer
									.previous();
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
						if (mediaState == MediaState.MUSIC_VIDEO) {
							player.quit();
						} else {
							mediaState = MediaState.MUSIC_VIDEO;
							binder.getLatestMediaServer().imageViewer.quit();
						}
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
					return e.getClass().getSimpleName() + ": " + e.getMessage();
				}
				return null;
			}
		};
		task.execute(new String[] {});
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
			case R.id.opt_search:
				searchLayout.setVisibility(View.VISIBLE);
				searchText.requestFocus();
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT);
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
				new BrowserLoadTask(this, null, false).execute();
				break;
			case R.id.opt_refresh:
				binder.getMediaServerByName(mediaServerName);
				new BrowserLoadTask(this, null, false).execute();
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
			case R.id.opt_tatort:
				askForStream();
				break;

			case R.id.opt_create_playlist:
				createNewPlaylist();
				break;
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void askForStream() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Get stream");
		alert.setMessage("Input the stream url");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(getResources().getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String url = input.getText().toString();
						PlayTatortTask task = new PlayTatortTask(
								BrowserActivity.this, url, binder);
						task.execute(new String[] {});
					}
				});

		alert.setNegativeButton(
				getResources().getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		alert.show();
	}
	
	/**
	 * find components by their id
	 */
	private void findComponents() {
		listView = (ListView) findViewById(R.id.fileList);
		searchText = (EditText) findViewById(R.id.txt_search);
		searchLayout = (LinearLayout) findViewById(R.id.layout_search);
		downloadLayout = (LinearLayout) findViewById(R.id.layout_download);
		dvdLayout = (LinearLayout) findViewById(R.id.layout_dvd_bar);
		dvdButton = (ImageView) findViewById(R.id.button_dvd);
		downloadProgress = (ProgressBar) findViewById(R.id.prg_donwload);
		downloadText = (TextView) findViewById(R.id.lbl_download);
		playButton = (ImageView) findViewById(R.id.button_play);
		mplayerButton = (ImageView) findViewById(R.id.button_mplayer);
		totemButton = (ImageView) findViewById(R.id.button_totem);
		omxButton = (ImageView) findViewById(R.id.button_omxplayer);
		filesystemButton = (ImageView) findViewById(R.id.button_filesystem);
		playlistButton = (ImageView) findViewById(R.id.button_playlist);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(VIEWER_STATE, viewerState.ordinal());
		outState.putInt(MEDIA_STATE, mediaState.ordinal());
		outState.putString(PLAYLIST, currentPlayList);
		outState.putInt(LISTVIEW_POSITION, listView.getFirstVisiblePosition());
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		viewerState = ViewerState.values()[bundle.getInt(VIEWER_STATE)];
		mediaState = MediaState.values()[bundle.getInt(MEDIA_STATE)];
		currentPlayList = bundle.getString(PLAYLIST);
		selectedPosition = bundle.getInt(LISTVIEW_POSITION);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final StationStuff mediaServer = binder.getLatestMediaServer();
		try {
			switch (item.getItemId()) {
			case R.id.opt_item_play:
				new PlayItemTask(this, selectedItem, binder)
						.execute(new String[] {});
				Toast.makeText(BrowserActivity.this, "Ordner abspielen",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.opt_item_addplaylist:
				new Thread() {
					public void run() {
						try {
							Intent i = new Intent(BrowserActivity.this,
									SelectPlaylistActivity.class);
							i.putExtra(SelectPlaylistActivity.PLS_LIST,
									mediaServer.pls.getPlayLists());
							startActivityForResult(i,
									SelectPlaylistActivity.SELECT_PLS_CODE);
						} catch (RemoteException e) {
						}
					};
				}.start();
				break;
			case R.id.opt_item_download:
				if (viewerState == ViewerState.PLAYLISTS) {
					binder.downloadPlaylist(mediaServer.browser,
							mediaServer.pls.listContent(selectedItem),
							selectedItem);
				} else if (selectedPosition < mediaServer.browser
						.getDirectories().length)
					binder.downloadDirectory(mediaServer.browser, selectedItem);
				else
					binder.downloadFile(mediaServer.browser, selectedItem);
				break;
			case R.id.opt_item_delete:
				selectedPosition = listView.getFirstVisiblePosition();
				new Thread() {
					public void run() {
						try {
							mediaServer.browser.delete(mediaServer.browser
									.getFullLocation() + selectedItem);
						} catch (RemoteException e) {
						}
					};
				}.start();
				((BufferBrowser) mediaServer.browser).setDirty();
				new BrowserLoadTask(this, null, false).execute();
				break;
			case R.id.opt_pls_delete:
				new Thread() {
					public void run() {
						try {
							mediaServer.pls.removePlayList(selectedItem);
						} catch (RemoteException e) {
						}
					};
				}.start();
				new BrowserLoadTask(this, null, false).execute();
				Toast.makeText(BrowserActivity.this,
						"Playlist '" + selectedItem + "' deleted",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.opt_pls_show:
				viewerState = ViewerState.PLS_ITEMS;
				currentPlayList = selectedItem;
				new BrowserLoadTask(this, null, false).execute();
				break;
			case R.id.opt_pls_item_delete:
				new Thread() {
					public void run() {
						try {
							mediaServer.pls.removeItem(currentPlayList,
									selectedItem);
						} catch (Exception e) {
						}
					};
				}.start();
				new BrowserLoadTask(this, null, false).execute();
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

	private void createNewPlaylist() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("New playlist");
		alert.setMessage("Input new playlist name");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		if (binder == null)
			return;
		final StationStuff mediaServer = binder.getLatestMediaServer();
		if (mediaServer == null)
			return;

		alert.setPositiveButton(getResources().getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						final String pls = input.getText().toString();
						new Thread() {
							public void run() {
								try {
									mediaServer.pls.addPlayList(pls);
								} catch (RemoteException e) {
								}
							};
						}.start();
						new BrowserLoadTask(BrowserActivity.this, null, false)
								.execute();
						Toast.makeText(BrowserActivity.this,
								"playlist '" + pls + "' added",
								Toast.LENGTH_SHORT).show();
					}
				});

		alert.setNegativeButton(
				getResources().getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		alert.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		final StationStuff mediaServer = binder.getLatestMediaServer();
		if (data == null)
			return;
		if (requestCode == SelectPlaylistActivity.SELECT_PLS_CODE) {
			if (data.getExtras() == null)
				return;
			final String pls = data.getExtras().getString(
					SelectPlaylistActivity.RESULT);
			new Thread() {
				public void run() {
					try {
						mediaServer.pls.extendPlayList(pls,
								mediaServer.browser.getFullLocation()
										+ selectedItem);
					} catch (Exception e) {
					}
				};
			}.start();
			Toast.makeText(BrowserActivity.this, selectedItem + " added",
					Toast.LENGTH_SHORT).show();
		}
		if (requestCode == FILE_REQUEST) {
			Uri uri = data.getData();
			binder.uploadFile(mediaServer.browser, new File(
					getFilePathByUri(uri)));
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
		if (binder.getLatestMediaServer() != null
				&& !binder.getLatestMediaServer().name.equals(mediaServerName)) {
			new BrowserLoadTask(this, null, false).execute();
		}
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String youtubeURL = extras.getString(Intent.EXTRA_TEXT);
			if (youtubeURL != null) {
				new PlayYoutubeTask(this, youtubeURL, binder)
						.execute(new String[] {});
				getIntent().removeExtra(Intent.EXTRA_TEXT);
			}
		}
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
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String youtubeURL = extras.getString(Intent.EXTRA_TEXT);
			if (youtubeURL != null) {
				new PlayYoutubeTask(this, youtubeURL, binder)
						.execute(new String[] {});
				getIntent().removeExtra(Intent.EXTRA_TEXT);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		// now getIntent() should always return the last received intent
	}

	public static boolean isImage(String item) {
		item = item.toLowerCase(Locale.US);
		for (String extension : IImageViewer.IMAGE_EXTENSIONS)
			if (item.endsWith(extension))
				return true;
		return false;
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
					new BrowserLoadTask(BrowserActivity.this, null, false)
							.execute();
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
			new BrowserLoadTask(BrowserActivity.this, null, false).execute();
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
			if (viewerState == ViewerState.DIRECTORIES) {
				try {
					if (position < mediaServer.browser.getDirectories().length) {
						selectedPosition = 0;
						new BrowserLoadTask(BrowserActivity.this, item, false)
								.execute();
						return;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			if (viewerState == ViewerState.PLS_ITEMS)
				item = plsFileMap.get(item);
			PlayItemTask task = new PlayItemTask(BrowserActivity.this, item,
					binder);
			task.execute(new String[] {});
		}
	}

	public void showFileSystem(View view) {
		viewerState = ViewerState.DIRECTORIES;
		new BrowserLoadTask(this, null, false).execute();
	}

	public void showPlaylist(View view) {
		viewerState = ViewerState.PLAYLISTS;
		new BrowserLoadTask(this, null, false).execute();
	}
	
	@Override
	public void startReceive(long size, String file) {
		maxDonwloadSize = size;
		downloadLayout.setVisibility(View.VISIBLE);
		downloadProgress.setProgress(0);
		if (file != null)
			downloadText.setText(file);
	}

	@Override
	public void startSending(long size) {
		maxDonwloadSize = size;
		downloadLayout.setVisibility(View.VISIBLE);
		downloadProgress.setProgress(0);
	}

	@Override
	public void progressReceive(long size, String file) {
		downloadLayout.setVisibility(View.VISIBLE);
		if (maxDonwloadSize == 0)
			maxDonwloadSize = binder.getReceiver().getFullSize();
		downloadProgress.setProgress((int) ((100d * size) / maxDonwloadSize));
		if (file != null)
			downloadText.setText(file);
	}

	@Override
	public void progressSending(long size) {
		downloadLayout.setVisibility(View.VISIBLE);
		if (maxDonwloadSize == 0)
			maxDonwloadSize = binder.getReceiver().getFullSize();
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

	@Override
	public void exceptionOccurred(Exception e) {
		downloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void downloadCanceled() {
		downloadLayout.setVisibility(View.GONE);
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