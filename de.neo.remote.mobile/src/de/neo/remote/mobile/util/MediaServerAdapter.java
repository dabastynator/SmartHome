package de.neo.remote.mobile.util;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.neo.remote.mobile.activities.SelectMediaServerActivity.SelectMSListener;
import de.neo.smarthome.api.IWebMediaServer.BeanMediaServer;
import de.neo.smarthome.api.PlayingBean.STATE;
import de.remote.mobile.R;

public class MediaServerAdapter extends ArrayAdapter<String> {

	private List<BeanMediaServer> mServer;
	private SelectMSListener mListener = null;

	public MediaServerAdapter(Context context, List<BeanMediaServer> server, String[] ids, SelectMSListener listener) {
		super(context, R.layout.mediaserver_row, R.id.lbl_ms_id, ids);
		mServer = server;
		mListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		BeanMediaServer s = mServer.get(position);
		TextView name = (TextView) view.findViewById(R.id.lbl_ms_name);
		TextView id = (TextView) view.findViewById(R.id.lbl_ms_id);
		ImageView image = (ImageView) view.findViewById(R.id.lbl_ms_playing);
		name.setText(s.getName());
		id.setText(s.getID());
		if (s.getCurrentPlaying() != null && s.getCurrentPlaying().getState() == STATE.PLAY)
			image.setImageResource(R.drawable.player_small_play);
		else
			image.setImageBitmap(null);
		OnClickServerListener clickListner = new OnClickServerListener(s);
		view.setOnClickListener(clickListner);
		return view;
	}

	public class OnClickServerListener implements OnClickListener {

		BeanMediaServer mServer;

		public OnClickServerListener(BeanMediaServer webServer) {
			mServer = webServer;
		}

		@Override
		public void onClick(View arg0) {
			if (mListener != null)
				mListener.onSelectSwitch(mServer);
		}

	}
}
