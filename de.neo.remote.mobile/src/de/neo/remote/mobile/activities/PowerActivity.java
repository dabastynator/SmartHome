package de.neo.remote.mobile.activities;

import java.util.Map;

import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import de.neo.remote.gpiopower.api.IInternetSwitch;
import de.neo.remote.gpiopower.api.IInternetSwitch.State;
import de.neo.remote.mobile.util.SwitchAdapter;
import de.remote.mobile.R;

public class PowerActivity extends BindedActivity {

	public static final String SWITCH_NUMBER = "switch_number";

	protected ListView switchList;

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
		switchList = (ListView) findViewById(R.id.list_switches);
	}

	@Override
	void onBinderConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		if (binder.isConnected()) {
			Map<String, IInternetSwitch> power = binder.getPower();
			String[] switches = power.keySet()
					.toArray(new String[power.size()]);
			switchList.setAdapter(new SwitchAdapter(this, switches, power));
		} else {
			switchList
					.setAdapter(new SwitchAdapter(this, new String[] {}, null));
		}
	}

	@Override
	void onStartConnecting() {
	}

	@Override
	public void onPowerSwitchChange(String switchName, State state) {
	}

}
