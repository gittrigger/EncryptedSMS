package com.rsasms.activity;

import com.rsasms.DbAdapter;
import com.rsasms.R;
import com.rsasms.SqliteWrapper;


public class MessageView extends Activity {
	
	@Override
	protected void onStop() {
		Log.i(TAG,"onStop() ++++++++++++++++++++++++++++++++++++++++s");
		mDataStore.close();
		super.onStop();
	}

	@Override
	protected void onPause() {
		Log.i(TAG,"onPause() ++++++++++++++++++++++++++++++++++++++++s");
		mDataStore.close();
		super.onPause();
	}

	
	private static final String TAG = "RSASMS MessageView";
	
	private DbAdapter mDataStore;
	
	private String mMessageUri;
	private Bundle mIntentExtras;
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Log.i(TAG,"layout");
		setContentView(R.layout.message);
		
		Log.i(TAG,"database");	
		mDataStore = new DbAdapter(this);
		mDataStore.loadDb();
		
		
		Log.i(TAG,"layout views");
		
		mIntentExtras = getIntent().getExtras();
		//mMessageUri = mIntentExtras != null ? mIntentExtras.getString("messageUri") : "";
		
		long localSMSId = mIntentExtras != null ? mIntentExtras.getLong("localSMSId") : 0;
		EditText bodyText = (EditText) this.findViewById(R.id.message);
		TextView originText = (TextView) this.findViewById(R.id.originator);
		
		
		
		String[] localSMS = lookupLocalSMS(Uri.parse("content://sms/" + localSMSId) );
    	if( localSMS == null ){ Log.e(TAG,"localSMS didn't result in a record.");return; }
    	//long localSMSId   = Long.parseLong(localSMS[0]);
    	String originator = localSMS[1];
    	String message    = localSMS[2];
    	String date       = localSMS[3];
		
		
		
        //startManagingCursor(lCursor);
        
        
        //String msgBody = lCursor.getString(2);
        bodyText.setText(message);
        originText.setText(originator);
        
        
        mDataStore.close();
	
	}

	public String[] lookupLocalSMS(Uri messageUri){

		if( messageUri == null ){
			Log.e(TAG,"lookupLocalSMS() recieved a null arg, return null.");
			return null;
		}
		
		String[] rS = new String[4];
		Log.w(TAG,messageUri.toString() + " LOOKUP _id,address,body,date");
        Cursor localSMS = SqliteWrapper.query(this, getContentResolver(), messageUri, 
        		new String[] { "_id", "address", "body", "date" }, 
        		null, null, null);
        
        Log.w(TAG,"query completed");
        if (localSMS == null) {Log.w(TAG,"processRSARequestMessage() Unable to acquire message using null Uri.");return null;}
        if (!localSMS.moveToFirst()) {Log.w(TAG,"processRSARequestMessage() Unable to acquire message, doesn't exist.");return null;}
        rS[0] = localSMS.getString(0);
        rS[1] = localSMS.getString(1);
        rS[2] = localSMS.getString(2);
        rS[3] = localSMS.getString(3);
		
		return rS;
	}

}
