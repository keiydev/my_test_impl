package com.example.mytcomtest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class MyTcomService extends ConnectionService {
    public static final String TAG = "MyTComService";

    public void onCreateIncomingConnectionFailed(@Nullable PhoneAccountHandle connectionManagerPhoneAccount, @Nullable ConnectionRequest request) {
        Log.e(TAG, "onCreateIncomingConnectionFailed");
    }

    public Connection onCreateIncomingConnection(@Nullable PhoneAccountHandle phoneAccount, @Nullable ConnectionRequest request) {
        Log.i(TAG, "onCreateIncomingConnection - handle=" + phoneAccount + ", request=" + request);
        Context var10002 = this.getApplicationContext();
        MyTcomConnection connection = new MyTcomConnection(var10002);
        bindService(new Intent(this.getApplicationContext(), MyCallService.class), (ServiceConnection)(new MyCallServiceConnection(connection, this)), 0);
        return (Connection)connection;
    }

    public void onCreateOutgoingConnectionFailed(@Nullable PhoneAccountHandle connectionManagerPhoneAccount, @Nullable ConnectionRequest request) {
        Log.e(TAG, "onCreateOutgoingConnectionFailed");
    }

    public Connection onCreateOutgoingConnection(@Nullable PhoneAccountHandle handle, @Nullable ConnectionRequest request) {
        Log.i(TAG, "onCreateOutgoingConnection - handle=" + handle + ", request=" + request);
        Context var10002 = this.getApplicationContext();
        MyTcomConnection connection = new MyTcomConnection(var10002);
        bindService(new Intent(this.getApplicationContext(), MyCallService.class), (ServiceConnection)(new MyCallServiceConnection(connection, this)), 0);
        return (Connection)connection;
    }
}
