package org.zack.music.update

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.ProgressBar
import org.zack.music.R

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/6/4
 */
class ProgressDialog: DialogFragment() {

    companion object {
        fun newInstance(): ProgressDialog {
            return ProgressDialog()
        }
    }

/*    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  AlertDialog.Builder(activity)
                .setView(R.layout.dialog_progress)
                .setCancelable(false)
                .create()
        val window = dialog.window
        window.setBackgroundDrawableResource(R.drawable.bg_setup_card)
        return dialog
    }*/

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(R.drawable.bg_setup_card)
        dialog.setCanceledOnTouchOutside(false)
        return inflater?.inflate(R.layout.dialog_progress, container, false)
    }



}