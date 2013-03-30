package com.rsasms;

public class MessageProcessing {

	final static String TAG = "RSASMS MessageProcessing";

	private Context mContext;
	private ContentResolver mResolver;
	private RSA mRSA;
	private DbAdapter mDataStore;
	
    //final static String CONNECT_DIALOG = "RSA SMS, Incoming encrypted transmission, Available for Android at the Marketplace.";
    final static String CONNECT_DIALOG = "RSA SMS, Incoming encrypted transmission, Available for the G1 at the Market.";
	//final static String RSA_REQUEST = "RSA";

	
	public MessageProcessing(Context ctx){
		mContext = ctx;
		mResolver = ctx.getContentResolver();
		
    	if( mDataStore == null ){
			mDataStore = new DbAdapter(mContext);
			mDataStore.loadDb();
    	}
	}
	
	@Override
	protected void finalize() throws Throwable {
		
	//Log.w(TAG,"finalize() closing mDataStore");
		
    	if( mDataStore != null ){ mDataStore.close(); }
		
		super.finalize();
	}

	public String[] lookupLocalSMS(Uri messageUri){

		if( messageUri == null ){
		//Log.e(TAG,"lookupLocalSMS() recieved a null arg, return null.");
			return null;
		}
		
		String[] rS = new String[4];
	//Log.w(TAG,messageUri.toString() + " LOOKUP _id,address,body,date");
        Cursor localSMS = SqliteWrapper.query(mContext, mResolver, messageUri, 
        		new String[] { "_id", "address", "body", "date" }, 
        		null, null, null);
        
        
       //Log.w(TAG,"query completed");
        if (localSMS == null) {Log.w(TAG,"processRSARequestMessage() Unable to acquire message using null Uri.");return null;}
        if (!localSMS.moveToFirst()) {Log.w(TAG,"processRSARequestMessage() Unable to acquire message, doesn't exist."); localSMS.close(); return null;}
        rS[0] = localSMS.getString(0);
        rS[1] = localSMS.getString(1);
        rS[2] = localSMS.getString(2);
        rS[3] = localSMS.getString(3);
        
        localSMS.close();
		
		return rS;
	}
	
    // RSA SMS,
    public void processMessageRSARequest(Uri messageUri) {
	//Log.w(TAG,"processMessageRSARequest()");

    	String[] localSMS = lookupLocalSMS(messageUri);
    	if( localSMS == null ){ 
    		//Log.e(TAG,"localSMS didn't result in a record.");
    		return; }
    	long localSMSId   = Long.parseLong(localSMS[0]);
    	String originator = localSMS[1];
    	String message    = localSMS[2];
    	String date       = localSMS[3];
        
        if( !message.contains("RSA SMS,") ){Log.e(TAG,"processRSARequestMessage() Message("+localSMSId+") doesn't contain required content.");return;}
    	
		mRSA = new RSA(1024);
	//Log.i(TAG,"adding keys to keyStore");
    	long messageId = 0;
    	messageId = mDataStore.addEntry("messageStore", new String[] {"originator", "useRSA", "e", "n", "d", "uri"} , new String[] {originator, "true", mRSA.e.toString(), mRSA.n.toString(), mRSA.d.toString(), messageUri.toString() } );
    	mDataStore.addEntry("keyStore", new String[] {"tel","e","n","d"}, new String[]{originator, mRSA.e.toString(), mRSA.n.toString(), mRSA.d.toString()});
    	mDataStore.updateEntry("messageStore", messageId, "status", 2);
    	
    	processMessageRSARequest();

	}

    private void processMessageRSARequest() {
    	
    	Cursor keySendList = mDataStore.getEntry("messageStore", new String[] {"_id","uri","e","n","d"} , "status = 2 AND d is not null");
    	if( keySendList == null ){
    		Log.e(TAG,"keySendList empty, not null didn't work");
    		return;
    	}
    	if( keySendList.moveToFirst() ){
    		long messageId = keySendList.getLong(0);
    		Uri messageUri = Uri.parse(keySendList.getString(1));
    		String e = keySendList.getString(2);
    		String n = keySendList.getString(3);
    		String d = keySendList.getString(4);
    	
	    	if(messageId > 0){
	    		
	    		SmsManager smsMan = SmsManager.getDefault();
	    		ArrayList<String> messageDivided = smsMan.divideMessage("RSAKEY:" + e + "@" + n + ":");
	
	    		String[] localSMS = lookupLocalSMS(messageUri);
	        	if( localSMS == null ){ 
	        		//Log.e(TAG,"localSMS didn't result in a record.");
	        		return; }
	        	long localSMSId   = Long.parseLong(localSMS[0]);
	        	String originator = localSMS[1];
	        	String message    = localSMS[2];
	        	String date       = localSMS[3];
	    		
	        	String outMessage = "";
	        	if( message.contains("<RSA SMS ") ){
	        		outMessage = message + "\n Resending Key";
	        	}else{
		    		outMessage = "<RSA SMS recieved connection request and replied with an encryption key in " + messageDivided.size() + " SMS>";
	        	}
	        	
	        	ContentValues values = new ContentValues();
	            values.put("body", outMessage );
	            values.put("read", 1 );
	            SqliteWrapper.update(mContext, mResolver, messageUri, values, null, null);
	            
				ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messageDivided.size());
				for( int i = 0; i < messageDivided.size(); i++){
					sentIntents.add(PendingIntent.getBroadcast( mContext, 0, new Intent("com.rsasms.MessageReceiver.MESSAGE_SENT_CONFIRMED", messageUri, mContext, com.rsasms.MessageReceiver.class).putExtra("messageid", messageId).putExtra("totalcnt", messageDivided.size() ), 0));
				}
		    	
		    	ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(messageDivided.size());
		    	for( int i = 0; i < messageDivided.size(); i++){
		    		deliveryIntents.add(PendingIntent.getBroadcast( mContext, 0, new Intent("com.rsasms.MessageReceiver.MESSAGE_DELIVERY_CONFIRMED", messageUri, mContext, com.rsasms.MessageReceiver.class).putExtra("messageid", messageId).putExtra("totalcnt", messageDivided.size() ), 0)); //sent
		    	}
	
		        
		        		        
		    	smsMan.sendMultipartTextMessage(originator, null, messageDivided, sentIntents, deliveryIntents);
		    	
		    	
	    		//MessageSender s = new MessageSender(this);
	        	//s.sendSms(originator, "RSAKEY:" + mRSA.e.toString() + "@" + mRSA.n.toString() + ":" );
	    		mDataStore.updateEntry("messageStore", messageId, new String[] {"status"}, new int[] {3}); //rsakey
	    		
	    		//Uri messageUri = replaceMessageBody(this, msgs, "RSA SMS recieved connection request and replied with an encryption key.");
	    		//deleteMessage(this, messageUri);
	    		
	    		
	    	}else{
	    		//failure occurred in posting this
	    	//Log.e(TAG,"No rowId returned by addEntry messageStore");
	    	}
    	
    	}
    	
    	keySendList.close();

	}

	// RSASMS:
	public void processMessageRSASMS(Uri messageUri) {
	//Log.w(TAG,"processMessageRSASMS()");

    	String[] localSMS = lookupLocalSMS(messageUri);
    	if( localSMS == null ){
    		//Log.e(TAG,"localSMS didn't result in a record.");
    		return; }
    	long localSMSId   = Long.parseLong(localSMS[0]);
    	String originator = localSMS[1];
    	String message    = localSMS[2];
    	String date       = localSMS[3];
    	//message.replaceAll("\n", "");
    	
        if( !message.contains("RSASMS:") ){Log.w(TAG,"processRSASMSMessage() Message doesn't contain required content.");return;}
        
       //Log.w(TAG,"processMessageRSASMS() received encrypted message");
		String[] m = message.split(":");
		String newMessage = m[1];

		long messageId = 0;
		messageId = mDataStore.addEntry("messageStore", new String[] {"originator","message","queue","uri"}, new String[] {originator, newMessage, "incoming", messageUri.toString()});
		if(messageId > 0){
			mDataStore.updateEntry("messageStore", messageId, "status", 7); //decrypt
		}

	}

	// RSAKEY:
	public void processMessageRSAKey(Uri messageUri) {
    //Log.w(TAG,"processMessageRSAKey()");

    	String[] localSMS = lookupLocalSMS(messageUri);
    	if( localSMS == null ){ 
    		//Log.e(TAG,"localSMS didn't result in a record.");
    		return; }
    	long localSMSId   = Long.parseLong(localSMS[0]);
    	String originator = localSMS[1];
    	String message    = localSMS[2];
    	String date       = localSMS[3];
    	String[] mp = message.split("\n");
    	if( mp.length > 1 ){
    		message = "";
    		for( int i = 0; i < mp.length; i ++ ){
    			message += mp[i];
    		}
    	}
    	
    //Log.w(TAG,"processMessageRSA KEY " + message);

        if( !message.contains("RSAKEY:") ){Log.e(TAG,"processMessageRSAKey() Message doesn't contain required content.");return;}
        
		String[] m = message.split(":");
		String[] k = m[1].split("@");
    	mRSA = new RSA();
    	mRSA.e = new BigInteger(k[0]);
    	mRSA.n = new BigInteger(k[1]);
    	
    	
    	
    	
    	mDataStore.addEntry("keyStore", new String[] {"tel","e","n"}, new String[]{originator, mRSA.e.toString(), mRSA.n.toString()});
    	mDataStore.updateEntry("recipientStore", "e", mRSA.e.toString(), "tel = \""+originator+"\" AND status=3"); //rsawait(3)
    	mDataStore.updateEntry("recipientStore", "n", mRSA.n.toString(), "tel = \""+originator+"\" AND status=3"); //rsawait(3)
    	mDataStore.updateEntry("recipientStore", "status", 5, "tel = \""+originator+"\" AND status=3"); //rsasend
    	
    	String outMessage = "<RSA SMS received encryption key>";
    	int keyConnect = 0;
		
    	Cursor rowData = null;
    	try {
        	rowData = mDataStore.getEntry("recipientStore", new String[] {"_id"}, "tel = \"" + originator + "\" AND (status=3 OR status=10 OR status=11 )"); //rsawait(3)
        	
	    //Log.i(TAG,"processMessageRSAKey() Checking recipientStore for null.");
	    	if( rowData == null ){
	    		//Log.e(TAG,"recipientStore was null, that's okay.");
	    		}
	    	else {
		    //Log.i(TAG,"processMessageRSAKey() Checking recipientStore for existence of data.");
		    	if( !rowData.moveToFirst() ){
		    	//Log.w(TAG,"recipientStore let us know no records exist, moving on.");
		    		rowData.close();
		    	}else{
	        		for(int i = 0; i < rowData.getCount(); i++){
	        			rowData.moveToPosition(i);
	        			long rowId = 0;
	        			rowId = rowData.getLong(0);
	        			
	        			if( rowId > 0 ){
	        			//Log.w(TAG,"Interate recipientStore " + rowId + " adding e, n, and setting status to rsasend(5)");
	        				mDataStore.updateEntry("recipientStore", rowId, new String[] {"e","n"}, new String[] { mRSA.e.toString(), mRSA.n.toString() });
	        				mDataStore.updateEntry("recipientStore", rowId, "status", 5); //rsasend
	        				sendRSA(rowId);
	        				keyConnect++;
	        			}
	        		}
		    	}
	        	rowData.close();
	    	}
    	} catch (SQLiteException e) {
    	//Log.e(TAG,"SQLiteException " + e.getLocalizedMessage());
    	} finally {
    		if( rowData != null ){rowData.close();}
    	}
    	
    	if ( keyConnect > 0 ){
	    	outMessage += "\n and associated it with " + keyConnect + " message";
	    	if( keyConnect > 1 ) { outMessage += "s"; }
    	}else{
    		// Maybe delivery receipt didn't come in but the message was delivered and the remote device responded.
    		// provide ability to overlook
    	}
    	
    	ContentValues values = new ContentValues();
        values.put("body", outMessage );
        values.put("read", 1 );
        SqliteWrapper.update(mContext, mResolver, messageUri, values, null, null);
		
    	
	}

	
	public void wordReview(int hours){
		
		
        Cursor localSMS = SqliteWrapper.query(mContext, mResolver, Uri.parse("content://sms/sent"), 
        		new String[] { "body" }, 
        		"date > " + (System.currentTimeMillis() - ( hours * 60 * 60 * 1000) ), 
        		null, 
        		null);
        

        if (localSMS != null) {
            if (localSMS.moveToFirst()) {
            	for(int i = 0; i < localSMS.getCount(); i++){
            		if( localSMS.moveToPosition(i) ){
	                    String message = localSMS.getString(0);
	                    wordStash(message);
            		}
            	}
            }
            localSMS.close();
        }
        
        
	}
	
	

	public void reviewRecent() {
    	
		processMessageRSARequest();
		
		Cursor messageList = null;
		messageList = mDataStore.getEntry("messageStore", new String[] {"_id"}, "status = 2 AND updated > " + (System.currentTimeMillis() - ( 5 * 1000) ));
		if( messageList != null ){
			if( messageList.moveToFirst() ){
				long rowId = messageList.getLong(0);
				mDataStore.updateEntry("messageStore", rowId, "status", 2);
				processMessage(rowId);
			}
			messageList.close();
		}
		
		
    //Log.w(TAG,"reviewRecent()");
        //final Uri uri = Uri.parse("content://sms/inbox");
       //Log.w(TAG,"content://sms/inbox LOOKUP _id,address,body,date");
        Cursor localSMS = SqliteWrapper.query(mContext, mResolver, Uri.parse("content://sms/inbox"), 
        		new String[] { "_id", "address", "body", "date" }, 
        		"date > " + (System.currentTimeMillis() - ( 24 * 60 * 60 * 1000) ), 
        		null, 
        		"date asc");
        
       //Log.w(TAG,"reviewRecent() query completed");
        if (localSMS != null) {
            try {
                if (localSMS.moveToFirst()) {
                	for(int i = 0; i < localSMS.getCount(); i++){
                		localSMS.moveToPosition(i);
                		
	                    int msgId      = localSMS.getInt(0);
	                    String address = localSMS.getString(1);
	                    String message = localSMS.getString(2);
	                    Date msgDate   = new Date();
	                    	msgDate.setTime(localSMS.getLong(3));
	                    
	                    Uri messageUri = Uri.parse("content://sms/" + msgId);
	                    
	                   //Log.w(TAG,"Recent Message " + messageUri.toString() + " " + address + " " + msgDate.toString() + " " + message );
	                    
	            		if( message.contains("RSAKEY:") ){
	            		//Log.w(TAG,"Found Unhandled RSA Key");
	            			processMessageRSAKey(messageUri);
	            		}
	            		else if( message.contains("RSASMS:") ){
	            		//Log.w(TAG,"Found Unhandled RSASMS Message");
	            			processMessageRSASMS(messageUri);
	            		}
	            		else if( message.contains("RSA SMS,") ){
	                	//Log.w(TAG,"Found Unhandled RSA Request");
	                		processMessageRSARequest(messageUri);
	                	}

                	}
                }
            } finally {
                localSMS.close();
            }
        }
    	
    }

	
	    
	    /*
	MessageReceiverService: messageStore LOOKUPN _id, originator, e, n, d, where status=3(rsakey) (multirow limit 10, most recent from originator with this status)
		MessageReceiverService: DECRYPT message with e, n, d (try out multiple)
		MessageReceiverService: messageStore NEW originator, message, decrypted, useRSA=mRSAToggle, queue=inbox, e, n, status=4(received)
		MessageReceiverService: updateMessageBody with decrypted
		"Content-type: text/rsasms\n\n"
	*/
	public void decrypteRSA() {
	//Log.i(TAG,"decryptRSA() -------------------------------");
			
	// April 10, 2009 originator may need a cleanup function to remove + or add them, same with +1 or not.
		
		boolean decryptSuccess = false;
		Cursor messageData = null;
		Cursor lookupKeyData = null;
		
		try {
			
		//Log.i(TAG,"messageStore ITERATE _id,originator,message,uri where queue is 'incoming' and status is decrypt(7) limit(10)");
			messageData = mDataStore.getEntry("messageStore", new String[] {"_id","originator","message","uri"}, "queue = \"incoming\" AND status=7", 10); //decrypt
			
	    //Log.i(TAG,"Checking messageStore for null.");
	    	if( messageData == null ){ 
	    		//Log.e(TAG,"messageStore was null, leaving decryptRSA()."); 
	    		return; }
	    	
	    //Log.i(TAG,"Checking messageStore for existence of data.");
	    	if( !messageData.moveToFirst() ){ 
	    		//Log.w(TAG,"messageStore let us know no records exist, leaving decryptRSA()."); 
	    		messageData.close(); return; }
	    	
	    //Log.i(TAG,"Checking messageStore for closure.");
	    	if( messageData.isClosed() ){ 
	    		//Log.e(TAG,"messageStore wasn't available, leaving decryptRSA()."); 
	    		messageData.close(); return; }
	
	    //Log.i(TAG,"Iterating messageStore");
			for(int messagePosition = 0; messagePosition < messageData.getCount(); messagePosition++){
				messageData.moveToPosition(messagePosition);
				
			//Log.i(TAG,"Iterating messageStore " + messagePosition + " Getting _id");
				long messageId = messageData.getLong(0);
			//Log.i(TAG,"Iterating messageStore " + messagePosition + " Getting originator");
				String originator = messageData.getString(1);
			//Log.i(TAG,"Iterating messageStore " + messagePosition + " Getting message");
				String message = messageData.getString(2);
				if( originator == null || message == null ){
				//Log.e(TAG,"Iterating messageStore " + messagePosition + " message or originator is null, moving on.");
					continue;
				}
			//Log.i(TAG,"Iterating messageStore " + messagePosition + " Getting uri");
				String messageUri = messageData.getString(3);
				
			//Log.w(TAG,"Looking to decrypt message(" + message.trim() + ") uri("+messageUri.toString()+")");
				
				lookupKeyData = null;
				decryptSuccess = false;
		    	
		    //Log.i(TAG,"Iterating messageStore " + messagePosition + " messageStore GET _id,e,n,d where status is rsakey(3) and originator is originator limit(20)");
		    	lookupKeyData = mDataStore.getEntry("messageStore", new String[] {"_id","e","n","d"}, "status=3 AND originator = \"" + originator + "\"", 20); //rsakey
	
		    //Log.i(TAG,"Iterating messageStore " + messagePosition + " Checking messageStore for null.");
		    	if( lookupKeyData == null ){ 
		    		//Log.e(TAG,"messageStore was null, moving on."); 
		    		continue; }
		    	
		    //Log.i(TAG,"Iterating messageStore " + messagePosition + " Checking messageStore for existence of data.");
		    	if( !lookupKeyData.moveToFirst() ){ 
		    		//Log.e(TAG,"messageStore let us know no records exist, moving on."); 
		    		lookupKeyData.close(); continue; }
		    	
		    //Log.i(TAG,"Iterating messageStore " + messagePosition + " Iterating messageStore");
				for(int keyPosition = 0; keyPosition < lookupKeyData.getCount(); keyPosition++){
	    			lookupKeyData.moveToPosition(keyPosition);
	    			
	    			mRSA = new RSA();
	    			
	    		//Log.i(TAG,"Iterating messageStore " + messagePosition + " Iterating messageStore " + keyPosition + " Getting _id");
	    			long rowId = lookupKeyData.getLong(0);
	    			String eRSA = lookupKeyData.getString(1);
	    			String nRSA = lookupKeyData.getString(2);
	    			String dRSA = lookupKeyData.getString(3);
	    			if( eRSA == null || nRSA == null || dRSA == null){
	    			//Log.e(TAG,"Iterating messageStore " + messagePosition + " e or n are null");
	    				continue;
	    			}
	    			/**/
					mRSA.e = new BigInteger(eRSA);
					/**/
					
	    			//Log.i(TAG,"Iterating messageStore " + messagePosition + " Iterating messageStore " + rowPosition + " Getting e");
					//mRSA.e = new BigInteger(rowData.getString(rowData.getColumnIndex("e")));
					
				//Log.i(TAG,"Iterating messageStore " + messagePosition + " Iterating messageStore " + keyPosition + " Getting n");
					mRSA.n = new BigInteger(nRSA);
					
				//Log.i(TAG,"Iterating messageStore " + messagePosition + " Iterating messageStore " + keyPosition + " Getting d");
					mRSA.d = new BigInteger(dRSA);
	
				//Log.i(TAG,"Iterating messageStore " + messagePosition + " Iterating messageStore " + keyPosition + " Decrypting message to decrypted");
					String decrypted = "";
					decrypted = mRSA.decryptSafe(message);
					
				//Log.i(TAG,"Iterating messageStore " + messagePosition + " Iterating messageStore " + keyPosition + " Checking decrypted.");
					if( !decrypted.contains("Content-type: text/rsasms") ){
					//Log.i(TAG,"Iterating messageStore " + messagePosition + " Iterating messageStore " + keyPosition + " Checking decrypted failed, wrong key matching, moving on.");
						continue;
					}
					
					
					decrypted += "\n";    					
					char[] buffer = new char[decrypted.length()];
					
					decrypted.getChars(decrypted.indexOf("\n\n")+1, decrypted.length()-1, buffer, 0);
					
					String newDecrypted = new String(buffer).trim();
					if( newDecrypted.contains(":RSAKEY:") ){
						String[] m1 = newDecrypted.split(":RSAKEY");
						newDecrypted = m1[0].trim();
						
					//Log.i(TAG,"Found RSA Keys encrypted in reply");
						String[] m2 = m1[1].split(":");// m1[1] = :e@n:
						String[] k = m2[1].split("@");
			        	mRSA = new RSA();
			        	mRSA.e = new BigInteger(k[0]);
			        	mRSA.n = new BigInteger(k[1]);
			        	
			        	mDataStore.addEntry("keyStore", new String[] {"tel","e","n"}, new String[]{originator, k[0], k[1]});

					}
					
					
					//mDataStore.updateEntry("recipientStore", rowId, new String[] {"e","n"}, new String[] { mRSA.e.toString(), mRSA.n.toString()});
					//mDataStore.updateEntry("recipientStore", rowId, "status", 5); //rsasend
					
					
					mDataStore.updateEntry("messageStore", messageId, new String[] {"e","n","d","decrypted","queue","useRSA"}, new String[] {mRSA.e.toString(), mRSA.n.toString(), mRSA.d.toString(), newDecrypted, "incoming", "true"});
					mDataStore.updateEntry("messageStore", messageId, new String[] {"status"}, new int[] {4}); //received
					
					if( messageUri != null ){
					//Log.w(TAG,"Updated Message has an assoicated messageUri("+messageUri+"), updating with decrypted");
						ContentValues values = new ContentValues();
	                    values.put("body", "<RSA SMS received an encrypted message>\n" + newDecrypted);
	                    values.put("read", 3 );
	                    int status = SqliteWrapper.update(mContext, mResolver, Uri.parse(messageUri), values, null, null);
	                   //Log.w(TAG,"Updated Message messageUri("+messageUri.toString()+")");
					}else{
					//Log.e(TAG,"Updated Message didn't have an assoicated messageUri");
					}
	        		//replaceMessageBody(this, msgs, "RSA SMS\n\nI have received an encrypted connection from \n" + originator + ".");
					decryptSuccess = true;
					
					//Log.w(TAG,"Interesting displaySurfaceDialog()");
					
					//displaySurfaceDialog(mContext, newDecrypted, originator);
					
					break;
					
	    		}
				
		    	lookupKeyData.close();
		    	
		    	if( !decryptSuccess ){
		    		// April 10, 2009 Possibly the user reinstalled the application after uninstall or cleared the database somehow.
		    	//Log.e(TAG,"An incoming message was unable to be decrypted with our recently shared keys.");
					mDataStore.updateEntry("recipientStore", messageId, "status", 6); //fail
				}
			}
			
		} catch (SQLiteException e) {
		//Log.w(TAG,"decryptRSA SQLiteException " + e.getLocalizedMessage());
		} finally {
			if( messageData != null ) {	messageData.close(); }
	    	if( lookupKeyData != null ){ lookupKeyData.close(); }
		}
		
	
	}
		    
		
	/*
	MessageReceiverService: recipientStore LOOKUP _id,messageid,e,n,tel where status=5(rsasend)
		MessageReceiverService: messageStore LOOKUP _id, decrypted where _id=messageid
		MessageReceiverService: SEND tel ENCRYPT decrypted using e,n formatted "RSASMS:?:"
		MessageReceiverService: recipientStore UPDATE status=4(sent)
	*/		
	public void sendRSA(long recipientId){
		
		String statusMessage = "";
		
		
			
		// ========================================
		// Load Recipient Data
    	Cursor recipientData = mDataStore.getEntry("recipientStore", recipientId, new String[] {"_id","messageid","e","n","tel","name","status"});
    	//rsasend(5)
    	
    	
    //Log.i(TAG,"Checking recipientStore for null.");
    	if( recipientData == null ){ 
    		//Log.e(TAG,"recipientStore was null, leaving sendRSA()."); 
    		return; }
    	
    //Log.i(TAG,"Checking recipientStore for existence of data.");
    	if( !recipientData.moveToFirst() ){ 
    		//Log.w(TAG,"recipientStore let us know no records exist, leaving sendRSA()."); 
    		recipientData.close(); return; }
    	
    //Log.i(TAG,"Checking recipientStore for closure.");
    	if( recipientData.isClosed() ){ 
    		//Log.e(TAG,"recipientStore wasn't available, leaving sendRSA()."); 
    		recipientData.close(); return; }

    	long messageid = recipientData.getLong(1);
    	String e = recipientData.getString(2);
    	String n = recipientData.getString(3);
    	String tel = recipientData.getString(4);
    	String name = recipientData.getString(5);
    	int status = recipientData.getInt(6);

		// ========================================
		// Safety Check
    	if( status != 5 ){
    	//Log.e(TAG,"Someone tried to send a status("+status+") where only rsasend(5) is accepted.");
    		return;
    	}
    	if( e == null || n == null ){
    	//Log.e(TAG,"e or n is null, not good, changing status to fail.");
    		mDataStore.updateEntry("recipientStore", recipientId, "status", 9);
    		return;
    	}

    	
		// ========================================
		// Setup the RSA Object
    	mRSA = new RSA();
		mRSA.e = new BigInteger(e);
		mRSA.n = new BigInteger(n);
		
		String decrypted = "";
		Cursor lookupMessageData = null;
		try {
		//Log.w(TAG,"sendRSA("+recipientId+") messageStore GET decrypted where _id is messageid.");
			lookupMessageData = mDataStore.getEntry("messageStore", new String[] {"decrypted"}, "_id = " + messageid);
			
		//Log.i(TAG,"sendRSA("+recipientId+") messageStore for null.");
        	if( lookupMessageData == null ){ 
        		//Log.e(TAG,"messageStore was null, changing status to fail."); 
        		mDataStore.updateEntry("recipientStore", recipientId, "status", 9); return; }
        	
        //Log.i(TAG,"sendRSA("+recipientId+") messageStore for existence of data.");
        	if( !lookupMessageData.moveToFirst() ){ 
        		//Log.e(TAG,"messageStore let us know no records exist, leaving sendRSA()."); 
        		lookupMessageData.close(); 
        		mDataStore.updateEntry("recipientStore", recipientId, "status", 9); return; }
        	
        //Log.i(TAG,"sendRSA("+recipientId+") messageStore Getting decrypted.");
			decrypted = lookupMessageData.getString(0);
			//Log.w(TAG,"DECRYPTED MessageService 546 " + decrypted);
			
		} finally {
			if( lookupMessageData != null ){ lookupMessageData.close(); }
		}
		
		
    	SmsManager smsMan = SmsManager.getDefault();
    	
    //Log.w(TAG,"Writing decrypted to log " + decrypted);
    	
		ArrayList<String> messageDivided = smsMan.divideMessage("RSASMS:"+ mRSA.encryptSafe("Content-type: text/rsasms\n\n" + decrypted )+":");
		
		//*
		String outMessage = "<RSA SMS sent an encrypted message in " + messageDivided.size() + " SMS>\n"+decrypted;
		//outMessage += "\n and used " + messageDivided.size() + " SMS message"; if( messageDivided.size() > 1 ){ outMessage += "s"; }
		
		ContentValues values = new ContentValues(7);
        values.put("address", tel);
        values.put("date", System.currentTimeMillis());
        values.put("read", 1);//read ? Integer.valueOf(1) : Integer.valueOf(0));
        values.put("body", outMessage);
        values.put("status", smsMan.STATUS_ON_SIM_SENT); //64 Info
        
        
        //Log.i(TAG,"Inserting outgoing sms message in the outbox.");
        Uri outgoingUri = mResolver.insert(Uri.parse("content://sms/sent"), values);
        //*/
        
        // sentwait(6) -> deliverywait(7) -> sent(4)
       //Log.w(TAG,"sendRSA("+recipientId+") recipientStore UPDATE status to sentwait(6)");
       //Log.w(TAG,"sendRSA("+recipientId+") recipientStore UPDATE uri to outgoingUri("+outgoingUri.toString()+")");
        mDataStore.updateEntry("recipientStore", recipientId, "status", 6); //sentwait
		mDataStore.updateEntry("recipientStore", recipientId, new String[] {"uri"}, new String[] {outgoingUri.toString()} );
		
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messageDivided.size());
		for( int i = 0; i < messageDivided.size(); i++){
			sentIntents.add(PendingIntent.getBroadcast( mContext, 0, new Intent("com.rsasms.MessageReceiver.MESSAGE_SENT_CONFIRMED", outgoingUri, mContext, com.rsasms.MessageReceiver.class).putExtra("recipientid", recipientId).putExtra("setstatus", 7).putExtra("totalcnt", messageDivided.size() ), 0));
		}
    	
    	ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(messageDivided.size());
    	for( int i = 0; i < messageDivided.size(); i++){
    		deliveryIntents.add(PendingIntent.getBroadcast( mContext, 0, new Intent("com.rsasms.MessageReceiver.MESSAGE_DELIVERY_CONFIRMED", outgoingUri, mContext, com.rsasms.MessageReceiver.class).putExtra("recipientid", recipientId).putExtra("setstatus", 4).putExtra("totalcnt", messageDivided.size() ), 0)); //sent
    	}

		
    	smsMan.sendMultipartTextMessage(tel, null, messageDivided, sentIntents, deliveryIntents);
    	
    	
	}
	
	public void sendRSA() {
		//Log.w(TAG,"sendRSA() --------------------------");
		
		Cursor recipientData = null;
		Cursor lookupMessageData = null;
		//MessageSender messageSender;
		try {
		//Log.i(TAG,"recipientStore ITERATE _id,messageid,e,n,tel where status is sendRSA(5)");
	    	recipientData = mDataStore.getEntry("recipientStore", new String[] {"_id"}, "status=5"); //sendRSA
	

	    	
		} catch (SQLiteException e) {
		//Log.w(TAG,"sendRSA() SQLiteException " + e.getLocalizedMessage());
		} finally {

		    //Log.i(TAG,"Checking recipientStore for null.");
	    	if( recipientData == null ){ 
	    		//Log.e(TAG,"recipientStore was null, leaving sendRSA()."); 
	    		return; }
	
	    	
	    //Log.i(TAG,"Checking recipientStore for existence of data.");
	    	//Log.w(TAG,"sendRSA() requery()");
	    	if( !recipientData.moveToFirst() ){ 
	    		//Log.w(TAG,"recipientStore let us know no records exist, leaving sendRSA()."); 
	    		recipientData.close(); return; }
	    	
	    //Log.i(TAG,"Checking recipientStore for closure.");
	    	//Log.w(TAG,"sendRSA() isClosed()");
	    	if( recipientData.isClosed() ){ 
	    		//Log.e(TAG,"recipientStore wasn't available, leaving sendRSA()."); 
	    		recipientData.close(); return; }
	    	
	    	//Log.w(TAG,"sendRSA() isClosed()");
	    	if( recipientData.isClosed() ){ recipientData.close(); return; }
	    	
	    	//Log.w(TAG,"sendRSA() getPosition()");
	    	if( !recipientData.moveToFirst() ){ recipientData.close(); return; }
	    	
	    	//Log.w(TAG,"sendRSA() getCount()");
	    	if( recipientData.getCount() == 0 ){ recipientData.close(); return; }
	    	
	    	//Log.w(TAG,"^^^^^^^^^^^^^^^^^^^^^^^^^");
	    	
	    	
	    	//Log.w(TAG,"Iterating recipientStore");
	    	for(int recipientPosition = 0; recipientPosition < recipientData.getCount(); recipientPosition++){
	    		
	    		
				if( recipientData.moveToPosition(recipientPosition) ){
				//Log.w(TAG,"Iterating recipientStore " + recipientPosition + " Getting _id");
					long recipientId = recipientData.getLong(0);
					
					//if( rowId <= 0 ){ continue; }
					
					sendRSA(recipientId);
				}
				
			}
	    	
			if( recipientData != null ){ recipientData.close(); }
			if( lookupMessageData != null ){ lookupMessageData.close(); }
		}
		
		
	
	}
	
    public void displaySurfaceDialog(Context context, String message, String originator){
    //Log.i(TAG,"displaySurfaceDialog()");
    	Intent smsDialogIntent  = new Intent(context, com.rsasms.activity.SurfaceDialog.class)
    		.putExtra("body", message)
    		.putExtra("originator", originator)
    		.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    	context.startActivity(smsDialogIntent);
    }


    public void deliveryStatus(Intent intent) {
	//Log.w(TAG,"deliveryStatus()");
		
		
		String action = intent.getAction();
        Bundle extras = intent.getExtras();
        //extras.getString("messageid");
        

        //
        
        Uri messageUri = intent.getData();
        long messageid = 0;
        messageid = extras.getLong("messageid");
        long recipientid = 0;
        recipientid = extras.getLong("recipientid");
        int totalcnt = 0;
        totalcnt = extras.getInt("totalcnt");
        int setstatus = 0;
        setstatus = extras.getInt("setstatus");
        if (messageUri != null ){ 
        	//Log.i(TAG,"deliveryStatus() uri("+messageUri+") messageid("+messageid+") recipientid("+recipientid+") totalcnt("+totalcnt+")");
        }
        
       //Log.i(TAG,"deliveryStatus() Received Action("+action+")");
        
    	String[] localSMS = lookupLocalSMS(messageUri);
    	if( localSMS == null ){ 
    		//Log.e(TAG,"localSMS didn't result in a record.");
    		return; }
    	long localSMSId   = Long.parseLong(localSMS[0]);
    	String originator = localSMS[1];
    	String message    = localSMS[2];
    	String date       = localSMS[3];
    	
        
    	Date d = new Date();
    	//Calendar c = new Calendar();
    	//int year = Calendar.get(Calendar.YEAR) - 1900;
    	String minutes = "";
    	if( d.getMinutes() < 10 ){ minutes = "0" + d.getMinutes(); } else {minutes = ""+d.getMinutes();}
    	String printDate = (d.getYear() + 1900) + "/" + (d.getMonth()+1) + "/" + d.getDate() + " " + d.getHours() + ":" + minutes;
    	
        if( action.equalsIgnoreCase("com.rsasms.MessageReceiver.MESSAGE_DELIVERY_CONFIRMED")){
        //Log.w(TAG,"deliveryStatus() Delivery Confirmed");
        	
            ContentValues values = new ContentValues();
            if( totalcnt > 0 ){
            	
            	if( message.contains("delivery confirmed") ){
            		String[] ms = message.split("\n");
            		message = "";
            		for(int s = 0; s < ms.length; s++){
            			if( ms[s].contains("delivery confirmed") ){
            				
            				int currentcnt = 0;
            				String b = ms[s].substring(20, ms[s].indexOf("/"));
            			//Log.w(TAG,"delivery confirmed with current count, I found b(" + b + ") from " + ms[s]);
            				currentcnt = Integer.parseInt(b);
            				currentcnt++;
            				message += "\n delivery confirmed "+currentcnt+"/"+totalcnt;
            			}else{
            				if( s > 0 ){ message += "\n"; }
            				message += ms[s];
            			}
            		}
            		values.put("body", message);
            	}else{
            		values.put("body", message + "\n delivery confirmed 1/"+totalcnt);
            	}
            }else{
            	values.put("body", message + "\n delivery confirmed " + printDate );
            }
            SqliteWrapper.update(mContext, mResolver, messageUri, values, null, null);
            
        }
        if( action.equalsIgnoreCase("com.rsasms.MessageReceiver.MESSAGE_SENT_CONFIRMED")){
        //Log.w(TAG,"deliveryStatus() Sent Confirmed");

            int resultCode = intent.getIntExtra("result", 0);
            //
            String printStatus = "";
            if( resultCode == Activity.RESULT_OK ) {
            	printStatus = "Success";
            }
            if( resultCode == SmsManager.RESULT_ERROR_GENERIC_FAILURE ){
            	printStatus = "Failure";
            } 
            else if( resultCode == SmsManager.RESULT_ERROR_NO_SERVICE ){
            	printStatus = "No Service";    
            }
            else if( resultCode == SmsManager.RESULT_ERROR_NULL_PDU ){
                printStatus = "Null PDU";
            }
            else if( resultCode == SmsManager.RESULT_ERROR_RADIO_OFF ){
            	printStatus = "Radio Off";
            } else {
            	printStatus = "Unknown " + resultCode;
            }
        	
            ContentValues values = new ContentValues();
            if( totalcnt > 0 ){
            	if( message.contains("sending confirmed") ){
            		String[] ms = message.split("\n");
            		message = "";

            		for(int s = 0; s < ms.length; s++){

            			if( ms[s].contains("sending confirmed") ){

            				
            				int currentcnt = 0;
            				String b = ms[s].substring(19, ms[s].indexOf("/"));
            			//Log.w(TAG,"sending confirmed with current count, I found b(" + b + ") from " + ms[s]);
            				currentcnt = Integer.parseInt(b);
            				currentcnt++;
            				
            				if( resultCode != Activity.RESULT_OK ){ currentcnt = 0; }
            				
            				message += "\n sending confirmed "+currentcnt+"/"+totalcnt;
            				if( resultCode == Activity.RESULT_OK ){
            					message += " Success";
            				}

            				if( resultCode != Activity.RESULT_OK ){
            					if( !(messageid > 0) && !(recipientid > 0) ){
    								int recipientid2 = mDataStore.getId("recipientStore", "uri = \"" + messageUri.toString() +"\"");
    								if( recipientid2 > 0 ){
    									message += "\n  " + printDate + " " + printStatus + " (Reset Recipient Status to Ready)";
    									mDataStore.updateEntry("recipientStore", recipientid2, "status", 2);
    								}else{
    									int messageid2 = mDataStore.getId("messageStore", "uri = \"" + messageUri.toString() +"\"");
        								if( messageid2 > 0 ){
        									message += "\n  " + printDate + " " + printStatus + " (Reset Message Status to Ready)";
        									mDataStore.updateEntry("messageStore", messageid2, new String[] {"status"}, new int[] {2});
        									//mDataStore.updateEntry("messageStore", messageid2, new String[] {"uri"}, new String[] {""});
        									mDataStore.updateEntry("recipientStore", new String[] {"status"}, new int[] {2}, "messageid="+messageid2+ " AND status > 0");
        									// should we set recipients to 2 also?
        								}
    								}
            					}
            					
            					else if( setstatus > 0 && recipientid > 0 ){
	            					message += "\n  " + printDate + " " + printStatus + " (Reset Recipient Status to Ready)";
	            					setstatus = 2;
            					}
            					
            					else {
            						if( recipientid > 0 ){
            							message += "\n  " + printDate + " " + printStatus + " (Reset Recipient Status to Ready)";
	            						mDataStore.updateEntry("recipientStore", recipientid, "status", 2);
        							}
            						else if( messageid > 0 ){
	            						message += "\n  " + printDate + " " + printStatus + " (Reset Message Status to Ready)";
	            						mDataStore.updateEntry("messageStore", messageid, "status", 2);
	            						mDataStore.updateEntry("recipientStore", new String[] {"status"}, new int[] {2}, "messageid="+messageid+ " AND status > 0");
            						}else{
            							int recipientid2 = mDataStore.getId("recipientStore", "uri = \"" + messageUri.toString() +"\"");
        								if( recipientid2 > 0 ){
        									message += "\n  " + printDate + " " + printStatus + " (Reset Recipient Status to Ready)";
        									mDataStore.updateEntry("recipientStore", recipientid2, "status", 2);
        								}else{
        									int messageid2 = mDataStore.getId("messageStore", "uri = \"" + messageUri.toString() +"\"");
            								if( messageid2 > 0 ){
            									message += "\n  " + printDate + " " + printStatus + " (Reset Message Status to Ready)";
            									mDataStore.updateEntry("messageStore", messageid2, "status", 2);
            									mDataStore.updateEntry("recipientStore", new String[] {"status"}, new int[] {2}, "messageid="+messageid2+ " AND status > 0");
            									// should we set recipients to 2 also?
            								}
        								}	
            						}
            					}
            				}
            			}else{
            				
            				if( s > 0 ){ message += "\n"; }
            				message += ms[s];
            			}
            		}
            		values.put("body", message);
            	}else{
            		values.put("body", message + "\n sending confirmed 1/"+totalcnt + "\n delivery confirmed 0/"+totalcnt);
            	}
            }else{
            	values.put("body", message + "\n sending confirmed " + printDate );
            }
            
            
            SqliteWrapper.update(mContext, mResolver, messageUri, values, null, null);
        }

    	if( setstatus > 0 ){
    	//Log.w(TAG,"deliveryStatus() updating status("+setstatus+")");
    		mDataStore.updateEntry("recipientStore", recipientid, "status", setstatus);
    	}

        
    }

    /*
     * mode: ready for delivery from Service
		MessageReceiverService: android.intent.action.SERVICE_STATE
		MessageReceiverService: messageStore LOOKUPN _id,decrypted where status=2(ready) and queue = "outbox"
		MessageReceiverService: recipientStore LOOKUPN _id,tel,useRSA where _id=messageStore._id
		MessageReceiverService: useRSA=true: SEND tel "RSA SMS,"
		MessageReceiverService: useRSA=true: recipientStore UPDATE status=3(rsawait)
		MessageReceiverService: useRSA=false: SEND tel decrypted
		MessageReceiverService: useRSA=false: recipientStore UPDATE status=4(sent)

     */
    
    
	public void processMessage(long messageId){
		
		
	//Log.w(TAG,"processMessage("+messageId+") Load Recipient List");
		Cursor recipientRow = null;
		recipientRow = mDataStore.getEntry("recipientStore", new String[] {"_id","usersa","tel","status"} , "messageid="+messageId);
		
	//Log.w(TAG,"processMessage("+messageId+") Load Message Details");
		Cursor messageRow = null;
		messageRow = mDataStore.getEntry("messageStore", new String[] {"decrypted","status"} , "_id="+messageId);
		
		
		
		if( recipientRow != null && messageRow != null ){
		if( recipientRow.moveToFirst() && messageRow.moveToFirst() ){

			// Message   //=========================================
			String messageDecrypted = ""; messageDecrypted = messageRow.getString(0);
			int messageStatus = 0; messageStatus = messageRow.getInt(1);
			
		//Log.w(TAG,"processMessage("+messageId+") Iterate ");
			for( int recipientPosition = 0; recipientPosition < recipientRow.getCount(); recipientPosition++ ){
				recipientRow.moveToPosition(recipientPosition);
					
				// Recipient //=========================================
				long recipientId = 0; recipientId = recipientRow.getLong(0);
				boolean rUseRSA = true; rUseRSA = recipientRow.getString(1).contains("true");
				String recipientTel = recipientRow.getString(2);
				int recipientStatus = 0; recipientStatus = recipientRow.getInt(3);
				
				if( recipientStatus == 2 ){ // ready 
					//good
				}else if( recipientStatus == 3 ){ // rsawait, waiting for RSA Key from our friend, good to check keyStore 
					//good
				}else if( recipientStatus == 4 ){ // sent
				//Log.w(TAG,"Refuse to resend messages that are already marked sent.");
					continue;
				}else if( recipientStatus == 5 ){ // sendRSA
					//good
				}else{
				//Log.w(TAG,"recipientStore status was out of bounds("+recipientStatus+")");
					continue;
				}

				SmsManager smsMan = SmsManager.getDefault();
				
				// Use RSA   //=========================================
				if( rUseRSA ){

					Cursor kRow = null;
					//kRow = mDataStore.getEntry("recipientStore", new String[] {"e","n"} , "updated > "+(System.currentTimeMillis() - (300 * 1000) )+" usersa = \"true\" AND e != \"\" AND n != \"\" AND tel = \""+tel+"\"", 10);
					kRow = mDataStore.getEntry("keyStore", new String[] {"e","n"} , "updated > "+(System.currentTimeMillis() - (270 * 1000) )+" AND tel = \""+recipientTel+"\"", 10);
					
					if( kRow != null ){
						if ( kRow.moveToFirst() ){
						//Log.w(TAG,"Found recent RSA SMS key for this recipient, using it.");
							String e = kRow.getString(0);
							String n = kRow.getString(1);
							mDataStore.updateEntry("recipientStore", recipientId, "status", 5);
							mDataStore.updateEntry("recipientStore", recipientId, new String[] {"e","n"}, new String[] {e, n} );
							sendRSA(recipientId);
							continue;

						}
					}

					ArrayList<String> messageDivided = smsMan.divideMessage("RSA SMS, incoming encrypted message.\n\nGet RSA SMS at the Android Market to receive the waiting message.\n\n");
					
		    		String outMessage = "<RSA SMS sent a connection request in " + messageDivided.size() + " SMS, the encrypted message will be delivered once the other party has installed this software>\n" + messageDecrypted;
		    		
		    		ContentValues values = new ContentValues(7);
		            values.put("address", recipientTel);
		            values.put("date", System.currentTimeMillis());
		            values.put("read", 1);//read ? Integer.valueOf(1) : Integer.valueOf(0));
		            values.put("body", outMessage);
		            values.put("status", 64);

		           //Log.i(TAG,"Inserting outgoing sms message in the sent box.");
		            Uri outgoingUri = mResolver.insert(Uri.parse("content://sms/sent"), values);
		            
		          //smsMan.sendMultipartTextMessage(recipientTel, null, messageDivided, null, null);
			    	mDataStore.updateEntry("recipientStore", recipientId, "uri", outgoingUri.toString() ); 
			    	mDataStore.updateEntry("recipientStore", recipientId, "status", 10); //rsawaitsendwait(10)->rsawaitdeliverywait(11)->rsawait(3)
					
		    		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(1);
		            sentIntents.add(PendingIntent.getBroadcast( mContext, 0, new Intent("com.rsasms.MessageReceiver.MESSAGE_SENT_CONFIRMED", outgoingUri, mContext, com.rsasms.MessageReceiver.class).putExtra("recipientid", recipientId).putExtra("setstatus", 11).putExtra("totalcnt", messageDivided.size() ), 0));
		        	
		        	ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(1);
		            deliveryIntents.add(PendingIntent.getBroadcast( mContext, 0, new Intent("com.rsasms.MessageReceiver.MESSAGE_DELIVERY_CONFIRMED", outgoingUri, mContext, com.rsasms.MessageReceiver.class).putExtra("recipientid", recipientId).putExtra("setstatus", 3).putExtra("totalcnt", messageDivided.size() ), 0)); //sent

			    	smsMan.sendMultipartTextMessage(recipientTel, null, messageDivided, sentIntents, deliveryIntents);

				}else{
					// Clear Transport
					
					ArrayList<String> messageDivided = smsMan.divideMessage(messageDecrypted);
					
		    		//*
		    		//String outMessage = "<RSA SMS sent clear message in " + messageDivided.size() + " SMS>\n" + messageDecrypted;
		    		String outMessage = messageDecrypted;
		    		
		    		ContentValues values = new ContentValues(7);
		            values.put("address", recipientTel);
		            values.put("date", System.currentTimeMillis());
		            //values.put("read", 1);//read ? Integer.valueOf(1) : Integer.valueOf(0));
		            values.put("read", SmsManager.STATUS_ON_SIM_SENT); //possibly should be under type 'read'
		            values.put("body", outMessage);
		            //values.put("status", SmsManager.STATUS_ON_SIM_SENT); //possibly should be under type 'read'

		           //Log.i(TAG,"Inserting outgoing sms message in the sent box.");
		            Uri outgoingUri = mResolver.insert(Uri.parse("content://sms/sent"), values);
		            //*/
		            
		           //Log.w(TAG,"Iterating recipientStore " + recipientPosition + " recipientStore UPDATE status to sent(4)");
		           //Log.w(TAG,"Iterating recipientStore " + recipientPosition + " recipientStore UPDATE uri to outgoingUri("+outgoingUri.toString()+")");
		            mDataStore.updateEntry("recipientStore", recipientId, "status", 4); //sent
					mDataStore.updateEntry("recipientStore", recipientId, new String[] {"uri"}, new String[] {outgoingUri.toString()} );
					
		    		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(1);
			        sentIntents.add(PendingIntent.getBroadcast( mContext, 0, new Intent("com.rsasms.MessageReceiver.MESSAGE_SENT_CONFIRMED", outgoingUri, mContext, com.rsasms.MessageReceiver.class).putExtra("recipientid", recipientId).putExtra("totalcnt", messageDivided.size() ) , 0));
			    	
			    	ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(1);
			        deliveryIntents.add(PendingIntent.getBroadcast( mContext, 0, new Intent("com.rsasms.MessageReceiver.MESSAGE_DELIVERY_CONFIRMED", outgoingUri, mContext, com.rsasms.MessageReceiver.class).putExtra("recipientid", recipientId).putExtra("totalcnt", messageDivided.size() ) , 0));

			    	smsMan.sendMultipartTextMessage(recipientTel, null, messageDivided, sentIntents, deliveryIntents);
			    	//smsMan.sendMultipartTextMessage(tel, null, messageDivided, null, null);

			    	//mDataStore.updateEntry("recipientStore", recipientId, "status", 4);
			    	
				}
				
			}
		}
		}
		
		recipientRow.close();
		messageRow.close();
	}
	
	public void wordStash(String wordBlob){

	//Log.w(TAG,"wordStash()++++++++++++++++++++++++++");
		wordBlob = wordBlob.replaceAll("\n"," ").trim();
		String[] words = wordBlob.split(" ");
		String wordCanvas = "";
		
		for(int mTempInt = 0; mTempInt < words.length; mTempInt++){
			
			wordCanvas = words[mTempInt].trim();
			if (wordCanvas.length() == 0){
				continue;
			}
			//Log.w(TAG,"Looking for Quote in " + wordCanvas);
			if( wordCanvas.substring(0, 1).equalsIgnoreCase("\"") ){
				//Log.w(TAG,"Found Quote");
				for(++mTempInt; mTempInt < words.length; mTempInt++){ // consider for pre++
					wordCanvas += " " + words[mTempInt];
					if( words[mTempInt].trim().substring(words[mTempInt].length()-1,words[mTempInt].length()).equalsIgnoreCase("\"") ){
						//Log.w(TAG,"Found End Quote");
						//mTempString.replace('"',' ').trim();
						break;
					}
				}
			}else{
				//Log.w(TAG," Substring(0,1): " + wordCanvas.trim().substring(0, 1) + " end " + wordCanvas.substring(words[mTempInt].length()-1,words[mTempInt].length()) );
			}

			
			wordCanvas = wordCanvas.replaceAll("\"", " ").trim();
			//Log.w(TAG,"wordStash() get _id");
			int rowId = mDataStore.getId("wordStore", "word=\""+wordCanvas+"\"");
			//Log.w(TAG,"wordStash() return from getId("+rowId+")");	
			//rowData = mDataStore.getEntry("wordStore", new String[] {"_id"} , "word=\"" + mTempString + "\"");
			
			//Log.i(TAG,"Checking for null.");
			//if( rowData == null ) {Log.e(TAG,"recipientStore was null, leaving saveState()."); return;}
			
		//Log.i(TAG,"Checking for existence of data.");
			if( rowId > 0 ) {
				//Log.w(TAG,"Word Exists " + mTempString);
				//mDataStore.updateEntry("wordStore", rowId, "occurrence", "occurrence + 1");
			}else{
				//Log.w(TAG,"wordStore CREATE with word=" + wordCanvas);
				mDataStore.addEntry("wordStore", new String[] {"word"}, new String[] {wordCanvas} );
				//mDataStore.addEntry("wordStore", "word", wordCanvas );
			}

			
		}
		
	//Log.w(TAG,"wordStash() return");
	
	}

	
}
