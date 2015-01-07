package com.fih.oclock.btservice;

import android.content.Context;
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
    	if(total_len > 5 &&
    			(byte)0x78 == metaBuffer[total_len - 1] &&		//	ObjectStreamConstants.TC_ENDBLOCKDATA
    			(byte)0xdd == metaBuffer[total_len - 2] &&
    			(byte)0xcc == metaBuffer[total_len - 3] &&
    			(byte)0xbb == metaBuffer[total_len - 4] &&
    			(byte)0xaa == metaBuffer[total_len - 5]) {
    		Log.d(TAG, "Complete!");
    		return true;
    	} else {
    		Log.d(TAG, String.format("{ %02x, %02x, %02x, %02x, %02x }", 
    				metaBuffer[total_len - 5],
    				metaBuffer[total_len - 4], 
    				metaBuffer[total_len - 3], 
    				metaBuffer[total_len - 2], 
    				metaBuffer[total_len - 1]));
    	}
    	Log.d(TAG, "NOT Complete!......."+total_len);
    	return false;
    }
    
    public byte[] parse(byte[] buf, int len)
    {
    	byte[] result = null;
    	Log.d(TAG, "MessageHandler::parse()::"+len);

    	if(metaBufferSize <= (total_len + len)) {
    		//	TODO: too many data.........
    		Log.d(TAG, "TOO MANY DATA!");
    		metaBuffer[total_len/2 + 0] = metaBuffer[total_len - 5];
    		metaBuffer[total_len/2 + 1] = metaBuffer[total_len - 4];
    		metaBuffer[total_len/2 + 2] = metaBuffer[total_len - 3];
    		metaBuffer[total_len/2 + 3] = metaBuffer[total_len - 2];
    		metaBuffer[total_len/2 + 4] = metaBuffer[total_len - 1];
    		Log.d(TAG, "TooMany::"+String.format("Total: %d -> %d", total_len, total_len/2 + 5));
    		total_len = total_len/2 + 5; 
    	}

    	if(metaBufferSize > (total_len + len)) {
    		System.arraycopy(buf, 0, metaBuffer, total_len, len);
    		total_len += len;
    		bComplete = checkIfComplete();
    	}

    	if(bComplete) {
	    	OclockPackage opackage = OclockPackage.getObject(metaBuffer);
	    	mContext.sendBroadcast(opackage.getIntent());
	    	if(opackage.ack) {
	    		Log.d(TAG, "ACK is Needed!");
	    		result = opackage.client_name.getBytes();
	    	} else {
	    		Log.d(TAG, "NO ACK Required...");
	    		result = null;
	    	}
	    	total_len = 0;
    	}
    	
    	return result;
    }

    final int metaBufferSize = 409600;
    byte[] metaBuffer = new byte[metaBufferSize];
    int total_len = 0;
    boolean bComplete = false;


}
