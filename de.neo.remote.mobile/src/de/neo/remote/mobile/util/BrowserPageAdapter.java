package de.neo.remote.mobile.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.KeyEvent;
import android.view.MenuItem;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.fragments.FileFragment;
import de.neo.remote.mobile.fragments.PlaylistFragment;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.services.RemoteService.StationStuff;

public class BrowserPageAdapter extends FragmentStatePagerAdapter {

	private PlaylistFragment mPlayListFragment;
	private FileFragment mFileFragment;
	public RemoteBinder mBinder;

	public BrowserPageAdapter(FragmentManager fm,
			AbstractConnectionActivity activity, StationStuff stationStuff) {
		super(fm);
		mBinder = activity.mBinder;
		mPlayListFragment = new PlaylistFragment(stationStuff);
		mFileFragment = new FileFragment(stationStuff);
	}

	@Override
	public Fragment getItem(int position) {
		if (position == 0)
			return mFileFragment;
		if (position == 1)
			return mPlayListFragment;
		return null;
	}

	@Override
	public int getCount() {
		return 2;
	}

	public boolean onContextItemSelected(MenuItem item, int position) {
		Fragment fragment = getItem(position);
		return fragment.onContextItemSelected(item);
	}

	public void onKeyDown(int keyCode, KeyEvent event, int position) {
		if (position == 0)
			mFileFragment.onKeyDown(keyCode, event);
		if (position == 1)
			mPlayListFragment.onKeyDown(keyCode, event);
	}

	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		mFileFragment.setPlayingFile(bean);
	}

	public void setThumbnail(String file, int width, int height, int[] thumbnail) {
		mFileFragment.setThumbnail(file, width, height, thumbnail);
	}

}
