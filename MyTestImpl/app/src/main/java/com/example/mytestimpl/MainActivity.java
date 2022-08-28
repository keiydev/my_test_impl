package com.example.mytestimpl;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(view -> {
            showDialogFragment();
        });
    }

    private void showDialogFragment() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean noticeDialogConfirmed = prefs.getBoolean("notice_dialog_confirmed", false);
        if (!noticeDialogConfirmed) {
            MyDialogFragment myDialogFragment = new MyDialogFragment();
            myDialogFragment.show(getFragmentManager(), "dialog");
        } else {
            Toast.makeText(this, "you selected 'don't show again' before in last showed dialog, dialog doesn't showing. Then, flag is cleared now.", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notice_dialog_confirmed", false);
            editor.commit();
        }
    }

}