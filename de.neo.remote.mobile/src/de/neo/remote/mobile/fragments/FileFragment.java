package de.neo.remote.mobile.fragments;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import de.neo.remote.api.IWebMediaServer.BeanFileSystem;
import de.neo.remote.api.IWebMediaServer.FileType;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.persistence.MediaServerState;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.tasks.PlayItemTask;
import de.neo.remote.mobile.tasks.PlayListTask;
import de.neo.remote.mobile.util.BrowserPageAdapter.BrowserFragment;
import de.remote.mobile.R;

public class FileFragment extends BrowserFragment implements IThumbnailListener {

	public static final int PREVIEW_SIZE = 128;

	private MediaServerState mMediaServer;
	private BeanFileSystem mSelectedItem;
	private PlayingBean mPlayingBean;
	private ArrayList<BeanFileSystem> mFileBeans;
	private Map<String, Bitmap> mThumbnails;
	private Handler mHandler;

	public FileFragment() {
		mThumbnails = new HashMap<String, Bitmap>();
		mHandler = new Handler();
	}

	@Override
	public void onStart() {
		super.onStart();
		getListView().setOnItemClickListener(new ListClickListener());
		getListView().setOnItemLongClickListener(new ListLongClickListener());
		getListView().setFastScrollEnabled(true);
		getActivity().registerForContextMenu(getListView());
		// refreshContent(getActivity(), null, false);
	}

	public void refreshContent(final Context context) {
		AsyncTask<String, Integer, Exception> task = new AsyncTask<String, Integer, Exception>() {

			private String[] mFileList;

			@Override
			protected void onPreExecute() {
				setListShown(false);
			}

			@Override
			protected Exception doInBackground(String... params) {
				mFileList = new String[] {};
				mFileBeans = new ArrayList<>();
				try {
					if (mMediaServer != null) {
						mFileBeans = mMediaServer.getFiles();
						mFileList = new String[mFileBeans.size()];
						for (int i = 0; i < mFileBeans.size(); i++)
							mFileList[i] = mFileBeans.get(i).getName();
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
						setListAdapter(new FileAdapter(context, new String[] {}, mFileBeans));
						setListShown(true);
						setEmptyText(result.getClass().getSimpleName());
					} else {
						if (getListAdapter() != null)
							setListShown(true);
						setListAdapter(new FileAdapter(context, mFileList, mFileBeans));
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
		mi.inflate(R.menu.item_pref, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		switch (item.getItemId()) {
		case R.id.opt_item_play:
			new PlayItemTask(activity, mMediaServer, mSelectedItem).execute();
			Toast.makeText(activity, getActivity().getResources().getString(R.string.player_play_directory),
					Toast.LENGTH_SHORT).show();
			return true;
		case R.id.opt_item_addplaylist:
			new PlayListTask(activity, mMediaServer).addItem(mSelectedItem.getName());
			return true;
		case R.id.opt_item_download:
			/*
			 * if (mSelectedItem.getFileType() == FileType.Directory)
			 * activity.mBinder.downloadDirectory(activity,
			 * mStationStuff.browser, mSelectedItem); else
			 * activity.mBinder.downloadFile(activity, mStationStuff.browser,
			 * mSelectedItem); return true;
			 */
		}
		return super.onOptionsItemSelected(item);
	}

	public class FileAdapter extends ArrayAdapter<String> {

		private ArrayList<BeanFileSystem> mFiles;

		public FileAdapter(Context context, String[] files, ArrayList<BeanFileSystem> fileBeans) {
			super(context, R.layout.mediaserver_browser_row, R.id.lbl_item_name, files);
			mFiles = fileBeans;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			ImageView image = (ImageView) view.findViewById(R.id.img_item);
			String file = ((TextView) view.findViewById(R.id.lbl_item_name)).getText().toString();
			BeanFileSystem fileBean = mFiles.get(position);
			if (mThumbnails.containsKey(file)) {
				image.setImageBitmap(mThumbnails.get(file));
			} else if (mPlayingBean != null && mPlayingBean.getState() != STATE.DOWN && mPlayingBean.getPath() != null
					&& mPlayingBean.getPath().equals(mMediaServer.getBrowserLocation() + file)) {
				image.setImageResource(R.drawable.playing);
			} else if (fileBean.getFileType() == FileType.Directory)
				image.setImageResource(R.drawable.folder);
			else if (file.toUpperCase(Locale.US).endsWith("MP3") || file.toUpperCase(Locale.US).endsWith("OGG")
					|| file.toUpperCase(Locale.US).endsWith("WAV"))
				image.setImageResource(R.drawable.music);
			else if (file.toUpperCase(Locale.US).endsWith("AVI") || file.toUpperCase(Locale.US).endsWith("MPEG")
					|| file.toUpperCase(Locale.US).endsWith("MPG"))
				image.setImageResource(R.drawable.movie);
			else if (file.toUpperCase(Locale.US).endsWith("JPG") || file.toUpperCase(Locale.US).endsWith("GIF")
					|| file.toUpperCase(Locale.US).endsWith("BMP") || file.toUpperCase(Locale.US).endsWith("PNG"))
				image.setImageResource(R.drawable.camera);
			else
				image.setImageResource(R.drawable.file);
			return view;
		}

	}

	public class ListLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
			mSelectedItem = mFileBeans.get(position);
			return false;
		}
	}

	public class ListClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			MediaServerActivity activity = (MediaServerActivity) getActivity();
			BeanFileSystem item = mFileBeans.get(position);
			if (item.getFileType() == FileType.Directory) {
				mMediaServer.goTo(item.getName());
				activity.saveMediaServer(mMediaServer);
				refreshContent(activity);
			} else {
				PlayItemTask task = new PlayItemTask(activity, mMediaServer, item);
				task.execute(new String[] {});
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMediaServer.goBack()) {
				activity.saveMediaServer(mMediaServer);
				refreshContent(activity);
				return true;
			}
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
