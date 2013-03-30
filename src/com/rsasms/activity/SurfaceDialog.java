package com.rsasms.activity;

import com.rsasms.R;

public class SurfaceDialog extends Activity {
	
	private CharSequence originatorChars;
	   protected void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        
	        //setTitle("RSA SMS");
	        //getWindow().setBackgroundDrawableResource(R.drawable.icon);
	        //getWindow().setTitle("Encrypted Message Received");

	        CharSequence messageChars =  getIntent().getCharSequenceExtra("body");
	        originatorChars =  getIntent().getCharSequenceExtra("originator");

	        new AlertDialog.Builder(this)
	                .setMessage(messageChars)
	                .setPositiveButton(android.R.string.ok, mOkListener)
	                .setNeutralButton("Reply to\n" + originatorChars, mReplyListener)
	                .setCancelable(false)
	                .show();
	    }

	    private final OnClickListener mOkListener = new OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            //SurfaceDialog.this.finish();
	        	Log.w("RSASMS SurfaceDialog","OK xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
	        	getWindow().closeAllPanels();
	            finish();
	            
	        }
	    };
	    
	    private final OnClickListener mReplyListener = new OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	Intent s = new Intent();
	        	s.setClassName("com.rsasms.activity", "RSASMS");
	        	//s.setComponent(com.rsasms.activity.RSASMS.class);
				s.putExtra("originator", originatorChars); 
				//sendIntent.setType("message/rfc822"); 
				//startActivity(Intent.createChooser(s, "RSASMS"));
				startActivity(s);
	            //SurfaceDialog.this.finish();
	            finish();
	        }
	    };
}
