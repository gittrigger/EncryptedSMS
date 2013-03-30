package com.rsasms.activity;

import com.rsasms.DbAdapter;
import com.rsasms.R;

public class WordList extends ListActivity {

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		String word = mDataStore.getString("wordStore", id, "word");
		Intent i = this.getIntent();
		i.putExtra("word", word);
		setResult(79, i);
		mDataStore.close();
		finish();

	}

	private static final String TAG = "RSASMS WordList";
	private DbAdapter mDataStore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Log.i(TAG,"layout");
		setContentView(R.layout.list);

		// Log.i(TAG,"database");
		mDataStore = new DbAdapter(this);
		mDataStore.loadDb();

		loadList();

	}

	private void loadList() {
		// TODO Auto-generated method stub
		Cursor lCursor = mDataStore.detailQuery("wordStore", new String[] {
				"_id", "word" }, "status > 0", null, null, null, "word", null);
		// Cursor lCursor = mDataStore.detailQuery("wordStore", new String[]
		// {"_id", "word"}, "status > 0", null, null, null, "updated", null);

		/*
		 * Cursor lCursor = SqliteWrapper.query(this, getContentResolver(),
		 * Uri.parse("content://sms"), new String[] { "_id", "address", "body",
		 * "date" }, "date > " + (System.currentTimeMillis() - ( 2 * 60 * 60 *
		 * 1000) ), null, "date desc"); //
		 */
		startManagingCursor(lCursor);

		String[] from = new String[] { "word" };
		int[] to = new int[] { R.id.listrowword };

		// setTitle("Manual Sending, " + lCursor.getCount() + " Entries");
		SimpleCursorAdapter entries = new SimpleCursorAdapter(this,
				R.layout.listrowword, lCursor, from, to);
		setListAdapter(entries);
		getListView().setTextFilterEnabled(true);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mDataStore.close();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mDataStore = new DbAdapter(this);
		mDataStore.loadDb();
		loadList();
	}

}
