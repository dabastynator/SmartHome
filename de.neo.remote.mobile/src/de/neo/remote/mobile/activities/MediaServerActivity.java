package de.neo.remote.mobile.activities;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.neo.remote.api.IDVDPlayer;
import de.neo.remote.api.IPlayer;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.fragments.BrowserFragment;
import de.neo.remote.mobile.fragments.PlayerButtonFragment;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.tasks.PlayListTask;
import de.neo.remote.mobile.tasks.PlayYoutubeTask;
import de.remote.mobile.R;

/**
 * the browser activity shows the current directory with all folders and files.
 * It provides icons to control the music player.
 * 
 * @author sebastian
 */
public class MediaServerActivity extends AbstractConnectionActivity {

	public static final String EXTRA_MEDIA_ID = "media_server_id";
	public static final int FILE_REQUEST = 3;

	/**
	 * viewer states of the browser
	 * 
	 * @author sebastian
	 */
	public enum ViewerState {
		DIRECTORIES, PLAYLISTS, PLS_ITEMS
	}

	protected IRemoteActionListener mRemoteListener;
	protected ProgressBar mDownloadProgress;
	public ImageView mMplayerButton;
	public ImageView mTotemButton;
	public ImageView mFilesystemButton;
	public ImageView mPlaylistButton;
	protected String mMediaServerID;
	protected LinearLayout mDvdLayout;
	protected ImageView mDvdButton;
	public ImageView mOmxButton;
	protected TextView mDownloadText;
	protected LinearLayout mDownloadLayout;
	public PlayingBean mPlayingBean;
	private BrowserFragment mBrowserFragment;
	private PlayerButtonFragment mButtonFragment;
	private PlayerButtonFragment mButtonFragmentRight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mediaserver_main);

		findComponents();

		if (getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey(EXTRA_MEDIA_ID)) {
			mMediaServerID = getIntent().getExtras().getString(EXTRA_MEDIA_ID);
		}
	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		if (mBinder == null || mBinder.getLatestMediaServer() == null
				|| !mediaserver.equals(mBinder.getLatestMediaServer().name))
			return;
		if (mButtonFragment != null)
			mButtonFragment.onPlayingBeanChanged(mediaserver, bean);
		if (mButtonFragmentRight != null)
			mButtonFragmentRight.onPlayingBeanChanged(mediaserver, bean);
		if (mBrowserFragment != null)
			mBrowserFragment.onPlayingBeanChanged(mediaserver, bean);
		mPlayingBean = bean;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.browser_pref, menu);
		return true;
	}

	public void disableScreen() {
		setTitle(getResources().getString(R.string.no_conneciton));
		setProgressBarVisibility(false);
	}

	/**
	 * update gui elements, show current directory or playlist
	 * 
	 * @param gotoPath
	 */

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final StationStuff mediaServer = mBinder.getLatestMediaServer();
		if (mBinder == null)
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
		if (mBrowserFragment != null) {
			if (mBrowserFragment.onKeyDown(keyCode, event))
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void showDVDBar(View v) {
		IPlayer player = mBinder.getLatestMediaServer().player;
		if (mDvdLayout.getVisibility() == View.VISIBLE) {
			mDvdLayout.setVisibility(View.GONE);
			mDvdButton.setBackgroundResource(0);
		} else {
			if (player instanceof IDVDPlayer) {
				try {
					((IDVDPlayer) player).playDVD();
					mDvdLayout.setVisibility(View.VISIBLE);
					mDvdButton.setBackgroundResource(R.drawable.image_border);
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
		unbindService(mPlayerConnection);
		super.onDestroy();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (mBrowserFragment.onContextItemSelected(item))
			return true;
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		StationStuff mediaServer = mBinder.getLatestMediaServer();
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
			case R.id.opt_refresh:
				// mBinder.getMediaServerByID(mediaServerID);
				// new BrowserLoadTask(this, null, false).execute();
				break;
			case R.id.opt_upload:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(
						Intent.createChooser(intent, "File Chooser"),
						FILE_REQUEST);
				break;
			case R.id.opt_create_playlist:
				new PlayListTask(this, mediaServer).createPlaylist();
				break;
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * find components by their id
	 */
	private void findComponents() {
		mDvdButton = (ImageView) findViewById(R.id.button_dvd);
		mMplayerButton = (ImageView) findViewById(R.id.button_mplayer);
		mTotemButton = (ImageView) findViewById(R.id.button_totem);
		mOmxButton = (ImageView) findViewById(R.id.button_omxplayer);
		mFilesystemButton = (ImageView) findViewById(R.id.button_filesystem);
		mPlaylistButton = (ImageView) findViewById(R.id.button_playlist);
		mBrowserFragment = (BrowserFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_browser);
		mButtonFragment = (PlayerButtonFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_button);
		mButtonFragmentRight = (PlayerButtonFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_button_right);
		mDvdLayout = (LinearLayout) findViewById(R.id.layout_dvd_bar);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mBrowserFragment != null)
			mBrowserFragment.onSaveInstanceState(outState);
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
		setTitle(getResources().getString(R.string.connecting));
		setProgressBarVisibility(true);
	};

	@Override
	protected void onResume() {
		super.onResume();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String youtubeURL = extras.getString(Intent.EXTRA_TEXT);
			if (youtubeURL != null) {
				new PlayYoutubeTask(this, youtubeURL, mBinder)
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

	@Override
	public void onServerConnectionChanged(RemoteServer server) {

		new AsyncTask<String, Integer, StationStuff>() {
			private Exception error;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				setProgressBarVisibility(true);
				setTitle(getResources().getString(R.string.mediaserver_load));
			}

			@Override
			protected StationStuff doInBackground(String... params) {
				try {
					return mBinder.getMediaServerByID(mMediaServerID);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(StationStuff station) {
				if (error != null)
					new AbstractTask.ErrorDialog(MediaServerActivity.this,
							error);
				else
					mBrowserFragment.setStation(station);
			}

		}.execute();
	}

	public void showFileSystem(View view) {
		mBrowserFragment.showView(ViewerState.DIRECTORIES);
	}

	public void showPlaylist(View view) {
		mBrowserFragment.showView(ViewerState.PLAYLISTS);
	}

	public void setTotem(View view) {
		StationStuff mediaServer = mBinder.getLatestMediaServer();
		if (mediaServer != null) {
			mediaServer.player = mediaServer.totem;
			mTotemButton.setBackgroundResource(R.drawable.image_border);
			mMplayerButton.setBackgroundResource(0);
			if (mOmxButton != null)
				mOmxButton.setBackgroundResource(0);
		}
	}

	public void setMPlayer(View view) {
		StationStuff mediaServer = mBinder.getLatestMediaServer();
		if (mediaServer != null) {
			mediaServer.player = mediaServer.mplayer;
			mMplayerButton.setBackgroundResource(R.drawable.image_border);
			mTotemButton.setBackgroundResource(0);
			if (mOmxButton != null)
				mOmxButton.setBackgroundResource(0);
		}
	}

	public void setOMXPlayer(View view) {
		StationStuff server = mBinder.getLatestMediaServer();
		if (server != null) {
			server.player = server.omxplayer;
			mOmxButton.setBackgroundResource(R.drawable.image_border);
			mTotemButton.setBackgroundResource(0);
			mMplayerButton.setBackgroundResource(0);
		}
	}

	@Override
	public void startSending(long size) {
		super.startSending(size);
		if (mBrowserFragment != null)
			mBrowserFragment.startSending(size);
	}

	@Override
	public void progressSending(long size) {
		super.progressSending(size);
		if (mBrowserFragment != null)
			mBrowserFragment.progressSending(size);
	}

	@Override
	public void endSending(long size) {
		super.endSending(size);
		if (mBrowserFragment != null)
			mBrowserFragment.endSending(size);
	}

	@Override
	public void sendingCanceled() {
		super.sendingCanceled();
		if (mBrowserFragment != null)
			mBrowserFragment.sendingCanceled();
	}

	@Override
	public void progressReceive(long size, String file) {
		super.progressReceive(size, file);
		if (mBrowserFragment != null)
			mBrowserFragment.progressReceive(size, file);
	}

	@Override
	public void endReceive(long size) {
		super.endReceive(size);
		if (mBrowserFragment != null)
			mBrowserFragment.endReceive(size);
	}

	@Override
	public void exceptionOccurred(Exception e) {
		super.exceptionOccurred(e);
		if (mBrowserFragment != null)
			mBrowserFragment.exceptionOccurred(e);
	}

	@Override
	public void downloadCanceled() {
		super.downloadCanceled();
		if (mBrowserFragment != null)
			mBrowserFragment.downloadCanceled();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mBrowserFragment != null)
			mBrowserFragment.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	void onRemoteBinder(RemoteBinder mBinder) {
		// mBrowserFragment.onRemoteBinder(mBinder);
		// if (mBinder != null && )
	}
}