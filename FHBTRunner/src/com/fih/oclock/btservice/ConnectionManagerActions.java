package com.fih.oclock.btservice;

public class ConnectionManagerActions {

	public static final String GET_CURRENT_VERSION = "com.fih.oclock.btservice.get.current.version";
	
	public static final String FHBT_CLIENT_COMMAND = "com.fih.oclock.btservice.action";

	//	Pairing Relationship Confirm
	public static final String PAIRING_CONFIRM = "com.fih.oclock.btservice.pairing.confirm";

	//	Control Action & Commands
	public static final String CONTROL_ACTION = "com.fih.oclock.btservice.action.control";			//	add into intent filter
	public static final String COMMAND_FIELD = "command";

	public static final String INIT_SERVER = "com.fih.oclock.btservice.action.control.initialize.server";

	public static final String BOND_CONNECT_TO = "com.fih.oclock.btservice.action.control.doBond.doConnect";
	public static final String CONNECT_TO = "com.fih.oclock.btservice.action.control.doConnect";
    public static final String DISCONNECT_FROM = "com.fih.oclock.btservice.action.control.doDisconnect";
    public static final String DEVICE_ADDRESS = "device_address";

    public static final String GET_CURRENT_STATE = "com.fih.oclock.btservice.action.control.getCurrentState";


    //	Notification Action for Attach/Detach
    public static final String NOTIFICATION_ATTACH = "com.fih.oclock.btservice.action.btdevice_connect";
    public static final String NOTIFICATION_DETACH = "com.fih.oclock.btservice.action.btdevice_disconnect";

    //	Action to update state
    public static final String RETURN_CURRENT_STATE = "com.fih.oclock.btservice.action.returnCurrentState";
    
    
    //	Action to reset (removeAll) bond devices
    public static final String RESET_BOND_DEVICES = "com.fih.oclock.btservice.reset.bond.devices";


    //	Action to send data
    public static final String ACTION_SEND_DATA = "com.fih.oclock.btservice.action.send";				//	add into intent filter
    public static final String DATA_FIELD = "data";
    public static final String CLIENT_FIELD = "client_name";
    public static final String CLIENT_UNKNOWN_NAME = "com.fih.oclock.UNKNOWN.DEVICE";

    
    //	Action to keep process work
    public static final String ALARM_MANAGER_CHECK = "com.fih.oclock.ALARM_MANAGER_ACTION";

}
