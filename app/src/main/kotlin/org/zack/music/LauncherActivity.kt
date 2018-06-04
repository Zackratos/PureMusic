package org.zack.music

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import org.zack.music.main.MainFragment
import org.zack.music.setup.SetupFragment
import org.zack.music.tools.CleanLeakUtils
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.zack.music.http.ApiService
import org.zack.music.http.RxUtils
import org.zack.music.service.PlayService
import org.zack.music.update.UpdateUtils
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * @Author  Zackratos
 * @Data    18-5-14
 * @Email   869649339@qq.com
 */
class LauncherActivity: AppCompatActivity() {

    private val mainFragment: MainFragment by lazy {
        MainFragment.newInstance()
    }

    private val compositeDisposable by lazy { CompositeDisposable() }

    var playBinder: PlayService.PlayBinder? = null

    private val connection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                playBinder = service as PlayService.PlayBinder
                playBinder?.sendStatus()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)
        immersionBar()
        addChild(mainFragment)
        checkUpdate()
//        if (findFragment(MainFragment::class.java) == null) {
//            loadRootFragment(R.id.fragment_container, MainFragment.newInstance())
//        }
//        loadRootFragment(R.id.fragment_container, SetupFragment.newInstance())
    }

    override fun onDestroy() {
        CleanLeakUtils.fixInputMethodManagerLeak(this)
        super.onDestroy()
        compositeDisposable.clear()
//        unbindService(connection)
//        playBinder?.stopWhenPause()
    }

    override fun onBackPressed() {
        if (topFragment is SetupFragment) {
            removeSetup()
            return
        }
        if (mainFragment.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun immersionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            val decorView = window.decorView
            val option = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.systemUiVisibility = option
            window.navigationBarColor = Color.TRANSPARENT
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    private fun checkUpdate() {
        val updateDisposable = UpdateUtils.checkUpdate(supportFragmentManager)
        compositeDisposable.add(updateDisposable)
    }



    private var binded = false
    fun startAndBindService() {
        val intent = Intent(this, PlayService::class.java)
        startService(intent)
        binded = bindService(intent, connection, BIND_AUTO_CREATE)
    }

    fun unbindService() {
        if (binded) {
            unbindService(connection)
        }
        playBinder?.stopWhenPause()
    }

    fun removeSetup() {
        supportFragmentManager.beginTransaction()
                .remove(topFragment)
                .runOnCommit {
                    topFragment = mainFragment
                    mainFragment.setToolbar()
                }
                .commit()
    }

    private var topFragment: Fragment? = null
    fun addChild(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .runOnCommit { topFragment = fragment }
                .commit()
    }


    private fun navigationBarExist(): Boolean {
        val d = windowManager.defaultDisplay
        val realDisplayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics)
        }
        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels
        val displayMetrics = DisplayMetrics()
        d.getMetrics(displayMetrics)
        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels
        return realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    }

    fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    fun getNavigationBarHeight(): Int {
        if (!navigationBarExist()) return 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

}