package com.rsasms.activity;

import com.rsasms.DbAdapter;
import com.rsasms.R;

public class Unsent extends ListActivity {
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		
		//String word = mDataStore.getString("messageStore", id, "word"); 
		Intent i = this.getIntent();
        i.putExtra("messageid", id);
        setResult(84, i);
        mDataStore.close();
        finish();

		
	}
	private static final String TAG = "RSASMS Unsent";
	private DbAdapter mDataStore;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//Log.i(TAG,"layout");
		setContentView(R.layout.list);
		
		//Log.i(TAG,"database");	
		mDataStore = new DbAdapter(this);
		mDataStore.loadDb();
				
        loadList();        
        
	}
	private void loadList() {
		// TODO Auto-generated method stub
		Cursor lCursor = mDataStore.detailQuery("messageStore", new String[] {"_id", "decrypted" ,"datetime("+DbAdapter.COL_TIMEUPDATE+"/1000, 'unixepoch', 'localtime') as date"}, "status = 1", null, null, null, DbAdapter.COL_TIMEUPDATE + " desc", null);
		//Cursor lCursor = mDataStore.detailQuery("wordStore", new String[] {"_id", "word"}, "status > 0", null, null, null, "updated", null);
		
		/*
		Cursor lCursor = SqliteWrapper.query(this, getContentResolver(), Uri.parse("content://sms"), 
        		new String[] { "_id", "address", "body", "date" }, 
        		"date > " + (System.currentTimeMillis() - ( 2 * 60 * 60 * 1000) ), 
        		null, 
        		"date desc");
		//*/
        startManagingCursor(lCursor);

        String[] from = new String[]{"decrypted","date" };
        int[] to = new int[]{R.id.listrowunsentDescription, R.id.listrowunsentDate};

        //setTitle("Manual Sending, " + lCursor.getCount() + " Entries");
        SimpleCursorAdapter entries = new SimpleCursorAdapter(this, R.layout.listrowunsent, lCursor, from, to);
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
