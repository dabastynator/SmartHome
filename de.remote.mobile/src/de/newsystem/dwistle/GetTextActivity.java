package de.newsystem.dwistle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class GetTextActivity extends Activity {

	public static final int RESULT_CODE = 1;

	public static final String RESULT = "result";

	private EditText text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gettext);
		findComponents();
	}

	private void findComponents() {
		text = (EditText) findViewById(R.id.txt_getText);
	}

	public void submit(View v) {
		Intent i = new Intent();
		i.putExtra(RESULT, text.getText().toString());
		setResult(RESULT_CODE, i);
		finish();
	}

}
