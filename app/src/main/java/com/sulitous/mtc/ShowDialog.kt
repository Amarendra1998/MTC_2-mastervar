package com.sulitous.mtc

import android.app.Activity
import android.app.Dialog
import android.widget.TextView

class ShowDialog(activity: Activity) {

    private val mDialog: Dialog
    private val mTitle: TextView
    private val mMessage: TextView

    init {

        val dialog = Dialog(activity)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.loading_dialog)
        this.mDialog = dialog
        mTitle = dialog.findViewById(R.id.dialog_title)
        mMessage = dialog.findViewById(R.id.dialog_message)
    }

    fun setTitle(title: String) {
        mTitle.text = title
    }

    fun setMessage(message: String) {
        mMessage.text = message
    }

    fun show() {
        mDialog.show()
    }

    fun hide() {
        mDialog.hide()
    }

    fun cancel() {
        mDialog.cancel()
    }
}
