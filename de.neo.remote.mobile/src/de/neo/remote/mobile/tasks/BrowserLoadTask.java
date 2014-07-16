package de.neo.remote.mobile.tasks;

import java.util.Arrays;

import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mediaserver.api.PlayerException;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.fragments.BrowserFragment;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

public class BrowserLoadTask extends AbstractTask {

	private MediaServerActivity activity;
	private String goTo;
	private boolean goBack;
	private String[] itemArray;
	private BrowserFragment browser_fragment;

	public BrowserLoadTask(MediaServerActivity activity, String goTo,
			boolean goBack) {
		super(activity, TaskMode.ActionBarTask);
		this.activity = activity;
		this.browser_fragment = (BrowserFragment) activity.getFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_browser);
		this.goTo = goTo;
		this.goBack = goBack;
	}

	@Override
	protected String getDialogTitle() {
		return activity.getResources().getString(R.string.str_loading);
	}

	@Override
	protected void onExecute() throws Exception {
		itemArray = loadItems(goTo);
	}

	private String[] loadItems(String gotoPath) throws RemoteException,
			PlayerException {
		StationStuff mediaServer = activity.binder.getLatestMediaServer();
		if (mediaServer == null) {
			activity.disableScreen();
			return new String[] {};
		}
		switch (browser_fragment.viewerState) {
		case DIRECTORIES:
			if (gotoPath != null)
				mediaServer.browser.goTo(gotoPath);
			if (goBack)
				goBack = !mediaServer.browser.goBack();
			mediaServer.browser.getLocation();
			mediaServer.browser.getFullLocation();
			String[] directories = mediaServer.browser.getDirectories();
			String[] files = mediaServer.browser.getFiles();
			String[] all = new String[directories.length + files.length];
			mediaServer.directoryCount = directories.length;
			System.arraycopy(directories, 0, all, 0, directories.length);
			System.arraycopy(files, 0, all, directories.length, files.length);
			return all;
		case PLAYLISTS:
			String[] playLists = mediaServer.pls.getPlayLists();
			Arrays.sort(playLists);
			return playLists;
		case PLS_ITEMS:
			browser_fragment.plsFileMap.clear();
			for (String item : mediaServer.pls
					.listContent(browser_fragment.currentPlayList))
				if (item.indexOf("/") >= 0)
					browser_fragment.plsFileMap.put(
							item.substring(item.lastIndexOf("/") + 1), item);
				else
					browser_fragment.plsFileMap.put(item, item);
			return browser_fragment.plsFileMap.keySet()
					.toArray(new String[] {});
		default:
			return new String[] {};
		}
	}

	@Override
	protected void onPostExecute(Exception result) {
		super.onPostExecute(result);
		if (exception == null)
			browser_fragment.onLoadedItems(itemArray, goBack);
		StationStuff mediaServer = activity.binder.getLatestMediaServer();
		IPlayer player = mediaServer.player;
		activity.totemButton.setBackgroundResource(0);
		activity.mplayerButton.setBackgroundResource(0);
		if (activity.omxButton != null)
		activity.omxButton.setBackgroundResource(0);
		if (player == activity.binder.getLatestMediaServer().mplayer)
		activity.mplayerButton
		.setBackgroundResource(R.drawable.image_border);
		if (player == activity.binder.getLatestMediaServer().totem)
		activity.totemButton.setBackgroundResource(R.drawable.image_border);
		if (player == activity.binder.getLatestMediaServer().omxplayer
		&& activity.omxButton != null)
		activity.omxButton.setBackgroundResource(R.drawable.image_border);
	}

}
