package com.fih.pairingserver;

import java.lang.reflect.Method;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.fih.oclock.connectionmanageraction.ConnectionManagerActions;

public class PairingReceiver extends BroadcastReceiver {
	private static final String TAG = "PairingReceiver";
	private static final int CONFIRM_TIMEOUT = 10000;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
			BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
			int preState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);
			Log.d(TAG, dev.getAddress());
			Log.d(TAG, String.format("state change: %d -> %d", preState, state));
			switch(state) {
				case BluetoothDevice.BOND_NONE:
					break;
				case BluetoothDevice.BOND_BONDING:
					break;
				case BluetoothDevice.BOND_BONDED:
					//	TODO: send CHECK and wait for ACK or unpair for unknown device
					h.postDelayed(r, CONFIRM_TIMEOUT);
					break;
				default:
					Log.d(TAG, "UNKNOWN STATE");
					break;
			}
		} else if(action.equals(ConnectionManagerActions.PAIRING_CONFIRM)) {
			h.removeCallbacks(r);
		}
	}

	Handler h = new Handler();
	Runnable r = new Runnable() {
		@Override
		public void run() {
			removeAll();
		}
	};

	void removeAll() {
		Set<BluetoothDevice> devs = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		try {
			for(BluetoothDevice dev:devs) {
				Method m = BluetoothDevice.class.getMethod("removeBond", (Class<?>[])null);
				m.invoke(dev, (Object[])null);
				Log.d(TAG, "REMOVE: "+dev.getAddress());
			}
		} catch(Throwable e) {
			Log.d(TAG, "PairingReceiver::removeAll");
		}
	}
}

