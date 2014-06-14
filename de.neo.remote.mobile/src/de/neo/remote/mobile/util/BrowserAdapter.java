package de.neo.remote.mobile.util;

import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.neo.remote.mediaserver.api.IBrowser;
import de.neo.remote.mediaserver.api.IThumbnailListener;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mediaserver.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.BrowserActivity.ViewerState;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

/**
 * the browser adapter sets the correct icon for the shown item.
 * 
 * @author sebastian
 */
public class BrowserAdapter extends ArrayAdapter<String> implements
		IThumbnailListener {

	public static final int PREVIEW_SIZE = 128;

	/**
	 * current viewer state
	 */
	private ViewerState viewerState;

	/**
	 * browser object
	 */
	private IBrowser browser;

	private Handler handler;

	private String path;

	private PlayingBean playingBean;

	private static Map<String, Bitmap> thumbnails = Collections
			.synchronizedMap(new HashMap<String, Bitmap>());

	public BrowserAdapter(Context context, IBrowser browser, String[] all,
			ViewerState state, PlayingBean bean) {
		super(context, R.layout.browser_row, R.id.lbl_item_name, all);
		handler = new Handler();
		thumbnails.clear();
		playingBean = bean;
		this.browser = browser;
		if (this.browser != null)
			new Thread() {
				public void run() {
					try {
						path = BrowserAdapter.this.browser.getFullLocation();
						BrowserAdapter.this.browser
								.fireThumbnails(BrowserAdapter.this,
										PREVIEW_SIZE, PREVIEW_SIZE);
					} catch (Exception e) {
					}
				}
			}.start();
		this.viewerState = state;
		this.setNotifyOnChange(true);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		try {
			ImageView image = (ImageView) v.findViewById(R.id.img_item);
			String file = ((TextView) v.findViewById(R.id.lbl_item_name))
					.getText().toString();
			if (thumbnails.containsKey(file))
				image.setImageBitmap(thumbnails.get(file));
			else
				switch (viewerState) {
				case DIRECTORIES:
					if (position < browser.getDirectories().length)
						image.setImageResource(R.drawable.folder);
					else {
						if (playingBean != null
								&& playingBean.getState() != STATE.DOWN
								&& playingBean.getPath() != null
								&& playingBean.getPath().equals(path + file)) {
							image.setImageResource(R.drawable.playing);
						} else if (file.toUpperCase(Locale.US).endsWith("MP3")
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
					}
					break;
				case PLAYLISTS:
					image.setImageResource(R.drawable.pls);
					break;
				case PLS_ITEMS:
					image.setImageResource(R.drawable.music);
					break;
				}
		} catch (RemoteException e) {

		}
		return v;
	}

	@Override
	public void setThumbnail(String file, int width, int height, int[] thumbnail)
			throws RemoteException {
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		IntBuffer buf = IntBuffer.wrap(thumbnail); // data is my array
		bm.copyPixelsFromBuffer(buf);
		thumbnails.put(file, bm);
		handler.post(new Runnable() {
			@Override
			public void run() {
				BrowserAdapter.super.notifyDataSetChanged();
			}
		});
	}

	public void setPlayingFile(PlayingBean bean) {
		this.playingBean = bean;
		notifyDataSetChanged();
	}

}
