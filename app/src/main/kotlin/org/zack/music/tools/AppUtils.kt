package org.zack.music.tools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import android.content.pm.PackageManager
import android.text.TextUtils
import org.zack.music.PureApp
import android.support.v4.content.FileProvider
import android.os.Build



/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/31
 */
object AppUtils {

    fun installApk(context: Context?, uri: Uri?) {
/*        val intent = Intent(Intent.ACTION_VIEW)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context?.startActivity(intent)*/
        if (uri != null) {
            context?.startActivity(getInstallAppIntent(context, uri))
        }
    }

    fun installApk(context: Context?, file: File) {
        context?.startActivity(getInstallAppIntent(context, file))
    }

    /**
     * Return the application's version code.
     *
     * @return the application's version code
     */
    fun getAppVersionCode(): Int {
        return getAppVersionCode(PureApp.app.packageName)
    }

    /**
     * Return the application's version code.
     *
     * @param packageName The name of the package.
     * @return the application's version code
     */
    private fun getAppVersionCode(packageName: String): Int {
        if (TextUtils.isEmpty(packageName)) return -1
        return try {
            val pm = PureApp.app.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            pi?.versionCode ?: -1
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }


    /**
     * Return the application's version name.
     *
     * @return the application's version name
     */
    fun getAppVersionName(): String? {
        return getAppVersionName(PureApp.app.packageName)
    }

    /**
     * Return the application's version name.
     *
     * @param packageName The name of the package.
     * @return the application's version name
     */
    private fun getAppVersionName(packageName: String): String? {
        if (TextUtils.isEmpty(packageName)) return null
        return try {
            val pm = PureApp.app.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            pi?.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }

    }

    /**
     * Return the intent of install app.
     *
     * Target APIs greater than 25 must hold
     * `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param file      The file.
     * @param isNewTask True to add flag of new task, false otherwise.
     * @return the intent of install app
     */
    private fun getInstallAppIntent(context: Context?, file: File): Intent {
//        if (file == null) return null
        val intent = getActionViewIntent()
//        val type = "application/vnd.android.package-archive"
        val uri: Uri = when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.N -> Uri.fromFile(file)
            else -> {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val authority = PureApp.app.packageName + ".INSTALL"
                FileProvider.getUriForFile(PureApp.app, authority, file)
            }
        }
        return getInstallAppIntent(intent, context, uri)
    }


    private fun getInstallAppIntent(intent: Intent, context: Context?, uri: Uri): Intent {
//        val intent = getActionViewIntent()
        val type = "application/vnd.android.package-archive"
        intent.setDataAndType(uri, type)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return intent
    }

    private fun getInstallAppIntent(context: Context?, uri: Uri): Intent {
        return getInstallAppIntent(getActionViewIntent(), context, uri)
    }

    private fun getActionViewIntent(): Intent = Intent(Intent.ACTION_VIEW)



}