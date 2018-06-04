package org.zack.music.main

import android.Manifest
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.zack.music.BaseFragment
import org.zack.music.Constants
import org.zack.music.LauncherActivity
import org.zack.music.R
import org.zack.music.bean.Song
import org.zack.music.bean.SongInfo
import org.zack.music.config.Config
import org.zack.music.config.ConfigHelper
import org.zack.music.event.BgChange
import org.zack.music.event.PlaySong
import org.zack.music.event.Status
import org.zack.music.http.ApiService
import org.zack.music.http.Girl
import org.zack.music.http.RxUtils
import org.zack.music.main.list.SongListFragment
import org.zack.music.main.play.PlayFragment
import org.zack.music.setup.SetupFragment
import org.zack.music.tools.RxBus
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import android.content.Intent
import android.provider.MediaStore
import java.io.File


/**
 * @Author  Zackratos
 * @Data    18-5-14
 * @Email   869649339@qq.com
 */
class MainFragment: BaseFragment() {

    companion object {

        private const val OPEN_ALBUM = 1

        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }


/*    private var playBinder: PlayService.PlayBinder? = null

    private val connection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                playBinder = service as PlayService.PlayBinder
                playBinder?.sendStatus()
            }
        }
    }*/


    override fun layoutId(): Int {
        return R.layout.fragment_main
    }

    private lateinit var parentActivity: LauncherActivity
//    private val bgType by lazy { ConfigHelper.getInstance().getBgType() }

    private var currentSong: Song? = null

    override fun initEventAndData() {
        parentActivity = activity as LauncherActivity
        setHasOptionsMenu(true)
        initView()
        addChildFragment()
        // 先注册接收状态事件，防止因未注册而接收不到
        receiveStatus()
        startAndBindService()
        receivePlaySong()
        receiveBgChange()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_main, menu)
    }



//    private val setupFragment by lazy { SetupFragment() }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_setup) {
            parentActivity.addChild(SetupFragment.newInstance())
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        parentActivity.unbindService()
    }


    // 初始化 view
    private fun initView() {
//        val rootActivity = activity as AppCompatActivity
//        rootActivity.setSupportActionBar(toolbar)
        setToolbar()

    }

    fun setToolbar() {
//        val rootActivity = activity as AppCompatActivity
        parentActivity.setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(activity, dl_main, toolbar, R.string.open_drawer, R.string.close_drawer)
        dl_main.addDrawerListener(toggle)
        toggle.syncState()
    }


    private val listFragment: SongListFragment by lazy { SongListFragment() }
    private val playFragment: PlayFragment by lazy { PlayFragment() }
    // 添加子 Fragment
    private fun addChildFragment() {
        childFragmentManager.beginTransaction()
                .add(R.id.fl_music_list, listFragment)
                .add(R.id.fl_play, playFragment)
                .commit()
/*        if (findChildFragment(SongListFragment::class.java) == null) {
            loadRootFragment(R.id.fl_music_list, SongListFragment.newInstance())
        }
        if (findChildFragment(PlayFragment::class.java) == null) {
            loadRootFragment(R.id.fl_play, PlayFragment.newInstance())
        }*/
    }


    // 请求读取手机储存的权限
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun requestPermission(): Observable<Boolean> {
        return RxPermissions(activity).request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .filter { it }
    }

    // 接收状态事件
    private fun receiveStatus() {
/*        val statusObservable = RxBus.getInstance().toObservable(Status::class.java)
                .filter { it.song != null }
                .subscribe {
                    toolbar.title = it.song?.title ?: getString(R.string.app_name)
//                    showBg(it.song)
                }
        addDisposable(statusObservable)*/
        val statusObservable = RxBus.getInstance().toObservable(Status::class.java)
/*                .map {
                    val position = it.config.position
                    when {
                        position != -1 -> it.songs[position].title
                        else -> getString(R.string.app_name)
                    }
                }*/
                .filter { it.config.position != -1 }
                .map { it.songs[it.config.position] }
                .doOnNext { currentSong = it }
                .subscribe {
                    toolbar.title = it.title
                    showBg(it)
                }
        addDisposable(statusObservable)
    }

    // 启动并绑定 service
    private fun startAndBindService() {
        val observable: Observable<Boolean> = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN -> requestPermission()
            else -> Observable.just(true)
        }
        val bindDisposable = observable.subscribe {
//            val intent = Intent(activity, PlayService::class.java)
//            activity.startService(intent)
//            activity.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            parentActivity.startAndBindService()
        }
        addDisposable(bindDisposable)
    }


    // 接收播放音乐的事件
    private fun receivePlaySong() {
        val playDisposable = RxBus.getInstance().toObservable(PlaySong::class.java)
                .map { it.songInfo.song }
                .doOnNext { currentSong = it }
                .subscribe {
                    toolbar.title = it.title
                    showBg(it)
                }
        addDisposable(playDisposable)
    }

    private fun receiveBgChange() {
        val bgDisposable = RxBus.getInstance().toObservable(BgChange::class.java)
                .subscribe {
                    bgType = it.type
                    showBg(currentSong)
                }
        addDisposable(bgDisposable)
    }

    private var bgType = ConfigHelper.getInstance().getBgType()

    private fun showBg(song: Song?) {
        if (song == null) return
        when(bgType) {
            Config.GIRL -> showGirl()
            Config.SONGS -> showSong(song)
            else -> showTrans()
        }
    }

    private var girl: Girl? = null
    private fun showGirl() {
        if (girl != null && girl?.bg?.isEmpty() == false) {
            showGirl(girl!!)
            return
        }
        val girlDisposable = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ApiService::class.java)
                .getBgConfig()
                .subscribeOn(Schedulers.io())
                .compose(RxUtils.resultData())
                .doOnNext { girl = it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ showGirl(it) }, { showTrans() })
        addDisposable(girlDisposable)
    }


    private val random: Random by lazy { Random() }
    private fun showGirl(girl: Girl) {
        val bg = girl.bg
        if (bg.isEmpty()) return
        val po = random.nextInt(bg.size)
        Glide.with(this).load(bg[po].url)
                .error(R.color.tran_default)
                .into(iv_bg)
    }

    private fun showSong(song: Song) {
        val songDisposable = Observable.create<Uri> {
            it.onNext(song.getCoverUri())
            it.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Glide.with(this)
                            .loadFromMediaStore(it)
                            .error(R.color.tran_default)
                            .into(iv_bg)
                }, {})
        addDisposable(songDisposable)
    }

    private fun showTrans() {
//        Glide.with(this).load(R.color.colorAccent).into(iv_bg)
        iv_bg.setImageResource(R.color.tran_default)
    }






    fun onBackPressed(): Boolean {
        if (dl_main.isDrawerOpen(Gravity.LEFT)) {
            dl_main.closeDrawer(Gravity.LEFT)
            return true
        }
        return false
    }



    // 以下为对外提供的方法

    fun startPlaySong(songInfo: SongInfo, newPlay: Boolean) {
        parentActivity.playBinder?.startPlaySong(songInfo, newPlay)
    }

    fun setPlayProgress(progress: Int) {
        parentActivity.playBinder?.setPlayProgress(progress)
    }

    fun playOrPause() {
        parentActivity.playBinder?.playOrPause()
    }

    fun cutSong(next: Boolean) {
        parentActivity.playBinder?.cutSong(next)
    }

    fun cycle() {
        parentActivity.playBinder?.cycle()
    }

    fun random() {
        parentActivity.playBinder?.random()
    }
}