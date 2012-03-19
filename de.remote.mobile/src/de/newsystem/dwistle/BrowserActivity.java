package de.newsystem.dwistle;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.newsystem.dwistle.services.IdefixService;
import de.newsystem.dwistle.services.IdefixService.PlayerBinder;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.PlayerException;

public class BrowserActivity extends Activity {

	private static final String VIEWER_STATE = "viewerstate";

	public static final int SELECT_PLS_CODE = 0;

	public enum ViewerState {
		DIRECTORIES, PLAYLISTS, PLS_ITEMS
	};

	private String[] directories;

	private String[] files;

	private ListView listView;

	private PlayerBinder binder;

	private ViewerState viewerState = ViewerState.DIRECTORIES;

	private ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			disableScreen();
			if (binder.getChatServer() == null)
				binder.connectToServer("192.168.1.3", new ShowFolderRunnable());
			else
				new ShowFolderRunnable().run();
		}
	};

	private ImageView buttonPlay;
	private ImageView buttonFull;
	private ImageView buttonNext;
	private ImageView buttonPref;
	private ImageView buttonQuit;

	public String selectedItem;

	private String currentPlayList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findComponents();
		listView.setBackgroundResource(R.drawable.idefix_dark);
		listView.setScrollingCacheEnabled(false);
		listView.setCacheColorHint(0);
		listView.setOnItemClickListener(new MyClickListener());
		listView.setOnItemLongClickListener(new MyLongClickListener());
		registerForContextMenu(listView);
	}

	private void findComponents() {
		listView = (ListView) findViewById(R.id.fileList);
		buttonPlay = (ImageView) findViewById(R.id.button_play);
		buttonFull = (ImageView) findViewById(R.id.button_full);
		buttonNext = (ImageView) findViewById(R.id.button_next);
		buttonPref = (ImageView) findViewById(R.id.button_pref);
		buttonQuit = (ImageView) findViewById(R.id.button_quit);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		System.out.println("createcontextmenu");
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
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		viewerState = ViewerState.values()[bundle.getInt(VIEWER_STATE)];
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = new Intent(this, IdefixService.class);
		startService(intent);
		boolean bound = bindService(intent, playerConnection,
				Context.BIND_AUTO_CREATE);
		if (!bound)
			Log.e("nicht verbunden!!!", "service nicht verbunden");
		// if (binder == null)
		// disableScreen();
		// else
		// showUpDateUI();
	}

	private void enableButtons(boolean b) {
		buttonPlay.setEnabled(b);
		buttonFull.setEnabled(b);
		buttonNext.setEnabled(b);
		buttonPref.setEnabled(b);
		buttonQuit.setEnabled(b);
	}

	@Override
	protected void onPause() {
		unbindService(playerConnection);
		super.onPause();
	}

	private void showUpDateUI() {
		if (binder == null || binder.getBrowser() == null){
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
				listView.setAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, all));
				setTitle(binder.getBrowser().getLocation());
			}
			if (viewerState == ViewerState.PLAYLISTS) {
				listView.setAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, binder
								.getPlayList().getPlayLists()));
				setTitle("Playlists");
			}
			if (viewerState == ViewerState.PLS_ITEMS) {
				listView.setAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, binder
								.getPlayList().listContent(currentPlayList)));
				setTitle("Playlist: " + currentPlayList);
			}
			enableButtons(true);
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
				if (viewerState == ViewerState.DIRECTORIES)
					if (binder.getBrowser().goBack()) {
						showUpDateUI();
						return true;
					}
				if (viewerState == ViewerState.PLAYLISTS) {
					viewerState = ViewerState.DIRECTORIES;
					showUpDateUI();
					return true;
				}
				if (viewerState == ViewerState.PLS_ITEMS) {
					viewerState = ViewerState.PLAYLISTS;
					showUpDateUI();
					return true;
				}
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

	public void playPause(View v) {
		try {
			binder.getPlayer().playPause();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public void stopPlayer(View v) {
		try {
			binder.getPlayer().quit();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public void next(View v) {
		try {
			binder.getPlayer().next();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public void prev(View v) {
		try {
			binder.getPlayer().previous();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void seekBwd(View w){
		try {
			binder.getPlayer().seekBackwards();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void seekFwd(View w){
		try {
			binder.getPlayer().seekForwards();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public void fullScreen(View v) {
		try {
			binder.getPlayer().fullScreen();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.opt_idefix:
				disableScreen();
				binder.connectToServer("192.168.1.4", new ShowFolderRunnable());
				break;
			case R.id.opt_inspiron:
				disableScreen();
				binder.connectToServer("192.168.1.3", new ShowFolderRunnable());
				break;
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
				finish();
				break;
			case R.id.opt_playlist:
				viewerState = ViewerState.PLAYLISTS;
				showUpDateUI();
				break;
			case R.id.opt_folder:
				viewerState = ViewerState.DIRECTORIES;
				showUpDateUI();
				break;
			case R.id.opt_create_playlist:
				Intent i = new Intent(this, GetTextActivity.class);
				startActivityForResult(i, GetTextActivity.RESULT_CODE);
				break;
			case R.id.opt_chat:
				Intent intent = new Intent(this, ChatActivity.class);
				startActivity(intent);
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
				startActivityForResult(i, SELECT_PLS_CODE);
				break;
			case R.id.opt_pls_delete:
				binder.getPlayList().removePlayList(selectedItem);
				showUpDateUI();
				Toast.makeText(BrowserActivity.this,
						"Playlist '" + selectedItem + "' deleted",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.opt_pls_show:
				viewerState = ViewerState.PLS_ITEMS;
				currentPlayList = selectedItem;
				showUpDateUI();
				break;
			case R.id.opt_pls_item_delete:
				binder.getPlayList().removeItem(currentPlayList, selectedItem);
				showUpDateUI();
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
			if (requestCode == SELECT_PLS_CODE) {
				if (data.getExtras() == null)
					return;
				String pls = data.getExtras().getString(
						SelectPlaylistActivity.RESULT);
				binder.getPlayList().extendPlayList(pls,
						binder.getBrowser().getFullLocation() + selectedItem);
				Toast.makeText(BrowserActivity.this, selectedItem + " added",
						Toast.LENGTH_SHORT).show();
			}
			if (requestCode == GetTextActivity.RESULT_CODE) {
				String pls = data.getExtras().getString(GetTextActivity.RESULT);
				binder.getPlayList().addPlayList(pls);
				showUpDateUI();
				Toast.makeText(BrowserActivity.this,
						"playlist '" + pls + "' added", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (RemoteException e) {
			Toast.makeText(BrowserActivity.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (PlayerException e) {
			Toast.makeText(BrowserActivity.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void disableScreen() {
		setTitle("connecting...");
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new String[] {}));
		enableButtons(false);
	}

	public class ShowFolderRunnable implements Runnable {
		@Override
		public void run() {
			showUpDateUI();
		}
	}

	public class MyClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			String item = ((TextView) view).getText().toString();
			try {
				if (viewerState == ViewerState.PLAYLISTS) {
					binder.getPlayer().playPlayList(item);
					Toast.makeText(BrowserActivity.this, "Playlist abspielen",
							Toast.LENGTH_SHORT).show();
				}
				if (viewerState == ViewerState.DIRECTORIES) {
					for (String str : directories)
						if (str.equals(item)) {
							binder.getBrowser().goTo(item);
							showUpDateUI();
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

	public class MyLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				int position, long arg3) {
			selectedItem = ((TextView) view).getText().toString();
			return false;
		}
	}

}