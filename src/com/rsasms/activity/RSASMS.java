package com.rsasms.activity;

import com.rsasms.DbAdapter;
import com.rsasms.MessageProcessing;
import com.rsasms.R;
import com.rsasms.RSA;
import com.rsasms.SqliteWrapper;

//import android.widget.ZoomControls;

public class RSASMS extends ListActivity implements OnClickListener, OnFocusChangeListener, OnKeyListener, OnLongClickListener, OnCheckedChangeListener, OnTouchListener {
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		if( mMode == "compose" ){
			Intent i = new Intent(this, MessageView.class);
	    	i.putExtra("localSMSId", id);
	    	//i.putExtra("messageid", mMessageId);
	    	startActivityForResult(i,54);
		}
		
		if( mMode == "to" ){
			for( int i = 0; i < mContactId.length; i ++){
				if( mContactId[i] == id ){
					addRecipient(i);
					break;
				}
			}
			
		}
	}



	private static final String TAG = "RSASMS";
	
    private DbAdapter mDataStore;
    private ContentResolver mContentResolver;
	private Bundle mIntentExtras;
    private String mMode;	
    private String mPhoneNumber;
	private long mMessageId;
	private String mQueue;
	private int mOutboxCount, mSentCount, mInboxCount, mDraftCount, mMessageCount, mWordCount;
	private RSA mRSA;

	// Temporary Space
	private String mTempString;
	private int mTempInt, mTempInt1;
	private char[] mTempChar1, mTempChar2, mTempChar3, mTempChar4;
	
    // Layout
	private EditText mCompose, mTitle;
	private LinearLayout mRecipientbar, mSelectorbar;
	private Button mRecipientstart;
	private ToggleButton mRSAToggle;
	private Button mRecipientlist;
	private Button mSelector1, mSelector2, mSelector3, mSelector4, mSelector5;
	private View mSelectorButton;
	private ListView mSMSListView; //LiveView, LinearView April 30 2009
	private int TRACKBALL_ACTION_SPACE = 300;
	private double TRACKBALL_FORCE_UP = .3;
	private double TRACKBALL_FORCE_DOWN = .3;

	// Compose
	private String mLastConsoleKeyTyped;
	private int mLastConsoleKey; //, mLastKeySpaceAt;
	private static final String SELECTOR_TEXT_A = "DCS";
	private static final String SELECTOR_TEXT_C = "Send";
	//private static final String SELECTOR_TEXT_N = "Send Now";
	private static final String SELECTOR_TEXT_T = "Contact";
	private static final String SELECTOR_TEXT_D = "Write";
	private static final String SELECTOR_TEXT_N = "New";
	//private static final String SELECTOR_TEXT_H = "Past";
	private static final String SELECTOR_TEXT_U = "Unsent";
	private static final String SELECTOR_TEXT_P = "Process"; //Process, Run, Process
	
	//private static final String SELECTOR_TEXT_OUTBOX = "Outbox";

    // Recipient
	private int mRecipientCount;
		
	// Contact
	private String[] mContactSelection;
	private String[] mContactName;
	private String[] mContactNumber;
	private long[] mContactId;
    private int mContactCount;
    private int mRecipientUseRSAOnce;
    
    // Word
    private String[] mWord;
    
	// Selector
    private static final int SELECTOR_MAX_HINT_CHARACTERS = 3;
	private int mSelectorValue1, mSelectorValue2, mSelectorValue3, mSelectorValue4, mSelectorValue5;
	private int mSelectorSize1, mSelectorSize2, mSelectorSize3, mSelectorSize4, mSelectorSize5;
	private int mReturnSelectorFocus, mLastSelectorFocus;
	private int[][] mSelectorHistory;
	private int mSelectorHistoryCursor;
	private long mActionEventTime;
	// start, size,
	private static final int SELECTOR_START = 1;
	private static final int SELECTOR_SIZE = 2;
	private static final int SELECTOR_HISTORY_GROW = 100; //100
	private static final int SELECTOR_HISTORY_BASE = 100; //100 Code will take care of +1, 0 doesn't count as a space
	
	// Sensor
	private SensorManager mSensorManagerOriginal;
	public int SENSOR_RESOLUTION = 1000; // 1 second intervals
	private long mSensorTimeframe;
	private int[] mSensorOrientation; // 0:x 1:y 2:z // 0:low 1:high 2:all 3:count aggregation method
	private int[] mSensorMagneticField; // 0:x 1:y 2:z // 0:low 1:high 2:all 3:count aggregation method
	private int[] mSensorAccelerometer; // 0:x 1:y 2:z // 0:low 1:high 2:all 3:count aggregation method
	private int[][][][] mSensorArray;
	private static final int SENSOR_ORIENTATION = 1;
	private static final int SENSOR_ACCELERATION = 2;
	private static final int SENSOR_MAGNETICFIELD = 3;
	private static final int SENSOR_LIGHT = 4;
	private Date mSensorDate;
	
	// Data Aggregation
	private static final int AGGREGATION_LOW   = 1;
	private static final int AGGREGATION_HIGH  = 2;
	private static final int AGGREGATION_ALL   = 3;
	private static final int AGGREGATION_COUNT = 4;
	private static final int AGGREGATION_DEPTH = 5;
	private static final int AGGREGATION_LASTCHANGE = 6;
	private static final int AGGREGATION_LASTCHANGEP1 = 7;
	private static final int AGGREGATION_LASTCHANGEP2 = 8;

	// Lookup SMS and Contacts Projections
	static final String[] PROJECTION = new String[] {
		//MmsSms.TYPE_DISCRIMINATOR_COLUMN, BaseColumns._ID, Conversations.THREAD_ID 
        "transport_type", "_id", "thread_id", //MmsSms.TYPE_DISCRIMINATOR_COLUMN
        // For SMS
        //Sms.ADDRESS, Sms.BODY, Sms.DATE, Sms.READ, Sms.TYPE, Sms.STATUS,
        "address", "body", "date", "read", "type", "status",
        // For MMS
        //Mms.SUBJECT, Mms.SUBJECT_CHARSET, Mms.DATE, Mms.READ, Mms.MESSAGE_TYPE, Mms.MESSAGE_BOX, Mms.DELIVERY_REPORT, Mms.READ_REPORT, PendingMessages.ERROR_TYPE,
        "sub", "sub_cs", "date", "read", "m_type", "msg_box", "d_rpt", "rr","err_type"
        
    };
	private static final String[] PEOPLE_PROJECTION = new String[] {
        //Contacts.People._ID, Contacts.People.PRIMARY_PHONE_ID, Contacts.People.TYPE, Contacts.People.NUMBER, Contacts.People.NUMBER_KEY, Contacts.People.LABEL, Contacts.People.NAME,
        Contacts.People._ID, Contacts.People.NUMBER_KEY, Contacts.People.NAME
    };
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//Log.i(TAG,"onSaveInstanceState() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		outState.putLong("messageid", mMessageId);
		outState.putString("mode", mMode);
	}
	@Override
	protected void onPause() {
		super.onPause();
//Log.i(TAG,"onPause() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		
		//mgr.unregisterListener(mSensorListener);
		saveState();
		mDataStore.close();
	}
	@Override
	protected void onStop() {
		super.onStop();
//Log.i(TAG,"onStop() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}
	@Override
	public Object onRetainNonConfigurationInstance() {
//Log.i(TAG,"onRetainNonConfigurationInstance() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		return super.onRetainNonConfigurationInstance();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
//Log.i(TAG,"onDestroy() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		mDataStore.close();
	}
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_LEFT_ICON);
	        boolean shortLife = false; // demo mode works for xTime
	        
	        
//Log.i(TAG,"onCreate() ++++++++++++++++++++++++++++++");
	        mRecipientUseRSAOnce = 0;
	        setContentView(R.layout.main);
	        //this.dispa
	
	        // -----------------------------------------------
	        // Data Storage
	        if( mDataStore != null ){
//Log.e(TAG,"Interesting, is it possible that mDataStore exists before onCreate()?");
		        if( mDataStore.isOpen() ){
//Log.e(TAG,"Interesting, is it possible that mDataStore remained open?");
		        	mDataStore.close();
		        }
	        }
	        mDataStore = new DbAdapter(this);
	        mDataStore.loadDb();
	        
	        if( shortLife ){
	        	int count = mDataStore.getCount("messageStore","created < " + (System.currentTimeMillis() - 24 * 60 * 60 * 1000) );
				if( count > 1 ){
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putString("message", "Your trial of RSA SMS has expired, uninstall and reinstall.");
					b.putInt("duration", 5000);
					msg.setData(b);
					//msg.setData(new Bundle().putString("message", "Leaving contacts open."));
					mToastHandler.sendMessage(msg);
					SystemClock.sleep(3000);
					finish();
				}else{
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putString("message", "RSA SMS Trial lasts 24 hours then you must uninstall and reinstall. Currently priced at $1, search the Market for \"rsa\".");
					b.putInt("duration", 3000);
					msg.setData(b);
					//msg.setData(new Bundle().putString("message", "Leaving contacts open."));
					mToastHandler.sendMessage(msg);
				}
	        }
	        
	        
	    	/* Persistence */
	    	String mode = savedInstanceState != null ? savedInstanceState.getString("mode") : "compose";
	    	setMode(mode);
//Log.w(TAG,"Setting mode to savedInstanceState.getString(\"mode\") or compose set("+mMode+")");
	    	
	    	//setMode();
	    	
	    	SharedPreferences sharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
	    	
	    	int runcount = sharedPreferences.contains("runcount") ? sharedPreferences.getInt("runcount", 0) : 0;
			mMessageId = savedInstanceState != null ? savedInstanceState.getLong("messageid") : 0;
			if (mMessageId == 0) {
				mMessageId = mIntentExtras != null ? mIntentExtras.getLong("messageid") : 0;
				//add more here
			}
			if( mMessageId == 0 ){
		        mMessageId = sharedPreferences.getLong("mMessageId", 0);
		        if( mMessageId > 0 ){
		        	Log.w(TAG,"Reloading from preferences.");
		        }
	    	}
			if( mMessageId > 0 ){
//Log.i(TAG,"messageStore UPDATE status to working(1)");
				mDataStore.updateEntry("messageStore", mMessageId, "status", 1); // working
			}else{
			/**/
				
			}
			if( runcount == 0 ){
	        	Intent i = new Intent(this, DialogBox.class);
	        	i.putExtra("title", "Welcome, let's get started.");
	        	i.putExtra("text", "Press Verify to watch the process unfold as you send an encrypted message to yourself.");
	        	//startActivity(i);
	        	mDataStore.close();
	        	startActivityForResult(i,99);
			}
			Editor spe = sharedPreferences.edit();
			spe.putInt("runcount", runcount+1);
			spe.commit();
	        
	        // -----------------------------------------------        
	        mContentResolver = getContentResolver();
	        mIntentExtras = getIntent().getExtras();
	        mLastConsoleKeyTyped = "";
	    	mLastConsoleKey = 0;
	    	//mLastKeySpaceAt = 0;
	    	mLastSelectorFocus = 0;
	    	mReturnSelectorFocus = 0;
	    	mTempString = "";
	    	mTempInt    = 0;
	    	mSelectorHistory = new int[SELECTOR_HISTORY_BASE+1][3];
	    	mSensorTimeframe = 0;
	    	mSensorOrientation = new int[] {0,0,0,0,0,0,0,0};
	    	mSensorAccelerometer = new int[] {0,0,0,0,0,0,0,0};
	    	mSensorMagneticField = new int[] {0,0,0,0,0,0,0,0};
	    	// null, LOW, HIGH, ALL, COUNT, DEPTH
	    	// [SENSOR][STORAGE][MINUTE][SECOND]
	    	mSensorArray = new int[5][10][10][60];
	    	mRecentLongClick = 0;
	    	mActionEventTime = 0;
	    	mOutboxCount = 0;
	        mSentCount = 0;
	        mDraftCount = 0;
	        mInboxCount = 0;
	        mWordCount = 0;

	
	
	        // -----------------------------------------------
	        // Layout Elements
	    	mSMSListView = (ListView) this.findViewById(android.R.id.list);
			mRecipientbar = (LinearLayout) this.findViewById(R.id.recipientbar);
			mRecipientstart = (Button) this.findViewById(R.id.recipientstart);
				mRecipientstart.setOnClickListener(this);
	        mRecipientlist = (Button) this.findViewById(R.id.recipientlist);
	        	mRecipientlist.setOnClickListener(this);
	        mTitle = (EditText) this.findViewById(R.id.messagetitle);
	        mCompose = (EditText) this.findViewById(R.id.compose);
	        	mCompose.setOnFocusChangeListener(this);
	        	mCompose.setOnKeyListener(this);
	        	mCompose.setOnTouchListener(this);
	        	mCompose.setMaxLines(5);
	        	//mCompose.setClickable(true);
	        	//mCompose.setOnClickListener(this);
	        mRSAToggle = (ToggleButton) this.findViewById(R.id.useRSA);
	        	mRSAToggle.setChecked(true);
	        	mRSAToggle.setOnCheckedChangeListener(this);
	        	mRSAToggle.setOnLongClickListener(this);
	        
	        mSelectorbar = (LinearLayout) this.findViewById(R.id.selectorbar);
	        mSelectorButton = null;
	        mSelector1 = (Button) this.findViewById(R.id.selector1);
	        	mSelector1.setOnClickListener(this);
	        	mSelector1.setOnFocusChangeListener(this);
	        	mSelector1.setOnLongClickListener(this);
	        	mSelector1.setOnTouchListener(this);
	        mSelector2 = (Button) this.findViewById(R.id.selector2);
	        	mSelector2.setOnClickListener(this);
	        	mSelector2.setOnFocusChangeListener(this);
	        	mSelector2.setOnLongClickListener(this);
	        	mSelector2.setOnTouchListener(this);
	        mSelector3 = (Button) this.findViewById(R.id.selector3);
	        	mSelector3.setOnClickListener(this);
	        	mSelector3.setOnFocusChangeListener(this);
	        	mSelector3.setOnLongClickListener(this);
	        	mSelector3.setOnTouchListener(this);
	        mSelector4 = (Button) this.findViewById(R.id.selector4);
	        	mSelector4.setOnClickListener(this);
	        	mSelector4.setOnFocusChangeListener(this);
	        	mSelector4.setOnLongClickListener(this);
	        	mSelector4.setOnTouchListener(this);
	        mSelector5 = (Button) this.findViewById(R.id.selector5);
	        	mSelector5.setOnClickListener(this);
	        	mSelector5.setOnFocusChangeListener(this);
	        	mSelector5.setOnLongClickListener(this);//startActivity(new Intent(ACTION_DIAL, Uri.parse("tel:6365551212")));
	        	mSelector5.setOnTouchListener(this);
	        
	        	
				
	        
	        populateFields(); //
	        
	        
	        
	        // check out onStart for intial window view
	
	        
	        //Calendar c = new Calendar();
	        //mSensorDate = new Date();
	        //Date sd = new java.sql.Date();
	        //Toast.makeText(this, "Activating Sensor Array", 800).show();
	        //SystemClock.sleep(1000);
	        //mSensorDate = new Date();// (new Date().getMinutes()) would be nice but I need two places to be more effective
	        //Toast.makeText(this, "\n\n-\nActivating Sensor Array " + mSensorDate.getMinutes() + ":" + mSensorDate.getSeconds(), 1000).show();
	        
	    	/**//*
	    	mSensorManagerOriginal = (SensorManager) this.getSystemService(SENSOR_SERVICE);
	    	mSensorManagerOriginal.registerListener(new SensorListener() {
	
				public void onAccuracyChanged(int sensor, int accuracy) {
//Log.w(TAG,"XXXXXXXXXXXXXXXXXXXXXX onAccuracyChanged() sensor("+sensor+") accuracy("+accuracy+")");
					
				}
	
				public void onSensorChanged(int sensor, float[] values) {
					
					long time = ( System.currentTimeMillis() / SENSOR_RESOLUTION );// millisecond base, /100=0.1 second
					if( time != mSensorTimeframe ){
//Log.i(TAG,"onSensorChanged()");
						mSensorTimeframe = time;
						if( mSensorOrientation[AGGREGATION_COUNT] > 0 ){
							mSensorOrientation[AGGREGATION_LASTCHANGEP2] = mSensorOrientation[AGGREGATION_LASTCHANGEP1];
							mSensorOrientation[AGGREGATION_LASTCHANGEP1] = mSensorOrientation[AGGREGATION_LASTCHANGE];
							mSensorOrientation[AGGREGATION_LASTCHANGE] = (mSensorOrientation[AGGREGATION_HIGH]-mSensorOrientation[AGGREGATION_LOW]);
							if( mSensorOrientation[AGGREGATION_LASTCHANGE] > mSensorOrientation[AGGREGATION_LASTCHANGEP1]){
								if(mSensorOrientation[AGGREGATION_DEPTH] > 0){
									mSensorOrientation[AGGREGATION_DEPTH]++; 
								}else{
									mSensorOrientation[AGGREGATION_DEPTH] = 1;
								}
							} else if ( mSensorOrientation[AGGREGATION_LASTCHANGE] < mSensorOrientation[AGGREGATION_LASTCHANGEP1]) {
								if( mSensorOrientation[AGGREGATION_DEPTH] < 0 ){
									mSensorOrientation[AGGREGATION_DEPTH]--;
								}else{
									mSensorOrientation[AGGREGATION_DEPTH] = -1;
								}
							}
//Log.i(TAG,"Sensor Orientation (1)23 low("+mSensorOrientation[AGGREGATION_LOW]+") high("+mSensorOrientation[AGGREGATION_HIGH]+") all("+mSensorOrientation[AGGREGATION_ALL]+") count("+mSensorOrientation[AGGREGATION_COUNT]+")  average("+(int) (mSensorOrientation[AGGREGATION_ALL]/mSensorOrientation[AGGREGATION_COUNT])+") change("+mSensorOrientation[AGGREGATION_LASTCHANGE]+") movement("+(mSensorOrientation[AGGREGATION_LASTCHANGE]-mSensorOrientation[AGGREGATION_LASTCHANGEP1])+") sustained("+((mSensorOrientation[AGGREGATION_LASTCHANGE]-mSensorOrientation[AGGREGATION_LASTCHANGEP1])+(mSensorOrientation[AGGREGATION_LASTCHANGE]-mSensorOrientation[AGGREGATION_LASTCHANGEP2]))+") depth("+mSensorOrientation[AGGREGATION_DEPTH]+")");
						}
						if( mSensorAccelerometer[AGGREGATION_COUNT] > 0 ){
							mSensorAccelerometer[AGGREGATION_LASTCHANGEP2] = mSensorAccelerometer[AGGREGATION_LASTCHANGEP1];
							mSensorAccelerometer[AGGREGATION_LASTCHANGEP1] = mSensorAccelerometer[AGGREGATION_LASTCHANGE];
							mSensorAccelerometer[AGGREGATION_LASTCHANGE] = (mSensorAccelerometer[AGGREGATION_HIGH]-mSensorAccelerometer[AGGREGATION_LOW]);
							if( mSensorAccelerometer[AGGREGATION_LASTCHANGE] > mSensorAccelerometer[AGGREGATION_LASTCHANGEP1]){
								if(mSensorAccelerometer[AGGREGATION_DEPTH] > 0){
									mSensorAccelerometer[AGGREGATION_DEPTH]++; 
								}else{
									mSensorAccelerometer[AGGREGATION_DEPTH] = 1;
								}
							} else if ( mSensorAccelerometer[AGGREGATION_LASTCHANGE] < mSensorAccelerometer[AGGREGATION_LASTCHANGEP1]) {
								if( mSensorAccelerometer[AGGREGATION_DEPTH] < 0 ){
									mSensorAccelerometer[AGGREGATION_DEPTH]--;
								}else{
									mSensorAccelerometer[AGGREGATION_DEPTH] = -1;
								}
							}
//Log.i(TAG,"Sensor Accelerometer (1)23 low("+mSensorAccelerometer[AGGREGATION_LOW]+") high("+mSensorAccelerometer[AGGREGATION_HIGH]+") all("+mSensorAccelerometer[AGGREGATION_ALL]+") count("+mSensorAccelerometer[AGGREGATION_COUNT]+")  average("+(int) (mSensorAccelerometer[AGGREGATION_ALL]/mSensorAccelerometer[AGGREGATION_COUNT])+") change("+mSensorAccelerometer[AGGREGATION_LASTCHANGE]+") movement("+(mSensorAccelerometer[AGGREGATION_LASTCHANGE]-mSensorAccelerometer[AGGREGATION_LASTCHANGEP1])+") depth("+mSensorAccelerometer[AGGREGATION_DEPTH]+")");
						}
						if( mSensorMagneticField[AGGREGATION_COUNT] > 0 ){
							mSensorMagneticField[AGGREGATION_LASTCHANGEP2] = mSensorMagneticField[AGGREGATION_LASTCHANGEP1];
							mSensorMagneticField[AGGREGATION_LASTCHANGEP1] = mSensorMagneticField[AGGREGATION_LASTCHANGE];
							mSensorMagneticField[AGGREGATION_LASTCHANGE] = (mSensorMagneticField[AGGREGATION_HIGH]-mSensorMagneticField[AGGREGATION_LOW]);
							if( mSensorMagneticField[AGGREGATION_LASTCHANGE] > mSensorMagneticField[AGGREGATION_LASTCHANGEP1]){
								if(mSensorMagneticField[AGGREGATION_DEPTH] > 0){
									mSensorMagneticField[AGGREGATION_DEPTH]++; 
								}else{
									mSensorMagneticField[AGGREGATION_DEPTH] = 1;
								}
							} else if ( mSensorMagneticField[AGGREGATION_LASTCHANGE] < mSensorMagneticField[AGGREGATION_LASTCHANGEP1]) {
								if( mSensorMagneticField[AGGREGATION_DEPTH] < 0 ){
									mSensorMagneticField[AGGREGATION_DEPTH]--;
								}else{
									mSensorMagneticField[AGGREGATION_DEPTH] = -1;
								}
							}
//Log.i(TAG,"Sensor MagneticField (1)23 low("+mSensorMagneticField[AGGREGATION_LOW]+") high("+mSensorMagneticField[AGGREGATION_HIGH]+") all("+mSensorMagneticField[AGGREGATION_ALL]+") count("+mSensorMagneticField[AGGREGATION_COUNT]+")  average("+(int) (mSensorMagneticField[AGGREGATION_ALL]/mSensorMagneticField[AGGREGATION_COUNT])+") change("+mSensorMagneticField[AGGREGATION_LASTCHANGE]+") movement("+(mSensorMagneticField[AGGREGATION_LASTCHANGE]-mSensorMagneticField[AGGREGATION_LASTCHANGEP1])+") depth("+mSensorMagneticField[AGGREGATION_DEPTH]+")");
							
						}
	
						// i for iterator. :)
						for(mTempInt = 0; mTempInt < mSensorOrientation.length; mTempInt++){ mSensorOrientation[mTempInt] = 0; mSensorAccelerometer[mTempInt] = 0; mSensorMagneticField[mTempInt] = 0; }
						
					}
	
					//mTempString = "";
					//for(int mTempInt = 0; mTempInt < values.length; mTempInt++){mTempString +=  mTempInt + "("+values[mTempInt]+") ";}
					switch(sensor){
					case SensorManager.SENSOR_ACCELEROMETER:
	//					/mSensorAccelerometer
//Log.w(TAG,"onSensorChanged() sensor(SENSOR_ACCELEROMETER) values("+values.length+") " + v);
						mTempInt = (int) (values[0] *= 1000);
						if( mTempInt < mSensorAccelerometer[AGGREGATION_LOW] ){ mSensorAccelerometer[AGGREGATION_LOW] = mTempInt; }
						if( mSensorAccelerometer[AGGREGATION_COUNT] == 0 && mSensorAccelerometer[AGGREGATION_LOW] < mTempInt ){ mSensorAccelerometer[AGGREGATION_LOW] = mTempInt; }
						if( mTempInt > mSensorAccelerometer[AGGREGATION_HIGH] ){ mSensorAccelerometer[AGGREGATION_HIGH] = mTempInt; }
						mSensorAccelerometer[AGGREGATION_ALL] += mTempInt;
						mSensorAccelerometer[AGGREGATION_COUNT] ++;
						break;
					case SensorManager.SENSOR_MAGNETIC_FIELD:
						//mSensorMagneticField
//Log.w(TAG,"onSensorChanged() sensor(SENSOR_MAGNETIC_FIELD) values("+values.length+") " + v);
						mTempInt = (int) (values[0] *= 1000);
						if( mTempInt < mSensorMagneticField[AGGREGATION_LOW] ){ mSensorMagneticField[AGGREGATION_LOW] = mTempInt; }
						if( mSensorMagneticField[AGGREGATION_COUNT] == 0 && mSensorMagneticField[AGGREGATION_LOW] < mTempInt ){ mSensorMagneticField[AGGREGATION_LOW] = mTempInt; }
						if( mTempInt > mSensorMagneticField[AGGREGATION_HIGH] ){ mSensorMagneticField[AGGREGATION_HIGH] = mTempInt; }
						mSensorMagneticField[AGGREGATION_ALL] += mTempInt;
						mSensorMagneticField[AGGREGATION_COUNT] ++;
						break;
					case SensorManager.SENSOR_ORIENTATION:
	//					/Log.w(TAG,"onSensorChanged() sensor(SENSOR_ORIENTATION) values("+values.length+") " + mTempString);
						mTempInt = (int) (values[0] *= 1000);
						if( mTempInt < mSensorOrientation[AGGREGATION_LOW] ){ mSensorOrientation[AGGREGATION_LOW] = mTempInt; }
						if( mSensorOrientation[AGGREGATION_COUNT] == 0 && mSensorOrientation[AGGREGATION_LOW] < mTempInt ){ mSensorOrientation[AGGREGATION_LOW] = mTempInt; }
						if( mTempInt > mSensorOrientation[AGGREGATION_HIGH] ){ mSensorOrientation[AGGREGATION_HIGH] = mTempInt; }
						mSensorOrientation[AGGREGATION_ALL] += mTempInt;
						mSensorOrientation[AGGREGATION_COUNT] ++;
						break;
					case SensorManager.SENSOR_PROXIMITY:
//Log.w(TAG,"onSensorChanged() sensor(SENSOR_PROXIMITY) values("+values.length+") " + v);
						break;
					case SensorManager.SENSOR_TRICORDER:
//Log.w(TAG,"onSensorChanged() sensor(SENSOR_TRICORDER) values("+values.length+") " + v);
						break;
					case SensorManager.SENSOR_TEMPERATURE:
//Log.w(TAG,"onSensorChanged() sensor(SENSOR_TEMPERATURE) values("+values.length+") " + v);
						break;
					}
				}
	
	    	}, mSensorManagerOriginal.SENSOR_ALL,mSensorManagerOriginal.SENSOR_DELAY_NORMAL);
	    	/**/
	    	    	
	        
	    }
	@Override
		protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
//Log.i(TAG,"onApplyThemeResource() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			super.onApplyThemeResource(theme, resid, first);
		}
	@Override
	public void onWindowAttributesChanged(LayoutParams params) {
//Log.i(TAG,"onWindowAttributesChanged() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		super.onWindowAttributesChanged(params);
	}
	@Override
	public void onContentChanged() {
//Log.i(TAG,"onContentChanged() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		super.onContentChanged();
	}
	@Override
	protected void onRestart() {
//Log.i(TAG,"onRestart() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		mDataStore = new DbAdapter(this); mDataStore.loadDb();
		// Looking for a solution to the post ActivityResult pre onRestart SQLite crash.
		//if( mDataStore == null ){ mDataStore = new DbAdapter(this); mDataStore.loadDb(); }
		//if( !mDataStore.isOpen() ){ mDataStore = new DbAdapter(this); mDataStore.loadDb(); }
		//mgr.registerListener(mSensorListener, mgr.SENSOR_ORIENTATION);
		//populateFields();
		super.onRestart();
	}
	@Override
	protected void onStart() {
		super.onStart();
//Log.i(TAG,"onStart() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//Log.i(TAG,"Checking mCompose size for a sign of having content.");
		
		mCompose.requestFocusFromTouch();
		mCompose.setSelection( mCompose.length() );
		
		
		/*
		if( mCompose.length() > 0 ){
//Log.i(TAG,"UI FOCUS mCompose at end");
			mCompose.requestFocus();
			mCompose.setSelection( mCompose.length() );
		}else{
//Log.i(TAG,"UI CLICK mSelector3");
			//mCompose.requestFocus();
			//Toast.makeText(this, "Touch Here to Return to the Main Menu", 800).show();
			mSelector3.requestFocusFromTouch();
			mSelector3.performClick();
	    	//Toast.makeText(this, "", 800).show();
		}//*/
	
	    //updateWindowTitle();
	    //initFocus();
	
	    // Register a BroadcastReceiver to listen on HTTP I/O process.
	    //registerReceiver(mHttpProgressReceiver, mHttpProgressFilter);
	
	    //populateFields();
	    //startMsgListQuery();
	    //startQueryForContactInfo();
	    //updateSendFailedNotification();
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
//Log.i(TAG,"onRestoreInstanceState() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		mMessageId = savedInstanceState.getLong("messageid");
		//mMode = savedInstanceState.getString("mode");
//Log.w(TAG,"Setting mode to \"" + mMode + "\"");
		
		String mode = savedInstanceState != null ? savedInstanceState.getString("mode") : "compose";
    	setMode(mode);
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
//Log.i(TAG,"onPostCreate() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		super.onPostCreate(savedInstanceState);
	}
	@Override
	protected void onResume() {
		super.onResume();
//Log.i(TAG,"onResume() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		loadListSMS();
		//super.onResume();
	}
	private void loadListSMS() {
		//Cursor cCursor = getContentResolver().
		//Cursor lCursor = mDataStore.getEntry("recipientStore", new String[] {"_id","name","messageid"}, "status >= 0");
		Cursor lCursor = null;
	
		for(int hours = 1; hours < 1000; hours++ ){
			lCursor = SqliteWrapper.query(this, getContentResolver(), Uri.parse("content://sms"), 
	        		//new String[] { "_id", "address", "body", "strftime(\"%Y-%m-%d %H:%M:%S\", date, \"unixepoch\", \"localtime\") as date" },
	        		//strftime("%Y-%m-%d %H:%M:%S"
	        		new String[] { "_id", "address", "body", "datetime(date/1000, 'unixepoch', 'localtime') as date" },
					//new String[] { "_id", "address", "body", "date" },
	        		"date > " + (System.currentTimeMillis() - ( hours * 60 * 60 * 1000) ), 
	        		null, 
	        		"date desc");
				startManagingCursor(lCursor);
	        if ( lCursor.getCount() > 0 ){
	        	break;
	        }
		}

        String[] from = new String[]{"address", "body", "date" };
        int[] to = new int[]{R.id.listrowTitle, R.id.listrowDescription, R.id.listrowDate};

        //setTitle("Manual Sending, " + lCursor.getCount() + " Entries");
        SimpleCursorAdapter entries = new SimpleCursorAdapter(this, R.layout.listrowsms, lCursor, from, to);
        setListAdapter(entries);
        getListView().setTextFilterEnabled(true);
        //mSMSListView.sets

	}
	
	private void loadListContact() {
		//Cursor cCursor = getContentResolver().
		//Cursor lCursor = mDataStore.getEntry("recipientStore", new String[] {"_id","name","messageid"}, "status >= 0");
		Cursor lCursor = null;
	
		//syncProcess.androidDataPrint("content://contacts/people","number_key is not null AND last_time_contacted > " + ( (System.currentTimeMillis() - (90*24*60*60*1000))/1000) );
		for(int hours = 1; hours < 1000; hours++ ){
			lCursor = SqliteWrapper.query(this, getContentResolver(), Uri.parse("content://contacts/people") 
	        		//new String[] { "_id", "address", "body", "strftime(\"%Y-%m-%d %H:%M:%S\", date, \"unixepoch\", \"localtime\") as date" },
	        		//strftime("%Y-%m-%d %H:%M:%S"
	        		,new String[] { "_id", "name", "number" }
					//new String[] { "_id", "address", "body", "date" },
	        		//"date > " + (System.currentTimeMillis() - ( hours * 60 * 60 * 1000) ),
	        		,"last_time_contacted is not null AND number_key is not null AND last_time_contacted > " + ( (System.currentTimeMillis() - (90*24*60*60*1000))/1000)
	        		,null 
	        		,"last_time_contacted desc");
				startManagingCursor(lCursor);
	        if ( lCursor.getCount() > 0 ){
	        	break;
	        }
		}

        String[] from = new String[]{ "name", "number" };
        int[] to = new int[]{ R.id.listrowTitle, R.id.listrowDescription };

        //setTitle("Manual Sending, " + lCursor.getCount() + " Entries");
        SimpleCursorAdapter entries = new SimpleCursorAdapter(this, R.layout.listrowcontact, lCursor, from, to);
        setListAdapter(entries);
        getListView().setTextFilterEnabled(true);
        //mSMSListView.sets

	}
	
	
	
	@Override
	protected void onPostResume() {
//Log.i(TAG,"onPostResume() ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		super.onPostResume();
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
//Log.i(TAG,"onWindowFocusChanged("+hasFocus+") ++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		super.onWindowFocusChanged(hasFocus);
	}
	
	
	
	private void saveState() {
//Log.i(TAG,"saveState() -------------------------");
		if( mDataStore == null ){
//Log.w(TAG,"saveState() DataStore is null, loading.");
			mDataStore = new DbAdapter(this);
			mDataStore.loadDb();
		}
		if( !mDataStore.isOpen() ){
//Log.w(TAG,"saveState() DataStore wasn't open");
			mDataStore.close();
			mDataStore = new DbAdapter(this);
			mDataStore.loadDb();
		}
		
		if( mMessageId == 0 && mCompose.length() == 0 && mRecipientCount == 0 ){
//Log.w(TAG,"Leaving saveState(no content)");
			return;
		}
		
        // -----------------------------------------------
		// Create mMessageId if it doesn't already exist. (what's a few empty records among friends?
		if( mMessageId == 0 ){
//Log.i(TAG,"saveState(new)");
			createNewMessage();
			//Toast.makeText(this, "Saved to Drafts", 300).show();
			//return;
		}
		
		SharedPreferences sp = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
		Editor spe = sp.edit();
		spe.putLong("mMessageId", mMessageId);
		spe.commit();
		
//Log.i(TAG,"saveState(exists)");
		// April 19 2009 955am, mRSAToggle is being converted to String true:false here.
//Log.i(TAG,"UI GET mRSAToggle");
		String useRSA = mRSAToggle.isChecked() ? "true" : "false";
		//Cursor rowData = null;
		try {
			mDataStore.updateEntry("messageStore", mMessageId, new String[] {"decrypted", "useRSA"}, new String[] {mCompose.getText().toString().trim(),useRSA} );
			mDataStore.updateEntry("messageStore", mMessageId, new String[] {"status"}, new int[] {1}); //working
			
			if( mCompose.length() > 0 ){
				MessageProcessing msgProcess = new MessageProcessing(this);
				msgProcess.wordStash(mCompose.getText().toString());
			}
		} catch(StringIndexOutOfBoundsException e){
//Log.w(TAG,"String index out of bounds: " + e.getLocalizedMessage());
		} catch(SQLiteException e){
//Log.w(TAG,"Saving State failed (SQLite), hopefully we already had it. " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
//Log.w(TAG,"Saving State failed (Illegal State), hopefully we already had it. " + e.getLocalizedMessage());
		} finally {
			//if( rowData != null ){ rowData.close(); }
			//Toast.makeText(this, "Saved", 300).show();
		}
		
	}
	
	
	private void createNewMessage(){
//Log.i(TAG,"createNewMessage() -------------------------");
		
		
		if( mDataStore == null ){
//Log.w(TAG,"createNewMessage() DataStore is null, loading.");
			mDataStore = new DbAdapter(this);
			mDataStore.loadDb();
		}
		if( !mDataStore.isOpen() ){
//Log.w(TAG,"saveState() DataStore wasn't open");
			mDataStore.close();
			mDataStore = new DbAdapter(this);
			mDataStore.loadDb();
		}

		mMessageId = 0;
//Log.i(TAG,"UI GET mRSAToggle");
		String useRSA = mRSAToggle.isChecked() ? "true" : "false";
//Log.i(TAG,"messageStore CREATE with decrytped=mCompose, queue=draft, useRSA=mRSAToggle");
		mMessageId = mDataStore.addEntry("messageStore", new String[] {"decrypted","queue","useRSA"}, new String[] {mCompose.getText().toString().trim(),"draft", useRSA});
		
		if (mMessageId == 0){
//Log.e(TAG,"Creation of new Message failed?");
		}
	}
	// -------------------------------------------------------------		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    super.onActivityResult(requestCode, resultCode, intent);
//Log.i(TAG,"onActivityResult() -------------------------------------------");
		
	    if( mDataStore == null ){
	    	mDataStore = new DbAdapter(this); mDataStore.loadDb(); 
	    }
	    if( !mDataStore.isOpen() ){
//Log.i(TAG,"mDataStore isn't open(), loading.");
	    	mDataStore = new DbAdapter(this); mDataStore.loadDb();
	    }else{
//Log.w(TAG,"mDataStore is already still open(). +++++++++++++++++++++++++++++++");
	    }
	    
	    
	//Log.w(TAG,"Interesting onActivityResult() requestCode("+requestCode+") resultCode("+resultCode+")");
		//Log.w(TAG,"Interesting onActivityResult() messageid from intentextras ("+ intent.getLongExtra("messageid", 0) +")");
		//mDataStore.loadDb();
		//populateFields();
		
		
		switch(requestCode){
		case 48: case 79: // Recipient List
			//finishActivity(48);
			//this.finishActivity(48);
			updateRecipientbar();
			setMode("compose");
			mCompose.requestFocus();
			mCompose.setSelection(mCompose.length());
			break;
		case 47: // queue replies extra queue:<previous queue> status:<previous status> messageid:<message id to load>
//Log.w(TAG,"queue replied, getting extra data caused failure, finding another way");
//Log.i(TAG,"Checking for expected intent data");
			if( intent == null ){
//Log.e(TAG,"intent is null, no wonder it crashes.");
				break;
			}
			if( resultCode == 0 ){
//Log.e(TAG,"resultCode is 0, moving on.");
				break;
			}
		    if( !intent.hasExtra("messageid") ){
//Log.e(TAG,"No messageid in Extras, peace out.");
		    	break;
		    }
//Log.w(TAG,"getting Extras from intent");
		    Bundle intentExtras = null;
		    intentExtras = intent.getExtras();
//Log.i(TAG,"Getting messageid");
			long newId = 0;
			newId = intent.getLongExtra("messageid", 0);
//Log.i(TAG,"Getting queue");
			String newQueue = "";
			newQueue = intent.getStringExtra("queue");
			
			/**/
			if( newId != mMessageId){
//Log.w(TAG,"Message ID sent is different, loading.");
				mMessageId = newId;
				mQueue = newQueue;
				setMode("compose");
				populateFields(); //compose
			}
			/**/
			break;
		case 53:
//Log.i(TAG,"Checking for expected intent data");
			if( intent == null ){
//Log.w(TAG,"intent is null, moving on.");
				break;
			}
			if( resultCode == 0 ){
//Log.w(TAG,"resultCode is 0, moving on.");
				break;
			}
		    if( !intent.hasExtra("word") ){
//Log.e(TAG,"No word in Extras, peace out.");
		    	break;
		    }
//Log.w(TAG,"getting Extras from intent");
		    Bundle intentExtras2 = null;
		    intentExtras2 = intent.getExtras();
//Log.i(TAG,"Getting word");
			String word = "";
			word = intentExtras2.getString("word");
			
			mCompose.append(word + " ");
//Log.w(TAG,"getSelectionStart("+mCompose.getSelectionStart()+") getSelectionEnd("+mCompose.getSelectionEnd()+")");
			// Log getSelectionStart/End results here for correct placement.
			
			break;
		case 55:
//Log.i(TAG,"Checking for expected intent data");
			if( intent == null ){
//Log.w(TAG,"intent is null, moving on.");
				break;
			}
			if( resultCode == 0 ){
//Log.w(TAG,"resultCode is 0, moving on.");
				break;
			}
		    if( !intent.hasExtra("messageid") ){
//Log.e(TAG,"No messageid in Extras, peace out.");
		    	break;
		    }
//Log.w(TAG,"getting Extras from intent");
		    Bundle intentExtras3 = null;
		    intentExtras3 = intent.getExtras();
//Log.i(TAG,"Getting messageid");
			long messageId = 0;
			messageId = intentExtras3.getLong("messageid");
			mMessageId = messageId;
			populateFields();
			//mCompose.append(word + " ");
//Log.w(TAG,"getSelectionStart("+mCompose.getSelectionStart()+") getSelectionEnd("+mCompose.getSelectionEnd()+")");
			// Log getSelectionStart/End results here for correct placement.
			break;
		}
	    //loadList();
		/**//*
	    Bundle b = null;
	    b = intent.getExtras();
	    if( b!= null ){
	        long messageid = b.getLong("messageid");
//Log.w(TAG,"Interesting onActivityResult messageid("+messageid+")");
	    }else{
//Log.w(TAG,"Interesting onActivityResult no extras");
	    }
	    /**/
	    //mDataStore.close();
//Log.i(TAG,"onActivityResult finish");
	}

	public void populateFields(){
//Log.i(TAG,"populateFields() -------------------------------------------");
//Log.i(TAG,"populateFields() loadContacts()");
		loadContacts();
		loadListSMS();
		
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		// GET LINE 1 NUMBER
    	// My Telephone Number
        // TelephonyManager t2 = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    	// mPhoneNumber = t2.getLine1Number();
    	
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    	// Waiting Messages Queue Count, messageStore/
//    	mOutboxCount = mDataStore.getCount("messageStore","status = 2");// ready
//        mSentCount = mDataStore.getCount("recipientStore","status = 4"); // sent
//        mDraftCount = mDataStore.getCount("recipientStore","status = 1"); // working (queue = draft)
//        mInboxCount = mDataStore.getCount("messageStore", "status = 4"); // received

        
        /*
        mTempString = "";
        
        if( mOutboxCount > 0 ){ mTempString += "Outbox\n"; }
        if( mSentCount > 0 ){ mTempString += "Sent\n"; }
        if( mDraftCount > 0 ){ mTempString += "Draft\n"; }
        if( mInboxCount > 0 ){ mTempString += "Received\n"; }
        /**/

        
		
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // messageStore LOOKUP useRSA,decrypted _id=mMessageId
        // SET mRSAToggle to useRSA(true,false)
        // SET mCompose to decrypted 

        if( mMessageId != 0 ){
        	
        	//Toast.makeText(this, "Activating Sensor Array", 800).show();
	        Cursor rowData = null;
			try {
//Log.i(TAG,"messageStore GET useRSA,decrypted");
				rowData = mDataStore.getEntry("messageStore", new String[] {"useRSA","decrypted"}, "_id="+mMessageId);
				startManagingCursor(rowData);
//Log.i(TAG,"Checking for null.");
				if( rowData == null ) {
					//Log.e(TAG,"messageStore was null, leaving populateFields()."); 
					return;}
//Log.i(TAG,"Checking for existence of data.");
				if( !rowData.moveToFirst() ) {
//Log.e(TAG,"messageStore let us know no records exist, setting mMessageId = 0, leaving populateFields().");
					//rowData.close(); 
					if(mMessageId > 0){
						
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putString("message", "The message you requested wasn't able to be acquired, sorry.");
						b.putInt("duration", 800);
						msg.setData(b);
						mToastHandler.sendMessage(msg);
						
						
					}
					mMessageId = 0; 
					//mCompose.setEnabled(true);
					return;
				}
//Log.i(TAG,"Getting useRSA");
				// Set the Toggle Switch
				boolean useRSA;
				useRSA = rowData.getString(rowData.getColumnIndex("useRSA")).contentEquals("true");
//Log.i(TAG,"UI UPDATE mRSAToggle to useRSA");
				mRSAToggle.setChecked(useRSA);

				// Set compose
//Log.i(TAG,"UI UPDATE mCompose to result of Getting decrypted");// <to>, <result of>, Getting decrypted
				mCompose.setText(rowData.getString(rowData.getColumnIndex("decrypted")));
				if( mCompose.getLineCount() > 1 ){
					mCompose.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				}
				//mCompose.setEnabled(true);
				
			} catch (NullPointerException e) {
//Log.e(TAG,"caught NullPointerException oddly " + e.getLocalizedMessage());
				
			} finally {
				//if( rowData != null ){ rowData.close(); } startManagingCursor
			}

        }
		
		/**/
//Log.i(TAG,"populateFields() updateRecipientbar()");
		updateRecipientbar();

		//setMode();
		
		/**/
			
//mPathText = row.getString(row.getColumnIndexOrThrow(DbAdapter.COL_PATH));
//mSavedFlag = row.getInt(row.getColumnIndexOrThrow(DbAdapter.COL_SAVED));
			
	}
	
	

	private void sendAction(){
//Log.i(TAG,"sendAction() -------------------------------------------");
//Log.i(TAG,"sendAction() saveState()");
		saveState();
//Log.i(TAG,"recipientStore UPDATE status to ready(2) where messageid = " + mMessageId);
		//mDataStore.updateEntry("recipientStore", "status > 0 && messageid = " + mMessageId, "status", 2);//ready
		mDataStore.updateEntry("recipientStore", "status", 2, "status > 0 AND messageid = " + mMessageId); //ready
//Log.i(TAG,"messageStore UPDATE status to ready(2)");
		mDataStore.updateEntry("messageStore", mMessageId, "status", 2);//ready
//Log.i(TAG,"messageStore UPDATE queue to outbox");
		mDataStore.updateEntry("messageStore", mMessageId, "queue", "outbox");

		long messageId = mMessageId;
		
		
		//thread process here to do this task for ones that are able to be instant.
		//ArrayList<String> messageDivided = smsMan.divideMessage(message);
    	//smsMan.sendMultipartTextMessage(mPhoneNumber, null, messageDivided, null, null);
		
		
		//Display display = getWindowManager().getDefaultDisplay();
	    //boolean isPortrait = display.getWidth() < display.getHeight();
	    //final int width = isPortrait ? display.getWidth() : display.getHeight();
	    //final int height = isPortrait ? display.getHeight() : display.getWidth();

		
		
		//mRecipientCount = 0;
		//mRecipientlist.setText("");
		//mRecipientlist.setVisibility(Button.INVISIBLE);
		mMessageId = 0;
		mSelectorSize1 = 0;
		mSelectorSize2 = 0;
		mSelectorSize3 = 0;
		mSelectorSize4 = 0;
		mSelectorSize5 = 0;
		mCompose.setText("");

		SharedPreferences sp = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
		Editor spe = sp.edit();
		spe.remove("mMessageId");
		//spe.putLong("mMessageId", mMessageId);
		spe.commit();
		setMode("compose");
		populateFields(); // compose
		
		//SELECTOR_TEXT_N
	

		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("message", "Delivering");
		b.putInt("duration", 500);
		msg.setData(b);
		mToastHandler.sendMessage(msg);
		
		//Toast.makeText(this, "Delivering", 500).show();
		//mRecipientlist.setText(SELECTOR_TEXT_C);
		
		MessageProcessing msgProcess = new MessageProcessing(this);
		msgProcess.processMessage(messageId);
		
	}



	private void updateRecipientbar() {
//Log.i(TAG,"updateRecipientbar() ---------------------------------");
		
		mRecipientCount = 0;
		
    	mOutboxCount = mDataStore.getCount("messageStore","queue = \"outbox\"");// ready
        mSentCount = mDataStore.getCount("recipientStore","status = 4"); // sent
        //mDraftCount = mDataStore.getCount("messageStore","queue = \"draft\""); // working (queue = draft)
        mDraftCount = mDataStore.getCount("messageStore","status = 1"); // working (queue = draft)
        mInboxCount = mDataStore.getCount("messageStore", "queue = \"incoming\""); // received
        mWordCount = mDataStore.getCount("wordStore", "status > 0"); // received

        // later this will be based on user assigned queues
        mMessageCount = (mOutboxCount + mSentCount + mDraftCount + mInboxCount);

        //if( mTempString.length() > 0 ){ mSelector4.setText(SELECTOR_TEXT_Q);mSelector4.setEnabled(true); }
        //else{ mSelector4.setText("");mSelector4.setEnabled(false); }
		
		if( mMessageId == 0 ) {
//Log.i(TAG,"No MessageID, leaving updateRecipientbar()");
			mRecipientlist.setText("");
			mRecipientlist.setVisibility(Button.GONE);
			mRecipientbar.setVisibility(LinearLayout.GONE);
			return;
		}
		
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		// recipientStore LOOKUP useRSA messageid=mMessageId groupBy useRSA
		//Count Recipients by RSA on/off
		// Layout 
		int usersaTrueCount = 0;
		int usersaFalseCount = 0;
		String name = "";
		String number = "";
		boolean useRSA = true;
		
		Cursor recipientData = null;
			recipientData = mDataStore.getEntry("recipientStore", new String[] {"name","tel","useRSA"}, "messageid=" + mMessageId);
		
		startManagingCursor(recipientData);
//Log.i(TAG,"Checking for null.");
			if( recipientData == null ) {
//Log.e(TAG,"recipientStore was null, leaving updateRecipientbar()."); 
				mRecipientlist.setText("");
				mRecipientlist.setVisibility(View.GONE);
				mRecipientstart.setVisibility(View.VISIBLE);
				return;
			}
//Log.i(TAG,"Checking for existence of data with moveToFirst()");
			if( !recipientData.moveToFirst() ) {
//Log.w(TAG,"recipientStore let us know no records exist, moving on.");
				
				mRecipientlist.setText("");
				mRecipientlist.setVisibility(View.GONE);
				mRecipientstart.setVisibility(View.VISIBLE);
				
			}else{
//Log.w(TAG,"Iteratting recipientStore");
				for( int recipientPosition = 0; recipientPosition < recipientData.getCount(); recipientPosition++){
					recipientData.moveToPosition(recipientPosition);
//Log.i(TAG,"Iteratting recipientStore " + recipientPosition + " Getting useRSA");	
					useRSA = recipientData.getString(recipientData.getColumnIndex("useRSA")).contentEquals("true");
//Log.i(TAG,"Iteratting recipientStore " + recipientPosition + " Getting name");
					name = recipientData.getString(recipientData.getColumnIndex("name"));
//Log.i(TAG,"Iteratting recipientStore " + recipientPosition + " Getting tel");
					number = recipientData.getString(recipientData.getColumnIndex("tel"));
//Log.i(TAG,"Iteratting recipientStore " + recipientPosition + " COUNT usersaTrue and useRSA False");
					if( useRSA ){ usersaTrueCount++; } else { usersaFalseCount++;}
				}
//Log.w(TAG,"Iteratting recipientStore done");
			}
		
		// April 17 2009, I've relocated this Cursor outside try{}, possibly this will nullify the Cursor not closed warning/errors.
		//if( recipientData != null ){ recipientData.close(); }
		// April 19 2009, removed, learned. 
		//if( recipientData != null ){ recipientData.close(); } startManagingCursor
			
			
		mRecipientCount = usersaTrueCount + usersaFalseCount;
		mRecipientlist.setText("");
		
		if( mRecipientCount > 1 ){
			if( usersaTrueCount > 0 ){ mRecipientlist.append(usersaTrueCount + " RSA encrypted recipients. "); }
			if( usersaFalseCount > 0 ){ mRecipientlist.append(usersaFalseCount + " Clear transport recipients. "); }
			//if( mRecipientCount == 0 ){mRecipientlist.setText("Press <To> Add Contacts");} //postarity
			//if( mRecipientCount == 0 ){mRecipientlist.setText("Send <Contacts> Messages");}
			mRecipientbar.setVisibility(Button.VISIBLE);
			mRecipientlist.setVisibility(Button.VISIBLE);
			mRecipientstart.setVisibility(View.GONE);
		}else if(mRecipientCount == 1){
			String rsastatus = " (clear)";
			if( useRSA ){ rsastatus = " (RSA)"; }
			mRecipientlist.setText(name + " at " + number + rsastatus);
			mRecipientbar.setVisibility(Button.VISIBLE);
			mRecipientlist.setVisibility(Button.VISIBLE);
			mRecipientstart.setVisibility(View.GONE);
		}else{
			mRecipientUseRSAOnce = 0;
			mRecipientlist.setVisibility(Button.GONE);
			mRecipientstart.setVisibility(TextView.VISIBLE);
			if( mMode != "to" ){
				mRecipientbar.setVisibility(LinearLayout.GONE);
			}
		}
		
		
		
		// Leave the RSA Toggle where the user last left it.  By default it is on.
        //mRSAToggle.setChecked(true);
		
	}
	
	private void loadContacts() {
//Log.i(TAG,"loadContacts() -------------------------------------------");
		
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	    // Get Contacts, getting contacts
	    /**/
	    
	    Cursor contactData = null;
		try {
//Log.i(TAG,"People.CONTENT_URI ITERATE Contacts.People._ID, Contacts.People.NUMBER_KEY, Contacts.People.NAME where People.Phones.NUMBER_KEY is not null order alphabetically.");
	        //Cursor c = mContentResolver.query(People.CONTENT_URI, PEOPLE_PROJECTION, "? is not null", new String[] {People.Phones.NUMBER_KEY}, People.NAME + " DESC");
	        contactData = mContentResolver.query(People.CONTENT_URI, PEOPLE_PROJECTION, People.Phones.NUMBER_KEY + " is not null", null, People.NAME + " ASC");
	        startManagingCursor(contactData); // maybe put this after tests
//Log.i(TAG,"Checking People.CONTENT_URI for null.");
	        if( contactData == null ){
	        	return; 
	        	}
//Log.i(TAG,"Checking People.CONTENT_URI for existence of data.");
    		if( !contactData.moveToFirst() ){ 
//Log.w(TAG,"Cursor informed us it is empty through false Cursor.moveToFirst()"); 
    		//contactData.close(); 
    		return; }
//Log.i(TAG,"Getting COUNT");
    		int newCount = contactData.getCount();
//Log.i(TAG,"Checking COUNT for mContactCount equality."); // April 19 2009 852am, found != where == should have been, corrected it, cause for no contacts in database error.
    		if( newCount == mContactCount ){Log.w(TAG,"Saved Loading of Contacts with persistent data."); 
    		//contactData.close(); 
    		return;}
    		
    		mContactCount = newCount; // April 19 2009, remembered the hard way why this is above the next array lines.
    		// April 9, 2009
    		// Sweet bird almighty, 0+9 doesn't equal 1+9, and when position and space is in consideration this makes a huge difference.  No more off by 1 errors.
    		// Respect Zero but it doens't need to be the start of a position of occupied space.
    		mContactId = new long[mContactCount+1];
            mContactName = new String[mContactCount+1];
            mContactNumber = new String[mContactCount+1];
            mContactSelection = new String[mContactCount+1];
//Log.i(TAG,"Iterating PEOPLE.CONTENT_URI");
			for( int peoplePosition = 0; peoplePosition < contactData.getCount(); peoplePosition++ ){    			
    			contactData.moveToPosition(peoplePosition);
//Log.i(TAG,"Iterating PEOPLE.CONTENT_URI " + peoplePosition + " Getting People._ID");
    			long id = 0;
            	id = contactData.getLong(contactData.getColumnIndex(People._ID));
//Log.i(TAG,"Iterating PEOPLE.CONTENT_URI " + peoplePosition + " Getting People.NAME");
            	String name = "";
            	name = contactData.getString(contactData.getColumnIndex(People.NAME));
//Log.i(TAG,"Iterating PEOPLE.CONTENT_URI " + peoplePosition + " Getting People.Phones.NUMBER_KEY");
            	String number_key = "";
            	number_key = contactData.getString(contactData.getColumnIndex(People.Phones.NUMBER_KEY));
//Log.i(TAG,"Iterating PEOPLE.CONTENT_URI " + peoplePosition + " Getting Number by reversal of NUMBER_KEY"); // adapt to use the last entry for relative context
            	String number = "";
            	if( number_key.length() > 0 ){
            		char cN;
	            	for( int ch = number_key.length() - 1; ch >= 0; ch --){
	            		cN = number_key.charAt(ch);
	            		if( cN == '+' ){continue;}
	            		number += cN;
	            	}
	            	// US, add the 1, I believe callerid always reports 1aaabbbgggg
	            	
	            	if( number.length() == 10 && number.charAt(0) != '1' && ( number.charAt(0) != '0' )){
//Log.i(TAG,"Iterating PEOPLE.CONTENT_URI " + peoplePosition + " Modifying number("+number+") length("+number.length()+") charAt(0)("+number.charAt(0)+") prepending +1.");
	            		number = "+1" + number;
	            	}
            	}
            	
            	mContactId[peoplePosition+1] = id;
            	mContactName[peoplePosition+1] = name;
            	mContactNumber[peoplePosition+1] = number;
            	mContactSelection[peoplePosition+1] = name + "\n" + number;
    		}
            	        
		} catch (CursorIndexOutOfBoundsException e){
//Log.e(TAG,"loadContacts() CursorIndexOutOfBoundsException " + e.getLocalizedMessage());
			
		} catch (ArrayIndexOutOfBoundsException e){
//Log.e(TAG,"loadContacts() ArrayIndexOutOfBoundsException " + e.getLocalizedMessage());
				
		} catch (NullPointerException e){
//Log.e(TAG,"loadContacts() NullPointerException " + e.getLocalizedMessage());
			
		} catch (SQLiteException e){
//Log.e(TAG,"loadContacts() SQLiteException " + e.getLocalizedMessage());
			
	    } finally {
	        //if( contactData != null ){ contactData.close(); } startManagingCursor
	    }
	    /**/
	
	}

	private void addRecipient(int contactid) {
//Log.i(TAG,"addRecipient("+Integer.toString(contactid)+")  -------------------------------------------");
		
		if ( mRecipientbar.getVisibility() != LinearLayout.VISIBLE ){
			mRecipientbar.setVisibility(LinearLayout.VISIBLE);
		}
		if ( mRecipientlist.getVisibility() != Button.VISIBLE ){
			mRecipientlist.setVisibility(Button.VISIBLE);
		}
		mRecipientstart.setVisibility(TextView.GONE);
		
		if( contactid >= mContactNumber.length){
//Log.e(TAG,"contactId is greater than total number of contacts, leaving setContact()"); 
			return; }
		if( mMessageId == 0 ){ 
//Log.w(TAG,"no Message Id, generating"); 
			createNewMessage(); 
			saveState();
			}
//Log.i(TAG,"recipientStore GET _id where messageid=mMessageId AND contactid=Selected Contact ID");
		long recipientid = 0;
		recipientid = mDataStore.getId("recipientStore","messageid = " + mMessageId + " AND contactid = " + mContactId[contactid]);
//Log.i(TAG,"Checking record id for existence.");
		if( recipientid == 0 ){
//Log.w(TAG,"recipientStore CREATE with tel = " + mContactNumber[contactid]);
			recipientid = mDataStore.addEntry("recipientStore", "tel", mContactNumber[contactid] );
		}
//Log.w(TAG,"Checking record id for substance."); 
		if(recipientid > 0){
//Log.i(TAG,"Getting useRSA from UI");
			String useRSA = mRSAToggle.isChecked() ? "true" : "false";
//Log.i(TAG,"recipientStore UPDATE contactid,status=working(1),messageid,tel,name,useRSA");
			mDataStore.updateEntry("recipientStore", recipientid, new String[] {"contactid","status","messageid"}, new long[] { mContactId[contactid], 1, mMessageId}); //working
			//mDataStore.updateEntry("recipientStore", rowId, "messageId",(long) mMessageId);
			mDataStore.updateEntry("recipientStore", recipientid, new String[] {"tel","name", "useRSA"}, new String[] {mContactNumber[contactid],mContactName[contactid],useRSA});
		}
		
		updateRecipientbar();
		
//Log.w(TAG,"UI focus on mCompose, at the end. " + mRecipientCount);
		if( mRecipientCount <= 2 ){
			setMode("compose");
			mCompose.requestFocusFromTouch();
			mCompose.setSelection(mCompose.length());
		} else if( mRecipientCount == 3 ){
			//*
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("message", "Leaving contacts open for additional selections. Scroll up to go up a level.");
			msg.setData(b);
			//msg.setData(new Bundle().putString("message", "Leaving contacts open."));
			mToastHandler.sendMessage(msg);
			mSelector3.requestFocusFromTouch();
			//*/
			//mToastHandler.sendMessage(new Message().setData(new Bundle().putString("message", "Leaving contacts open.")));
		}
	
	}
	
	

	public void recipientSelector(int start, int size){
//Log.i(TAG,"recipientSelector start("+start+") size("+size+") mContactCount("+mContactCount+") -------------------------------------------");	
		//mConsole.append("recipientSelector start("+start+") size("+size+")\n");
		//mSelectorHistory += ":" + start +","+ size +"\n";
		
		if( start == 0 && size == 0 ){
//Log.i(TAG,"Reseting Selector History"); //ing
			mSelectorHistoryCursor = 0;
			for(int i = 0;i < mSelectorHistory.length; i++){
				mSelectorHistory[i][SELECTOR_START] = 0;
				mSelectorHistory[i][SELECTOR_SIZE] = 0;
			}
		}
		
		if( mSelectorHistoryCursor > mSelectorHistory.length - 2 ){
//Log.w(TAG,"Growing History Size by " + SELECTOR_HISTORY_GROW + " from " + (mSelectorHistory.length - 1));
			int[][] copy = mSelectorHistory.clone();
//Log.w(TAG,"create new");
			mSelectorHistory = new int[mSelectorHistory.length + SELECTOR_HISTORY_GROW][3];
//Log.w(TAG,"iterate");
			for(int i = 0;i < copy.length; i++){
				mSelectorHistory[i][SELECTOR_START] = copy[i][SELECTOR_START];
				mSelectorHistory[i][SELECTOR_SIZE] = copy[i][SELECTOR_SIZE];
			}
		}
		
		if( start == mSelectorHistory[mSelectorHistoryCursor][SELECTOR_START] && size == mSelectorHistory[mSelectorHistoryCursor][SELECTOR_SIZE] ){
//Log.w(TAG,"selectionAction requested appears to be a call for the last history entry. ?");
		} else {
			//mSelectorHistoryCursor
			mSelectorHistoryCursor++;
			mSelectorHistory[mSelectorHistoryCursor][SELECTOR_START] = start;
			mSelectorHistory[mSelectorHistoryCursor][SELECTOR_SIZE] = size;
			//recipientSelector(mSelectorHistory[mSelectorHistoryCursor-1][SELECTOR_START],mSelectorHistory[mSelectorHistoryCursor-1][SELECTOR_SIZE]);
		}
		
		if( mContactCount < 1 ){
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("message", "Hello Android Owner, you have no contacts in your database.\nLong Press <"+SELECTOR_TEXT_T+">.");
			b.putInt("duration", 5000);
			msg.setData(b);
			mToastHandler.sendMessage(msg);
		}
		//mMode = "to";
//Log.w(TAG,"Setting mode to to");
		//mSelector3.performClick();
		//*
		if( mReturnSelectorFocus != 0 ){
			View returnFocus = this.findViewById(mReturnSelectorFocus);
			//returnFocus.requestFocus();
			returnFocus.requestFocusFromTouch();
			mLastSelectorFocus = mReturnSelectorFocus;
			mReturnSelectorFocus = 0;
		}else{
			//mSelector3.requestFocus();
//Log.w(TAG,"mSelector3 reset select");
			mSelector3.requestFocusFromTouch();
		}//*/
		
		//mCompose.clearFocus();
		//mSelectorBar.requestFocus();
		//this.setTitleColor(Color.BLACK);
		//this.mRecipient = "";
		//this.setTitle("RSA SMS Select a contact");

		if( start > ( mContactCount - 3) ){
//Log.w(TAG,"start("+start+") > (mContactCount("+mContactCount+") - 3) start = mContactCount - 2"); 
			start = mContactCount - 2; }
		if( start < 1 ){ 
//Log.w(TAG,"start("+start+") < 1 start = 1 "); 
			start = 1; }
		if( size == 0 ){ 
//Log.w(TAG,"size == 0 size = mContactCount("+mContactCount+") - start("+start+") "); 
			size = mContactCount - (start - 1); 
		} // patent    x - +1 + -1 (0 and -2) April 16th 2009 6:42 PM
		if( size < 3 ){ size = 3; 
//Log.w(TAG,"size("+size+") < 3. size = 3");
		} 
		int end = start + size - 1;
		if( end > mContactCount ){
//Log.w(TAG,"end("+end+") > mContactCount("+mContactCount+")");
			end = mContactCount; // annoying: start = end - 1; size = 1;
		}
		
		int splitSize = (size / 3);
		if( splitSize < 1 ){
//Log.w(TAG,"splitSize("+splitSize+") < 1 splitSize = 1"); 
			splitSize = 1; }
		mSelectorSize2 = splitSize;
		mSelectorSize3 = splitSize;
		mSelectorSize4 = splitSize;
		
		int extraPart = (size - (splitSize*3) );
		if(extraPart > 0) {
			mSelectorSize4 += extraPart;
		//if(extraPart == 2){
//Log.i(TAG,"Good catch, we almost ignored " + extraPart + " records from the interface.");
			//mSelectorSize3 ++;
			//mSelectorSize4 ++;
		//}else if(extraPart == 1){
//Log.i(TAG,"Good catch, we almost ignored 1 record from the interface.");
			//mSelectorSize4 ++;
		}else if(extraPart < 0 || extraPart > 2){
			// April 9, 2009: I'm working to solve a prediction and logic issue with selecting contacts, love to make this work for language.
			// This condition should never occur but if it does, I'd like to know.
			// ... The first space of the list of contacts is a whole number. (i.e. 1)
//Log.e(TAG,"Interesting: The sum of the Selector contents is out of bounds, 0-2, but I have " + extraPart +" = size("+size+") - splitSize("+splitSize+")*3");
		}else{
//Log.i(TAG,"Cool, All good, equality between selectors.");
		}
		
		
		mSelectorValue1 = 0;
		mSelectorValue2 = start;
		// I've removed the +1 from these two v2 and v3, let's see the result
		// not so good.  hum...  cond: 9, 3 3 3, 0-2 3-5 6-8
		//      1  2  3  4  5  6  7  8  9 10 11
		//      0  1  2  3  4  5  6  7  8  9 10
		//      9 10 11 12 13 14 15 16 17 18
		
		mSelectorValue3 = mSelectorValue2 + mSelectorSize2;
		mSelectorValue4 = mSelectorValue3 + mSelectorSize3;
		
		mSelectorValue5 = 0;
//Log.i(TAG,"start("+start+") end("+end+") split("+splitSize+") extra("+extraPart+") 2("+mSelectorValue2+","+mSelectorSize2+") 3("+mSelectorValue3+","+mSelectorSize3+") 4("+mSelectorValue4+","+mSelectorSize4+")\n");
		
		
		mSelector1.setText("<");//mSelector1.setEnabled(true);
		
		// Selector ()(2)()()()
//Log.i(TAG,"selector 2 mSectionValue2("+mSelectorValue2+") mSectionSize2("+mSelectorSize2+")\n");
		updateSelector(mSelector2, mSelectorValue2, mSelectorSize2, mContactSelection);

		// Selector ()()(3)()()
//Log.i(TAG,"selector 3 mSelectorValue3("+mSelectorValue3+") mSelectorSize3("+mSelectorSize3+")\n");
		updateSelector(mSelector3, mSelectorValue3, mSelectorSize3, mContactSelection);
		
		// Selector ()()()(4)()
//Log.i(TAG,"selector 4 mSelectorValue4("+mSelectorValue4+") mSelectorSize4("+mSelectorSize4+")\n");
		updateSelector(mSelector4, mSelectorValue4, mSelectorSize4, mContactSelection);
//Log.i(TAG,"completed Selector");
		
        mSelector5.setText(">");//mSelector5.setEnabled(true);
        
        //mCompose.setFocusable(false);
        //mSelector3.requestFocusFromTouch();
        //mSelector3.requestFocus();
        //mCompose.setFocusable(true);
//Log.w(TAG,"Setting mode to to") mMode = "to";
        setMode("to");
//Log.w(TAG,"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
	}
	
	private void updateSelector(Button selectorButton, int selectorValue, int selectorSize, String[] selectorList) {
//Log.i(TAG,"updateSelector() --------------------------------------");
		

		if( selectorList == null || selectorButton == null || selectorValue < 0 || selectorSize < 0 ){
		//Log.e(TAG,"updateSelector() expects (String[] selectorList, Button selectorButton, int selectorValue, int selectorSize)");
			if( selectorList == null ){ 
				Log.e(TAG,"updateSelector() selectorList is null, leaving updateSelector().");
				}
			else if( selectorButton == null ){ 
				Log.e(TAG,"updateSelector() selectorList is null, leaving updateSelector()."); 
				}
			else { 
				Log.e(TAG,"updateSelector() selectorValue or selectorSize is null, leaving updateSelector()."); 
				}
			return;
		}
		
		
		// We use whole numbers to represent positive content
//Log.i(TAG,"updateSelector() selectorValue("+selectorValue+") selectorSize("+selectorSize+") arraySize("+(selectorList.length-1)+")");
		
		if( selectorSize < 1 ){
			selectorButton.setText("");
			selectorButton.setEnabled(false);
			return;
		}
		
	//Log.w(TAG,"updateSelector() XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		if( selectorList[selectorValue].length() ==  0 ){
			Log.e(TAG,"Unusual, no array record for selectorValue");
			return;
		}
	//Log.w(TAG,"updateSelector() XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		
		if( selectorSize == 1 ){
			selectorButton.setText(selectorList[selectorValue]);
		//}else if( selectorSize == 2 ){
			//selector.setText(mContactName[selectorValue] + "\n" + mContactName[selectorValue+1]);
		//}else if( selectorSize == 3 ){
			//selector.setText(mContactName[selectorValue] + "\n" + mContactName[selectorValue+1] + "\n" + mContactName[selectorValue+2]);
		}else if( selectorSize < 1 ){
		Log.e(TAG,"updateSelector() selectorSize("+selectorSize+") < 1");
		} else {
		//Log.w(TAG,"updateSelector() XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			mTempChar1 = new char[10];mTempChar2 = new char[10];mTempChar3 = new char[10];mTempChar4 = new char[10];//mTempChar3 is used to check the next group, bad place for this code, not used at the end :)
			mTempInt1 = 1;
			
			// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			// CRAZY LOGIC NOTE: The order of these is determined by priority.
		//Log.w(TAG,"updateSelector() XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			// From start to finish within this selector, base for charsize
			// We use whole numbers to represent positive content
			if( (selectorList.length ) >= (selectorValue+selectorSize) ){
				for( ; mTempInt1 <= SELECTOR_MAX_HINT_CHARACTERS; mTempInt1 ++ ){
					selectorList[selectorValue].getChars(0, mTempInt1, mTempChar2, 0); // MYLEFT
					selectorList[selectorValue+selectorSize-1].getChars(0, mTempInt1, mTempChar3, 0); // MYRIGHT
		        	if(new String(mTempChar2).trim().equalsIgnoreCase(new String(mTempChar3).trim())){ continue; }
		        //Log.w(TAG,"MYLEFT("+new String(mTempChar2).trim()+") MYRIGHT("+new String(mTempChar3).trim()+")");
		        	break;
				}
			}
			
		//Log.w(TAG,"updateSelector() XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			// Between myself and before
			if( (selectorValue-1) > 0 ){
				for( mTempInt = mTempInt1 ; mTempInt <= SELECTOR_MAX_HINT_CHARACTERS; mTempInt ++ ){
					selectorList[selectorValue-1].getChars(0, mTempInt, mTempChar1, 0); // LEFT(RIGHT OF ANOTHER SELECTOR POSSIBLY)
					selectorList[selectorValue].getChars(0, mTempInt, mTempChar2, 0); // MYLEFT
					if(new String(mTempChar1).trim().equalsIgnoreCase(new String(mTempChar2).trim())){ continue; }
		        //Log.w(TAG,"LEFT("+new String(mTempChar1).trim()+") MYLEFT("+new String(mTempChar2).trim()+")");
		        	break;
				}
			}
		
		//Log.w(TAG,"updateSelector() XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			// From my end to the next
			// We use whole numbers to represent positive content
			if( (selectorList.length - 1) >= (selectorValue+selectorSize) ){
				for( mTempInt = mTempInt1 ; mTempInt <= SELECTOR_MAX_HINT_CHARACTERS; mTempInt ++ ){
					selectorList[selectorValue+selectorSize-1].getChars(0, mTempInt, mTempChar3, 0); // MYRIGHT
					selectorList[selectorValue+selectorSize].getChars(0, mTempInt, mTempChar4, 0); // RIGHT(LEFT OF ANOTHER SELECTOR POSSIBLY)
					if(new String(mTempChar3).trim().equalsIgnoreCase(new String(mTempChar4).trim())){ continue; }
		        //Log.w(TAG,"MYRIGHT("+new String(mTempChar3).trim()+") RIGHT("+new String(mTempChar4).trim()+")");
		        	break;
				}
			}

		//Log.w(TAG,"updateSelector() XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			selectorButton.setText(new String(mTempChar2).trim() + " - " + new String(mTempChar3).trim());
		}
	//Log.w(TAG,"updateSelector() XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		selectorButton.setEnabled(true);
	
	}
	
	// -------------------------------------------------------------
	// *************************************************************
	// -------------------------------------------------------------
	
	public void onClick(View v) {
		mTempString = "";
		switch (v.getId()) {
		case R.id.compose: mTempString = "compose"; break;
		case R.id.recipientlist: mTempString = "recipientlist"; break;
		case R.id.selector1: mTempString = "selector1"; break;
		case R.id.selector2: mTempString = "selector2"; break;
		case R.id.selector3: mTempString = "selector3"; break;
		case R.id.selector4: mTempString = "selector4"; break;
		case R.id.selector5: mTempString = "selector5"; break;
		case R.id.recipientstart: mTempString = "recipientstart"; break;
		case R.id.useRSA: mTempString = "Use RSA"; break;
		}
		
//Log.i(TAG,"onClick() id("+v.getId()+") name("+mTempString+") isPressed("+v.isPressed()+") -------------------------------------------");
		if( mRecentLongClick == v.getId() ){
	//Log.w(TAG,"Recognized recent long click as self, ignoring.");
			mRecentLongClick = 0;
			return;
		}
		
		
		switch(v.getId()) {
		case R.id.recipientstart:
			Intent i2 = new Intent(this, DialogBox.class);
        	i2.putExtra("title", "RSA SMS Contact Selection");
        	i2.putExtra("text", mRecipientstart.getText().toString());
        	mDataStore.close();
        	startActivityForResult(i2,56);
			break;
		case R.id.selector1:
			if ( mSelector1.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_A) ) {
		//Log.i(TAG,"Dialog Layout");
	        	Intent i = new Intent(this, DialogBox.class);
	        	i.putExtra("title", "RSA SMS");
	        	i.putExtra("text", "Thank you for asking.  My name is Haven Skys and this is the first offering by Doc Chomps Software.  Our user license agreement is such that we are protected from suit and we receive the benifit of profit.");
	        	//startActivity(i);
	        	mDataStore.close();
	        	startActivityForResult(i,50);
	        	break;
			}
			else if( mSelector1.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_N) ){
				newMessage();
				break;
			}
			else if( mSelector1.getText().toString().equalsIgnoreCase("<") ){
				// April 9, 2009: I reversed the action of the buttons on the end.
				if( mLastSelectorFocus == R.id.selector2 || mLastSelectorFocus == R.id.selector1){ mReturnSelectorFocus = R.id.selector2; }
				
				//recipientSelector(mSelectorValue2-1,3);
				
				int first = mSelectorValue2;
				if( mSelectorSize1 == 1 ){first++;}
				if( mLastSelectorFocus == R.id.selector4 || mLastSelectorFocus == R.id.selector5 ){
					mReturnSelectorFocus = R.id.selector4;
					if( mSelectorSize4 > 1 ){ first = mSelectorValue4 - 1; }
				}
				if( mLastSelectorFocus == R.id.selector3 ){
					mReturnSelectorFocus = R.id.selector3;
					if( mSelectorSize3 > 1 ){ first = mSelectorValue3 - 1; }
				}
				recipientSelector( first-1, 3 );
			}
			break;
		case R.id.selector2:
	//Log.i(TAG,"Selector 2 mMode("+mMode+") mSelectorValue2("+mSelectorValue2+") mSelectorSize2("+mSelectorSize2+")\n");

			if(mMode == "to") {
				if( mSelectorSize2 == 1 ){
					//mRecipientList.append( mContactName[mSelectorValue2] + "<" + mContactNumber[mSelectorValue2] + ">; ");
					addRecipient(mSelectorValue2); }
				else{ recipientSelector(mSelectorValue2, mSelectorSize2); }
			}
			
			if( mSelector2.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_T) ){
				// Compose Mode, Add a tel to mRecipient

				if( mRecipientCount == 0 ){ mRecipientlist.setVisibility(Button.GONE);
				mRecipientstart.setVisibility(TextView.VISIBLE);}
				else{ mRecipientlist.setVisibility(Button.VISIBLE);
				mRecipientstart.setVisibility(TextView.GONE);}
				mRecipientbar.setVisibility(LinearLayout.VISIBLE);
				recipientSelector(0,0);
			}
			break;
		case R.id.selector3:
	//Log.i(TAG,"Selector 3 mMode("+mMode+") mSelectorValue3("+mSelectorValue3+") mSelectorSize3("+mSelectorSize3+")\n");
			if( mSelector3.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_D) ){
				
	        	Intent i = new Intent(this, WordList.class);
	        	//i.putExtra("title", mRecipientCount + " Address");
	        	i.putExtra("cursorposition", mCompose.getSelectionStart());
	        	//start of sentence, etc...
	        	startActivityForResult(i,53);
	        	mDataStore.close();
			}
			
			if(mMode == "to") {
				if( mSelectorSize3 == 1 ){ addRecipient(mSelectorValue3); }
				else { recipientSelector(mSelectorValue3, mSelectorSize3); }
			}
			break;
		case R.id.selector4:
	//Log.i(TAG,"Selector 4 mMode("+mMode+") mSelectorValue4("+mSelectorValue4+") mSelectorSize4("+mSelectorSize4+")\n");
			
			
			if ( mSelector4.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_U) ) {
				//

				//Intent goFish = new Intent(this,Send.class);
		        //goFish.putExtra("queue", "outbox");
		        //startActivityForResult(goFish, 47);
		        //mDataStore.close();
		        //startActivity(goFish);
				
		        Intent i = new Intent(this, Unsent.class);
	        	//i.putExtra("title", mRecipientCount + " Address");
	        	//i.putExtra("messageid", mMessageId);
	        	startActivityForResult(i,55);
	        	mDataStore.close();
	        	
			}
			if(mMode == "to") {
				if( mRecentLongClick == v.getId() ){
			//Log.w(TAG,"Recognized recent long click as self, ignoring.");
					break;
				}
				if( mSelectorSize4 == 1 ){
					addRecipient(mSelectorValue4);
				}else{
			//Log.w(TAG,"I'm sending mSelectorValue4("+mSelectorValue4+"), mSelectorSize4("+mSelectorSize4+") ");
					recipientSelector(mSelectorValue4, mSelectorSize4);
				}
			}
			break;
		case R.id.selector5:
			if( mSelector5.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_C) ){
				// Compose Mode, Send the contents of mCompose
				
	        	//i.putExtra("messageid", mMessageId);
	        	//i.putExtra("text", "Thank you for asking.  My name is Haven Skys and this is the first offering by Doc Chomps Software.");
	        	//startActivity(i);
	        	//mDataStore.close();
	        	
				if( mMessageId > 0 && mRecipientCount > 0){
					sendAction();
				} else if( mMessageId > 0 && mRecipientCount == 0 && mCompose.length() == 0 ){
					mDataStore.deleteEntry("messageStore", mMessageId);
					mMessageId = 0;
				} else if( mRecipientCount == 0 ){
					/*
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putString("message", "Select a contact to send to.");
					msg.setData(b);
					mToastHandler.sendMessage(msg);
					mSelector2.requestFocus();
					mSelector2.requestFocusFromTouch();
					//*/
					mSelector2.performClick();
				}
				
				
				break;
			} 
			else if( mSelector5.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_N) ){
				newMessage();
				break;
			}
			else if( mSelector5.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_P) ){
				
				Toast.makeText(this, "Running the full service of reviewing, sending, and decrypting.", Toast.LENGTH_LONG).show();
				
				//Context ctx = this.getApplicationContext();
				Thread t = new Thread() {
		            public void run() {
		            	//Log.w(TAG,"Thread Message Processing");
		            	MessageProcessing msgProcess = new MessageProcessing(getApplicationContext());
		            	//Log.w(TAG,"reviewRecent()");
				    	msgProcess.reviewRecent();
				    	//Log.w(TAG,"sendRSA()");
				    	msgProcess.sendRSA();
				    	//Log.w(TAG,"decryptRSA()");
				    	msgProcess.decrypteRSA();
				    	//Log.w(TAG,"wordReview()");
				    	msgProcess.wordReview(5);
				    	
		            }
		        };
		        t.start();
		        		    	
		    	break;
			}
			else if( mSelector5.getText().toString().equalsIgnoreCase(">") ){
			//Log.w(TAG,")))))))))))))))))))))))))))))))))))))");
				int first = mSelectorValue2;
				if( mSelectorSize1 == 1 ){first++;}
				if( mLastSelectorFocus == R.id.selector4 || mLastSelectorFocus == R.id.selector5 ){
			//Log.w(TAG,"Recognized selector 4 was where this came from.");
					mReturnSelectorFocus = R.id.selector4;
					if( mSelectorSize4 > 1 ){ first = mSelectorValue4 - 1; }
				}
				if( mLastSelectorFocus == R.id.selector3 ){
			//Log.w(TAG,"Recognized selector 3 was where this came from.");
					mReturnSelectorFocus = R.id.selector3;
					if( mSelectorSize3 > 1 ){ first = mSelectorValue3 - 1; }
				}
		//Log.w(TAG,"Selector 5 S1S("+mSelectorSize1+") S2V("+mSelectorValue2+") Position("+first+")");
				recipientSelector( first + 1, 3 );
				
				/**/
			}
			break;
		case R.id.recipientlist:
			/*
			if( mRecipientlist.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_N)){
				mRecipientlist.setText("");
				mRecipientlist.setVisibility(Button.INVISIBLE);
				Intent i = new Intent(this, DialogBox.class);
	        	i.putExtra("title", "RSA SMS: Active Sending");
	        	// April 20 2009, 
	        	i.putExtra("text", "This is a place holder for this functionality.  It will provide direct delivery attempt access.");
	        	//startActivity(i);
	        	startActivityForResult(i,51);
	        	break;
			}
			//*/
			if( mRecipientCount > 0 ){
				saveState();
				
	        	Intent i = new Intent(this, ToList.class);
	        	i.putExtra("title", mRecipientCount + " Address");
	        	i.putExtra("messageid", mMessageId);
	        	startActivityForResult(i,48);
	        	mDataStore.close();
	    	}
			break;
		}
		
	}
	
	private void newMessage() {
		saveState();
		mCompose.setText("");
		mCompose.requestFocus();
		createNewMessage();
		setMode("compose");
		populateFields();
		saveState();
	}
	@Override
	public boolean dispatchTrackballEvent(MotionEvent ev) {

		if( mMode != "to" ){
			return super.dispatchTrackballEvent(ev);
		}
		
		// MOVEMENT, UP or DOWN in to mode
		if( mMode == "to" && ev.getAction() == ev.ACTION_MOVE && ev.getY() != 0 ){
			// selector_text_t should be the mode
			
			if( mLastSelectorFocus == 0 ){
				return super.dispatchTrackballEvent(ev);
			}
			
			if( (ev.getEventTime() - mActionEventTime) < TRACKBALL_ACTION_SPACE ){
		//Log.w(TAG,"Canceling Trackball motion("+(ev.getEventTime() - mActionEventTime)+") < " + TRACKBALL_ACTION_SPACE);
				ev.setAction(ev.ACTION_CANCEL);
				return super.dispatchTrackballEvent(ev);
			}
	
		
			
			if( mLastSelectorFocus == 0 ){mLastSelectorFocus = R.id.selector2;}
			//if( mLastSelectorFocus > 0 ){ }else{ ev.setAction(ev.ACTION_CANCEL); return super.dispatchTrackballEvent(ev); }
			mSelectorButton = (Button) findViewById(mLastSelectorFocus);
				
			
				
		//Log.w(TAG,"Y" + ev.getY());//0.16_7, 0.3_, 0.5, 0.6_7
		//Log.w(TAG,"X" + ev.getX());
		//Log.w(TAG,"T" + ev.getEventTime());
		//Log.w(TAG,"Ps" + ev.getPressure());
		//Log.w(TAG,"YPi" + ev.getYPrecision());
		//Log.w(TAG,"XPi" + ev.getXPrecision());
		//Log.w(TAG,"History size " + ev.getHistorySize()); // April 20 2009
			/*
			if( ev.getHistorySize() > 0 ){
			
				for(int i = 0; i < ev.getHistorySize(); i++){
					Log.w(TAG,"Historical size as Y Data " + ev.getHistoricalY(ev.getHistorySize()-i));
				}
			}//*/
				
		//Log.i(TAG,"Trackball motion occurs occurrence as recent as " + (ev.getEventTime() - mActionEventTime) + "ms");
			if( ev.getY() < 0 && (ev.getY() * -1) > TRACKBALL_FORCE_UP  ){ // UP -0.16_7, -0.3_, -0.5, -0.6_, ??? 
		//Log.w(TAG,"Trackball motion appears UP above limit "+(ev.getY()*-1)+" > "+TRACKBALL_FORCE_UP+".");
				mActionEventTime = ev.getEventTime();
				mReturnSelectorFocus = mLastSelectorFocus;
				selectorHistoryBack();
				//button.setEnabled(false);
				//button.setClickable(true);
			} else if(ev.getY() < 0){
			//Log.i(TAG,"Trackball motion appears UP and under limit "+(ev.getY()*-1)+" < .1, cancelling activity.");
			}

			if( ev.getY() > 0 && ev.getY() > TRACKBALL_FORCE_DOWN ){ // DOWN .16_7, .3_, .5, .6_, ???
		//Log.w(TAG,"Trackball motion appears DOWN above limit "+ev.getY()+" > "+TRACKBALL_FORCE_DOWN+".");
				mActionEventTime = ev.getEventTime();
				mReturnSelectorFocus = mLastSelectorFocus;
				mSelectorButton.performClick();				
			}else if(ev.getY() > 0){
			//Log.i(TAG,"Trackball motion appears DOWN and under limit "+ev.getY()+" < .3, cancelling activity.");
			}
			
			ev.setAction(ev.ACTION_CANCEL);
			
			
			
			
			
			
		} else if(ev.getAction() == ev.ACTION_MOVE && ev.getY() != 0 && mLastSelectorFocus != 0){
			
			mSelectorButton = (View) findViewById(mLastSelectorFocus);
			if( !mSelectorButton.isClickable() ){ return super.dispatchTrackballEvent(ev); }
			if( !mSelectorButton.hasFocus() ){ return super.dispatchTrackballEvent(ev);} 
			
			//if( mCompose.hasFocus() ){return super.dispatchTrackballEvent(ev);}
			
			//if( mLastSelectorFocus == 0 ){ return super.dispatchTrackballEvent(ev); }
			if( (ev.getEventTime() - mActionEventTime) < TRACKBALL_ACTION_SPACE ){
		//Log.w(TAG,"Canceling Trackball motion("+(ev.getEventTime() - mActionEventTime)+") < " + TRACKBALL_ACTION_SPACE);
				ev.setAction(ev.ACTION_CANCEL);
				return super.dispatchTrackballEvent(ev);
			}
			
			//if( mLastSelectorFocus == 0 ){mLastSelectorFocus = R.id.selector3;}
			//if( mLastSelectorFocus > 0 ){ }else{ ev.setAction(ev.ACTION_CANCEL); return super.dispatchTrackballEvent(ev); }
				
		//Log.i(TAG,"Trackball motion occurs occurrence as recent as " + (ev.getEventTime() - mActionEventTime) + "ms");
			if( ev.getY() < 0 && (ev.getY() * -1) > TRACKBALL_FORCE_UP  ){ // UP -0.16_7, -0.3_, -0.5, -0.6_, ??? 
		//Log.w(TAG,"Trackball motion appears UP above limit "+(ev.getY()*-1)+" > "+TRACKBALL_FORCE_UP+".");
				mActionEventTime = ev.getEventTime();
				mReturnSelectorFocus = mLastSelectorFocus;
				mSelectorButton.clearFocus();
				mCompose.requestFocus();
				mCompose.requestFocusFromTouch();
				mCompose.setSelection(mCompose.length());
			} else if(ev.getY() < 0){
			//Log.i(TAG,"Trackball motion appears UP and under limit "+(ev.getY()*-1)+" < .1, cancelling activity.");
			}

			if( ev.getY() > 0 && ev.getY() > TRACKBALL_FORCE_DOWN ){ // DOWN .16_7, .3_, .5, .6_, ???
		//Log.w(TAG,"Trackball motion appears DOWN above limit "+ev.getY()+" > "+TRACKBALL_FORCE_DOWN+".");
				mActionEventTime = ev.getEventTime();
				mReturnSelectorFocus = mLastSelectorFocus;
				mSelectorButton.performClick();				
			}else if(ev.getY() > 0){
			//Log.i(TAG,"Trackball motion appears DOWN and under limit "+ev.getY()+" < .3, cancelling activity.");
			}
			
			ev.setAction(ev.ACTION_CANCEL);
			
		}
		
		
		return super.dispatchTrackballEvent(ev);
	}
	
	
	
	
	public void onFocusChange(View v, boolean hasFocus) {
		
		if( !hasFocus || !v.hasFocus() ){ return; }
		if( !v.isFocused() ){ return; }
		//if(mMode == "to" && mLastSelectorFocus > 0){ View d = this.findViewById(mLastSelectorFocus); d.requestFocusFromTouch(); }
	
		// NAME
		mTempString = "";
		switch (v.getId()) {
			case R.id.recipientlist: mTempString = "recipientlist"; break;
			case R.id.compose: mTempString = "compose"; break;
			case R.id.selector1: mTempString = "selector1"; break;
			case R.id.selector2: mTempString = "selector2"; break;
			case R.id.selector3: mTempString = "selector3"; break;
			case R.id.selector4: mTempString = "selector4"; break;
			case R.id.selector5: mTempString = "selector5"; break;
			case R.id.useRSA: mTempString = "Use RSA"; break;
		}
		
		// CHECKING
	//Log.i(TAG,"onFocusChange() id("+v.getId()+") name("+mTempString+") hasFocus("+hasFocus+") -------------------------------------------");
	//Log.i(TAG,"Focus has changed to " + mTempString);
	
		
		// GENERIC
		switch (v.getId()) {
		case R.id.compose:
			/*if( !mSelector3.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_T) ){
				if(mMode == "to" && mLastSelectorFocus > 0){
					View d = this.findViewById(mLastSelectorFocus);
					d.requestFocusFromTouch();
				}
				break;
			}//*/
		//Log.w(TAG,"Setting mode to compose because compose has focus. ");
			if( mCompose.hasFocus()  ){//&& mCompose.hasSelection()
				setMode("compose");
				return;
			}
			//mSelector1.setNextFocusUpId(R.id.compose);
			//mSelector2.setNextFocusUpId(R.id.compose);
			//mSelector3.setNextFocusUpId(R.id.compose);
			//mSelector4.setNextFocusUpId(R.id.compose);
			//mSelector5.setNextFocusUpId(R.id.compose);
			
			/*
			mSelector1.setText(SELECTOR_TEXT_A);mSelector1.setEnabled(true);// April 20 2009, DCS

			if( mRecipientCount > 0 ){
				mSelector2.setText(SELECTOR_TEXT_C);mSelector3.setEnabled(true); // April 20 2009, Send
			}else{
				mSelector2.setText("");mSelector2.setEnabled(false);
			}
			
			mSelector3.setText(SELECTOR_TEXT_T);mSelector3.setEnabled(true);// April 20 2009, to
			
			if( mMessageCount > 0 ){
				mSelector4.setText(SELECTOR_TEXT_Q);mSelector4.setEnabled(true);// April 20 2009, Q
			}else{
				mSelector4.setText("");mSelector4.setEnabled(false);
			}
			
	        mSelector5.setText("");mSelector5.setEnabled(false);
	        //*/
			
	        //setMenu();
	        break;
		case R.id.selector1: case R.id.selector5:
		//Log.w(TAG,"((((((((((Setting return selector focus to previous selector focus.)))))))))))");
			
			break;
		case R.id.selector2:
			//setMode("to");
		//Log.i(TAG,"Focus has changed to Selector 2");
			//if( mRecipientlist.getText().toString().equalsIgnoreCase("Select from the menu below.") ){ mRecipientlist.setText("RSA SMS, Thank you."); }
			break;
		case R.id.selector3:
			break;
		case R.id.selector4:
			//setMode("to");
			//mSelectorReturnFocus = R.id.selector4; // risky for future use, looks like a big number, not nice
			//if( mRecipientlist.getText().toString().equalsIgnoreCase("Select from the menu below.") ){ mRecipientlist.setText("RSA SMS, Thank you."); }
			break;
		}
	
		
		if( mMode == "to"){

		//Log.i(TAG,"Setting mode to to.");
		setMode("to");
		
			switch (v.getId()) {
				case R.id.selector1:
					mSelector1.performClick();
					mReturnSelectorFocus = mLastSelectorFocus;
					break;
				case R.id.selector2:
					break;
				case R.id.selector3:
					break;
				case R.id.selector4:
					break;
				case R.id.selector5:
					mSelector5.performClick();
					mReturnSelectorFocus = mLastSelectorFocus;
					break;
			}
			//mSelectorButton = (View) findViewById(mLastSelectorFocus);
			//mSelectorButton.clearFocus();
		}
	
		mLastSelectorFocus = v.getId();
	}
	
	
	
	private Handler mToastHandler = new Handler() {
	    public void handleMessage(Message msg) {
	   //Log.i(TAG,"mToastHandler()");
	    	Bundle b = msg.getData();
	    	String message = b.containsKey("message") ? b.getString("message") : "";
	    	int duration = b.containsKey("duration") ? b.getInt("duration") : Toast.LENGTH_SHORT;
	        Toast.makeText(RSASMS.this, message, duration).show();
	    }
	    /*
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("message", "Mode " + mode);
		msg.setData(b);
		mToastHandler.sendMessage(msg);
		//*/
	    
	};
	

	private void setMode(String mode) {
		
		//mMode = "computer";
//Log.w(TAG,"Changing mode from " + mMode + " to " + mode);
		if ( mMode != null ){
			if ( mMode == mode ){
		//Log.w(TAG, "Mode isn't changing.");
				//return;
			}
		}
		mMode = mode;
		
		String title = "RSA SMS ";
		Date d = new Date();
		title += (d.getYear()-100) + "." + (d.getMonth()+1) + "." + d.getDate();
		
		if( mMessageId > 0 ){
			//title += " MESSAGE " + mMessageId;
			if( mTitle != null ){
				mTitle.setText("MESSAGE " + mMessageId);
			}
		}else{
			if( mTitle != null ){
				mTitle.setText("MESSAGE");
			}
		}
		setTitle(title);
		
	//Log.w(TAG,"MMMMMMMMMMMMMode " + mMode);
		//Toast.makeText(this, "Setting Mode " + mode, 10).show();
	
		/*
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("message", "Mode " + mode);
		msg.setData(b);
		mToastHandler.sendMessage(msg);
		//*/
		if( mSelector1 == null ){
	//Log.w(TAG,"caught mode selection while onCreate hadn't been run yet.");
			return;
		}
		
		if( mMode == "compose" ){
			
			mSelector1.setEnabled(true);
			if( mMessageId > 0 || mCompose.length() > 0 ){
				mSelector1.setText(SELECTOR_TEXT_N);
			}else {
				mSelector1.setText(SELECTOR_TEXT_A);// April 20 2009, DCS
			}
			
			mSelector2.setEnabled(true);
			mSelector2.setText(SELECTOR_TEXT_T); // April 30 2009, to
			
			if( mRecipientCount > 0 ){
				mSelector1.setNextFocusUpId(R.id.recipientlist);
				mSelector2.setNextFocusUpId(R.id.recipientlist);
				mSelector3.setNextFocusUpId(R.id.compose);
				mSelector4.setNextFocusUpId(R.id.compose);
				mSelector5.setNextFocusUpId(R.id.useRSA);
			}else{
				mRecipientbar.setVisibility(LinearLayout.GONE);
				mSelector1.setNextFocusUpId(R.id.compose);
				mSelector2.setNextFocusUpId(R.id.compose);
				mSelector3.setNextFocusUpId(R.id.compose);
				mSelector4.setNextFocusUpId(R.id.compose);
				mSelector5.setNextFocusUpId(R.id.compose);
			}
			
			
			
			mSelector3.setEnabled(true);
			if( this.mWordCount > 0 ){
				mSelector3.setText(SELECTOR_TEXT_D);// April 20 2009, to, April 30 2009, words.
			}else{
				mSelector3.setText("");
			}
			//mSelector3.setNextFocusUpId(R.id.compose);
			
			mSelector4.setEnabled(true);
			if( this.mDraftCount > 0 ){
				mSelector4.setText(SELECTOR_TEXT_U);mSelector4.setEnabled(true);// April 20 2009, History/Past
			}else{
				mSelector4.setText("");
			}
			//mSelector4.setNextFocusUpId(R.id.compose);
			
			mSelector5.setEnabled(true);
	        //mSelector5.setText("SMS");
			if ( mCompose.length() == 0 && mRecipientCount == 0 && mMessageId < 1 ){
				//mSelector5.setText(SELECTOR_TEXT_P); // April 30 2009, Process
				mSelector5.setText(SELECTOR_TEXT_N); // May 7 2009, New
			} else {
				mSelector5.setText(SELECTOR_TEXT_C); // April 20 2009, Send
			}
	        //mSelector5.setNextFocusUpId(R.id.useRSA);
			loadListSMS();
		}

		if( mMode == "to" ){
			
			mSelector1.setEnabled(true);
			mSelector1.setText("<");
			//mSelector1.setNextFocusUpId(R.id.selector1);
			
			mSelector2.setEnabled(true);
			//mSelector2.setText("");
			mSelector2.setNextFocusUpId(R.id.selector2);
			
			mSelector3.setEnabled(true);
			//mSelector3.setText("");
			mSelector3.setNextFocusUpId(R.id.selector3);
			
			
			mSelector4.setEnabled(true);
			//mSelector4.setText("");
			mSelector4.setNextFocusUpId(R.id.selector4);
			
			mSelector5.setEnabled(true);
	        mSelector5.setText(">");
	        //mSelector5.setNextFocusUpId(R.id.useRSA);
	        
	        loadListContact();
		}
		
        
	}
	/*
 	mode: draft
		RSASMS: messageStore NEW decrypted=message text, queue=draft, useRSA=mRSAToggle, status=1(working)
		RSASMS: recipientStore NEW rowId from draft as messageid, tel=number, contactid=_id(provider.contacts), status=1(working)
	 */	
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			if( mCompose.length() == 1 ){
				setMode("compose");
				saveState();
			}
            return false;
        }
		
		

		switch (v.getId()){
		case R.id.compose:
			if( keyCode == event.KEYCODE_ENTER ){
				saveState();
			}
			if( keyCode == event.KEYCODE_SPACE ){

				if( mLastConsoleKey == event.KEYCODE_PERIOD ){
					//Thread t = new Thread() { public void run() { saveState(); } };
					saveState();
			        //t.start();
					
				}else if( mLastConsoleKey == event.KEYCODE_SPACE ){
					//Thread t = new Thread() { public void run() { saveState(); } };
					saveState();
			        //t.start();
				}else if( mCompose.length() > 0 ){
					
					/*
					int selection = 0;
					int start = 0;
					selection = mCompose.getSelectionEnd();
					//if(selection < 1 ){selection = mCompose.length();}
					start = selection - 100;
					if( start < 0 ){ start = 0; }
					mTempString = mCompose.getText().toString().trim();
					mTempString.getChars(start, selection, mTempChar1, 0);
					mTempString = new String(mTempChar1);
					Log.w(TAG,"FOUND SECTION: " + mTempString);
					/**//*
					mTempString = "";
					char c = ' ';
					for(mTempInt = mLastConsoleKeyTyped.length() - 1; mTempInt > 0; mTempInt--){
						c = mLastConsoleKeyTyped.charAt(mTempInt);
						if( c == event.KEYCODE_SPACE ){
							break;
						}
						mTempString = mTempString + "" + c;
					}
					/**/
					
					//mTempInt = mLastConsoleKeyTyped.length() - mLastKeySpaceAt;
					//mTempChar1 = new char[mTempInt];
					//mLastConsoleKeyTyped.getChars(mLastKeySpaceAt, mLastConsoleKeyTyped.length()-1, mTempChar1, 0);
					//mTempString = new String(mTempChar1);
					//Log.w(TAG,"FOUND TYPED WORD("+mTempString+") mLastKeySpaceAt("+mLastKeySpaceAt+") mTempChar1("+mTempChar1.toString()+")");
					//long rowId = mDataStore.addEntry("wordStore", new String[] {"word"}, new String[] {mTempChar1.toString()});
				}
				//mLastKeySpaceAt = mLastConsoleKeyTyped.length();
			}
			if( keyCode == event.KEYCODE_O ){
				String searchWord = "audio";
				if(mLastConsoleKeyTyped.length() < (searchWord.length() - 2) ){
					try {
						int end = mLastConsoleKeyTyped.length()-1;
						int start = end - searchWord.length()-3;
						char[] buffer = new char[searchWord.length()];
						if( start >= 0 ){
							mLastConsoleKeyTyped.getChars(start, end, buffer, 0);
							String word = new String(buffer).trim();
							//Toast.makeText(this, "Found O " + word, 500).show();
					//Log.w(TAG,"KEYCODE_O " + word);
						}
					} catch (NullPointerException e){
				//Log.w(TAG,"NullPointerException KEYCODE_O " + e.getLocalizedMessage());
					} finally {
						
					}
				}
			}
			mLastConsoleKey = keyCode;
			//mLastConsoleKeyTyped += ""+ String.
		//Log.i(TAG,"mLastConsole Keys("+mLastConsoleKeyTyped+")");
			break;
		}
		return false;
	}
	
	private int mRecentLongClick;
	public boolean onLongClick(View v) {
		
		mTempString = "";
		switch (v.getId()) {
			case R.id.recipientlist: mTempString = "recipientlist"; break;
			case R.id.compose: mTempString = "compose"; break;
			case R.id.selector1: mTempString = "selector1"; break;
			case R.id.selector2: mTempString = "selector2"; break;
			case R.id.selector3: mTempString = "selector3"; break;
			case R.id.selector4: mTempString = "selector4"; break;
			case R.id.selector5: mTempString = "selector5"; break;
			case R.id.useRSA: mTempString = "Use RSA"; break;
		}
		
//Log.i(TAG,"onLongClick() id("+v.getId()+") name("+mTempString+") -------------------------------------------");
		mRecentLongClick = v.getId();
		switch (v.getId()) {
		case R.id.selector1: case R.id.selector5:
			if(mMode == "to") { selectorHistoryBack(); } // never runs
			break;
		case R.id.selector2:
	//Log.i(TAG,"Selector 2 mMode("+mMode+") mSelectorValue2("+mSelectorValue2+") mSelectorSize2("+mSelectorSize2+")\n");
			
			if( mSelector2.getText().toString().equalsIgnoreCase(SELECTOR_TEXT_T) ){
		//Log.i(TAG,"Starting Activity Insert or Edit");
				Intent d = new Intent(Intent.ACTION_INSERT_OR_EDIT);
				d.setType(Contacts.People.CONTENT_ITEM_TYPE);
		        //d.putExtra(Insert.PHONE, address);
		        //d.putExtra(Insert.NAME, address);
				startActivityForResult(d,79);
			}
			
			if(mMode == "to") {
				if( mSelectorSize2 == 1 ){
			//Log.i(TAG,"Starting Activity Call");
					Intent d = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+mContactNumber[mSelectorValue2]));
					startActivity(d);
				} else if( mSelectorSize2 > 1 ){
					selectorHistoryBack();
				} else if( this.mSelectorHistoryCursor == 1 ){
					mCompose.requestFocusFromTouch();
				}
			}

			
			
			break;
		case R.id.selector3:
		
			if(mMode == "to") {
				if( mSelectorSize3 == 1){
			//Log.i(TAG,"Starting Activity Call");
					Intent d = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+mContactNumber[mSelectorValue3]));
					startActivity(d);
				} else if( mSelectorSize3 > 1 ){
					selectorHistoryBack();
				}
			}
			
			
			break;
		case R.id.selector4:
	//Log.i(TAG,"Selector 4 mMode("+mMode+") mSelectorValue4("+mSelectorValue4+") mSelectorSize4("+mSelectorSize4+")\n");

			if(mMode == "to") {
				if( mSelectorSize4 == 1 ){
			//Log.i(TAG,"Starting Activity Call");
					Intent d = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+mContactNumber[mSelectorValue4]));
					startActivity(d);
				} else if( mSelectorSize4 > 1 ){
					selectorHistoryBack();
				}
			}
			break;
		}
		
		return false;
	}
	
	private void selectorHistoryBack() {
//Log.i(TAG,"LongClick requested SelectorHistoryBack Currently at " + mSelectorHistoryCursor);
		if( mSelectorHistoryCursor > 0 ){
			mSelectorHistoryCursor--;
			recipientSelector(mSelectorHistory[mSelectorHistoryCursor][SELECTOR_START],mSelectorHistory[mSelectorHistoryCursor][SELECTOR_SIZE]);
		}else{
			setMode("compose");
		}
	}
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if( buttonView.equals(mRSAToggle) ){
			if( mRecipientCount == 1 && mMessageId > 0 && mRecipientUseRSAOnce == 0){
				mRecipientUseRSAOnce++;
				String useRSA = mRSAToggle.isChecked() ? "true" : "false";
		//Log.i(TAG,"mRSAToggle changed with one tel.");
				mDataStore.updateEntry("recipientStore", "useRSA", useRSA, "messageid=" + mMessageId );
				updateRecipientbar();
			}
		}
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		
		if( true ){
			
		
		//if( mCompose.hasFocus()  ){//&& mCompose.hasSelection()
			//setMode("compose");
		//}
		//else 
			if( mMode == "to" && (v.getId() == R.id.selector1 || v.getId() == R.id.selector5)  ){
			
			if( (event.getEventTime() - mActionEventTime) < 300 ){ return true; }
			mActionEventTime = event.getEventTime();
			
			switch (v.getId()) {
				case R.id.selector1:
					mSelector1.performClick();
					mSelector1.clearFocus();
					return true;
				case R.id.selector5:
					mSelector5.performClick();
					mSelector5.clearFocus();
					return true;
			}
	//Log.w(TAG,"_____Touch Event ended.");
			//Log.i(TAG,"Setting mode to to.");
			
			setMode("to");
			mMode = "to";
		}
		}
		
		return false;
		//*/
	}
	
}




