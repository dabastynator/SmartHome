package de.neo.remote.mobile.tasks;

import java.util.Arrays;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.SimpleTask.BackgroundAction;
import de.remote.mobile.R;

public class PlayListTask {

	private AbstractConnectionActivity mActivity;
	private StationStuff mMedia;
	private String mItem;

	public PlayListTask(AbstractConnectionActivity activity, StationStuff media) {
		mActivity = activity;
		mMedia = media;
	}

	public void addItem(String item) {
		mItem = item;
		new SelectPlayListTask(mActivity).execute(item);
	}

	public void addItemToPlayList(final String item, final String playlist) {
		new SimpleTask(mActivity)
				.setSuccess(
						mActivity.getString(R.string.str_entry_add) + " :"
								+ item).setAction(new BackgroundAction() {

					@Override
					public void run() throws Exception {
						mMedia.pls.extendPlayList(playlist,
								mMedia.browser.getFullLocation() + item);
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

		alert.setPositiveButton(mActivity.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						createPlaylist(input.getText().toString());

					}
				});

		alert.setNegativeButton(mActivity.getString(android.R.string.cancel),
				null);
		alert.show();
	}

	public void deletePlaylist(final String playlist) {
		new SimpleTask(mActivity)
				.setAction(new BackgroundAction() {

					@Override
					public void run() throws Exception {
						mMedia.pls.removePlayList(playlist);
					}
				})
				.setSuccess(
						mActivity.getString(R.string.playlist_delete) + ": "
								+ playlist).execute();
	}

	public void deleteItemFromPlaylist(final String playlist, final String item) {
		new SimpleTask(mActivity)
				.setAction(new BackgroundAction() {

					@Override
					public void run() throws Exception {
						mMedia.pls.removeItem(playlist, item);
					}
				})
				.setSuccess(
						mActivity.getString(R.string.playlist_remove_item)
								+ ": " + item).execute();
	}

	public void createPlaylist(final String playlist) {
		new SimpleTask(mActivity)
				.setAction(new BackgroundAction() {

					@Override
					public void run() throws Exception {
						mMedia.pls.addPlayList(playlist);
					}
				})
				.setSuccess(
						mActivity.getString(R.string.playlist_added) + ": "
								+ playlist).execute();
	}

	class SelectPlayListTask extends SimpleTask {

		private String[] mPlayLists;

		public SelectPlayListTask(AbstractConnectionActivity activity) {
			super(activity);
			setAction(new BackgroundAction() {

				@Override
				public void run() throws Exception {
					mPlayLists = mMedia.pls.getPlayLists();
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
}
