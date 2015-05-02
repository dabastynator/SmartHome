package de.neo.remote.mobile.fragments;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
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
import de.neo.remote.api.IThumbnailListener;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.tasks.PlayItemTask;
import de.neo.remote.mobile.tasks.PlayListTask;
import de.neo.remote.mobile.util.BrowserPageAdapter.BrowserFragment;
import de.remote.mobile.R;

public class FileFragment extends ListFragment implements BrowserFragment,
		IThumbnailListener {

	public static final int PREVIEW_SIZE = 128;

	private StationStuff mStationStuff;
	private String mSelectedItem;
	private String mPath;
	private PlayingBean mPlayingBean;
	private int mSelectedPosition;
	private Map<String, Bitmap> mThumbnails;
	private Handler mHandler;

	public FileFragment() {
		mThumbnails = new HashMap<String, Bitmap>();
		mHandler = new Handler();
		setRetainInstance(false);
	}

	public void setStation(StationStuff station) {
		mStationStuff = station;
	}

	public void refreshContent(Context context) {
		refreshContent(context, null, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		getListView().setOnItemClickListener(new ListClickListener());
		getListView().setOnItemLongClickListener(new ListLongClickListener());
		getActivity().registerForContextMenu(getListView());
		refreshContent(getActivity(), null, false);
	}

	private void refreshContent(final Context context, final String goTo,
			final boolean goBack) {
		AsyncTask<String, Integer, Exception> task = new AsyncTask<String, Integer, Exception>() {

			private String[] mFileList;
			private boolean mGoBack = false;

			@Override
			protected void onPreExecute() {
				setListShown(false);
			}

			@Override
			protected Exception doInBackground(String... params) {
				try {
					if (mStationStuff == null)
						mFileList = new String[] {};
					else {
						if (goTo != null)
							mStationStuff.browser.goTo(goTo);
						if (goBack)
							mGoBack = !mStationStuff.browser.goBack();
						mStationStuff.browser.getLocation();
						mPath = mStationStuff.browser.getFullLocation();
						String[] directories = mStationStuff.browser
								.getDirectories();
						String[] files = mStationStuff.browser.getFiles();
						mFileList = new String[directories.length
								+ files.length];
						mStationStuff.directoryCount = directories.length;
						System.arraycopy(directories, 0, mFileList, 0,
								directories.length);
						System.arraycopy(files, 0, mFileList,
								directories.length, files.length);
						mStationStuff.browser.fireThumbnails(FileFragment.this,
								PREVIEW_SIZE, PREVIEW_SIZE);
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
					setEmptyText(result.getClass().getSimpleName());
				} else {
					if (getListAdapter() != null)
						setListShown(true);
					setListAdapter(new FileAdapter(context, mFileList));
				}
				if (mGoBack)
					getActivity().onBackPressed();
			}
		};
		task.execute();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater mi = new MenuInflater(getActivity().getApplication());
		mi.inflate(R.menu.item_pref, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		switch (item.getItemId()) {
		case R.id.opt_item_play:
			new PlayItemTask(activity, mSelectedItem, activity.mBinder,
					ViewerState.DIRECTORIES).execute();
			Toast.makeText(
					activity,
					getActivity().getResources().getString(
							R.string.player_play_directory), Toast.LENGTH_SHORT)
					.show();
			return true;
		case R.id.opt_item_addplaylist:
			new PlayListTask(activity, mStationStuff).addItem(mSelectedItem);
			return true;
		case R.id.opt_item_download:
			if (mSelectedPosition < mStationStuff.directoryCount)
				activity.mBinder.downloadDirectory(activity,
						mStationStuff.browser, mSelectedItem);
			else
				activity.mBinder.downloadFile(activity, mStationStuff.browser,
						mSelectedItem);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class FileAdapter extends ArrayAdapter<String> {

		public FileAdapter(Context context, String[] files) {
			super(context, R.layout.mediaserver_browser_row,
					R.id.lbl_item_name, files);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			ImageView image = (ImageView) view.findViewById(R.id.img_item);
			String file = ((TextView) view.findViewById(R.id.lbl_item_name))
					.getText().toString();
			if (mThumbnails.containsKey(file)) {
				image.setImageBitmap(mThumbnails.get(file));
			} else if (mPlayingBean != null
					&& mPlayingBean.getState() != STATE.DOWN
					&& mPlayingBean.getPath() != null
					&& mPlayingBean.getPath().equals(mPath + file)) {
				image.setImageResource(R.drawable.playing);
			} else if (position < mStationStuff.directoryCount)
				image.setImageResource(R.drawable.folder);
			else if (file.toUpperCase(Locale.US).endsWith("MP3")
					|| file.toUpperCase(Locale.US).endsWith("OGG")
					|| file.toUpperCase(Locale.US).endsWith("WAV"))
				image.setImageResource(R.drawable.music);
			else if (file.toUpperCase(Locale.US).endsWith("AVI")
					|| file.toUpperCase(Locale.US).endsWith("MPEG")
					|| file.toUpperCase(Locale.US).endsWith("MPG"))
				image.setImageResource(R.drawable.movie);
			else if (file.toUpperCase(Locale.US).endsWith("JPG")
					|| file.toUpperCase(Locale.US).endsWith("GIF")
					|| file.toUpperCase(Locale.US).endsWith("BMP")
					|| file.toUpperCase(Locale.US).endsWith("PNG"))
				image.setImageResource(R.drawable.camera);
			else
				image.setImageResource(R.drawable.file);
			return view;
		}

	}

	public class ListLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				int position, long arg3) {
			mSelectedItem = ((TextView) view.findViewById(R.id.lbl_item_name))
					.getText().toString();
			mSelectedPosition = position;
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
			if (position < mStationStuff.browser.getDirectoriesInt().length) {
				mSelectedPosition = 0;
				refreshContent(activity, item, false);
			} else {
				PlayItemTask task = new PlayItemTask(activity, item,
						activity.mBinder, ViewerState.DIRECTORIES);
				task.execute(new String[] {});
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		if (activity.mBinder != null && keyCode == KeyEvent.KEYCODE_BACK) {
			mSelectedPosition = 0;
			refreshContent(activity, null, true);
			return true;
		}
		return false;
	}

	public void setThumbnail(String file, int width, int height, int[] thumbnail) {
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		IntBuffer buf = IntBuffer.wrap(thumbnail); // data is my array
		bm.copyPixelsFromBuffer(buf);
		mThumbnails.put(file, bm);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (getListAdapter() != null)
					((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			}
		});
	}

	public void setPlayingFile(PlayingBean bean) {
		mPlayingBean = bean;
		if (getListAdapter() != null)
			((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
	}

}
