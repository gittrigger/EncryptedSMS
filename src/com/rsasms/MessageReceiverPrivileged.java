package com.rsasms;

public class MessageReceiverPrivileged extends MessageReceiver {
	private static final String TAG = "RSASMS MessageReceiverPrivileged";
	public void onReceive(Context c, Intent i){
		Log.i(TAG,"onReceive() sending to onReceiveWithPrivilege() in MessageReceiver");
		onReceiveWithPrivilege(c,i,true);
	}
}
