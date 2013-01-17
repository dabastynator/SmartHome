package de.remote.mobile.util;

import java.util.Locale;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.newsystem.rmi.api.Oneway;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;
import de.remote.api.IThumbnailListener;
import de.remote.mobile.R;
import de.remote.mobile.activities.BrowserBase.ViewerState;

/**
 * the browser adapter sets the correct icon for the shown item.
 * 
 * @author sebastian
 */
public class BrowserAdapter extends ArrayAdapter<String> implements
		IThumbnailListener {

	/**
	 * current viewer state
	 */
	private ViewerState viewerState;

	/**
	 * browser object
	 */
	private IBrowser browser;

	public BrowserAdapter(Context context, IBrowser browser, String[] all,
			ViewerState state) {
		super(context, R.layout.browser_row, R.id.lbl_item_name, all);
		this.browser = browser;
		try {
			this.browser.addThumbnailListener(this);
		} catch (RemoteException e) {
		}
		this.viewerState = state;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		try {
			ImageView image = (ImageView) v.findViewById(R.id.img_item);
			switch (viewerState) {
			case DIRECTORIES:
				if (position < browser.getDirectories().length)
					image.setImageResource(R.drawable.folder);
				else {
					String file = ((TextView) v
							.findViewById(R.id.lbl_item_name)).getText()
							.toString();
					if (file.toUpperCase(Locale.US).endsWith("MP3")
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
	@Oneway
	public void setThumbnail(String file, byte[] thumbnail)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

}
