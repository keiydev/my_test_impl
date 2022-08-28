package com.example.mytestimpl.kt

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mytestimpl.MyDialogFragment
import com.example.mytestimpl.databinding.ActivityMainKtBinding

class MainActivityKt : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivityKt"
    }

    private lateinit var binding: ActivityMainKtBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main_kt)
        binding = ActivityMainKtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.button1.setOnClickListener {
            val v = it as TextView
            Log.d(TAG, "${v.text}'s button onClick called")
            showDialogFragment()
        }

    }

    private fun showDialogFragment() {
        val prefs = getSharedPreferences(packageName, MODE_PRIVATE)
        val noticeDialogConfirmed = prefs.getBoolean("notice_dialog_confirmed", false)
        if (!noticeDialogConfirmed) {
            val myDialogFragment = MyDialogFragment()
            myDialogFragment.show(fragmentManager, "dialog")
        } else {
            Toast.makeText(
                this,
                "you selected 'don't show again' before in last showed dialog, dialog doesn't showing. Then, flag is cleared now.",
                Toast.LENGTH_SHORT
            ).show()
            val editor = prefs.edit()
            editor.putBoolean("notice_dialog_confirmed", false)
            editor.commit()
        }
    }
}