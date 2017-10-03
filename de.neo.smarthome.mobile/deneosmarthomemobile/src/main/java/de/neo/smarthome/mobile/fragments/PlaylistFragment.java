package de.neo.smarthome.mobile.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import de.neo.smarthome.mobile.api.IWebMediaServer.BeanPlaylist;
import de.neo.smarthome.mobile.api.IWebMediaServer.BeanPlaylistItem;
import de.neo.smarthome.mobile.activities.MediaServerActivity;
import de.neo.smarthome.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.smarthome.mobile.activities.WebAPIActivity;
import de.neo.smarthome.mobile.persistence.MediaServerState;
import de.neo.smarthome.mobile.tasks.AbstractTask;
import de.neo.smarthome.mobile.tasks.PlayItemTask;
import de.neo.smarthome.mobile.tasks.PlayListTask;
import de.neo.smarthome.mobile.util.BrowserPageAdapter.BrowserFragment;
import de.neo.smarthome.mobile.util.ParcelableWebBeans.ParcelBeanPlsItem;
import de.neo.smarthome.mobile.util.ParcelableWebBeans.ParcelBeanPlsList;
import de.remote.mobile.R;

public class PlaylistFragment extends BrowserFragment {

	public static final String BEAN_PLS = "bean_pls";
	public static final String BEAN_ITEMS = "bean_items";
	public static final String VIEWER_STATE = "viewer_state";

	private BeanPlaylistItem mSelectedItem;
	private BeanPlaylist mCurrentPlayList;
	private ArrayList<BeanPlaylist> mPlaylists;
	private ArrayList<BeanPlaylistItem> mPlsItems;
	private ViewerState mViewerState;
	private MediaServerState mMediaServer;
	private boolean mHasContent;

	public PlaylistFragment() {
		mViewerState = ViewerState.PLAYLISTS;
		mHasContent = false;
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

			private ArrayList<BeanPlaylist> mPls = mPlaylists;
			private ArrayList<BeanPlaylistItem> mItems = mPlsItems;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				setListShown(false);
			}

			@Override
			protected Exception doInBackground(String... params) {
				try {
					if (mMediaServer == null) {
						if (mPls != null)
							mPls.clear();
						if (mItems != null)
							mItems.clear();
					} else if (mViewerState == ViewerState.PLAYLISTS) {
						mPls = mMediaServer.getPlayLists();
						Collections.sort(mPls);
					} else {
						mItems = mMediaServer.getPlayListContent(mCurrentPlayList.getName());
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
						setContent(mPls, mItems);
					}
				}
			}
		};
		task.execute();

	}

	protected void setContent(ArrayList<BeanPlaylist> pls, ArrayList<BeanPlaylistItem> plsItems) {
		mPlaylists = pls;
		mPlsItems = plsItems;
		mHasContent = true;
		ArrayList<String> items = new ArrayList<>();
		if (mViewerState == ViewerState.PLAYLISTS)
			for (BeanPlaylist p : pls)
				items.add(p.getName());
		else
			for (BeanPlaylistItem i : plsItems)
				items.add(i.getName());
		String[] itemArray = items.toArray(new String[items.size()]);

		if (getListAdapter() != null)
			setListShown(true);
		setListAdapter(new PlaylistAdapter(getContext(), itemArray));
		if (mListPosition != null) {
			getListView().onRestoreInstanceState(mListPosition);
			mListPosition = null;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mPlaylists != null) {
			ArrayList<ParcelBeanPlsList> copy = new ArrayList<>();
			for (BeanPlaylist bean : mPlaylists)
				copy.add(new ParcelBeanPlsList(bean));
			outState.putParcelableArrayList(BEAN_PLS, copy);
		}
		if (mPlsItems != null) {
			ArrayList<ParcelBeanPlsItem> copy = new ArrayList<>();
			for (BeanPlaylistItem bean : mPlsItems)
				copy.add(new ParcelBeanPlsItem(bean));
			outState.putParcelableArrayList(BEAN_ITEMS, copy);
		}
		outState.putInt(VIEWER_STATE, mViewerState.ordinal());
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		if (bundle != null) {
			if (bundle.containsKey(VIEWER_STATE))
				mViewerState = ViewerState.values()[bundle.getInt(VIEWER_STATE)];
			if (bundle.containsKey(BEAN_PLS)) {
				List<?> list = bundle.getParcelableArrayList(BEAN_PLS);
				mPlaylists = (ArrayList<BeanPlaylist>) list;
			}
			if (bundle.containsKey(BEAN_ITEMS)) {
				List<?> list = bundle.getParcelableArrayList(BEAN_ITEMS);
				mPlsItems = (ArrayList<BeanPlaylistItem>) list;
			}
			if ((mViewerState == ViewerState.PLAYLISTS && mPlaylists != null)
					|| (mViewerState == ViewerState.PLS_ITEMS && mPlsItems != null))
				setContent(mPlaylists, mPlsItems);
		}
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
			task.deleteItemFromPlaylist(mCurrentPlayList.getName(), mSelectedItem.getName());
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
		if (mMediaServer != null && !mHasContent)
			refreshContent(getActivity());
	}

	@Override
	public void setMediaServer(MediaServerState mediaServer) {
		mMediaServer = mediaServer;
		if (getView() != null && !mHasContent)
			refreshContent(getActivity());
	}

}
