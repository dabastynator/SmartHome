package de.remote.mobile.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RemoteViews;
import android.widget.TextView;
import de.remote.gpiopower.api.IGPIOPower.Switch;
import de.remote.mobile.R;
import de.remote.mobile.database.PowerSwitchDao;
import de.remote.mobile.database.RemoteDatabase;

public class SelectSwitchActivity extends Activity {

	public static final String WIDGET_PREFS = "prefs";

	private TextView lableA;
	private TextView lableB;
	private TextView lableC;
	private TextView lableD;
	private int appWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.powerpoint);

		Bundle extras = getIntent().getExtras();
		appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);

		findComponents();
		RemoteDatabase serverDB = new RemoteDatabase(this);
		updateLables(serverDB.getPowerSwitchDao());
		serverDB.close();

	}

	private void findComponents() {
		View buttonA = findViewById(R.id.switch_a);
		View buttonB = findViewById(R.id.switch_b);
		View buttonC = findViewById(R.id.switch_c);
		View buttonD = findViewById(R.id.switch_d);

		buttonA.setVisibility(View.GONE);
		buttonB.setVisibility(View.GONE);
		buttonC.setVisibility(View.GONE);
		buttonD.setVisibility(View.GONE);

		lableA = (TextView) findViewById(R.id.powerlable_1);
		lableB = (TextView) findViewById(R.id.powerlable_2);
		lableC = (TextView) findViewById(R.id.powerlable_3);
		lableD = (TextView) findViewById(R.id.powerlable_4);
	}

	private void updateLables(PowerSwitchDao powerDao) {
		TextView[] views = new TextView[] { lableA, lableB, lableC, lableD };
		for (Switch s : Switch.values()) {
			String name = powerDao.getNameOfSwitch(s.ordinal());
			if (name == null)
				name = "Switch " + s.ordinal();
			views[s.ordinal()].setText(name);
			views[s.ordinal()].setOnClickListener(new SelectSwitchListener(s));
		}
	}

	public class SelectSwitchListener implements OnClickListener {
		private Switch s;

		public SelectSwitchListener(Switch s) {
			this.s = s;
		}

		@Override
		public void onClick(View v) {

			// Speichere Auswahl
			SharedPreferences prefs = SelectSwitchActivity.this
					.getSharedPreferences(WIDGET_PREFS, 0);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("" + appWidgetId, s.ordinal());
			edit.commit();

			AppWidgetManager manager = AppWidgetManager
					.getInstance(SelectSwitchActivity.this);
			RemoteViews views = new RemoteViews(SelectSwitchActivity.this.getPackageName(),
					R.layout.power_widget);
			manager.updateAppWidget(appWidgetId, views);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}

	}

}
