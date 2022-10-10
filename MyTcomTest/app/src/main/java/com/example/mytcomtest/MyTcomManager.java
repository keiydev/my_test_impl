package com.example.mytcomtest;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

class MyTcomManager {
   private static final String TAG = "MyTcomManager";

   private Context mContext;
   private TelecomManager mTelecomManager;

   public MyTcomManager(Context context) {
      mContext = context;
      mTelecomManager = mContext.getSystemService(TelecomManager.class);
   }

   public boolean registerAccount() {
      PhoneAccountHandle accountHandle = getAccountHandle();
      PhoneAccount phoneAccount = mTelecomManager.getPhoneAccount(accountHandle);
      if (phoneAccount == null) {
         PhoneAccount.Builder builder = PhoneAccount.builder(accountHandle, BuildConfig.APPLICATION_ID);
         builder.setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED);
         builder.setAddress(Uri.fromParts(PhoneAccount.SCHEME_TEL, "99999", null));
         builder.setShortDescription("shortDescription");
         builder.setSubscriptionAddress(Uri.fromParts(PhoneAccount.SCHEME_TEL, "100000", null));
         phoneAccount = builder.build();
         Log.d(TAG, "TelecomManager registerPhoneAccount start");
         mTelecomManager.registerPhoneAccount(phoneAccount);
         Log.d(TAG, "TelecomManager registerPhoneAccount end");
      }
      return true;
   }

   private PhoneAccountHandle getAccountHandle() {
      String phoneAccountLabel = BuildConfig.APPLICATION_ID;
      ComponentName componentName = new ComponentName(mContext, MyTcomService.class);
      return new PhoneAccountHandle(componentName, phoneAccountLabel);
   }

   void addIncomingCall() {
      Bundle extras = new Bundle();
      Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, "12356", null);
      extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, getAccountHandle());
      extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);
      PhoneAccountHandle phoneAccountHandle = getAccountHandle();
      Log.d(TAG, "TelecomManager addNewIncomingCall start, phoneAccountHandle=" + phoneAccountHandle + ", extras=" + extras);
      mTelecomManager.addNewIncomingCall(phoneAccountHandle, extras);
      Log.d(TAG, "TelecomManager addNewIncomingCall end");
   }

   void endCall() {
      //mTelecomManager.endCall();
   }

   @SuppressLint("MissingPermission")  // CALL_PHONE is not required for self-managed ConnectionService
   void addOutgoingCall() {
      Bundle extras = new Bundle();
      extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, getAccountHandle());
      mTelecomManager.placeCall(Uri.parse("tel:123456"), extras);
   }
}
