package com.fih.oclock.btservice;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.fih.oclock.connectionmanageraction.ConnectionManagerActions;


public class BTActivity extends Activity {

	BTClientReceiver receiver;
	IntentFilter filter = new IntentFilter(ConnectionManagerActions.RETURN_CURRENT_STATE);
	
	private static final int REQUEST_ENABLE_BT = 102;
	
	void doInitServer() {
		Intent i = new Intent();
        i.setAction(ConnectionManagerActions.CONTROL_ACTION);
        i.putExtra(ConnectionManagerActions.COMMAND_FIELD, ConnectionManagerActions.INIT_SERVER);
        sendBroadcast(i);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        receiver = new BTClientReceiver(this);
        
        findViewById(R.id.connectBtn).setOnClickListener(clickConnButton);
        findViewById(R.id.disconnectBtn).setOnClickListener(clickConnButton);
        findViewById(R.id.waitingBtn).setOnClickListener(clickConnButton);
    }

    @Override
    protected void onStart() {
    	super.onStart();
    	this.registerReceiver(receiver, filter);
    	
    	Intent i = new Intent();
    	i.setAction(ConnectionManagerActions.CONTROL_ACTION);
    	i.putExtra(ConnectionManagerActions.COMMAND_FIELD, ConnectionManagerActions.GET_CURRENT_STATE);
    	this.sendBroadcast(i);
    	
    	// If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
    	BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if ( ! ba.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
        	doInitServer();
        }
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	this.unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bt, menu);
        return true;
    }
    

    public void UpdateConnectionState(int state) {
    	TextView tv1 = (TextView)findViewById(R.id.textView1);
    	String s1;
    	switch(state) {
    	case 0:			//	Disconnected
    		s1 = getString(R.string.disconnected);
    		findViewById(R.id.connectBtn).setVisibility(View.VISIBLE);
    		findViewById(R.id.disconnectBtn).setVisibility(View.GONE);
    		findViewById(R.id.waitingBtn).setVisibility(View.GONE);
    		break;
    	case 1:			//	Connected
    		s1 = getString(R.string.connected);
    		findViewById(R.id.connectBtn).setVisibility(View.GONE);
    		findViewById(R.id.disconnectBtn).setVisibility(View.VISIBLE);
    		findViewById(R.id.waitingBtn).setVisibility(View.GONE);
    		break;
    	case 2:			//	Waiting
    		s1 = getString(R.string.waiting);
    		findViewById(R.id.connectBtn).setVisibility(View.GONE);
    		findViewById(R.id.disconnectBtn).setVisibility(View.GONE);
    		findViewById(R.id.waitingBtn).setVisibility(View.VISIBLE);
    		break;
		default:		//	Waiting
			s1 = getString(R.string.waiting);
			findViewById(R.id.connectBtn).setVisibility(View.GONE);
    		findViewById(R.id.disconnectBtn).setVisibility(View.GONE);
    		findViewById(R.id.waitingBtn).setVisibility(View.VISIBLE);
			break;
    	}
    	tv1.setText(s1);
    }
    
    
    View.OnClickListener clickConnButton = new View.OnClickListener() {
    	
    	private static final String TAG = "BTActivity.OnClickListener";
    	
		@Override
		public void onClick(View v) {
			Intent i = new Intent();
			switch(v.getId()) {
			case R.id.connectBtn:
				Log.d(TAG, "R.id.connectBtn");
				i.setClass(BTActivity.this, DeviceListActivity.class);
				startActivityForResult(i, DEVICE_LIST);
				break;
			case R.id.disconnectBtn:
			case R.id.waitingBtn:
				Log.d(TAG, "R.id.disconnectBtn");
				i.setAction(ConnectionManagerActions.CONTROL_ACTION);
				i.putExtra(ConnectionManagerActions.COMMAND_FIELD, ConnectionManagerActions.DISCONNECT_FROM);
				sendBroadcast(i);
				break;
			default:
				break;
			}
		}
	};
	
	private static final int DEVICE_LIST = 101;	
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(DEVICE_LIST == requestCode && RESULT_OK == resultCode) {
			String address = data.getStringExtra("address");
			Intent i = new Intent();
			i.setAction(ConnectionManagerActions.CONTROL_ACTION);
			i.putExtra(ConnectionManagerActions.COMMAND_FIELD, ConnectionManagerActions.CONNECT_TO);
			i.putExtra(ConnectionManagerActions.DEVICE_ADDRESS, address);
			sendBroadcast(i);
		} else if(REQUEST_ENABLE_BT == requestCode) {
			if(RESULT_OK != resultCode) {
				finish();
			} else {
				doInitServer();
			}
		}
	}

}



