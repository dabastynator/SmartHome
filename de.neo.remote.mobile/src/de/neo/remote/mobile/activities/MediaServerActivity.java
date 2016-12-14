package de.neo.remote.mobile.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
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
import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoException;
import de.neo.android.persistence.DaoFactory;
import de.neo.remote.api.IWebMediaServer.BeanMediaServer;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.fragments.PlayerButtonFragment;
import de.neo.remote.mobile.persistence.MediaServerState;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.tasks.PlayListTask;
import de.neo.remote.mobile.tasks.PlayYoutubeTask;
import de.neo.remote.mobile.util.BrowserPageAdapter;
import de.neo.remote.mobile.util.BrowserPageAdapter.BrowserFragment;
import de.neo.remote.mobile.util.VolumeDialogBuilder;
import de.neo.rmi.protokol.RemoteException;
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
	private MediaServerState mMediaServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mediaserver_main);

		findComponents();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(EXTRA_MEDIA_ID)) {
			mMediaServer = createMediaServerForId(getIntent().getExtras().getString(EXTRA_MEDIA_ID));
			mMediaServer.initialize(mWebMediaServer);
			updatePlayerButtons();
			mBrowserPageAdapter.setMediaServer(mMediaServer);
			refreshTitle();
		} else {
			setTitle(getString(R.string.mediaserver_no_server));
			mMediaServer = null;
		}
	}

	public MediaServerState getMediaServerState() {
		return mMediaServer;
	}

	public static MediaServerState createMediaServerForId(String remoteID) {
		Dao<MediaServerState> dao = DaoFactory.getInstance().getDao(MediaServerState.class);
		try {
			for (MediaServerState ms : dao.loadAll())
				if (ms.getMediaServerID().equals(remoteID)) {
					return ms;
				}
			MediaServerState ms = new MediaServerState();
			ms.setBrowserLocation("");
			ms.setMediaServerID(remoteID);
			ms.setPlayer("mplayer");
			dao.save(ms);
			return ms;
		} catch (DaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.browser_pref, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// super.onCreateContextMenu(menu, v, menuInfo);
		Fragment fragment = mBrowserPageAdapter.getItem(mListPager.getCurrentItem());
		if (fragment != null)
			fragment.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			VolumeDialogBuilder dialog = new VolumeDialogBuilder(this, mMediaServer);
			dialog.changeVolume(-1);
			dialog.show();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			VolumeDialogBuilder dialog = new VolumeDialogBuilder(this, mMediaServer);
			dialog.changeVolume(1);
			dialog.show();
			return true;
		}

		BrowserFragment fragment = mBrowserPageAdapter.getItem(mListPager.getCurrentItem());
		if (fragment != null && fragment.onKeyDown(keyCode, event))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (mBrowserPageAdapter.onContextItemSelected(item, mListPager.getCurrentItem()))
			return true;
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.opt_mplayer:
				setMPlayer(null);
				break;
			case R.id.opt_totem:
				setTotem(null);
				break;
			case R.id.opt_light_off:
				// TODO
				// mediaServer.control.displayDark();
				break;
			case R.id.opt_light_on:
				// TODO
				// mediaServer.control.displayBride();
				break;
			case R.id.opt_shutdown:
				// TODO
				// mediaServer.control.shutdown();
				break;
			case R.id.opt_audiotrack:
				// TODO
				// mediaServer.player.nextAudio();
				break;
			case R.id.opt_left:
				// TODO
				// mediaServer.player.moveLeft();
				break;
			case R.id.opt_right:
				// TODO
				// mediaServer.player.moveRight();
				break;
			case R.id.opt_refresh:
				refreshContent();
				break;
			case R.id.opt_upload:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILE_REQUEST);
				break;
			case R.id.opt_create_playlist:
				new PlayListTask(this, mMediaServer).createPlaylist();
				break;
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return super.onOptionsItemSelected(item);
	}

	private void updatePlayerButtons() {
		if ("mplayer".equals(mMediaServer.getPlayer()))
			mMplayerButton.setBackgroundResource(R.drawable.image_border);
		else
			mMplayerButton.setBackgroundResource(0);
		if ("omxplayer".equals(mMediaServer.getPlayer()))
			mOmxButton.setBackgroundResource(R.drawable.image_border);
		else
			mOmxButton.setBackgroundResource(0);
		if ("totem".equals(mMediaServer.getPlayer()))
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
			mBrowserPageAdapter = new BrowserPageAdapter(getSupportFragmentManager());
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
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			if (cursor.moveToFirst()) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
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

	private void refreshTitle() {
		new AsyncTask<String, Integer, String>() {

			protected void onPreExecute() {
				setTitle(getString(R.string.connecting));
				setProgressBarVisibility(true);
			};

			@Override
			protected String doInBackground(String... params) {
				try {
					ArrayList<BeanMediaServer> mediaServer = mWebMediaServer
							.getMediaServer(mMediaServer.getMediaServerID());
					if (mediaServer.size() > 0)
						return mediaServer.get(0).getName();
				} catch (RemoteException e) {
					return getString(R.string.error_load_server);
				}
				return getString(R.string.mediaserver_no_server);
			}

			protected void onPostExecute(String result) {
				setTitle(result);
				setProgressBarVisibility(false);
			};

		}.execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkIntentForAction();
	}

	private void checkIntentForAction() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String youtubeURL = extras.getString(Intent.EXTRA_TEXT);
			if (youtubeURL != null) {
				new PlayYoutubeTask(this, youtubeURL, mMediaServer).execute(new String[] {});
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

	public void showFileSystem(View view) {
		showView(ViewerState.DIRECTORIES);
	}

	public void showPlaylist(View view) {
		showView(ViewerState.PLAYLISTS);
	}

	public void setTotem(View view) {
		mMediaServer.setPlayer("totem");
		saveMediaServer(mMediaServer);
		mMplayerButton.setBackgroundResource(0);
		mOmxButton.setBackgroundResource(0);
		mTotemButton.setBackgroundResource(R.drawable.image_border);
	}

	public void saveMediaServer(MediaServerState mediaServer) {
		Dao<MediaServerState> dao = DaoFactory.getInstance().getDao(MediaServerState.class);
		try {
			dao.update(mediaServer);
		} catch (DaoException e) {
			new AbstractTask.ErrorDialog(getApplicationContext(), e).show();
		}
	}

	private void updateFilePlsButtons() {
		if (mListPager.getCurrentItem() == 0)
			mFilesystemButton.setBackgroundResource(R.drawable.image_border);
		else
			mFilesystemButton.setBackgroundResource(0);
		if (mListPager.getCurrentItem() == 1)
			mPlaylistButton.setBackgroundResource(R.drawable.image_border);
		else
			mPlaylistButton.setBackgroundResource(0);
	}

	public void setMPlayer(View view) {
		mMediaServer.setPlayer("mplayer");
		saveMediaServer(mMediaServer);
		mMplayerButton.setBackgroundResource(R.drawable.image_border);
		mTotemButton.setBackgroundResource(0);
		mOmxButton.setBackgroundResource(0);
	}

	public void setOMXPlayer(View view) {
		mMediaServer.setPlayer("mplayer");
		saveMediaServer(mMediaServer);
		mMplayerButton.setBackgroundResource(0);
		mOmxButton.setBackgroundResource(R.drawable.image_border);
		mTotemButton.setBackgroundResource(0);
	}

	public void cancelDownload(View v) {
		// TODO
		/*
		 * AbstractReceiver receiver = mBinder.getReceiver();
		 * mDownloadLayout.setVisibility(View.GONE); if (receiver != null) {
		 * receiver.cancel(); } else { Toast.makeText(this,
		 * "no receiver available", Toast.LENGTH_SHORT).show();
		 * mDownloadLayout.setVisibility(View.GONE); }
		 */
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null)
			return;
		// TODO
		// if (requestCode == MediaServerActivity.FILE_REQUEST) {
		// Uri uri = data.getData();
		// mBinder.uploadFile(mediaServer.browser, new
		// File(getFilePathByUri(uri)));
		// }
	}

	public void refreshContent() {
		updatePlayerButtons();
		BrowserFragment fragment = mBrowserPageAdapter.getItem(mListPager.getCurrentItem());

		if (fragment != null)
			fragment.refreshContent(this);
	}
}