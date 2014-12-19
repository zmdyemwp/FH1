package com.fih.oclock.btservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fih.oclock.connectionmanageraction.ConnectionManagerActions;

public class BTServiceReceiver extends BroadcastReceiver {

    private String TAG = "BTServiceReceiver";
    private static Communicator mBTCommunicator;

    public BTServiceReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
		Log.i(TAG, " onReceive::"+action);
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (bluetoothState)
            {
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.i(TAG," BluetoothAdapter.STATE_CONNECTED");
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Log.i(TAG," BluetoothAdapter.STATE_DISCONNECTED");
					break;
                case BluetoothAdapter.STATE_TURNING_OFF:
					Log.i(TAG," BluetoothAdapter.STATE_TURNING_OFF");
                    if (mBTCommunicator != null) {
                        mBTCommunicator.stop();									//	Stop Service
					}
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Log.i(TAG," BluetoothAdapter.STATE_TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.i(TAG," BluetoothAdapter.STATE_ON") ;
                    mBTCommunicator =  new Communicator(context);
                    mBTCommunicator.start();									//	Start Service
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.i(TAG," BluetoothAdapter.STATE_TURNING_ON") ;
                    break;
                default:
					Log.e(TAG, "UNKNOWN BluetoothAdapter STATE");
                    break;
            }

        } else if(action.equals(ConnectionManagerActions.CONTROL_ACTION)) {								//	Control commands
			try {
				String command = intent.getStringExtra(ConnectionManagerActions.COMMAND_FIELD);

				if(command.contains(ConnectionManagerActions.INIT_SERVER)) {
					if(null == mBTCommunicator) {
						mBTCommunicator = new Communicator(context);
						mBTCommunicator.start();
					}
				} else if(command.contains(ConnectionManagerActions.CONNECT_TO)) {
					String address = intent.getStringExtra(ConnectionManagerActions.DEVICE_ADDRESS);

					BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
					if(null == mBTCommunicator) {
						mBTCommunicator = new Communicator(context);
						mBTCommunicator.start();
					}
					if(null != device) {
						mBTCommunicator.connect(device, true);						//	Connect to Server
					} else {
						if(null == device) Log.d(TAG, "###### Device NOT Found");
						if(null == mBTCommunicator)	Log.d(TAG, "###### Communicator::NULL");
					}
				} else if(command.contains(ConnectionManagerActions.DISCONNECT_FROM)) {
					if (mBTCommunicator != null) {
						mBTCommunicator.start();									//	Disconnect from Server
					}
				} else if(command.contains(ConnectionManagerActions.GET_CURRENT_STATE)) {
					int state = Communicator.STATE_NONE;
					if(null != mBTCommunicator) {
						Log.d(TAG, "GET_CURRENT_STATE");
						state = mBTCommunicator.getState();						//	Get current state
					}
					Intent i = new Intent();
					i.setAction(ConnectionManagerActions.RETURN_CURRENT_STATE);
					i.putExtra(ConnectionManagerActions.DATA_FIELD, state);
					context.sendBroadcast(i);
				} else {
					Log.e(TAG, "UNKNOWN Command from Client");
				}
			} catch(NullPointerException n) {
				Log.d(TAG, "NullPointerException::"+action);
			}
		 } else if(action.contains(ConnectionManagerActions.ACTION_SEND_DATA)) {
			try{
				//	TODO: Send Data
				String NAME = intent.getStringExtra(ConnectionManagerActions.CLIENT_FIELD);
				byte[] name = NAME.getBytes();
				byte[] data = intent.getByteArrayExtra(ConnectionManagerActions.DATA_FIELD);
				byte[] data2send = new byte[1 + name.length + data.length + 4];
				int offset = 0;
				System.arraycopy(name, 0, data2send, offset, name.length);
				offset += name.length;
				data2send[offset] = (byte) 0xff;
				offset ++;
				System.arraycopy(data, 0, data2send, offset, data.length);
				offset += data.length;
				data2send[offset] = (byte)0xaa;
				data2send[offset + 1] = (byte)0xbb;
				data2send[offset + 2] = (byte)0xcc;
				data2send[offset + 3] = (byte)0xdd;
				
				if(null != mBTCommunicator) {
					mBTCommunicator.write(data2send);							//	Send data
				}
			} catch(NullPointerException n) {
				// Do nothing
				Log.d(TAG, "NullPointerException::"+action);
			}
        } else {
			Log.e(TAG, "UNKNOWN Broadcast ACTION::"+action);
		}
    }
}
