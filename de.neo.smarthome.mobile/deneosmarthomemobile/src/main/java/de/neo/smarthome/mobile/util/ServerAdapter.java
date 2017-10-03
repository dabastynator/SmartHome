package de.neo.smarthome.mobile.util;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.neo.smarthome.mobile.persistence.RemoteServer;
import de.remote.mobile.R;

/**
 * adapter for the list view to show a server with name, ip, and favorite icon.
 * 
 * @author sebastian
 */
public class ServerAdapter extends ArrayAdapter<RemoteServer> {

	public ServerAdapter(Context context, List<RemoteServer> serverList) {
		super(context, R.layout.server_row, R.id.server_row_name, serverList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		RemoteServer server = getItem(position);
		ImageView image = (ImageView) v.findViewById(R.id.server_row_img);
		TextView ip = (TextView) v.findViewById(R.id.server_row_ip);

		ip.setText(server.getEndPoint());
		if (server.isFavorite()) {
			image.setVisibility(View.VISIBLE);
		} else {
			image.setVisibility(View.INVISIBLE);
		}
		return v;
	}

}
