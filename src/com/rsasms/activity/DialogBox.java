package com.rsasms.activity;

import com.rsasms.DbAdapter;
import com.rsasms.MessageProcessing;
import com.rsasms.R;
import com.rsasms.SqliteWrapper;

public class DialogBox extends Activity implements OnClickListener {
	
	private static final String TAG = "RSASMS About Dialog";
	private Button mTriggerOne;
	private Button mTriggerTwo;
	private Button mTriggerThree;
	private Button mTriggerFour;
	private String mPhoneNumber;
	
	private TextView mDialogBoxText;
	private String mTitle;
	private String mText;
	private Intent mIntent;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialogbox);
		
		mDialogBoxText = (TextView) this.findViewById(R.id.dialogbox);
		mTriggerOne = (Button) this.findViewById(R.id.button_one);
		mTriggerTwo = (Button) this.findViewById(R.id.button_two);
		mTriggerThree = (Button) this.findViewById(R.id.button_three);
		mTriggerFour = (Button) this.findViewById(R.id.button_four);
		
		mIntent = getIntent();
		if( mIntent != null ){
			Bundle extras = mIntent.getExtras();
			if( extras != null){
				mTitle = extras.getString("title");
				mText = extras.getString("text");
			//Log.w(TAG,"DialogBox getAction " + mIntent.getAction() );
			}
		}
		

		if(mTitle != null){this.setTitle(mTitle);}
		if(mText != null){mDialogBoxText.setText(mText);}
		
	
		mTriggerOne.setOnClickListener(this);
		mTriggerTwo.setOnClickListener(this);
		mTriggerThree.setOnClickListener(this);
		mTriggerFour.setOnClickListener(this);
		//mTriggerTwo.setVisibility(View.GONE);
		mTriggerThree.setVisibility(View.GONE);
		mTriggerFour.setVisibility(View.GONE);
		
	
		TelephonyManager t2 = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mPhoneNumber = "";
    	mPhoneNumber = "+" + t2.getLine1Number();
    	
    	
    	//loadRecentMessagesIntoLog();
    	
	}

	private void loadRecentMessagesIntoLog() {
    //Log.w(TAG,"Getting inbox");
        final Uri uri = Uri.parse("content://sms/inbox");
        ContentResolver resolver = getContentResolver();
        Cursor c = SqliteWrapper.query(this, resolver, uri, 
        		new String[] { "_id", "thread_id", "address", "body", "date" }, 
        		"date > " + (System.currentTimeMillis() - (1 * 60 * 60 * 1000) ), 
        		null, 
        		"date asc");
        
       //Log.w(TAG,"query completed");
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                	for(int i = 0; i < c.getCount(); i++){
                		c.moveToPosition(i);
                		
	                    int msgId = c.getInt(c.getColumnIndex("_id"));
	                    String msgText = c.getString(c.getColumnIndex("body"));
	                    Long msgDate = c.getLong(c.getColumnIndex("date"));
	                    Date d = new Date();
	                    d.setTime(msgDate);
	                    
	                    String address = c.getString(c.getColumnIndex("address"));
	                    int threadId = c.getInt(c.getColumnIndex("thread_id"));
	                    Uri msgUri = Uri.parse("content://sms/" + msgId);
	                    
	                    
	                   //Log.w(TAG,"Recent Message " + msgUri.toString() + " " + address + " " + d.toString() + " " + msgText );

	                    if( msgText.contains("RSA SMS ") ){
	                    //Log.w(TAG,"Found the message of Interest");
		                    ContentValues values = new ContentValues();
		                    values.put("body", msgText + "\n seen by Dialog " + new Date().toString() );
		                    SqliteWrapper.update(this, resolver, msgUri, values, null, null);
	                    }
                	}
                }
            } finally {
                c.close();
            }
        }

		
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.button_one:
			// Close this Dialog
			this.finish();
			break;
		case R.id.button_two:
			// Open an editing pane for a reply to this originator
			//Intent i2 = new Intent(Intent.ACTION_SEND); 
			//i2.putExtra(Intent.EXTRA_TEXT, "Body");
			//i2.putExtra(Intent.EXTRA_SUBJECT, "Header");
			//i2.putExtra("sms_body", mText); 
			//i2.setType("vnd.android-dir/mms-sms"); 
			//startActivity(Intent.createChooser(i2, "SMS"));
			
			
	    	/*
        	Toast t = new Toast(this);
			t.setGravity(android.view.Gravity.CENTER, 200, 100);
			t.setGravity(android.view.Gravity.LEFT, 200, 100);
			t.setText("Sending message to yourself via Android SMS Outbox.");
			t.setDuration(1000);
			t.setView(t.getView());
			t.show();
			//*/
	    	
	    	//MessageSender s = new MessageSender(this);
        	//s.sendSms(phoneNumber, "Hello Android Owner, this message was sent by your request to yourself to test SMS sending.");
        	//Toast cView = null;
        	//cView = Toast.makeText(this, "", 500);
        	//cView.setText("Sending message to " + mPhoneNumber + " via built in SMS Adapter");
        	//cView.setDuration(cView.LENGTH_LONG);
        	//cView.show();
        	//cView.
	    	
	    	Date d = new Date();
	    	SmsManager smsMan = SmsManager.getDefault();
	    	String message = "Hello Android Owner, Thank you for choosing RSA SMS.\n";
	    	//String message = "Hello Android Owner, this message was sent by your request to yourself to verify sending.\n\nSent " + d.toString();
	    	//double div = d.getMinutes() / 2;
	    	//int divI = (int) div;
	    	//double diff = div - divI;
	    	
	    	long start = 0;
	    	
	    	if( true ){ // 
	    		
	    		//start = System.currentTimeMillis();
	    		//cView.cancel();
	    		//cView.setText("Sending message as a multipart message.");
	    		//cView.show();
	    		//message += "\n via multipart adapter.\n Bytes ";
	    		//message += (message.length() + new String(""+message.length()).length() );
	    	
	    		DbAdapter mDataStore = new DbAdapter(this);
		        mDataStore.loadDb();
		        
	    		
	    		long mid = mDataStore.addEntry("messageStore", new String[] {"useRSA","decrypted"} , new String[] { "true", message} );
	    		long rid = mDataStore.addEntry("recipientStore", new String[] {"useRSA","tel","name"} , new String[] {"true",mPhoneNumber,"Me"} );
	    		mDataStore.updateEntry("recipientStore", rid, "messageid", mid);
	    		mDataStore.updateEntry("recipientStore", rid, "status", 2);
	    		//mDataStore.updateEntry("messageStore", mid, "queue", "outbox");
	    		mDataStore.updateEntry("messageStore", mid, "status", 2);
	    		
	    		MessageProcessing msgProcess = new MessageProcessing(this);
	    		msgProcess.processMessage(mid);
	    		finish();
	    		//msgProcess.reviewRecent();
	    		
	    		/*
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
				//*/

	    		/*
				ContentValues values = new ContentValues(7);
	            values.put("address", mPhoneNumber);
	            values.put("date", System.currentTimeMillis());
	            values.put("read", 1);//read ? Integer.valueOf(1) : Integer.valueOf(0));
	            values.put("body", message);
	            values.put("status", 64);

	           //Log.i(TAG,"Inserting outgoing sms message in the outbox.");
	            Uri recordUri = getContentResolver().insert(Uri.parse("content://sms/sent"), values);
	    		
	    		ArrayList<String> messageDivided = smsMan.divideMessage(message);
	    		
	    		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(1);
		        sentIntents.add(PendingIntent.getBroadcast( this, 0, 
		        		new Intent("com.rsasms.MessageReceiver.MESSAGE_SENT_CONFIRMED", recordUri, this, com.rsasms.MessageReceiver.class).putExtra("messageid", 0), 0));
		    	
		    	ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(1);
		        deliveryIntents.add(PendingIntent.getBroadcast( this, 0,
                        new Intent("com.rsasms.MessageReceiver.MESSAGE_DELIVERY_CONFIRMED", recordUri, this, com.rsasms.MessageReceiver.class).putExtra("messageid", 0), 0));
		        		        
		    	smsMan.sendMultipartTextMessage(mPhoneNumber, null, messageDivided, sentIntents, deliveryIntents);
		    	Toast.makeText(this, "Multipart took " + (System.currentTimeMillis() - start) + " ms", 100).show();
		    	SystemClock.sleep(110);
		    	//*/
		        //Toast.makeText(this, "Multipart took " + (System.currentTimeMillis() - start) + " ms", 100).show();
		        
	    	}
	    	
	    	
	    	if( false ){// 111, 160 limit
	    		start = System.currentTimeMillis();
	    		//cView.cancel();
	    		//cView.setText("Sending message as a single message.");
	    		//cView.show();
	    		message = "Hello Android Owner, Verify sending.\n-0000-0000-0000-0000-0000-0000-0000\n-0000-0000-00\nSent " + d.toString();
	    		message += "\n via single message adapter.\n Bytes ";
	    		message += (message.length() + new String(""+message.length()).length() );
	    		smsMan.sendTextMessage(mPhoneNumber, null, message, null, null);
	    		Toast.makeText(this, "\nSingle took " + (System.currentTimeMillis() - start) + " ms", 100).show();
	    	}
	    	
	    	//cView.cancel();
    		//cView.setText("Message Sent to " + this.mPhoneNumber);
    		//cView.show();
    		//displaySurfaceDialog(this,"This is what an incoming message will appear as.");
    		/*
    		Intent smsDialogIntent  = new Intent(this, com.rsasms.activity.SurfaceDialog.class)
    			.putExtra("body", "This is what an incoming message will appear as.")
    			.putExtra("originator", phoneNumber)
    			.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        	startActivity(smsDialogIntent);
        	*/
        	
        	//t.cancel();
        	
        	//t.setText("Message Sent to yourself via Android SMS Outbox.");
			//t.show();
			//SystemClock.sleep(500);
        	
    		
    		
    		
			
			break;
		case R.id.button_three:
			Intent smsDialogIntent  = new Intent(this, com.rsasms.activity.SurfaceDialog.class)
					.putExtra("body", "This is what an incoming message will appear as.")
					.putExtra("originator", mPhoneNumber)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			
			startActivity(smsDialogIntent);
			break;
		case R.id.button_four:
			// Send an acknowledgment to the message originator
			Intent sendIntent = new Intent(this, RSASMS.class);
			sendIntent.putExtra("originator", mTitle); 
			//sendIntent.setType("message/rfc822"); 
			startActivity(Intent.createChooser(sendIntent, "RSASMS"));
			break;
		}
	}
	
}
