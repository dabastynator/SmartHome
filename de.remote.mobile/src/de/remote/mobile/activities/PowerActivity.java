package de.remote.mobile.activities;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import de.remote.controlcenter.api.IControlUnit;
import de.remote.gpiopower.api.IInternetSwitch;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mobile.R;
import de.remote.mobile.services.PlayerBinder;
import de.remote.mobile.util.SwitchAdapter;

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
	void binderConnected() {
		// TODO Auto-generated method stub

	}

	public static Map<String, IInternetSwitch> getPower(PlayerBinder binder) {
		Map<String, IInternetSwitch> power = new HashMap<String, IInternetSwitch>();
		for (IControlUnit unit : binder.getUnits().keySet()) {
			Object object = binder.getUnits().get(unit);
			String name = binder.getUnitNames().get(unit);
			if (object instanceof IInternetSwitch) {
				power.put(name, (IInternetSwitch) object);
			}
		}
		return power;
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		if (binder.isConnected()) {
			Map<String, IInternetSwitch> power = getPower(binder);
			String[] switches = power.keySet()
					.toArray(new String[power.size()]);
			switchList.setAdapter(new SwitchAdapter(this, switches, power));
		} else {
			switchList
					.setAdapter(new SwitchAdapter(this, new String[] {}, null));
		}
	}

	@Override
	void startConnecting() {
	}

	@Override
	public void onPowerSwitchChange(String switchName, State state) {
	}

}
