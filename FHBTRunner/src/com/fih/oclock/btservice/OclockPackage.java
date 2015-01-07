package com.fih.oclock.btservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Intent;
import android.util.Log;

public class OclockPackage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2132319284694708287L;

	private static final String TAG = "OclockPackage";

	//	Data Fields
	boolean ack = false;
	String client_name = null;
	byte[] data = null;
	byte[] icon = null;


	OclockPackage() {}
	OclockPackage(Intent i) {
		ack = i.getBooleanExtra(ConnectionManagerActions.ACK_FIELD, false);
		client_name = i.getStringExtra(ConnectionManagerActions.CLIENT_FIELD);
		if(null == client_name) {
			client_name = ConnectionManagerActions.CLIENT_UNKNOWN_NAME;
		}
		data = i.getByteArrayExtra(ConnectionManagerActions.DATA_FIELD);
		icon = i.getByteArrayExtra(ConnectionManagerActions.ICON_FIELD);
	}


	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		// write 'this' to 'out'...
		
		//		ACK
		if(ack) {
			out.write(1);
		} else {
			out.write(0);
		}
		
		int length = 0;
		//		Client Name
		if(null != client_name) {
			length = client_name.getBytes().length;
			out.write(length >> 24);
			out.write(length >> 16);
			out.write(length >> 8);
			out.write(length);
			out.write(client_name.getBytes());
		} else {
			out.write(0);
			out.write(0);
			out.write(0);
			out.write(0);
		}
		//		Data
		if(null != data) {
			length = data.length;
			out.write(length >> 24);
			out.write(length >> 16);
			out.write(length >> 8);
			out.write(length);
			out.write(data);
		} else {
			out.write(0);
			out.write(0);
			out.write(0);
			out.write(0);
		}
		//		Icon
		if(null != icon) {
			length = icon.length;
			out.write(length >> 24);
			out.write(length >> 16);
			out.write(length >> 8);
			out.write(length);
			out.write(icon);
		} else {
			out.write(0);
			out.write(0);
			out.write(0);
			out.write(0);
		}
		//		END
		out.write(0xaa);
		out.write(0xbb);
		out.write(0xcc);
		out.write(0xdd);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	   	// populate the fields of 'this' from the data in 'in'...
		//		ACK
		byte b = in.readByte();
		if(0 == b) {
			ack = false;
		} else {
			ack = true;
		}

		int length = 0;
		//		Client Name
		length = in.readInt();
		if(0 < length) {
			byte[] name = new byte[length];
			in.read(name, 0, length);
			client_name = new String(name);
		} else {
			client_name = "";
		}

		//		Data
		length = in.readInt();
		if(0 < length) {
			data = new byte[length];
			in.read(data, 0, length);
		} else {
			data = new byte[0];
		}

		//		Icon
		length = in.readInt();
		if(0 < length) {
			icon = new byte[length];
			in.read(icon, 0, length);
		} else {
			icon = new byte[0];
		}
	}

	
	public static byte[] getByteArray(OclockPackage obj) {
		byte[] result = null;
		try {
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			ObjectOutputStream objout = new ObjectOutputStream(ba);
			objout.writeObject(obj);
			result = ba.toByteArray();
		} catch(NullPointerException n) {
		} catch(IOException ioe) {
			Log.d(TAG, ioe.getLocalizedMessage());
		} catch(Exception e) {}
		return result;
	}
	
	public static OclockPackage getObject(byte[] obj) {
		OclockPackage result = new OclockPackage();
		try {
			ByteArrayInputStream ba = new ByteArrayInputStream(obj);
	        ObjectInputStream objin = new ObjectInputStream(ba);
	        result = (OclockPackage) objin.readObject();
		} catch(NullPointerException n) {
		} catch(IOException ioe) {
			//Log.d(TAG, ioe.getLocalizedMessage());
		} catch(Exception e) {}
		return result;
	}
	
	public Intent getIntent() {
		Intent result = new Intent();
		result.setAction(client_name);
		if(null != data) {
			result.putExtra(ConnectionManagerActions.DATA_FIELD, data);
		}
		if(null != icon) {
			result.putExtra(ConnectionManagerActions.ICON_FIELD, icon);
		}
		
		return result;
	}
	
}
