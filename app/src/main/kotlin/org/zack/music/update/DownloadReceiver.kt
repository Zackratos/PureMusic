package org.zack.music.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.zack.music.tools.AppUtils

/**
 * 监听下载完成事件
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/31
 */
class DownloadReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
//        if (intent == null) return
        if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri: Uri? = downloadManager.getUriForDownloadedFile(id)
            AppUtils.installApk(context, uri)

/*            val intent2 = Intent(Intent.ACTION_VIEW)
            intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent2.setDataAndType(uri, "application/vnd.android.package-archive")
            context.startActivity(intent2)*/
        }
    }

}