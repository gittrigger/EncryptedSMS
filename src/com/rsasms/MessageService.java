package com.rsasms;



public class MessageService extends Service {
  
	private static final String TAG = "RSASMS MessageService";
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private DbAdapter mDataStore;
    
	public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	public static final String SMS_SENT_ACTION = "android.provider.Telephony.SMS_SENT";
	public static final String SERVICE_STATE = "android.intent.action.SERVICE_STATE";
	public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

	private MessageProcessing mProcess; 
	
    //private static final String[] SEND_PROJECTION = new String[] { "_id", "thread_id", "address", "body" };
    
	static final String[] PROJECTION = new String[] {
        // TODO: should move this symbol into android.provider.Telephony.
        "trasport_type",
        "_id",
        "thread_id",
        // For SMS
        "address",
        "body",
        "date",
        "read",
        "type",
        "status",
        // For MMS
        "sub",
        "sub_cs",
        "date",
        "read",
        "m_type",
        "msg_box",
        "d_rpt",
        "rr",
        "err_type"
    };

    // This must match SEND_PROJECTION.
    //private static final int SEND_COLUMN_ID         = 0;
    //private static final int SEND_COLUMN_THREAD_ID  = 1;
    //private static final int SEND_COLUMN_ADDRESS    = 2;
    //private static final int SEND_COLUMN_BODY       = 3;

    
    //final RemoteCallbackList<IRemoteServiceCallback> mCallbacks = new RemoteCallbackList<IRemoteServiceCallback>();
    
    /*
     * MessageReceiverService onCreate() onStart() onDestroy() onBind()
     */

    public void onCreate() {
    	
    //Log.i(TAG,"onCreate() +++++++++++++++++++++++++++++++++++++++++");

        mDataStore = new DbAdapter(this);
		mDataStore.loadDb();
		
		mProcess = new MessageProcessing(this);
		
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
    //Log.i(TAG,"onCreate() HandlerThread");
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
       //Log.i(TAG,"onCreate() HandlerThread.start()");
        thread.start();

       //Log.i(TAG,"onCreate() HandlerThread.getLooper()");
        mServiceLooper = thread.getLooper();
        
       //Log.i(TAG,"onCreate() ServiceHandler");
        mServiceHandler = new ServiceHandler(mServiceLooper);

       //Log.i(TAG,"onCreate() end");
        //mHandler.sendEmptyMessage(REPORT_MSG);
        
        
    }

    public void onStart(Intent intent, int startId) {
    //Log.i(TAG,"onStart() ++++++++++++++++++++++++++++++++++++++++++++");
        //mResultCode = intent.getIntExtra("result", 0);
        //mIntent = intent;
        //mIntentExtras = mIntent.getExtras();

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
       //Log.i(TAG,"onStart() Complete");
    }

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	//Log.i(TAG,"onRebind() ++++++++++++++++++++++++++++++++++++++++++");
	}

	@Override
	public boolean onUnbind(Intent intent) {
	//Log.i(TAG,"onUnbind() ++++++++++++++++++++++++++++++++++++++++++");
		return super.onUnbind(intent);
	}

	public void onDestroy() {
    //Log.i(TAG,"onDestroy() +++++++++++++++++++++++++++++++++++++++++++");

		mDataStore.close();
		
        mServiceLooper.quit();
        
		
    	/**//*	
		try {
			mDataStore.close();
		} catch (SQLiteException e) {
		//Log.e(TAG,"mDataStore.close() in onStop() failed. " + e.getLocalizedMessage());
		}
		/**/
        
        //Log.i(TAG,"onDestroy() mCallbacks.kill()");
        // Unregister all callbacks.
        //mCallbacks.kill();
        
        // Remove the next pending message to increment the counter, stopping
        // the increment loop.
        //mHandler.removeMessages(REPORT_MSG);

    }

    public IBinder onBind(Intent intent) {
    //Log.i(TAG,"onBind() ++++++++++++++++++++++++++++++++++++++++");
        // Select the interface to return.  If your service only implements
        // a single interface, you can just return it here without checking
        // the Intent.
    	/**//*
        if (IRemoteService.class.getName().equals(intent.getAction())) {
            return mBinder;
        }
        if (ISecondary.class.getName().equals(intent.getAction())) {
            return mSecondaryBinder;
        }
        /**/
        return null;
    }
    
   //private static final int REPORT_MSG = 1;
    
    /**
     * Our Handler used to execute operations on the main thread.  This is used
     * to schedule increments of our value.
     */
    /**//*
    private final Handler mHandler = new Handler() { 
        public void handleMessage(Message msg) {
        //Log.i(TAG,"Handler handleMessage() what:" + msg.what);
            switch (msg.what) {
                
                // It is time to bump the value!
                case REPORT_MSG: {
                    // Up it goes.
                    int value = ++mValue;
                    
                    // Broadcast to all clients the new value.
                    final int N = mCallbacks.beginBroadcast();
                    for (int i=0; i<N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).valueChanged(value);
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();
                    
                    // Repeat every 1 second.
                    sendMessageDelayed(obtainMessage(REPORT_MSG), 1*1000);
                } break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    /**/

    /* ****************************
     * Service Handler
     */
    private final class ServiceHandler extends Handler {
    	
        public ServiceHandler(Looper looper) { super(looper); }

        public void handleMessage(Message msg) {
        //Log.i(TAG,"ServiceHandler() handleMessage()");
            int serviceId = msg.arg1;
            Intent intent = (Intent) msg.obj;
            String action = intent.getAction();

            

            /*
            if ( SMS_SENT_ACTION.equals(action) ) {
            	if(intent == null){
            	//Log.e(TAG,"about to send an empty intent to handleMessageSent()");
            	}
                handleMessageSent(intent);
            } else if ( SMS_RECEIVED_ACTION.equals(action) ) {
            	handleMessageReceived(intent);
            } else if ( BOOT_COMPLETED.equals(action) ) {
                handleBootCompleted();
            } else if ( SERVICE_STATE.equals(action) ) {
            	handleServiceState();
            } else if ( action.length() > 0 ) {
        	   //mServiceState = (ServiceState) ((AsyncResult) msg.obj).result;
               //handleServiceStateChanged(intent);
            }
            //**/

            if ( SMS_RECEIVED_ACTION.equals(action) ) { handleMessageReceived(intent); }
            
            mProcess.deliveryStatus(intent);
            
            mProcess.reviewRecent();
            
            mProcess.sendRSA();
            
            mProcess.decrypteRSA();
            
            int wordCount = mDataStore.getCount("wordStore", "status == 1");
			if( wordCount < 50 ){
				mProcess.wordReview(30 * 24);
			}
            
            MessageReceiver.finishHostingService(MessageService.this, serviceId);
        }


		
    }

    
    

            
    private void handleMessageReceived(Intent intent) {
    //Log.i(TAG,"handleMessageReceived() -------------------------------------");
    	
    	Uri uri = intent.getData();
       //Log.i(TAG,"handleMessageReceived() uri("+uri+")");
    	
        mDataStore = new DbAdapter(this);
		mDataStore.loadDb();
		
		
		//Serializable[] messages = (Serializable[]) intent.getSerializableExtra("pdus");
		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        //Object[] messages = Object[];
        byte[][] pduObjs = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++) { pduObjs[i] = (byte[]) messages[i]; }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
      //SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
		
        
        //*
    	
    	String message = "";
    	String originator = "";
		originator = msgs[0].getOriginatingAddress();
    	for( int i = 0; i < msgs.length; i++ ){
    		//Log.i(TAG,"handleMessageReceived() SmsMessage["+i+"] from " + msgs[i].getOriginatingAddress());
    		//Log.i(TAG,"handleMessageReceived() SmsMessage["+i+"] body " + msgs[i].getMessageBody());
    		if( msgs[i] == null ){
    		//Log.e(TAG,"gathering the message body parts resulted in a null at part # " + i + ", I hope we got it all. <critical warning>");
    		} else {
    			message += msgs[i].getMessageBody();
    		}
    	}
    	//*/


    	/*			
			mode: receive key request reply
			MessageReceiverService: android.provider.Telephony.SMS_RECEIVED body contains "RSAKEY:e@n:"
			MessageReceiverService: recipientStore LOOKUPN _id where tel=originator and status=3(rsawait)
			MessageReceiverService: recipientStore UPDATE e, n, status=5(rsasend) (mid-state useful for diagnosing mid-process crashing, symptom duplicate encrypted replies)
			rsaSend() (Logic to be run on android.provider.Telephony.SMS_RECEIVED and android.intent.action.SERVICE_STATE and android.intent.action.BOOT_COMPLETE)
			MessageReceiverService: recipientStore LOOKUPN _id,messageid,e,n,tel where status=5(rsasend)
			MessageReceiverService: messageStore LOOKUP _id, decrypted where _id=messageid
			MessageReceiverService: SEND tel ENCRYPT decrypted using e,n formatted "RSASMS:?:"
			MessageReceiverService: recipientStore UPDATE status=4(sent)
    	 */
    	
    	Uri messageUri = getMessageUri(this,msgs);
    	
    	
		if( message.contains("RSAKEY:") ){
		//Log.i(TAG,"Found RSA Key Pairs");
			mProcess.processMessageRSAKey(messageUri);
		}
		
		
		
		/*
		 	mode: receive message
			MessageReceiverService: android.provider.Telephony.SMS_RECEIVED body contains "RSASMS:message:"
			MessageReceiverService: messageStore NEW originator, message, queue=incoming, status=4(received)
			rsaDecode()
			MessageReceiverService: messageStore LOOKUPN originator, e, n, d, where status=3(rsakey) (multirow limit 10, most recent from originator with this status)
			MessageReceiverService: DECRYPT message with e, n, d (try out multiple)
			MessageReceiverService: messageStore NEW originator, message, decrypted, useRSA=mRSAToggle, queue=incoming, e, n, status=4(received)
			MessageReceiverService: updateMessageBody with decrypted
			"Content-type: text/rsasms\n\n"
		 */
		else if( message.contains("RSASMS:") ){
		//Log.i(TAG,"Found RSASMS Message");
			mProcess.processMessageRSASMS(messageUri);
		}
		
		
		/*
		 	mode: key request
			MessageReceiverService: Receiver on android.provider.Telephony.SMS_RECEIVED body contains "RSA SMS,"
			MessageReceiverService: GENERATE NEW RSA 1024 bit for e, n, d
			MessageReceiverService: messageStore NEW originator, e, n, d, useRSA=true, status=3(rsakey)
			MessageReceiverService: reply to originator with "RSAKEY:e@n:"
		 */
		
		else if( message.contains("RSA SMS,") ){
    	//Log.i(TAG,"Found RSA Request");
    		mProcess.processMessageRSARequest(messageUri);
    	}

    	mDataStore.close();
    }
    
    
    
    
    
            
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

	private void handleSmsReceived(Intent intent) {
    //Log.i(TAG,"handleSmsReceived()");
    	//SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
    	
		//Serializable[] messages = (Serializable[]) intent.getSerializableExtra("pdus");
		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        //Object[] messages = Object[];
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }

    	
        Uri messageUri = insertMessage(this, msgs);
       //Log.i(TAG,"handleSmsReceived() messageUri("+messageUri.toString()+")");
        
        if (messageUri != null) {
            //MessagingNotification.updateNewMessageIndicator(this, true);
        }

    }
    
    
    /* ****************************
     * Message Storage Control
     */

    public static final int NOTIFICATION_NEW_MESSAGE = 1;

    // This must match REPLACE_PROJECTION.
    //private static final int REPLACE_COLUMN_ID = 0;

    // This must match the column IDs below.
    private final static String[] REPLACE_PROJECTION = new String[] { "_id", "address", "protocol" };
    
    public synchronized void sendFirstQueuedMessage() {
    //Log.i(TAG,"sendFirstQueuedMessage()");
        // get all the queued messages from the database
        final Uri uri = Uri.parse("content://sms/queued");
        ContentResolver resolver = getContentResolver();
        Cursor c = SqliteWrapper.query(this, resolver, uri, new String[] { "_id", "thread_id", "address", "body" }, null, null, null);

        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    int msgId = c.getInt(c.getColumnIndex("_id"));
                    String msgText = c.getString(c.getColumnIndex("body"));
                    String[] address = new String[1];
                    address[0] = c.getString(c.getColumnIndex("address"));
                    int threadId = c.getInt(c.getColumnIndex("thread_id"));
                    
                   //Log.i(TAG,"sendFirstQueuedMessage() requesting RSASMS.sendSms()");
                    //sendSms(address,msgText);
                }
            } finally {
                c.close();
            }
        }
    }
    
    private void moveOutboxMessagesToQueuedBox() {
    //Log.i(TAG,"moveOutboxMessagesToQueuedBox()");
        ContentValues values = new ContentValues(1);
        values.put("type", 6); // Sms.MESSAGE_TYPE_QUEUED

        SqliteWrapper.update( getApplicationContext(), getContentResolver(), Uri.parse("content://sms/outbox"), values, "type = " + 4, null); //Sms.MESSAGE_TYPE_OUTBOX

    }

    private Uri insertMessage(Context context, SmsMessage[] msgs) {
    //Log.i(TAG,"insertMessage()");
        // Build the helper classes to parse the messages.
        SmsMessage sms = msgs[0];

        if (sms.getMessageClass() == SmsMessage.MessageClass.CLASS_0) {
        //Log.w(TAG,"Interesting CLASS_0 Message originator(" + sms.getDisplayOriginatingAddress() + ") message(" + sms.getDisplayMessageBody() +")");
        	//displayClassZeroMessage(context, sms);
            return null;
        } else if (sms.isReplace()) {
            return replaceMessage(context, msgs);
        } else {
            return storeMessage(context, msgs);
        }
    }
   
	private Uri getMessageUri(Context context, SmsMessage[] msgs) {
	//Log.i(TAG,"getMessageUri()");
        SmsMessage sms = msgs[0];
        //ContentValues values = extractContentValues(sms);

        ContentResolver resolver = context.getContentResolver();
        String originatingAddress = sms.getOriginatingAddress();
        int protocolIdentifier = sms.getProtocolIdentifier();
        String selection = "address" + " = ? AND " + "protocol" + " = ?";
        String[] selectionArgs = new String[] { originatingAddress, Integer.toString(protocolIdentifier) };
        Cursor cursor = SqliteWrapper.query(context, resolver, Uri.parse("content://sms/inbox"), REPLACE_PROJECTION, selection, selectionArgs, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long messageId = cursor.getLong(0);
                    //android.provider.Telephony.Sms.CONTENT_URI
                    Uri messageUri = ContentUris.withAppendedId(Uri.parse("content://sms"), messageId); //Sms.CONTENT_URI
                    return messageUri;
                }
            } finally {
                cursor.close();
            }
        }
        
		return null;
	}

    
    private Uri replaceMessageBody(Context context, SmsMessage[] msgs, String body) {
    //Log.i(TAG,"replaceMessageBody()");
        SmsMessage sms = msgs[0];
        ContentValues values = extractContentValues(sms);
        
        //values.put("body", sms.getMessageBody());
        values.put("body", body);

        ContentResolver resolver = context.getContentResolver();
        String originatingAddress = sms.getOriginatingAddress();
        int protocolIdentifier = sms.getProtocolIdentifier();
        String selection = "address" + " = ? AND " + "protocol" + " = ?";
        String[] selectionArgs = new String[] { originatingAddress, Integer.toString(protocolIdentifier) };
        Cursor cursor = SqliteWrapper.query(context, resolver, Uri.parse("content://sms/inbox"), REPLACE_PROJECTION, selection, selectionArgs, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long messageId = cursor.getLong(0);
                    //android.provider.Telephony.Sms.CONTENT_URI
                    Uri messageUri = ContentUris.withAppendedId(Uri.parse("content://sms"), messageId); //Sms.CONTENT_URI
                    
                   //Log.w(TAG,"Updating SMS Message Body");
                   //Log.w(TAG,"messageUri: " + messageUri.toString());
                   //Log.w(TAG,"values.size(): " + values.size());
                    //for(int i = 0; i < values.size(); i++){}
                    SqliteWrapper.update(context, resolver, messageUri, values, null, null);
                    return messageUri;
                }
            } finally {
                cursor.close();
            }
        }
        return storeMessage(context, msgs);
    }

    private Uri replaceMessage(Context context, SmsMessage[] msgs) {
    //Log.i(TAG,"replaceMessage()");
        SmsMessage sms = msgs[0];
        ContentValues values = extractContentValues(sms);

        values.put("body", sms.getMessageBody());

        ContentResolver resolver = context.getContentResolver();
        String originatingAddress = sms.getOriginatingAddress();
        int protocolIdentifier = sms.getProtocolIdentifier();
        String selection =
                "address" + " = ? AND " +
                "protocol" + " = ?";
        String[] selectionArgs = new String[] {
            originatingAddress,
            Integer.toString(protocolIdentifier)
        };

        Cursor cursor = SqliteWrapper.query(context, resolver, Uri.parse("content://sms/inbox"), REPLACE_PROJECTION, selection, selectionArgs, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long messageId = cursor.getLong(0);
                    Uri messageUri = ContentUris.withAppendedId(Uri.parse("content://sms"), messageId);//Sms.CONTENT_URI
                    SqliteWrapper.update(context, resolver, messageUri, values, null, null);
                    return messageUri;
                }
            } finally {
                cursor.close();
            }
        }
        return storeMessage(context, msgs);
    }

    private void deleteMessage(Context context, Uri msgId){
    //Log.w(TAG,"deleteMessage("+msgId.toString()+")");
    	//ContentResolver resolver = context.getContentResolver();
    	//SqliteWrapper.delete(context, resolver, Uri.parse("content://sms/inbox"), null, null);
	}
    
    private Uri storeMessage(Context context, SmsMessage[] msgs) {
    //Log.i(TAG,"storeMessage()");
        SmsMessage sms = msgs[0];

        // Store the message in the content provider.
        ContentValues values = extractContentValues(sms);
        int pduCount = msgs.length;

        if (pduCount == 1) {
            // There is only one part, so grab the body directly.
            values.put("body", sms.getMessageBody());
        } else {
            // Build up the body from the parts.
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < pduCount; i++) {
                sms = msgs[i];
                body.append(sms.getMessageBody());
            }
            values.put("body", body.toString());
        }

        ContentResolver resolver = context.getContentResolver();

        return SqliteWrapper.insert(context, resolver, Uri.parse("content://sms/inbox"), values);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /* ****************************
     * Utilities
     */
    private ContentValues extractContentValues(SmsMessage sms) {
    //Log.i(TAG,"extractContentValues()");
        // Store the message in the content provider.
        ContentValues values = new ContentValues();

        values.put("address", sms.getOriginatingAddress());

        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        values.put("date", new Long(System.currentTimeMillis()));
        values.put("protocol", sms.getProtocolIdentifier());
        values.put("read", Integer.valueOf(0));
        if (sms.getPseudoSubject().length() > 0) {
            values.put("subject", sms.getPseudoSubject());
        }
        values.put("reply_path_present", sms.isReplyPathPresent() ? 1 : 0);
        values.put("service_center", sms.getServiceCenterAddress());
        return values;
    }
        

    /**/
    
    
}
