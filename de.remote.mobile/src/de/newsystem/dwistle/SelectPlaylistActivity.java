package de.newsystem.dwistle;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectPlaylistActivity extends ListActivity {

	public static final String PLS_LIST = "plsList";
	public static final String RESULT = "result";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] plsList = (String[]) getIntent().getExtras()
				.getCharSequenceArray(PLS_LIST);
		getListView().setAdapter(
				new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, plsList));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String pls = ((TextView) v).getText().toString();
		Intent i = new Intent();
		i.putExtra(RESULT, pls);
		setResult(BrowserActivity.SELECT_PLS_CODE, i);
		finish();
	}
}
