package com.example.mytestimpl.kt

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import com.example.mytestimpl.R
import com.example.mytestimpl.databinding.DialogFragmentLayoutBinding

class MyDialogFragmentKt : DialogFragment() {
    companion object {
        private const val TAG = "MyDialogFragmentKt"
    }

    private lateinit var binding: DialogFragmentLayoutBinding
    private var mCheckBox: CheckBox? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //binding = DialogFragmentLayoutBinding.inflate(activity.layoutInflater)
        //val customView = binding.root
        val customView = activity.layoutInflater.inflate(R.layout.dialog_fragment_layout, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customView)
//        binding.buttonOk.setOnClickListener {
//            val v = it as Button
//            Log.d(TAG, "${v.text} onClick called")
//            dialogConfirmed(binding.checkbox.isChecked)
//            dismiss()
//        }
        mCheckBox = customView.findViewById<CheckBox>(R.id.checkbox)
        val button = customView.findViewById<Button>(R.id.button_ok)
        customView.findViewById<Button>(R.id.button_ok).setOnClickListener {
            val v = it as Button
            Log.d(TAG, "${v.text} onClick called")
            dialogConfirmed(mCheckBox!!.isChecked)
            dismiss()
        }

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    private fun dialogConfirmed(isChecked: Boolean) {
        val context = getContext()
        if (isChecked) {
            val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("notice_dialog_confirmed", true)
            editor.commit()
        }
    }

}