package de.remote.mobile.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.gpiopower.api.IGPIOPower;
import de.remote.gpiopower.api.IGPIOPower.State;
import de.remote.gpiopower.api.IGPIOPower.Switch;
import de.remote.mobile.R;
import de.remote.mobile.util.BrowserAdapter;

public class PowerActivity extends BindedActivity {

	private ToggleButton buttonA;
	private ToggleButton buttonB;
	private ToggleButton buttonC;
	private ToggleButton buttonD;
	private IGPIOPower powerObject;
	private ProgressBar progress;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.powerpoint);

		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(false);

		findComponents();
	};

	private void findComponents() {
		buttonA = (ToggleButton) findViewById(R.id.switch_a);
		buttonB = (ToggleButton) findViewById(R.id.switch_b);
		buttonC = (ToggleButton) findViewById(R.id.switch_c);
		buttonD = (ToggleButton) findViewById(R.id.switch_d);
		progress = (ProgressBar) findViewById(R.id.power_progress);

		buttonA.setOnCheckedChangeListener(new SwitchChangeListener(Switch.A));
		buttonB.setOnCheckedChangeListener(new SwitchChangeListener(Switch.B));
		buttonC.setOnCheckedChangeListener(new SwitchChangeListener(Switch.C));
		buttonD.setOnCheckedChangeListener(new SwitchChangeListener(Switch.D));

		buttonA.setEnabled(false);
		buttonB.setEnabled(false);
		buttonC.setEnabled(false);
		buttonD.setEnabled(false);
		
		progress.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.power_pref, menu);
		return true;
	}

	@Override
	void binderConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	void remoteConnected() {
		powerObject = null;
		IGPIOPower power = binder.getPower();

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
	void disableScreen() {
		buttonA.setEnabled(false);
		buttonB.setEnabled(false);
		buttonC.setEnabled(false);
		buttonD.setEnabled(false);
	}

	public class SwitchChangeListener implements
			CompoundButton.OnCheckedChangeListener {

		private Switch powerSwitch;

		public SwitchChangeListener(Switch powerSwitch) {
			this.powerSwitch = powerSwitch;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				final boolean isChecked) {
			if (powerObject == null)
				return;
			new AsyncTask<Integer, Integer, String[]>() {

				Exception exeption = null;

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					setProgressBarVisibility(true);
					progress.setVisibility(View.VISIBLE);
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
					progress.setVisibility(View.GONE);
					if (result != null && result.length > 0)
						Toast.makeText(PowerActivity.this, result[0],
								Toast.LENGTH_LONG).show();
				}

			}.execute(new Integer[] {});
		}

	}

}
