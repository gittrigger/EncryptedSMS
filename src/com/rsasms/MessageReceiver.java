package com.rsasms;

public class MessageReceiver extends BroadcastReceiver {

	private final static String TAG = "RSASMS MessageReceiver";
	
	static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private static Object mStartingServiceSync = new Object();
	private static WakeLock mWakeService;
	private static Context mContext;
	
	public void onReceive(Context context, Intent intent) {
	//Log.i(TAG,"onReceive(Action Received:"+intent.getAction()+") ++++++++++++++++++++++++++++++++++++++++++++++++");
		onReceiveWithPrivilege(context, intent, false);
		return;
	}
	
	protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
		mContext = context;
	//Log.i(TAG,"onReceiveWithPrivilege(privileged:"+privileged+", Action Received:"+intent.getAction()+") +++++++++++++++++++++++++++++++++++++++++");
		if(!privileged && intent.getAction().equals(SMS_RECEIVED_ACTION)){
			return;
		}
		
//		SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
//    	for( int i = 0; i < msgs.length; i++){
//    		intent.setClass(context, MessageReceiverService.class);
//    		intent.putExtra("result", getResultCode());
//    	}
		// Have the ability to get details of this
		
		intent.setClass(mContext, MessageService.class);
		intent.putExtra("result", getResultCode());
		
		beginHostingService(context,intent);
	//Log.i(TAG,"onReceiveWithPrivilege() Back from beginHostingService()");
	}

	public static void beginHostingService(Context context, Intent intent) {
	//Log.i(TAG,"beginHostingService()");
		mContext = context;
		synchronized (mStartingServiceSync){
			/*
			 * PARTIAL_WAKE_LOCK will ensure the CPU is active but not enable the screen.  
			 * Good for late night auto upgrading application, run through all updates 
			 * available and update them and just leave the notifications (or create them) 
			 * to indicate each update, the user will notice when they use the device next, 
			 * they don't need to be "alerted". 
			 */
			//Log.i(TAG,"beginHostingService() synchronized()");
			if(mWakeService == null){
			//Log.i(TAG,"beginHostingService() PowerManager");
				PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				mWakeService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StartingAlertService");
				mWakeService.setReferenceCounted(false);
			}
			//Log.i(TAG,"beginHostingService() acquire()");
			mWakeService.acquire();
			
			/*
			 * startService();
			 */
			//Log.i(TAG,"beginHostingService() startService()");
			context.startService(intent);
			//Log.i(TAG,"beginHostingService() startService() return");
		}
	}
	
	public static void finishHostingService(MessageService service, int serviceId) {
	//Log.i(TAG,"finishHostingService()");
		synchronized (mStartingServiceSync){
			if(mStartingServiceSync != null){
				
				//Toast.makeText(mContext, "Shutting Down Message Receiver Service (or service thread?)",Toast.LENGTH_SHORT).show();
				
				/*
				 * Stop the Service, or Service Thread, if this service receives messages
				 * after this point and the Activity .RSASMS is not running then the service
				 * is still running and this must be a "Thread" and not a "Service" that is ending.
				 */
				if( service.stopSelfResult(serviceId) ){
					mWakeService.release();
				}
			}
		}		
	}


}
