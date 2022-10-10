package com.example.mytcomtest;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.Nullable;

class MyCallServiceConnection implements ServiceConnection {
   private final MyTcomConnection mTcomConnection;
   private Context mContext;

   public void onServiceConnected(@Nullable ComponentName name, @Nullable IBinder binder) {
      if (binder == null) {
         throw new NullPointerException("null cannot be cast to non-null type com.example.mytcomtest.MyCallService.CallServiceBinder");
      } else {
         MyCallService.CallServiceBinder callSrvBinder = (MyCallService.CallServiceBinder) binder;
         callSrvBinder.getCallService().addConnection(this.mTcomConnection);
         mContext.unbindService((ServiceConnection)this);
      }
   }

   public void onServiceDisconnected(@Nullable ComponentName name) {
   }

   public MyCallServiceConnection(MyTcomConnection tcomConnection, Context context) {
      super();
      this.mTcomConnection = tcomConnection;
      mContext = context;
   }
}