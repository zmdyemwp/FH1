package com.fih.oclock.btservice;

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

    boolean checkIfComplete() {
    	if(total_len > 4 &&
    			(byte)0xdd == metaBuffer[total_len - 1] &&
    			(byte)0xcc == metaBuffer[total_len - 2] &&
    			(byte)0xbb == metaBuffer[total_len - 3] &&
    			(byte)0xaa == metaBuffer[total_len - 4]) {
    		Log.d(TAG, "Complete!");
    		return true;
    	} else {
    		Log.d(TAG, String.format("{ %02x, %02x, %02x, %02x }", 
    				metaBuffer[total_len - 4], 
    				metaBuffer[total_len - 3], 
    				metaBuffer[total_len - 2], 
    				metaBuffer[total_len - 1]));
    	}
    	Log.d(TAG, "NOT Complete!......."+total_len);
    	return false;
    }
    
    public void parse(byte[] buf, int len)
    {
    	Log.d(TAG, "MessageHandler::parse()::"+len);
    	
    	if(metaBufferSize <= (total_len + len)) {
    		//	TODO: too many data.........
    		Log.d(TAG, "TOO MANY DATA!");
    		metaBuffer[total_len/2 + 0] = metaBuffer[total_len - 4];
    		metaBuffer[total_len/2 + 1] = metaBuffer[total_len - 3];
    		metaBuffer[total_len/2 + 2] = metaBuffer[total_len - 2];
    		metaBuffer[total_len/2 + 3] = metaBuffer[total_len - 1];
    		Log.d(TAG, "TooMany::"+String.format("Total: %d -> %d", total_len, total_len/2 + 4));
    		total_len = total_len/2 + 4; 
    	}
    	
    	if(metaBufferSize > (total_len + len)) {
    		System.arraycopy(buf, 0, metaBuffer, total_len, len);
    		total_len += len;
    		bComplete = checkIfComplete();
    	}
    	
    	if(bComplete) {
	    	int index = 0;
	    	for(; index < total_len; index++) {
	    		if((byte)0xff == metaBuffer[index]) {
	    			break;
	    		}
	    	}
	    	
	    	if(index == total_len) {
	    		//	TODO: 0xff NOT found
	    		Log.d(TAG, "ACTION NOT FOUND");
	    		total_len = 0;
	    		bComplete = false;
	    	} else if(0 < index) {
	    		Log.d(TAG, "INDEX/LENG:"+index+"/"+total_len);
		    	byte[] action = new byte[index];
		    	System.arraycopy(metaBuffer, 0, action, 0, index);
		    	int data_len = total_len - 4 - 1 - index;
		    	byte[] data = new byte[data_len];
		    	System.arraycopy(metaBuffer, index + 1, data, 0, data_len);
		    	
		    	Intent i = new Intent();
		    	String szAction = new String(action);
		    	i.setAction(szAction);
		    	i.putExtra(ConnectionManagerActions.DATA_FIELD, data);
		    	mContext.sendBroadcast(i);
		    	Log.d(TAG, "parse()::sendBroadcast()::"+szAction+"("+szAction.length()+")");
	    	}
	    	total_len = 0;
    	}
    }

    final int metaBufferSize = 409600;
    byte[] metaBuffer = new byte[metaBufferSize];
    int total_len = 0;
    boolean bComplete = false;


}
