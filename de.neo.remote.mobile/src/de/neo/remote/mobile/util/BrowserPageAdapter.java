package de.neo.remote.mobile.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.fragments.FileFragment;
import de.neo.remote.mobile.fragments.PlaylistFragment;
import de.neo.remote.mobile.persistence.MediaServerState;

public class BrowserPageAdapter extends FragmentStatePagerAdapter {

	private FileFragment mFileFragment;
	private PlaylistFragment mPlaylistFragment;

	public BrowserPageAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public BrowserFragment getItem(int position) {
		if (position == 0) {
			if (mFileFragment == null)
				mFileFragment = new FileFragment();
			return mFileFragment;
		}
		if (position == 1) {
			if (mPlaylistFragment == null)
				mPlaylistFragment = new PlaylistFragment();
			return mPlaylistFragment;
		}
		return null;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container,
				position);
		if (fragment instanceof FileFragment)
			mFileFragment = (FileFragment) fragment;
		if (fragment instanceof PlaylistFragment)
			mPlaylistFragment = (PlaylistFragment) fragment;
		return fragment;
	}

	public void setMediaServer(MediaServerState server) {
		if (mFileFragment != null)
			mFileFragment.setMesiaServer(server);
		if (mPlaylistFragment != null)
			mPlaylistFragment.setMesiaServer(server);
	}

	public boolean onContextItemSelected(MenuItem item, int position) {
		Fragment fragment = getItem(position);
		return fragment.onContextItemSelected(item);
	}

	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		if (mFileFragment != null)
			mFileFragment.setPlayingFile(bean);
	}

	public void setThumbnail(String file, int width, int height, int[] thumbnail) {
		if (mFileFragment != null)
			mFileFragment.setThumbnail(file, width, height, thumbnail);
	}

	public static abstract class BrowserFragment extends ListFragment {

		public static final String LIST_POSITION = "save_position";

		protected Parcelable mListPosition;

		protected boolean mActive = false;

		public abstract void refreshContent(Context context);

		public abstract boolean onKeyDown(int keyCode, KeyEvent event);

		public abstract void setMesiaServer(MediaServerState mediaServer);

		@Override
		public void onStart() {
			super.onStart();
			mActive = true;
		}

		@Override
		public void onStop() {
			super.onStop();
			mActive = false;
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putParcelable(LIST_POSITION, getListView()
					.onSaveInstanceState());
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			if (savedInstanceState != null
					&& savedInstanceState.containsKey(LIST_POSITION))
				mListPosition = savedInstanceState.getParcelable(LIST_POSITION);
		}
	}

}
