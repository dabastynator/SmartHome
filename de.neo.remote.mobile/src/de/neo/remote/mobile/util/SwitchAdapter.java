package de.neo.remote.mobile.util;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.neo.remote.api.IInternetSwitch;
import de.neo.remote.api.IWebSwitch.State;
import de.neo.remote.mobile.activities.SelectSwitchActivity.SelectSwitchListener;
import de.neo.remote.mobile.services.RemoteService.BufferdUnit;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

public class SwitchAdapter extends ArrayAdapter<String> {

	private Map<String, BufferdUnit> mSwitches;
	private SelectSwitchListener mListener = null;
	private String[] mIDs;

	public SwitchAdapter(Context context, String[] ids,
			Map<String, BufferdUnit> switches, SelectSwitchListener listener) {
		super(context, R.layout.switch_row, R.id.lbl_switch_name, ids);
		mSwitches = switches;
		mIDs = ids;
		mListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		String id = mIDs[position];
		BufferdUnit internetSwitch = mSwitches.get(id);
		ToggleButton button = (ToggleButton) view
				.findViewById(R.id.btn_switch_state);
		TextView lable = (TextView) view.findViewById(R.id.lbl_switch_name);
		lable.setText(internetSwitch.mName);
		OnClickSwitchListener switchListner = new OnClickSwitchListener(id,
				internetSwitch.mName);
		lable.setOnClickListener(switchListner);
		view.setOnClickListener(switchListner);
		button.setOnCheckedChangeListener(new SwitchChangeListener(
				(IInternetSwitch) internetSwitch.mObject, button));
		return view;
	}

	public class SwitchChangeListener implements
			CompoundButton.OnCheckedChangeListener {

		private IInternetSwitch power;
		private ToggleButton button;

		public SwitchChangeListener(IInternetSwitch power, ToggleButton button) {
			this.power = power;
			this.button = button;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				final boolean isChecked) {
			if (power == null)
				return;
			new AsyncTask<Integer, Integer, String[]>() {

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					((Activity) getContext()).setProgressBarVisibility(true);
					button.setEnabled(false);
				}

				@Override
				protected String[] doInBackground(Integer... params) {
					try {
						if (isChecked)
							power.setState(State.ON);
						else
							power.setState(State.OFF);
					} catch (RemoteException e) {
						e.printStackTrace();
						return new String[] { e.getMessage() };
					}
					return null;
				}

				@Override
				protected void onPostExecute(String[] result) {
					super.onPostExecute(result);
					((Activity) getContext()).setProgressBarVisibility(false);
					button.setEnabled(true);
					if (result != null && result.length > 0)
						Toast.makeText(getContext(), result[0],
								Toast.LENGTH_LONG).show();
				}

			}.execute(new Integer[] {});
		}
	}

	public class OnClickSwitchListener implements OnClickListener {

		private String mID;
		private String mName;

		public OnClickSwitchListener(String id, String name) {
			mID = id;
			mName = name;
		}

		@Override
		public void onClick(View arg0) {
			if (mListener != null)
				mListener.onSelectSwitch(mID, mName);
		}

	}
}
