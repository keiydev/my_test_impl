package com.example.mytcomtest;

import android.content.Context;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;
import android.widget.Toast;

import androidx.arch.core.util.Function;

class MyTcomConnection extends Connection {
    public static final String TAG = "MyTcomConnection";
    private Function mListener;
    private Context mContext;

    public final Function getListener() {
        return this.mListener;
    }

    public final void setListener(Function var1) {
        this.mListener = var1;
    }

    public void onShowIncomingCallUi() {
        super.onShowIncomingCallUi();
        Toast.makeText(this.mContext, (CharSequence)"SHOW UI", Toast.LENGTH_SHORT).show();
        Log.i("TComConnection", "onShowIncomingCallUi");
    }

    public void onStateChanged(int state) {
        super.onStateChanged(state);
        Log.i("TComConnection", "onStateChanged, state=" + Connection.stateToString(state));
        if (mListener != null) {
            mListener.apply(state);
        }
    }

    public void onReject() {
        Log.i("TComConnection", "onReject");
        this.close();
    }

    public void onDisconnect() {
        Log.i("TComConnection", "onDisconnect");
        this.close();
    }

    private final void close() {
        this.setDisconnected(new DisconnectCause(4));
        this.destroy();
    }

    public final boolean isClosed() {
        return this.getState() == 6;
    }

    public MyTcomConnection(Context context) {
        super();
        this.mContext = context;
        this.mListener = null;
        this.setConnectionProperties(PROPERTY_SELF_MANAGED);
        setAudioModeIsVoip(true);
    }
}
