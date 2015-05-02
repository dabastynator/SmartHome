package de.neo.remote.mobile.fragments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
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
import android.widget.TextView;
import android.widget.Toast;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.tasks.PlayItemTask;
import de.neo.remote.mobile.tasks.PlayListTask;
import de.neo.remote.mobile.util.BrowserPageAdapter.BrowserFragment;
import de.remote.mobile.R;

public class PlaylistFragment extends ListFragment implements BrowserFragment {

	private StationStuff mStationStuff;
	private String mSelectedItem;
	private String mCurrentPlayList;
	private Map<String, String> mPlsFileMap = new HashMap<String, String>();
	private ViewerState mViewerState;

	public PlaylistFragment() {
		mViewerState = ViewerState.PLAYLISTS;
		setRetainInstance(false);
	}

	public void setStation(StationStuff station) {
		mStationStuff = station;
	}

	@Override
	public void onStart() {
		super.onStart();
		getListView().setOnItemClickListener(new ListClickListener());
		getListView().setOnItemLongClickListener(new ListLongClickListener());
		getActivity().registerForContextMenu(getListView());
		refreshContent(getActivity());
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
					if (mStationStuff == null)
						mItems = new String[] {};
					else if (mViewerState == ViewerState.PLAYLISTS) {
						mItems = mStationStuff.pls.getPlayLists();
						Arrays.sort(mItems);
					} else {
						mItems = mStationStuff.pls
								.listContent(mCurrentPlayList);
						mPlsFileMap.clear();
						for (String item : mItems)
							if (item.indexOf("/") >= 0)
								mPlsFileMap.put(item.substring(item
										.lastIndexOf("/") + 1), item);
							else
								mPlsFileMap.put(item, item);
						mItems = mPlsFileMap.keySet().toArray(
								new String[mPlsFileMap.size()]);
					}
				} catch (Exception e) {
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Exception result) {
				if (result != null) {
					new AbstractTask.ErrorDialog(context, result).show();
					setListAdapter(new PlaylistAdapter(context, new String[] {}));
					setListShown(true);
					setEmptyText(result.getClass().getSimpleName());
				} else {
					if (getListAdapter() != null)
						setListShown(true);
					setListAdapter(new PlaylistAdapter(context, mItems));
				}
			}
		};
		task.execute();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater mi = new MenuInflater(getActivity().getApplication());
		if (mViewerState == ViewerState.PLAYLISTS)
			mi.inflate(R.menu.pls_pref, menu);
		if (mViewerState == ViewerState.PLS_ITEMS)
			mi.inflate(R.menu.pls_item_pref, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		final StationStuff mediaServer = activity.mBinder
				.getLatestMediaServer();
		switch (item.getItemId()) {
		case R.id.opt_item_play:
			new PlayItemTask(activity, mSelectedItem, activity.mBinder,
					mViewerState).execute();
			Toast.makeText(
					activity,
					getActivity().getResources().getString(
							R.string.player_play_directory), Toast.LENGTH_SHORT)
					.show();
			return true;
		case R.id.opt_item_addplaylist:
			new PlayListTask(activity, mediaServer).addItem(mSelectedItem);
			return true;
		case R.id.opt_item_download:
			activity.mBinder.downloadPlaylist(mediaServer.browser, mPlsFileMap
					.values().toArray(new String[mPlsFileMap.size()]),
					mSelectedItem);
			return true;
		case R.id.opt_pls_delete:
			new PlayListTask(activity, mediaServer)
					.deletePlaylist(mSelectedItem);
			return true;
		case R.id.opt_pls_show:
			mViewerState = ViewerState.PLS_ITEMS;
			mCurrentPlayList = mSelectedItem;
			refreshContent(getActivity());
			return true;
		case R.id.opt_pls_item_delete:
			new PlayListTask(activity, mediaServer).deleteItemFromPlaylist(
					mCurrentPlayList, mSelectedItem);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public class PlaylistAdapter extends ArrayAdapter<String> {

		public PlaylistAdapter(Context context, String[] playlists) {
			super(context, R.layout.mediaserver_browser_row,
					R.id.lbl_item_name, playlists);
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
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				int position, long arg3) {
			mSelectedItem = ((TextView) view.findViewById(R.id.lbl_item_name))
					.getText().toString();
			if (mViewerState == ViewerState.PLS_ITEMS)
				mSelectedItem = mPlsFileMap.get(mSelectedItem);
			return false;
		}
	}

	public class ListClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			MediaServerActivity activity = (MediaServerActivity) getActivity();
			String item = ((TextView) view.findViewById(R.id.lbl_item_name))
					.getText().toString();
			if (mViewerState == ViewerState.PLS_ITEMS)
				item = mPlsFileMap.get(item);
			PlayItemTask task = new PlayItemTask(activity, item,
					activity.mBinder, mViewerState);
			task.execute(new String[] {});
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		if (activity.mBinder != null && keyCode == KeyEvent.KEYCODE_BACK
				&& mViewerState == ViewerState.PLS_ITEMS) {
			mViewerState = ViewerState.PLAYLISTS;
			refreshContent(activity);
			return true;
		}
		return false;
	}
}
