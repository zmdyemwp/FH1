package com.fih.oclock.btservice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fih.oclock.connectionmanageraction.ConnectionManagerActions;

public class BTClientReceiver extends BroadcastReceiver {

	private static final String TAG = "BTClientReceiver";
	BTActivity ui;
	int current_state = 0;
	
	BTClientReceiver(Activity activity) {
		ui = (BTActivity)activity;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		
		if(action.equals(ConnectionManagerActions.RETURN_CURRENT_STATE)) {
			current_state = intent.getIntExtra(ConnectionManagerActions.DATA_FIELD, 99);
			Log.d(TAG, "onReceive()::RETURN_CURRENT_STATE::" + current_state);
			ui.runOnUiThread(r);
		}
	}
	
	Runnable r = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG, "Current State:" + current_state);
			switch(current_state) {
			case 0:
			case 1:
				ui.UpdateConnectionState(0);
				break;
			case 2:
				ui.UpdateConnectionState(2);
				break;
			case 3:
				ui.UpdateConnectionState(1);
				break;
			}
		}
		
	};

}
