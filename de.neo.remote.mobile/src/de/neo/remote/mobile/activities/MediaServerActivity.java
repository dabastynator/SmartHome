package de.neo.remote.mobile.activities;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.neo.remote.mediaserver.api.IDVDPlayer;
import de.neo.remote.mediaserver.api.IImageViewer;
import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mobile.fragments.BrowserFragment;
import de.neo.remote.mobile.fragments.PlayerButtonFragment;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.BrowserLoadTask;
import de.neo.remote.mobile.tasks.PlayTatortTask;
import de.neo.remote.mobile.tasks.PlayYoutubeTask;
import de.neo.remote.mobile.util.AI;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.transceiver.AbstractReceiver.ReceiverState;
import de.remote.mobile.R;

/**
 * the browser activity shows the current directory with all folders and files.
 * It provides icons to control the music player.
 * 
 * @author sebastian
 */
public class MediaServerActivity extends AbstractConnectionActivity {

	public static final String EXTRA_MEDIA_NAME = "mediaServerName";
	public static final int FILE_REQUEST = 3;

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
	 * listener for remote actions
	 */
	protected IRemoteActionListener remoteListener;

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

	/**
	 * area that contains the download progress and cancel button
	 */
	protected LinearLayout downloadLayout;
	public PlayingBean playingBean;
	private BrowserFragment browserFragment;
	private PlayerButtonFragment buttonFragment;
	private PlayerButtonFragment buttonFragmentRight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.mediaserver_main);

		findComponents();

		if (getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey(EXTRA_MEDIA_NAME)) {
			mediaServerName = getIntent().getExtras().getString(
					EXTRA_MEDIA_NAME);
		}

		ai = new AI(this);

		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);

		// set listener

	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		if (binder == null || binder.getLatestMediaServer() == null
				|| !mediaserver.equals(binder.getLatestMediaServer().name))
			return;
		if (buttonFragment != null)
			buttonFragment.onPlayingBeanChanged(mediaserver, bean);
		if (buttonFragmentRight != null)
			buttonFragmentRight.onPlayingBeanChanged(mediaserver, bean);
		playingBean = bean;
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
		if (browserFragment != null) {
			if (browserFragment.onKeyDown(keyCode, event))
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
				if (browserFragment != null) {
					browserFragment.viewerState = ViewerState.PLAYLISTS;
					new BrowserLoadTask(this, null, false).execute();
				}
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
								MediaServerActivity.this, url, binder);
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
		dvdButton = (ImageView) findViewById(R.id.button_dvd);
		mplayerButton = (ImageView) findViewById(R.id.button_mplayer);
		totemButton = (ImageView) findViewById(R.id.button_totem);
		omxButton = (ImageView) findViewById(R.id.button_omxplayer);
		filesystemButton = (ImageView) findViewById(R.id.button_filesystem);
		playlistButton = (ImageView) findViewById(R.id.button_playlist);
		browserFragment = (BrowserFragment) getFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_browser);
		buttonFragment = (PlayerButtonFragment) getFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_button);
		buttonFragmentRight = (PlayerButtonFragment) getFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_button_right);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (browserFragment != null)
			browserFragment.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		if (browserFragment != null)
			browserFragment.onRestoreInstanceState(bundle);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (browserFragment != null)
			browserFragment.onContextItemSelected(item);
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
						new BrowserLoadTask(MediaServerActivity.this, null, false)
								.execute();
						Toast.makeText(MediaServerActivity.this,
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
		if (browserFragment != null)
			browserFragment.browserContentView
					.setAdapter(new ArrayAdapter<String>(this,
							android.R.layout.simple_list_item_1,
							new String[] {}));
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
					Toast.makeText(MediaServerActivity.this, exeption.getMessage(),
							Toast.LENGTH_SHORT).show();
				if (binder.getLatestMediaServer() != null) {
					new BrowserLoadTask(MediaServerActivity.this, null, false)
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
			new BrowserLoadTask(MediaServerActivity.this, null, false).execute();
			if (binder.getReceiver() != null
					&& binder.getReceiver().getState() == ReceiverState.LOADING) {
				downloadLayout.setVisibility(View.VISIBLE);
				downloadProgress.setProgress((int) ((100d * binder
						.getReceiver().getDownloadProgress()) / binder
						.getReceiver().getFullSize()));
			}
		}
	}

	public void showFileSystem(View view) {
		if (browserFragment != null) {
			browserFragment.viewerState = ViewerState.DIRECTORIES;
			new BrowserLoadTask(this, null, false).execute();
		}
	}

	public void showPlaylist(View view) {
		if (browserFragment != null) {
			browserFragment.viewerState = ViewerState.PLAYLISTS;
			new BrowserLoadTask(this, null, false).execute();
		}
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

}