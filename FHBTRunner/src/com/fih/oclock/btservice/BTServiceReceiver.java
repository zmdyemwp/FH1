package com.fih.oclock.btservice;

import java.lang.reflect.Method;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

public class BTServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "BTServiceReceiver";
    private static Communicator mBTCommunicator;
    private static final boolean bIsServer = false;

    public BTServiceReceiver() {}

    private void setDiscoverable(boolean bDiscoverable) {
    	BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
    	try {
    		Class<?>[] paramClass = new Class<?>[] {int.class, int.class};
    		Method m = ba.getClass().getMethod("setScanMode", paramClass);
    		Object[] paramObject;
    		if(bDiscoverable) {
    			paramObject = new Object[] {BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 0};
    		} else {
    			paramObject = new Object[] {BluetoothAdapter.SCAN_MODE_CONNECTABLE, 0};
    		}
    		m.invoke(ba, paramObject);
    		Log.d(TAG, "setDiscoverable()::setScanMode");
    	} catch(Exception e) {
    		Log.d(TAG, "SOMETHING WRONG AS SET SCAN MODE");
    	}
    }
    private void checkBondDeviceSetDiscoverable() {
    	if( ! bIsServer) {
    		return;
    	}
    	Set<BluetoothDevice> devs = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
    	if(0 == devs.size()) {
    		//	NO bond devices
    		setDiscoverable(true);
    	} else {
    		//	there is some BOND device
    		setDiscoverable(false);
    	}
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
		Log.i(TAG, " onReceive::"+action);
		if(action.equals(ConnectionManagerActions.GET_CURRENT_VERSION)) {
			Log.d(TAG, "Current Version: "+context.getString(R.string.version));
		} else if(action.equals(ConnectionManagerActions.RESET_BOND_DEVICES)) {
			removeAll();
		} else if (action.equals(ConnectionManagerActions.NOTIFICATION_ATTACH)) {
			confirmIsClient();
		} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {						//				ADAPTER STATUS
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            doAdapterStateChange(context, bluetoothState);
        } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {					//				DEVICE BOND STATUS
        	BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        	int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
    		int preState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);
        	doBondStateChange(dev, state, preState);
        } else if(action.equals(ConnectionManagerActions.PAIRING_CONFIRM)) {
        	stopTimer();
        } else if(action.equals(ConnectionManagerActions.CONTROL_ACTION)) {						//				CONTROL COMMANDS
			try {
				handleCommand(context, intent);
			} catch(NullPointerException n) {
				Log.d(TAG, "NullPointerException::"+action);
			}
		 } else if(action.contains(ConnectionManagerActions.ACTION_SEND_DATA)) {				//				SEND DATA
			 try {
				 doSendData(intent);
			 } catch(Exception e) {
				 Log.d(TAG, "++NullPointerException::"+action);
			 }
			
        } else if(action.equals(ConnectionManagerActions.ALARM_MANAGER_CHECK)) {
        	doInit(context, true);
        } else if(action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
        	this.doBondConfirm();
        } else {
			Log.e(TAG, "UNKNOWN Broadcast ACTION::"+action);
		}
    }
    
    
    
    
    /**
     * 
     * */
    void doAdapterStateChange(Context context, int bluetoothState) {
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
				doDeinit(context);														//	-Stop Service
                break;
            case BluetoothAdapter.STATE_OFF:
                Log.i(TAG," BluetoothAdapter.STATE_TURNING_OFF");
                break;
            case BluetoothAdapter.STATE_ON:										//	-Start Service
                Log.i(TAG," BluetoothAdapter.STATE_ON") ;
                doInit(context);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.i(TAG," BluetoothAdapter.STATE_TURNING_ON") ;
                break;
            default:
				Log.e(TAG, "UNKNOWN BluetoothAdapter STATE");
                break;
        }
    }
    
    static BluetoothDevice sDev = null;
    void doBondConfirm(BluetoothDevice dev) {
		sDev = dev;
		Log.d(TAG, "Set SDEV");
		return;
    }
    void doBondConfirm() {
    	if(null == sDev || !bIsServer) {	//	if device is NOT available DO NOTHING!
    		Log.d(TAG, "doBondConfirm()::sDev is NULL");
    		return;
    	}

    	if(!bIsServer) {
    		Log.d(TAG, "doBondConfirm()::this is NOT Server");		//	if this is not the SERVER DO NOTHING!
    		return;
    	}

    	try {
        	//sDev.setPairingConfirmation(true));
			sDev.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(sDev, true);
			//sDev.getClass().getMethod("cancelPairingUserInput", (Class<?>[])null).invoke(sDev, (Object[])null);
			Log.d(TAG, "doBondStateChange()::BOND_BONDING::setPairingConfirmation - *cancelPairingUserInput");
		} catch(NullPointerException n) {
			Log.d(TAG, "doBondStateChange()::BOND_BONDING::Null Pointer Exception");
		} catch(Exception e) {
			Log.d(TAG, e.getLocalizedMessage());
		}
    }

    void doBondStateChange(BluetoothDevice dev, int state, int preState) {
		Log.d(TAG, dev.getAddress());
		Log.d(TAG, String.format("state change: %d -> %d", preState, state));
		switch(state) {
			case BluetoothDevice.BOND_NONE:
				checkBondDeviceSetDiscoverable();
				break;
			case BluetoothDevice.BOND_BONDING:
				doBondConfirm(dev);
				break;
			case BluetoothDevice.BOND_BONDED:
				//	TODO: send CHECK and wait for ACK or Unboned for unknown device
				checkBondDeviceSetDiscoverable();
				startTimer();
				connectIfClient(dev);
				break;
			default:
				Log.d(TAG, "UNKNOWN STATE");
				break;
		}
    }
    
    
    void doSendData(Intent intent) throws Exception{
		//	TODO: Send Data
    	OclockPackage opackage = new OclockPackage(intent);
		if(null != mBTCommunicator) {
			mBTCommunicator.write(OclockPackage.getByteArray(opackage));								//		-Send data
		}
    }
    
    
    void handleCommand(Context context, Intent intent) {
		String command = intent.getStringExtra(ConnectionManagerActions.COMMAND_FIELD);
		
		if(command.contains(ConnectionManagerActions.INIT_SERVER)) {
			doInit(context);
		} else if(command.contains(ConnectionManagerActions.BOND_CONNECT_TO)) {
			String address = intent.getStringExtra(ConnectionManagerActions.DEVICE_ADDRESS);
			BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
			BluetoothDevice device = ba.getRemoteDevice(address);
			if(null != device) {
				//device.createBond();
				try {														//		-Bond device
					Method m = device.getClass().getMethod("createBond", (Class<?>[])null);
					m.invoke(device, (Object[])null);
				} catch (Exception e) {
					Log.d(TAG, "BOND_CONNECT_TO::Exception");
				}
			}
			
		} else if(command.contains(ConnectionManagerActions.CONNECT_TO)) {
			String address = intent.getStringExtra(ConnectionManagerActions.DEVICE_ADDRESS);
		
			BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
			if(null == mBTCommunicator) {
				mBTCommunicator = new Communicator(context);
				mBTCommunicator.start();
			}
			if(null != device) {
				mBTCommunicator.connect(device, true);						//		-Connect to Server
			} else {
				if(null == device) Log.d(TAG, "###### Device NOT Found");
				if(null == mBTCommunicator)	Log.d(TAG, "###### Communicator::NULL");
			}
		} else if(command.contains(ConnectionManagerActions.DISCONNECT_FROM)) {
			if (mBTCommunicator != null) {
				mBTCommunicator.start();									//		-Disconnect from Server
			}
		} else if(command.contains(ConnectionManagerActions.GET_CURRENT_STATE)) {
			int state = Communicator.STATE_NONE;
			if(null != mBTCommunicator) {
				Log.d(TAG, "GET_CURRENT_STATE");
				state = mBTCommunicator.getState();							//		-Get current state
			}
			Intent i = new Intent();
			i.setAction(ConnectionManagerActions.RETURN_CURRENT_STATE);
			i.putExtra(ConnectionManagerActions.DATA_FIELD, state);
			context.sendBroadcast(i);
		} else {
			Log.e(TAG, "UNKNOWN Command from Client");
		}
    }
    
    
    PendingIntent ai;
    void doInit(Context context) {
    	doInit(context, false);
    }
	void doInit(Context context, boolean bIsFromAlarm) {
		if(null == mBTCommunicator) {
			mBTCommunicator = new Communicator(context);
			mBTCommunicator.start();
			
			/**	start Alarm to keep process alive
			 * 		We want the alarm to go off 300 seconds from now.
			 */
			if( !bIsFromAlarm && bIsServer) {
				long firstTime = SystemClock.elapsedRealtime();
				Intent intent = new Intent();
		        intent.setAction(ConnectionManagerActions.ALARM_MANAGER_CHECK);
				ai = PendingIntent.getBroadcast(context, 0, intent, 0);
				// Schedule the alarm!
				AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
								firstTime, 300*1000, ai);
			}
		} else {
			Log.d(TAG, "Current Version: "+context.getString(R.string.version));
		}
		checkBondDeviceSetDiscoverable();
	}
	
	void doDeinit(Context context) {
		if (mBTCommunicator != null) {
            mBTCommunicator.stop();										//	-Stop Service
            mBTCommunicator = null;
		}
		try {
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			am.cancel(ai);
		} catch(Throwable e) {}
	}
    
    /**
     * Methods for bond device management
     * 
     * */
    private static final int CONFIRM_TIMEOUT = 5000;
	static Handler h = new Handler();
	static Runnable r = new Runnable() {
		@Override
		public void run() {
			removeAll();
			Log.d(TAG, "startTimer()::"+CONFIRM_TIMEOUT+"::removeAll()");
		}
	};

	void connectIfClient(BluetoothDevice dev) {
		if(bIsServer) {
			return;
		}
		mBTCommunicator.connect(dev, true);
	}
	
	void confirmIsClient() {
		if(bIsServer) {
			return;
		}
		OclockPackage opackage = new OclockPackage();
		opackage.client_name = ConnectionManagerActions.PAIRING_CONFIRM; 
		byte[] p = OclockPackage.getByteArray(opackage);
		int index = 0;
		for(byte b:p) {
			Log.d(TAG, String.format("[%d] 0x%02x", index, b));
			index++;
		}
		mBTCommunicator.write(p);
	}

	void startTimer() {
		if( ! bIsServer) {
			return;
		}
		h.postDelayed(r, CONFIRM_TIMEOUT);
		Log.d(TAG, "startTimer()::"+CONFIRM_TIMEOUT);
	}

	void stopTimer() {
		if( ! bIsServer) {
			return;
		}
		h.removeCallbacks(r);
		Log.d(TAG, "stopTimer()");
	}

	static void removeAll() {
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
