package org.zack.music

import android.app.Application
import android.content.Context
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.tencent.bugly.crashreport.CrashReport


/**
 * @Author  Zackratos
 * @Data    18-5-17
 * @Email   869649339@qq.com
 */
class PureApp: Application() {

    companion object {
        fun getRefWatcher(context: Context): RefWatcher {
            val application = context.applicationContext as PureApp
            return application.refWatcher
        }

        private const val APPID = "f35a39a627"
        lateinit var app: Application

    }

    private lateinit var refWatcher: RefWatcher

    override fun onCreate() {
        super.onCreate()
        app = this
        CrashReport.initCrashReport(applicationContext, APPID, false)
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        refWatcher = LeakCanary.install(this)
    }

//    lateinit var app: Application

}