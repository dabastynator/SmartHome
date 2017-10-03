package de.neo.smarthome.mobile.util;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;
import de.neo.smarthome.mobile.activities.SelectSwitchActivity.SelectSwitchListener;
import de.neo.smarthome.mobile.api.IWebSwitch;
import de.remote.mobile.R;

public class SwitchAdapter extends ArrayAdapter<String> {

	private List<IWebSwitch.BeanSwitch> mSwitches;
	private SelectSwitchListener mListener = null;

	public SwitchAdapter(Context context, List<IWebSwitch.BeanSwitch> switches, String[] ids, SelectSwitchListener listener) {
		super(context, R.layout.switch_row, R.id.lbl_switch_name, ids);
		mSwitches = switches;
		mListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		IWebSwitch.BeanSwitch s = mSwitches.get(position);
		ToggleButton button = (ToggleButton) view.findViewById(R.id.btn_switch_state);
		TextView lable = (TextView) view.findViewById(R.id.lbl_switch_name);
		lable.setText(s.getName());
		OnClickSwitchListener switchListner = new OnClickSwitchListener(s);
		lable.setOnClickListener(switchListner);
		view.setOnClickListener(switchListner);
		button.setChecked(s.getState() == IWebSwitch.State.ON);
		button.setFocusable(false);
		return view;
	}

	public class OnClickSwitchListener implements OnClickListener {

		IWebSwitch.BeanSwitch mSwitch;

		public OnClickSwitchListener(IWebSwitch.BeanSwitch webSwitch) {
			mSwitch = webSwitch;
		}

		@Override
		public void onClick(View arg0) {
			if (mListener != null)
				mListener.onSelectSwitch(mSwitch);
		}

	}
}
