package de.neo.remote.mobile.activities;

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.fragments.PlayerButtonFragment;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.tasks.PlayListTask;
import de.neo.remote.mobile.tasks.PlayYoutubeTask;
import de.neo.remote.mobile.util.BrowserPageAdapter;
import de.neo.remote.mobile.util.BrowserPageAdapter.BrowserFragment;
import de.neo.rmi.transceiver.AbstractReceiver;
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

	public static final String LISTVIEW_POSITION = "listviewPosition";
	public static final String SPINNER_POSITION = "spinnerPosition";
	public static final String VIEWER_STATE = "viewerstate";
	public static final String MEDIA_STATE = "mediastate";
	public static final String PLAYLIST = "playlist";

	/**
	 * viewer states of the browser
	 * 
	 * @author sebastian
	 */
	public enum ViewerState {
		DIRECTORIES, PLAYLISTS, PLS_ITEMS
	}

	protected IRemoteActionListener mRemoteListener;
	public ImageView mMplayerButton;
	public ImageView mTotemButton;
	public ImageView mFilesystemButton;
	public ImageView mPlaylistButton;
	protected String mMediaServerID;
	public ImageView mOmxButton;
	protected TextView mDownloadText;
	public PlayingBean mPlayingBean;
	private PlayerButtonFragment mButtonFragment;
	private PlayerButtonFragment mButtonFragmentRight;
	private ViewPager mListPager;
	private BrowserPageAdapter mBrowserPageAdapter;

	protected LinearLayout mDownloadLayout;
	protected ProgressBar mDownloadProgress;
	private long mMaxDonwloadSize = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		mBrowserPageAdapter.onPlayingBeanChanged(mediaserver, bean);
		mPlayingBean = bean;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.browser_pref, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// super.onCreateContextMenu(menu, v, menuInfo);
		Fragment fragment = mBrowserPageAdapter.getItem(mListPager
				.getCurrentItem());
		if (fragment != null)
			fragment.onCreateContextMenu(menu, v, menuInfo);
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
		BrowserFragment fragment = mBrowserPageAdapter.getItem(mListPager
				.getCurrentItem());
		if (fragment != null && fragment.onKeyDown(keyCode, event))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		unbindService(mPlayerConnection);
		super.onDestroy();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (mBrowserPageAdapter.onContextItemSelected(item,
				mListPager.getCurrentItem()))
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
			case R.id.opt_refresh:
				setStation(mediaServer);
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

	public void setStation(StationStuff station) {
		mBrowserPageAdapter.setStation(station);
		updateFilePlsButtons();
		updatePlayerButtons();
		if (station != null)
			setTitle(station.name);
		else
			setTitle(getString(R.string.mediaserver_no_server));
	}

	private void updatePlayerButtons() {
		StationStuff server = mBinder.getLatestMediaServer();
		if (server != null && server.player == server.mplayer)
			mMplayerButton.setBackgroundResource(R.drawable.image_border);
		else
			mMplayerButton.setBackgroundResource(0);
		if (server != null && server.player == server.omxplayer)
			mOmxButton.setBackgroundResource(R.drawable.image_border);
		else
			mOmxButton.setBackgroundResource(0);
		if (server != null && server.player == server.totem)
			mTotemButton.setBackgroundResource(R.drawable.image_border);
		else
			mTotemButton.setBackgroundResource(0);
	}

	public void showView(ViewerState state) {
		if (state == ViewerState.DIRECTORIES)
			mListPager.setCurrentItem(0);
		if (state == ViewerState.PLAYLISTS)
			mListPager.setCurrentItem(1);
		updateFilePlsButtons();
	}

	/**
	 * find components by their id
	 */
	private void findComponents() {
		mMplayerButton = (ImageView) findViewById(R.id.button_mplayer);
		mTotemButton = (ImageView) findViewById(R.id.button_totem);
		mOmxButton = (ImageView) findViewById(R.id.button_omxplayer);
		mFilesystemButton = (ImageView) findViewById(R.id.button_filesystem);
		mPlaylistButton = (ImageView) findViewById(R.id.button_playlist);
		mButtonFragment = (PlayerButtonFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_button);
		mButtonFragmentRight = (PlayerButtonFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_button_right);

		mListPager = (ViewPager) findViewById(R.id.media_pager);
		mListPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				updateFilePlsButtons();
			}
		});
		if (mListPager.getAdapter() == null || mBrowserPageAdapter == null) {
			mBrowserPageAdapter = new BrowserPageAdapter(
					getSupportFragmentManager());
			if (mBinder != null)
				mBrowserPageAdapter.setStation(mBinder.getLatestMediaServer());
			mListPager.setAdapter(mBrowserPageAdapter);
		}
		mDownloadLayout = (LinearLayout) findViewById(R.id.layout_download);
		mDownloadProgress = (ProgressBar) findViewById(R.id.prg_donwload);
		mDownloadText = (TextView) findViewById(R.id.lbl_download);
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
		setTitle(getString(R.string.connecting));
		setProgressBarVisibility(true);
	};

	@Override
	protected void onResume() {
		super.onResume();
		checkIntentForAction();
	}

	private void checkIntentForAction() {
		Bundle extras = getIntent().getExtras();
		if (extras != null && mBinder != null) {
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
		AsyncTask<String, Integer, StationStuff> task = new AsyncTask<String, Integer, StationStuff>() {
			private Exception error;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				setProgressBarVisibility(true);
				setTitle(getString(R.string.mediaserver_load));
			}

			@Override
			protected StationStuff doInBackground(String... params) {
				try {
					if (mMediaServerID == null)
						return mBinder.getLatestMediaServer();
					else
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
							error).show();
				else
					setStation(station);
			}

		};
		if (mIsActive)
			task.execute();
	}

	public void showFileSystem(View view) {
		showView(ViewerState.DIRECTORIES);
	}

	public void showPlaylist(View view) {
		showView(ViewerState.PLAYLISTS);
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

	private void updateFilePlsButtons() {
		StationStuff mediaServer = mBinder.getLatestMediaServer();
		if (mediaServer != null && mListPager.getCurrentItem() == 0)
			mFilesystemButton.setBackgroundResource(R.drawable.image_border);
		else
			mFilesystemButton.setBackgroundResource(0);
		if (mediaServer != null && mListPager.getCurrentItem() == 1)
			mPlaylistButton.setBackgroundResource(R.drawable.image_border);
		else
			mPlaylistButton.setBackgroundResource(0);
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
		mMaxDonwloadSize = size;
		mDownloadLayout.setVisibility(View.VISIBLE);
		mDownloadProgress.setProgress(0);
		mDownloadText.setText(getString(R.string.str_upload));
	}

	@Override
	public void progressSending(long size) {
		super.progressSending(size);
		mDownloadLayout.setVisibility(View.VISIBLE);
		if (mMaxDonwloadSize == 0)
			mMaxDonwloadSize = mBinder.getReceiver().getFullSize();
		mDownloadProgress.setProgress((int) ((100d * size) / mMaxDonwloadSize));
	}

	@Override
	public void endSending(long size) {
		super.endSending(size);
		mDownloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void sendingCanceled() {
		super.sendingCanceled();
		mDownloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void startReceive(long size, String file) {
		super.startReceive(size, file);
		mMaxDonwloadSize = size;
		mDownloadLayout.setVisibility(View.VISIBLE);
		mDownloadProgress.setProgress(0);
		if (file != null)
			mDownloadText.setText(file);
		else
			mDownloadText.setText(getString(R.string.str_download));
	}

	@Override
	public void progressReceive(long size, String file) {
		super.progressReceive(size, file);
		mDownloadLayout.setVisibility(View.VISIBLE);
		if (mMaxDonwloadSize == 0)
			mMaxDonwloadSize = mBinder.getReceiver().getFullSize();
		mDownloadProgress.setProgress((int) ((100d * size) / mMaxDonwloadSize));
		if (file != null)
			mDownloadText.setText(file);
	}

	@Override
	public void endReceive(long size) {
		super.endReceive(size);
		mDownloadLayout.setVisibility(View.GONE);
	}

	@Override
	public void exceptionOccurred(Exception e) {
		super.exceptionOccurred(e);
		mDownloadLayout.setVisibility(View.GONE);
		new AbstractTask.ErrorDialog(this, e).show();
	}

	@Override
	public void downloadCanceled() {
		super.downloadCanceled();
		mDownloadLayout.setVisibility(View.GONE);
	}

	public void cancelDownload(View v) {
		AbstractReceiver receiver = mBinder.getReceiver();
		mDownloadLayout.setVisibility(View.GONE);
		if (receiver != null) {
			receiver.cancel();
		} else {
			Toast.makeText(this, "no receiver available", Toast.LENGTH_SHORT)
					.show();
			mDownloadLayout.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		final StationStuff mediaServer = mBinder.getLatestMediaServer();
		if (data == null)
			return;
		if (requestCode == MediaServerActivity.FILE_REQUEST) {
			Uri uri = data.getData();
			mBinder.uploadFile(mediaServer.browser, new File(
					getFilePathByUri(uri)));
		}
	}

	@Override
	void onRemoteBinder(RemoteBinder mBinder) {
		checkIntentForAction();
	}

	public void refreshContent() {
		BrowserFragment fragment = mBrowserPageAdapter.getItem(mListPager
				.getCurrentItem());

		if (fragment != null)
			fragment.refreshContent(this);
	}
}