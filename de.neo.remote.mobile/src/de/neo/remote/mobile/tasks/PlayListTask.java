package de.neo.remote.mobile.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.EditText;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.WebAPIActivity;
import de.neo.remote.mobile.persistence.MediaServerState;
import de.neo.remote.mobile.services.RemoteService;
import de.neo.remote.mobile.tasks.SimpleTask.BackgroundAction;
import de.neo.smarthome.api.IWebMediaServer.BeanPlaylist;
import de.neo.smarthome.api.IWebMediaServer.BeanPlaylistItem;
import de.remote.mobile.R;

public class PlayListTask {

	private WebAPIActivity mActivity;
	private MediaServerState mMedia;
	private String mItem;

	public PlayListTask(WebAPIActivity activity, MediaServerState mediaserver) {
		mActivity = activity;
		mMedia = mediaserver;
	}

	public void addItem(String item) {
		mItem = item;
		new SelectPlayListTask(mActivity).execute(item);
	}

	public void addItemToPlayList(final String item, final String playlist) {
		new SimpleTask(mActivity).setSuccess(mActivity.getString(R.string.str_entry_add) + " :" + item)
				.setAction(new BackgroundAction() {

					@Override
					public void run() throws Exception {
						mMedia.extendPlayList(playlist, item);
					}
				}).execute();
	}

	public void createPlaylist() {
		AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);

		alert.setTitle(mActivity.getString(R.string.playlist_add));
		alert.setMessage(mActivity.getString(R.string.playlist_enter_name));

		// Set an EditText view to get user input
		final EditText input = new EditText(mActivity);
		alert.setView(input);

		alert.setPositiveButton(mActivity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				createPlaylist(input.getText().toString());

			}
		});

		alert.setNegativeButton(mActivity.getString(android.R.string.cancel), null);
		alert.show();
	}

	public void deletePlaylist(final String playlist) {
		SimpleTask task = new SimpleTask(mActivity) {
			@Override
			protected void onPostExecute(Exception result) {
				super.onPostExecute(result);
				if (mActivity instanceof MediaServerActivity)
					((MediaServerActivity) mActivity).refreshContent();
			}
		};
		task.setAction(new BackgroundAction() {

			@Override
			public void run() throws Exception {
				mMedia.playlistDelete(playlist);
			}
		});
		task.setSuccess(mActivity.getString(R.string.playlist_delete) + ": " + playlist);
		task.execute();

	}

	public void deleteItemFromPlaylist(final String playlist, final String item) {
		SimpleTask task = new SimpleTask(mActivity) {
			@Override
			protected void onPostExecute(Exception result) {
				super.onPostExecute(result);
				if (mActivity instanceof MediaServerActivity)
					((MediaServerActivity) mActivity).refreshContent();
			}
		};
		task.setAction(new BackgroundAction() {

			@Override
			public void run() throws Exception {
				mMedia.playlistDeleteItem(playlist, item);
			}
		});
		task.setSuccess(mActivity.getString(R.string.playlist_remove_item) + ": " + item);
		task.execute();
	}

	public void createPlaylist(final String playlist) {
		SimpleTask task = new SimpleTask(mActivity) {
			@Override
			protected void onPostExecute(Exception result) {
				super.onPostExecute(result);
				if (mActivity instanceof MediaServerActivity)
					((MediaServerActivity) mActivity).refreshContent();
			}
		};
		task.setAction(new BackgroundAction() {

			@Override
			public void run() throws Exception {
				mMedia.playlistCreate(playlist);
			}
		});
		task.setSuccess(mActivity.getString(R.string.playlist_added) + ": " + playlist);
		task.execute();
	}

	class SelectPlayListTask extends SimpleTask {

		private String[] mPlayLists;

		public SelectPlayListTask(WebAPIActivity activity) {
			super(activity);
			setAction(new BackgroundAction() {

				@Override
				public void run() throws Exception {
					List<String> items = new ArrayList<>();
					for (BeanPlaylist pls : mMedia.getPlayLists())
						items.add(pls.getName());
					mPlayLists = items.toArray(new String[items.size()]);
					Arrays.sort(mPlayLists);
				}
			});
		}

		@Override
		protected void onPostExecute(Exception result) {
			if (result != null) {
				super.onPostExecute(result);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle(mActivity.getString(R.string.playlist_select));
				builder.setItems(mPlayLists, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						addItemToPlayList(mItem, mPlayLists[which]);
					}
				});
				builder.show();
			}
		}
	}

	public void download(final String name) {
		new AsyncTask<String, Void, List<BeanPlaylistItem>>() {

			private Exception mError;

			@Override
			protected List<BeanPlaylistItem> doInBackground(String... name) {
				try {
					return mMedia.getPlayListContent(name[0]);
				} catch (Exception e) {
					mError = e;
				}
				return null;
			}

			protected void onPostExecute(List<BeanPlaylistItem> result) {
				if (mError != null)
					new AbstractTask.ErrorDialog(mActivity, mError).show();
				else if (result != null && result.size() > 0) {
					String[] items = new String[result.size()];
					for (int i = 0; i < result.size(); i++)
						items[i] = result.get(i).getPath();
					Intent intent = new Intent(mActivity, RemoteService.class);
					intent.setAction(RemoteService.ACTION_DOWNLOAD);
					intent.putExtra(RemoteService.EXTRA_ID, mMedia.getMediaServerID());
					intent.putExtra(RemoteService.EXTRA_DOWNLOAD, items);
					intent.putExtra(RemoteService.EXTRA_DOWNLOAD_DESTINY,
							mMedia.getRemoteServer().getName() + File.separator + name);
					mActivity.startService(intent);
				}
			};

		}.execute(name);
	}
}
