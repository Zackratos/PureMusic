package org.zack.music.update

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.TextView
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import org.zack.music.R
import org.zack.music.tools.AppUtils
import java.io.File

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/31
 */
class UpdateDialog: DialogFragment() {


    companion object {
        private const val CHECK = "check"
        private const val apkUrl = "http://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk"
        fun newInstance(check: Update.Check): UpdateDialog {
            val dialog  = UpdateDialog()
            val arg = Bundle()
            arg.putParcelable(CHECK, check)
            dialog.arguments = arg
            return dialog
        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val check: Update.Check = arguments.getParcelable(CHECK)
        val dialog =  AlertDialog.Builder(activity)
                .setIcon(R.drawable.ic_music)
                .setTitle(String.format(getString(R.string.update_title), check.version))
                .setMessage(check.updateContent)
                .setCancelable(false)
                .setPositiveButton(R.string.update_update) { _, _ -> downloadNewVersion(check.url, check.fileName) }
                .setNegativeButton(R.string.update_refuse, null)
                .create()
        dialog.setCanceledOnTouchOutside(false)
        val window = dialog.window
        window.setBackgroundDrawableResource(R.drawable.bg_setup_card)
        dialog.setOnShowListener {
            val titleView = window.findViewById(R.id.alertTitle) as TextView
            titleView.setTextColor(Color.WHITE)
            val messageView = window.findViewById(android.R.id.message) as TextView
            messageView.setTextColor(Color.WHITE)
        }
        return dialog
    }


    private fun downloadNewVersion(url: String, name: String) {
        val file = File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name)
        if (file.exists() && file.isFile) {
            AppUtils.installApk(activity, file)
            return
        }
        val folder = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (folder != null) {
            deleteFile(folder)
        }
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(apkUrl))
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, name)
        val id = downloadManager.enqueue(request)
    }

    private fun deleteFile(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) {
            file.listFiles().forEach { f -> deleteFile(f) }
//            file.delete()//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete()
        }
    }

}