package de.remote.mobile.activies;

import de.remote.mobile.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * activity for simple text input.
 * 
 * @author sebastian
 */
public class GetTextActivity extends Activity {

	/**
	 * result code for this activity
	 */
	public static final int RESULT_CODE = 1;

	/**
	 * name of the extra data value
	 */
	public static final String RESULT = "result";

	/**
	 * name of default value for the text
	 */
	public static final String DEFAULT_TEXT = "dafaultValue";

	/**
	 * textfield with the value
	 */
	private EditText text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gettext);
		findComponents();
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey(DEFAULT_TEXT))
			text.setText(getIntent().getExtras().getString(DEFAULT_TEXT));
	}

	/**
	 * find all components by their id
	 */
	private void findComponents() {
		text = (EditText) findViewById(R.id.txt_getText);
	}

	/**
	 * perform submit, return value
	 * 
	 * @param v
	 */
	public void submit(View v) {
		Intent i = new Intent();
		i.putExtra(RESULT, text.getText().toString());
		setResult(RESULT_CODE, i);
		finish();
	}

}
