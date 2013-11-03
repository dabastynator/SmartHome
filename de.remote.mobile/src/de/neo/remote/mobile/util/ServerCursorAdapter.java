package de.remote.mobile.util;

import de.remote.mobile.R;
import de.remote.mobile.database.ServerTable;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * adapter for the list view to show a server with name, ip, and favorite icon.
 * 
 * @author sebastian
 */
public class ServerCursorAdapter extends SimpleCursorAdapter {

	public ServerCursorAdapter(Context context, Cursor c) {
		super(context, R.layout.favorite_row, c, new String[] {
				ServerTable.NAME, ServerTable.IP, ServerTable.FAVORITE },
				new int[] { R.id.lbl_server_name, R.id.lbl_server_ip,
						R.id.lbl_server_favorite });
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		String favorite = ((TextView) v.findViewById(R.id.lbl_server_favorite))
				.getText().toString();
		ImageView image = (ImageView) v.findViewById(R.id.img_favorite);
		if (favorite.equals("1")){
			image.setVisibility(View.VISIBLE);
		}else{
			image.setVisibility(View.INVISIBLE);
		}
		return v;
	}

}
