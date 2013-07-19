package de.remote.mobile.util;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.gpiopower.api.IInternetSwitch;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mobile.R;
import de.remote.mobile.activities.SelectSwitchActivity.SelectSwitchListener;

public class SwitchAdapter extends ArrayAdapter<String> {

	private Map<String, IInternetSwitch> power;
	private String[] switchNames;
	private SelectSwitchListener listener = null;

	public SwitchAdapter(Context context, String[] all,
			Map<String, IInternetSwitch> power) {
		super(context, R.layout.switch_row, R.id.lbl_switch_name, all);
		this.power = power;
		this.switchNames = all;
	}

	public SwitchAdapter(Context context, String[] all,
			Map<String, IInternetSwitch> power, SelectSwitchListener listener) {
		this(context, all, power);
		this.listener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		ToggleButton button = (ToggleButton) view
				.findViewById(R.id.btn_switch_state);
		TextView lable = (TextView) view.findViewById(R.id.lbl_switch_name);
		String switchName = lable.getText().toString();
		OnClickSwitchListener switchListner = new OnClickSwitchListener(
				switchName);
		lable.setOnClickListener(switchListner);
		view.setOnClickListener(switchListner);
		button.setOnCheckedChangeListener(new SwitchChangeListener(power
				.get(switchName), button));
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

		private String name;

		public OnClickSwitchListener(String switchName) {
			this.name = switchName;
		}

		@Override
		public void onClick(View arg0) {
			if (listener != null)
				listener.onSelectSwitch(name);
		}

	}
}
