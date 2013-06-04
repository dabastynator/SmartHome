package de.remote.mobile.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.gpiopower.api.IGPIOPower;
import de.remote.gpiopower.api.IGPIOPower.State;
import de.remote.gpiopower.api.IGPIOPower.Switch;
import de.remote.mobile.R;
import de.remote.mobile.util.AI;

public class PowerActivity extends BindedActivity {

	private ToggleButton buttonA;
	private ToggleButton buttonB;
	private ToggleButton buttonC;
	private ToggleButton buttonD;
	private IGPIOPower powerObject;

	/**
	 * The artificial intelligence recognize speech
	 */
	protected AI ai;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    requestWindowFeature(Window.FEATURE_PROGRESS);
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(false);

		setContentView(R.layout.powerpoint);
		
		findComponents();
	};

	private void findComponents() {
		buttonA = (ToggleButton) findViewById(R.id.switch_a);
		buttonB = (ToggleButton) findViewById(R.id.switch_b);
		buttonC = (ToggleButton) findViewById(R.id.switch_c);
		buttonD = (ToggleButton) findViewById(R.id.switch_d);

		buttonA.setOnCheckedChangeListener(new SwitchChangeListener(Switch.A,
				buttonA));
		buttonB.setOnCheckedChangeListener(new SwitchChangeListener(Switch.B,
				buttonB));
		buttonC.setOnCheckedChangeListener(new SwitchChangeListener(Switch.C,
				buttonC));
		buttonD.setOnCheckedChangeListener(new SwitchChangeListener(Switch.D,
				buttonD));

		buttonA.setEnabled(false);
		buttonB.setEnabled(false);
		buttonC.setEnabled(false);
		buttonD.setEnabled(false);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.power_pref, menu);
		ai = new AI(this);
		return true;
	}

	@Override
	void binderConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServerConnectionChanged(String serverName) {
		powerObject = null;
		IGPIOPower power = binder.getPower();
		
		if (power == null){
			setTitle("No Power@" + binder.getServerName());
			buttonA.setEnabled(false);
			buttonB.setEnabled(false);
			buttonC.setEnabled(false);
			buttonD.setEnabled(false);
			return;
		}

		setTitle("Power@" + binder.getServerName());

		try {

			buttonA.setChecked(power.getState(Switch.A) == State.ON);
			buttonB.setChecked(power.getState(Switch.B) == State.ON);
			buttonC.setChecked(power.getState(Switch.C) == State.ON);
			buttonD.setChecked(power.getState(Switch.D) == State.ON);

			powerObject = power;
			buttonA.setEnabled(true);
			buttonB.setEnabled(true);
			buttonC.setEnabled(true);
			buttonD.setEnabled(true);
		} catch (RemoteException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	@Override
	void startConnecting() {
		buttonA.setEnabled(false);
		buttonB.setEnabled(false);
		buttonC.setEnabled(false);
		buttonD.setEnabled(false);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_record:
			ai.record();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public class SwitchChangeListener implements
			CompoundButton.OnCheckedChangeListener {

		private Switch powerSwitch;
		private ToggleButton button;

		public SwitchChangeListener(Switch powerSwitch, ToggleButton button) {
			this.powerSwitch = powerSwitch;
			this.button = button;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				final boolean isChecked) {
			if (powerObject == null)
				return;
			new AsyncTask<Integer, Integer, String[]>() {

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					setProgressBarVisibility(true);
					button.setEnabled(false);
				}

				@Override
				protected String[] doInBackground(Integer... params) {
					try {
						if (isChecked)
							powerObject.setState(State.ON, powerSwitch);
						else
							powerObject.setState(State.OFF, powerSwitch);
					} catch (RemoteException e) {
						e.printStackTrace();
						return new String[] { e.getMessage() };
					}
					return null;
				}

				@Override
				protected void onPostExecute(String[] result) {
					super.onPostExecute(result);
					setProgressBarVisibility(false);
					button.setEnabled(true);
					if (result != null && result.length > 0)
						Toast.makeText(PowerActivity.this, result[0],
								Toast.LENGTH_LONG).show();
				}

			}.execute(new Integer[] {});
		}

	}

}
