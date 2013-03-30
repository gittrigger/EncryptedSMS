package com.rsasms.activity;

import com.rsasms.DbAdapter;
import com.rsasms.R;


public class ToList extends Activity implements OnClickListener, OnCheckedChangeListener, OnLongClickListener {
	
	@Override
	protected void onStop() {
	//Log.i(TAG,"onStop() ++++++++++++++++++++++++++++++++++++++++s");
		mDataStore.close();
		super.onStop();
	}

	@Override
	protected void onPause() {
	//Log.i(TAG,"onPause() ++++++++++++++++++++++++++++++++++++++++s");
		mDataStore.close();
		super.onPause();
	}

	
		private static final String TAG = "RSASMS ToList";
		
		private DbAdapter mDataStore;
		private Button mBaseButton;
		
		private Button mTriggerOne;
		//private Button mTriggerTwo;
		//private Button mTriggerThree;
		
		private RadioButton mRadioButton;
		private ToggleButton mRSAToggle;
		private LinearLayout mLinearLayout;
		private LinearLayout mToList;
		private ScrollView mScrollView;
		
		private final static int BUTTON_ALPHA = 1;
		private final static int BUTTON_BETA = 2;
		private final static int BUTTON_GAMMA = 3;
		
		private LinearLayout mButtonbar, mContactview;
		private String mTitle;
		private String mText;
		private long mMessageId;
		private Bundle mIntentExtras;
		
		private int mTempInt;
		
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Log.i(TAG,"layout");
			setContentView(R.layout.tolist);
			
		//Log.i(TAG,"database");	
			mDataStore = new DbAdapter(this);
			mDataStore.loadDb();
			
			
		//Log.i(TAG,"layout views");
			mScrollView = (ScrollView) this.findViewById(R.id.tolist_scrollview);
			mToList = (LinearLayout) this.findViewById(R.id.tolist);
			mLinearLayout = (LinearLayout) this.findViewById(R.id.tolist_linearlayout);
				//mLinearLayout.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				mLinearLayout.setOrientation(LinearLayout.VERTICAL);
				mLinearLayout.setVisibility(LinearLayout.VISIBLE);
			mBaseButton = (Button) this.findViewById(R.id.tolist_button);
				//mBaseButton.setVisibility(Button.VISIBLE);
			
		//Log.i(TAG,"Create Contact View");
			mContactview = new LinearLayout(this);
				mContactview.setLayoutParams(mLinearLayout.getLayoutParams());
				mContactview.setVisibility(LinearLayout.VISIBLE);
				//mContactview.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				mContactview.setOrientation(LinearLayout.HORIZONTAL);
				mContactview.setPadding(5, 5, 0, 0);
				
		//Log.i(TAG,"layout Buttonbar");
			//mButtonbar = new LinearLayout(this);
				//mButtonbar.setLayoutParams( new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT) );
				//mButtonbar.setOrientation(LinearLayout.HORIZONTAL);
				//mButtonbar.setVisibility(LinearLayout.VISIBLE);
			mButtonbar = (LinearLayout) this.findViewById(R.id.tolist_buttonbar);
				//mToList.addView(mButtonbar);
			mTriggerOne = new Button(this);
				LayoutParams params = mBaseButton.getLayoutParams();
				mTriggerOne.setLayoutParams(params);
				
				//mTriggerOne.setPadding(10, 10, 0, 0);
				mTriggerOne.setId(BUTTON_ALPHA);
				mTriggerOne.setVisibility(Button.VISIBLE);
				//mTriggerOne.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				mButtonbar.addView(mTriggerOne);
				mButtonbar.setPadding(5, 0, 5, 0);
				/**//*
			mTriggerTwo = new Button(this);
				mTriggerTwo.setLayoutParams(mBaseButton.getLayoutParams());
				mTriggerTwo.setId(BUTTON_BETA);
				mTriggerTwo.setVisibility(Button.VISIBLE);
				//mTriggerTwo.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				mButtonbar.addView(mTriggerTwo);
			mTriggerThree = new Button(this);
				mTriggerThree.setLayoutParams(mBaseButton.getLayoutParams());
				mTriggerThree.setVisibility(Button.VISIBLE);
				mTriggerThree.setId(BUTTON_GAMMA);
				//mTriggerThree.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				mButtonbar.addView(mTriggerThree);
				/**/
			
			//mButtonbar = (LinearLayout) this.findViewById(R.id.listbuttonbar);
			
			//mRadioButton = (RadioButton) this.findViewById(R.id.listrowrsa_active);
			//mRSAToggle = (ToggleButton) this.findViewById(R.id.listrowrsa_usersa);
			
			mIntentExtras = getIntent().getExtras();
			mMessageId = mIntentExtras != null ? mIntentExtras.getLong("messageid") : 0;
			this.setTitle("Long Press to Exclude");
		//Log.i(TAG,"get entries from recipientStore where messageid="+mMessageId);
			
			populateList();
		//Log.i(TAG,"ready for action");
			
			mTriggerOne.setText("Close");
			mTriggerOne.setOnClickListener(this);
			//mTriggerTwo.setText("");
			//mTriggerTwo.setOnClickListener(this);
			//mTriggerThree.setText("");
			//mTriggerThree.setOnClickListener(this);
			
			//mRadioButton.setOnClickListener(this);
			//mRSAToggle.setOnClickListener(this);
			/**/
		
		
		}

		private void populateList() {
		//Log.i(TAG,"populateList() -----------------------");
			
			Cursor rowData = null;
		//Log.i(TAG,"recipientStore GET _id,name,tel,useRSA,status where messageid=mMessageId");
			rowData = mDataStore.getEntry("recipientStore", new String[] {"_id","name","tel","useRSA","status"}, "messageid="+mMessageId );
			if( rowData == null ){Log.e(TAG,"recipientStore is null, Setting Title to Message, leaving populateList()"); setTitle("Nothing to Report."); return;}
			if( !rowData.moveToFirst() ){Log.e(TAG,"recipientStore is empty, Setting Title to Message, leaving populateList()"); setTitle("Nothing to Report."); rowData.close(); return;}
			startManagingCursor(rowData);
			
			ToggleButton tempToggleButton = new ToggleButton(this);
			LinearLayout tempLinearLayout = new LinearLayout(this);
			//tempLinearLayout.setLayoutParams(mLinearLayout.getLayoutParams());
			LayoutParams params = mLinearLayout.getLayoutParams();
			params.width = params.WRAP_CONTENT;
			tempLinearLayout.setWeightSum((float) 1.0);
			tempLinearLayout.setLayoutParams(params);
			tempLinearLayout.setOrientation(LinearLayout.VERTICAL);
			tempLinearLayout.setVisibility(LinearLayout.VISIBLE);
			
			tempLinearLayout.setEnabled(true);
			
			mTempInt = 0;
			int colCount = (int) (rowData.getCount() / 3);
			//colCount = colCount > 1 ? colCount : 1;
			//colCount = 3;
			int diff = rowData.getCount() - ( 3 * colCount); 
			
			int thisColumn = 1;
			
			for(int i = 0; i < rowData.getCount(); i++){
				rowData.moveToPosition(i);
				String name = rowData.getString(rowData.getColumnIndex("name"));
				Long rowId = rowData.getLong(rowData.getColumnIndex("_id"));
				Boolean useRSA = rowData.getString(rowData.getColumnIndex("useRSA")).contentEquals("true");
			//Log.i(TAG,"Recipient name("+name+")");
				// April 17 2009, possibly this should be tempToggleButton = baseButtonDesign.copy();
				tempToggleButton = new ToggleButton(this);
				tempToggleButton.setLayoutParams(mBaseButton.getLayoutParams());
				//tempToggleButton.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				tempToggleButton.setEllipsize(TextUtils.TruncateAt.MIDDLE);
				tempToggleButton.setText(name);
				tempToggleButton.setMinWidth(100);
				tempToggleButton.setMaxWidth(100);
				tempToggleButton.setTextOn(name);
				tempToggleButton.setTextOff(name);
				//tempToggleButton.setEnabled(true);
				//tempToggleButton.setClickable(true);
				tempToggleButton.setChecked(useRSA);
				tempToggleButton.setOnCheckedChangeListener(this);
				tempToggleButton.setOnClickListener(this);
				tempToggleButton.setOnLongClickListener(this);
				tempToggleButton.setTag(rowId);
				
				tempLinearLayout.addView(tempToggleButton);
				mTempInt++;
				
				if( mTempInt >= colCount && thisColumn < 3 ){
					thisColumn++;
					mTempInt = 0;
					mContactview.addView(tempLinearLayout);
					tempLinearLayout = new LinearLayout(this);
					//tempLinearLayout.setLayoutParams(mLinearLayout.getLayoutParams());
					tempLinearLayout.setWeightSum((float) 1.0);
					tempLinearLayout.setLayoutParams(params);
					tempLinearLayout.setOrientation(LinearLayout.VERTICAL);
					tempLinearLayout.setVisibility(LinearLayout.VISIBLE);
					tempLinearLayout.setEnabled(true);
				}
				
			}
			
			if( mTempInt > 0 ){
				mContactview.addView(tempLinearLayout);
			}
			
			mLinearLayout.addView(mContactview);
			
			//rl.moveToFirst();
			
			//Log.i(TAG,"create simple cursor adapter");
			//String[] from = new String[] { "name", "tel" };
			//int[] to = new int[] { R.id.listrowrsa_name, R.id.listrowrsa_number };
			//SimpleCursorAdapter sca = new SimpleCursorAdapter(this, R.layout.listrowrsa, rl, from, to);
			
			//Log.i(TAG,"set list adapter to sca");
			
			//mToList.setAdapter(sca);
			
			//setListAdapter(sca);
			//getListView().setAdapter(sca);
			//getListView().setTextFilterEnabled(true);
			rowData.close();
						
		}

		public void onClick(View v) {
			
			mTempInt = v.getId();
			
			switch( v.getId() ){
			case BUTTON_ALPHA:
			//Log.i(TAG,"onClick() close mTempInt("+mTempInt+") alphaId("+BUTTON_ALPHA+")");
				finish();
				break;
			case BUTTON_BETA:
			//Log.i(TAG,"onClick() trigger beta");
				
				int rowId = Integer.parseInt(v.getTag().toString());
				if( rowId > 0 ){
					//mDataStore.updateEntry("recipientStore", rowId, new String[] {"useRSA"}, new String[] {useRSA});
					mDataStore.deleteEntry("recipientStore", rowId);
					v.setVisibility(Button.INVISIBLE);
					//mTriggerTwo.setText("");
					//mTriggerTwo.setTag(0);
				}
				// Open an editing pane for a reply to this originator
				//Intent i2 = new Intent(Intent.ACTION_SEND); 
				//i2.putExtra(Intent.EXTRA_TEXT, "Body");
				//i2.putExtra(Intent.EXTRA_SUBJECT, "Header");
				//i2.putExtra("sms_body", mText); 
				//i2.setType("vnd.android-dir/mms-sms"); 
				//startActivity(Intent.createChooser(i2, "SMS"));
				//int rowId = this.
				//Toast.makeText(this, "getSelectedItemPosition("+this.getSelectedItemPosition()+")\ngetSelectedItemId("+this.getSelectedItemId()+")", 1500).show();
			case BUTTON_GAMMA:
			//Log.i(TAG,"onClick() trigger gamma");
				// Send an acknowledgment to the message originator
				Intent sendIntent = new Intent(this, RSASMS.class);
				sendIntent.putExtra("messageid", mMessageId); 
				//sendIntent.setType("message/rfc822"); 
				//startActivity(Intent.createChooser(sendIntent, "RSASMS"));
				startActivity(sendIntent);
			}
			
		}


		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			int rowId = Integer.parseInt(v.getTag().toString());
		//Log.i(TAG,"onCheckedChange() rowId("+rowId+") isSelected("+v.isSelected()+") isClickable("+v.isClickable()+") isFocused("+v.isFocused()+")");
			String useRSA = "true";
			if( !isChecked ){ useRSA = "false"; }
			mDataStore.updateEntry("recipientStore", rowId, new String[] {"useRSA"}, new String[] {useRSA});
		}

		
		public boolean onLongClick(View v) {
			int rowId = Integer.parseInt(v.getTag().toString());
			
			int status = 0;
			status = mDataStore.getInteger("recipientStore", rowId, "status");
			
		//Log.w(TAG,"First time using getInteger command. value=" + status);
			if( status == 0 ){
				mDataStore.updateEntry("recipientStore", rowId, "status", "1");
				v.setVisibility(Button.VISIBLE);
				//v.setEnabled(true);
			}else{
				mDataStore.deleteEntry("recipientStore", rowId);
				v.setVisibility(Button.INVISIBLE);
				//v.setEnabled(false);
				//v.clearFocus();
				//v.setClickable(true);
				mTriggerOne.requestFocusFromTouch();
			}
			v.clearFocus();
			//mTriggerTwo.setText("");
			//mTriggerTwo.setTag(0);
			return false;
		}
		
	}

