package com.example.mytestimpl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

public class MyDialogFragment extends DialogFragment {
   private static final String TAG = "MyDialogFragment";
   private CheckBox mCheckBox;

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      LayoutInflater inflater = getActivity().getLayoutInflater();
      final View customView = inflater.inflate(R.layout.dialog_fragment_layout, null);
      customView.findViewById(R.id.button_ok).setOnClickListener(l -> {
         Log.d(TAG, "button_ok onClick called, isChecked = " + mCheckBox.isChecked());
         dialogConfirmed(getContext(), mCheckBox.isChecked());
         dismiss();
      });
      mCheckBox = customView.findViewById(R.id.checkbox);
      builder.setView(customView);

      Dialog dialog = builder.create();
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      return dialog;
   }

   public void dialogConfirmed(Context context, boolean isChecked) {
      if (isChecked) {
         SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = prefs.edit();
         editor.putBoolean("notice_dialog_confirmed", true);
         editor.commit();
      }
   }
}
