package de.neo.smarthome.mobile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import de.neo.smarthome.mobile.services.RemoteService;
import de.remote.mobile.R;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String TRIGGER = "de.neo.smarthome.connection_trigger";
	public static final String FOREGROUND = "de.neo.smarthome.stay_foreground";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Use deprecated method to support older devices
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
		Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
		root.addView(bar, 0); // insert at top
		bar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Intent serviceIntent = new Intent(this, RemoteService.class);
		serviceIntent.setAction(RemoteService.ACTION_FOREGROUND);
		startService(serviceIntent);
	}

}
