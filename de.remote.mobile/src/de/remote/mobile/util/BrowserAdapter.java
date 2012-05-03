package de.remote.mobile.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;
import de.remote.mobile.R;
import de.remote.mobile.activies.BrowserBase.ViewerState;

/**
 * the browser adapter sets the correct icon for the shown item.
 * 
 * @author sebastian
 */
public class BrowserAdapter extends ArrayAdapter<String> {

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
					if (file.toUpperCase().endsWith("MP3")
							|| file.toUpperCase().endsWith("OGG")
							|| file.toUpperCase().endsWith("WAV"))
						image.setImageResource(R.drawable.music);
					else if (file.toUpperCase().endsWith("AVI")
							|| file.toUpperCase().endsWith("MPEG")
							|| file.toUpperCase().endsWith("MPG"))
						image.setImageResource(R.drawable.movie);
					else if (file.toUpperCase().endsWith("JPG")
							|| file.toUpperCase().endsWith("GIF")
							|| file.toUpperCase().endsWith("BMP")
							|| file.toUpperCase().endsWith("PNG"))
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

}
