package de.neo.remote.mobile.fragments;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import de.neo.remote.api.IWebMediaServer.BeanPlaylist;
import de.neo.remote.api.IWebMediaServer.BeanPlaylistItem;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.activities.WebAPIActivity;
import de.neo.remote.mobile.persistence.MediaServerState;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.tasks.PlayItemTask;
import de.neo.remote.mobile.tasks.PlayListTask;
import de.neo.remote.mobile.util.BrowserPageAdapter.BrowserFragment;
import de.remote.mobile.R;

public class PlaylistFragment extends BrowserFragment {

	private BeanPlaylistItem mSelectedItem;
	private BeanPlaylist mCurrentPlayList;
	private ArrayList<BeanPlaylist> mPlaylists;
	private ArrayList<BeanPlaylistItem> mPlsItems;
	private ViewerState mViewerState;
	private MediaServerState mMediaServer;

	public PlaylistFragment() {
		mViewerState = ViewerState.PLAYLISTS;
	}

	@Override
	public void onStart() {
		super.onStart();
		getListView().setOnItemClickListener(new ListClickListener());
		getListView().setOnItemLongClickListener(new ListLongClickListener());
		getListView().setFastScrollEnabled(true);
		getActivity().registerForContextMenu(getListView());
		// refreshContent(getActivity());
	}

	public void refreshContent(final Context context) {
		AsyncTask<String, Integer, Exception> task = new AsyncTask<String, Integer, Exception>() {

			private String[] mItems;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				setListShown(false);
			}

			@Override
			protected Exception doInBackground(String... params) {
				try {
					if (mMediaServer == null)
						mItems = new String[] {};
					else if (mViewerState == ViewerState.PLAYLISTS) {
						ArrayList<String> items = new ArrayList<>();
						mPlaylists = mMediaServer.getPlayLists();
						Collections.sort(mPlaylists);
						for (BeanPlaylist pls : mPlaylists) {
							items.add(pls.getName());
						}
						mItems = items.toArray(new String[items.size()]);
					} else {
						mPlsItems = mMediaServer.getPlayListContent(mCurrentPlayList.getName());
						ArrayList<String> items = new ArrayList<>();
						for (BeanPlaylistItem item : mPlsItems) {
							items.add(item.getName());
						}
						mItems = items.toArray(new String[items.size()]);
					}
				} catch (Exception e) {
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Exception result) {
				if (mActive) {
					if (result != null) {
						new AbstractTask.ErrorDialog(context, result).show();
						setListAdapter(new PlaylistAdapter(context, new String[] {}));
						setListShown(true);
						setEmptyText(result.getClass().getSimpleName());
					} else {
						if (getListAdapter() != null)
							setListShown(true);
						setListAdapter(new PlaylistAdapter(context, mItems));
						if (mListPosition != null) {
							getListView().onRestoreInstanceState(mListPosition);
							mListPosition = null;
						}
					}
				}
			}
		};
		task.execute();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater mi = new MenuInflater(getActivity().getApplication());
		if (mViewerState == ViewerState.PLAYLISTS)
			mi.inflate(R.menu.pls_pref, menu);
		if (mViewerState == ViewerState.PLS_ITEMS)
			mi.inflate(R.menu.pls_item_pref, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		PlayListTask task = new PlayListTask(activity, mMediaServer);
		switch (item.getItemId()) {
		case R.id.opt_item_play:
			new PlayItemTask(activity, mMediaServer, mSelectedItem).execute();
			Toast.makeText(activity, getActivity().getResources().getString(R.string.player_play_directory),
					Toast.LENGTH_SHORT).show();
			return true;
		case R.id.opt_item_addplaylist:
			task.addItem(mCurrentPlayList.getName());
			return true;
		case R.id.opt_item_download:
			task.download(mCurrentPlayList.getName());
			return true;
		case R.id.opt_pls_delete:
			task.deletePlaylist(mCurrentPlayList.getName());
			return true;
		case R.id.opt_pls_show:
			mViewerState = ViewerState.PLS_ITEMS;
			refreshContent(getActivity());
			return true;
		case R.id.opt_pls_item_delete:
			task.deleteItemFromPlaylist(mCurrentPlayList.getName(),
					mSelectedItem.getName());
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public class PlaylistAdapter extends ArrayAdapter<String> {

		public PlaylistAdapter(Context context, String[] playlists) {
			super(context, R.layout.mediaserver_browser_row, R.id.lbl_item_name, playlists);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			ImageView image = (ImageView) view.findViewById(R.id.img_item);
			if (mViewerState == ViewerState.PLAYLISTS)
				image.setImageResource(R.drawable.pls);
			if (mViewerState == ViewerState.PLS_ITEMS)
				image.setImageResource(R.drawable.music);
			return view;
		}

	}

	public class ListLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
			if (mViewerState == ViewerState.PLS_ITEMS)
				mSelectedItem = mPlsItems.get(position);
			else
				mCurrentPlayList = mPlaylists.get(position);
			return false;
		}
	}

	public class ListClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			WebAPIActivity activity = (WebAPIActivity) getActivity();
			PlayItemTask task = null;
			if (mViewerState == ViewerState.PLS_ITEMS) {
				mSelectedItem = mPlsItems.get(position);
				task = new PlayItemTask(activity, mMediaServer, mSelectedItem);
			} else {
				mCurrentPlayList = mPlaylists.get(position);
				task = new PlayItemTask(activity, mMediaServer, mCurrentPlayList);
			}

			task.execute(new String[] {});
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		if (keyCode == KeyEvent.KEYCODE_BACK && mViewerState == ViewerState.PLS_ITEMS) {
			mViewerState = ViewerState.PLAYLISTS;
			refreshContent(activity);
			return true;
		}
		return false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mMediaServer != null)
			refreshContent(getActivity());
	}

	@Override
	public void setMediaServer(MediaServerState mediaServer) {
		mMediaServer = mediaServer;
		if (getView() != null)
			refreshContent(getActivity());
	}
}
