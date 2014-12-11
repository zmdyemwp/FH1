package com.fih.oclock.btservice;

import com.fih.oclock.connectionmanageraction.ConnectionManagerActions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by kinden on 2014/9/10.
 */
public class MessageHandler {
    private static final String TAG = "MsgHandler";
    private Context mContext;
    public MessageHandler(Context context)
    {
        mContext = context;
    }

    public void parse(byte[] buf, int len)
    {
    	Log.d(TAG, "MessageHandler::parse()::"+len);
    	/*for(int i = 0; i < len; i++) {
    		Log.d(TAG, String.format("%02x", buf[i]));
    	}*/
    	int index = 0;
    	for(; index < len; index++) {
    		if((byte)0xff == buf[index]) {
    			break;
    		}
    	}
    	if(0 < index) {
    		Log.d(TAG, "INDEX/LENG:"+index+"/"+len);
	    	byte[] action = new byte[index];
	    	System.arraycopy(buf, 0, action, 0, index);
	    	int data_len = len - 1 - index;
	    	byte[] data = new byte[data_len];
	    	System.arraycopy(buf, index + 1, data, 0, data_len);
	    	
	    	Intent i = new Intent();
	    	i.setAction(new String(action));
	    	i.putExtra(ConnectionManagerActions.DATA_FIELD, data);
	    	mContext.sendBroadcast(i);
    	}
    }




}
