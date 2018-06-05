package org.zack.music.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.zack.music.tools.RxBus
import org.zack.music.bean.Song
import org.zack.music.bean.SongInfo
import org.zack.music.config.Config
import org.zack.music.config.ConfigHelper
import org.zack.music.event.*
import org.zack.music.tools.SongLoader
import java.util.concurrent.TimeUnit
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.support.annotation.RequiresApi
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.NotificationTarget
import org.zack.music.LauncherActivity
import org.zack.music.R
import org.zack.music.rxplayer.PlayerObservable
import java.util.*
import java.util.Random


/**
 *
 * Created by zackratos on 18-5-11.
 */
class PlayService: Service() {

    companion object {
        private const val CHANNEL = "play"

        private const val BROAD_CAST_PLAY = "com.zack.music.PLAY"
        private const val BROAD_CAST_PREVIOUS = "com.zack.music.PREVIOUS"
        private const val BROAD_CAST_NEXT = "com.zack.music.NEXT"
    }

    private var songs: MutableList<Song>? = null

    private var lastPosition = 0

    private lateinit var config: Config
    private var notificationControl: Boolean = false

    private val randomList: MutableList<Int> by lazy { ArrayList<Int>() }

    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

    private val mp: MediaPlayer by lazy { MediaPlayer() }


    override fun onCreate() {
        super.onCreate()
        // 先获取 config，防止 config 的数据为空
        config = ConfigHelper.getInstance().getConfig()
        notificationControl = ConfigHelper.getInstance().isNotificationControl()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mp.setAudioAttributes(AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC).build())
        } else {
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        mp.setOnCompletionListener {

            if (config.cycle == Config.SINGLE && mp.isLooping) {

            } else {
                cutSong(true)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val progressDisposable = Observable.interval(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .filter { isMpPlaying() }
                .map { getMpProgress() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { RxBus.getInstance().post(ProgressChange(it)) }
        addDisposable(progressDisposable)

        val intentFilter = IntentFilter()
        intentFilter.addAction(BROAD_CAST_NEXT)
        intentFilter.addAction(BROAD_CAST_PLAY)
        intentFilter.addAction(BROAD_CAST_PREVIOUS)
        registerReceiver(notificationReceiver, intentFilter)

    }




    override fun onBind(intent: Intent?): IBinder {
        return PlayBinder(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        unregisterReceiver(notificationReceiver)
        config.progress = getMpProgress()
        ConfigHelper.getInstance().putConfig(config)
        mp.stop()
        mp.release()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (!isMpPlaying()) {
            stopSelf()
        }
        return super.onUnbind(intent)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL, CHANNEL, NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    private val pi by lazy {
        val intent = Intent(this, LauncherActivity::class.java)
        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

//    private var target: NotificationTarget? = null

    private fun getNotification(song: Song, rm: RemoteViews): Notification {
        val build = NotificationCompat.Builder(this, CHANNEL)
                .setContentTitle(song.title)
                .setContentText(song.artistName)
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pi)
        if (notificationControl) {
            build.setCustomBigContentView(rm)
        }
        return build.build()
/*        return  NotificationCompat.Builder(this, CHANNEL)
                .setContentTitle(song.title)
                .setContentText(song.artistName)
                .setSmallIcon(R.drawable.ic_music)
                .setCustomBigContentView(rm)
                .setContentIntent(pi)
                .build()*/
    }



    private val rm by lazy {
        val remoteViews = RemoteViews(packageName, R.layout.notification_play)
        val playIntent = Intent(BROAD_CAST_PLAY)
        remoteViews.setOnClickPendingIntent(R.id.iv_play,
                PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        val previousIntent = Intent(BROAD_CAST_PREVIOUS)
        remoteViews.setOnClickPendingIntent(R.id.iv_previous,
                PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        val nextIntent = Intent(BROAD_CAST_NEXT)
        remoteViews.setOnClickPendingIntent(R.id.iv_next,
                PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        remoteViews
    }

    private fun getRm(song: Song, cutSong: Boolean, playStatus: Boolean): RemoteViews {
        if (cutSong) {
            rm.setTextViewText(R.id.tv_artist, song.artistName)
            rm.setTextViewText(R.id.tv_title, song.title)
            rm.setTextViewText(R.id.tv_album, song.albumName)
        }
        if (playStatus) {
            rm.setImageViewResource(R.id.iv_play, when {
                isMpPlaying() -> R.drawable.ic_pause
                else -> R.drawable.ic_play_arrow
            })
        }
        return rm
    }

    private val notificationReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action) {
                    BROAD_CAST_PREVIOUS -> cutSong(false)
                    BROAD_CAST_PLAY -> playOrPause()
                    BROAD_CAST_NEXT -> cutSong(true)
                }
            }
        }
    }

    private var notificationShowed = false

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

//    private var target: NotificationTarget? = null

    /**
     * 显示 notification
     */
    private fun updateNotification(song: Song, cutSong: Boolean, playStatus: Boolean) {
        val rm = getRm(song, cutSong, playStatus)
        val notification = getNotification(song, rm)
        if (notificationShowed) {
            notificationManager.notify(1, notification)
        } else {
            startForeground(1, notification)
            notificationShowed = true
        }
        if (cutSong && notificationControl) {
            val target = NotificationTarget(this, rm, R.id.iv_cover, notification, 1)
            Observable.create<Uri> {
                it.onNext(song.getCoverUri())
                it.onComplete()
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Glide.with(this)
                                .loadFromMediaStore(it)
                                .asBitmap()
                                .error(R.drawable.ic_album)
                                .into(target)
                    }, { rm.setImageViewResource(R.id.iv_cover, R.drawable.ic_album) })
        }
    }


    private fun addDisposable(disposable: Disposable?) {
        if (disposable != null)
            compositeDisposable.add(disposable)
    }

    private fun removeDisposable(disposable: Disposable?) {
        if (disposable != null)
            compositeDisposable.remove(disposable)
    }




    // 发送音乐列表
/*    fun sendStatus() {
        if (songs != null) {
            RxBus.getInstance().post(SongsLoaded(songs!!))
            return
        }
        val loadDisposable = Observable.create<MutableList<Song>> {
            val loadSongs = SongLoader.getAllSongs(this)
            it.onNext(loadSongs)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
                .doOnNext {
                    songs = it
                    // 若保存的音乐的位置大于音乐总数，把音乐位置设为 -1,
                    // 防止因音乐删除导致的数组越界
                    if (config.position > it.size) {
                        config.position = -1
                        ConfigHelper.getInstance().putPosition(config.position)
                    }

                    if (config.position != -1) {
                        // 把当前位置加入到随机列表中， 否则会数组越界
                        randomList.add(config.position)
                        // 如果位置不为 -1，那它即为上次播放的位置
                        lastPosition = config.position
                        // 如果进度比当前歌曲的最大进度大，把进度设为 0
                        if (config.progress > it[config.position].duration) {
                            config.progress = 0
                            ConfigHelper.getInstance().putProgress(0)
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    RxBus.getInstance().post(SongsLoaded(it))
                }

        addDisposable(loadDisposable)

    }*/


    fun sendStatus() {
        val statusDisposable = Observable.create<Status> {
            when (songs) {
                null -> songs = SongLoader.getAllSongs(this)
            }
            // 若保存的音乐的位置大于音乐总数，把音乐位置设为 -1,
            // 防止因音乐删除导致的数组越界
            if (config.position > songs!!.size) {
                config.position = -1
                ConfigHelper.getInstance().putPosition(config.position)
            }

            if (config.position != -1) {
                // 把当前位置加入到随机列表中， 否则会数组越界
                randomList.add(config.position)
                // 如果位置不为 -1，那它即为上次播放的位置
                lastPosition = config.position
                // 如果进度比当前歌曲的最大进度大，把进度设为 0
                if (config.progress > songs!![config.position].duration) {
                    config.progress = 0
                }
            }
            config.progress = getMpProgress()
            ConfigHelper.getInstance().putProgress(config.progress)
            it.onNext(Status(songs!!, config, isMpPlaying()))
            it.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { RxBus.getInstance().post(it) }

        addDisposable(statusDisposable)
    }


/*    fun sendStatus() {
        val position = config.position
        // -1 表示未选中歌曲，不需要发送状态
//        if (position == -1) return
        val songs = songs ?: return
        config.progress = getMpProgress()
        val song = when (position) {
            -1 -> null
            else -> songs[position]
        }
        val status = Status(config, song, isMpPlaying())
        RxBus.getInstance().post(status)
    }*/


    fun setPlayProgress(progress: Int) {
        if (config.position == -1) return
        mpSeekTo(progress)
        config.progress = progress
        ConfigHelper.getInstance().putProgress(progress)
        RxBus.getInstance().post(ProgressChange(progress))
    }

    /**
     * @newPlay 是否是新播放
     */
    fun startPlaySong(songInfo: SongInfo, newPlay: Boolean) {
        PlayerObservable(mp, songInfo.song.data, config.cycle)
/*                .observeOn(Schedulers.io())
                .doOnNext {
                    val lyric = songInfo.song.getLyricFile()
                }
                .observeOn(AndroidSchedulers.mainThread())*/
                .doOnNext {
                    // 发送播放事件
                    RxBus.getInstance().post(PlaySong(songInfo, lastPosition, newPlay))

                    // 将当前播放音乐的位置存放到内存和本地
                    config.position = songInfo.position
                    ConfigHelper.getInstance().putPosition(songInfo.position)
                    // 更新上次播放的位置
                    lastPosition = songInfo.position
                    mpPrepared = true
                    if (newPlay) {
                        config.progress = 0
                    }
                    mpSeekTo(config.progress)
                }
                .subscribe {
                    mp.start()
                    updateNotification(songInfo.song, true, true)
                }
    }

    private val random: Random by lazy { Random() }

    private fun cutSong(next: Boolean) {
        if (config.position == -1) return
        val songsCopy = songs ?: return
        if (config.random) {
            if (next) {
                config.position = random.nextInt(songsCopy.size)
                randomList.add(config.position)
            } else {
                if (randomList.size >= 2) {
                    randomList.removeAt(randomList.size - 1)
                    config.position = randomList[randomList.size - 1]
                }
            }
        } else {
            if (next) {
                if (config.position < songsCopy.size - 1) {
                    config.position++
                } else {
                    if (config.cycle == Config.ALL) {
                        config.position = 0
                    } else {
                        return
                    }
                }
            } else {
                if (config.position > 0) {
                    config.position--
                } else {
                    if (config.cycle == Config.ALL) {
                        config.position = songsCopy.size - 1
                    } else {
                        return
                    }
                }
            }
        }
        startPlaySong(SongInfo(songsCopy[config.position], config.position), true)
    }

    fun cycle() {
        when {
            config.cycle == Config.ORDER -> {
                config.cycle = Config.ALL
                setMpLooping(false)
            }
            config.cycle == Config.ALL -> {
                config.cycle = Config.SINGLE
                setMpLooping(true)
            }
            config.cycle == Config.SINGLE -> {
                config.cycle = Config.ORDER
                setMpLooping(false)
            }
        }
        RxBus.getInstance().post(Cycle(config.cycle))
        ConfigHelper.getInstance().putCycle(config.cycle)
    }

    fun random() {
        randomList.clear()
        if (config.position != -1) {
            randomList.add(config.position)
        }
        config.random = config.random.not()
        RxBus.getInstance().post(IsRandom(config.random))
        ConfigHelper.getInstance().putRandom(config.random)
    }

    fun playOrPause() {
        if (mpPrepared) {
            if (isMpPlaying()) {
                mpPause()
                config.progress = getMpProgress()
                ConfigHelper.getInstance().putProgress(config.progress)
            }
            else mpStart()
            RxBus.getInstance().post(PlayOrPause(isMpPlaying()))
            val songs = songs ?: return
            updateNotification(songs[config.position], false, true)
        } else {
            val position = config.position
            if (position == -1) return
            val songs = songs ?: return
            val songInfo = SongInfo(songs[position], position)
            startPlaySong(songInfo, false)
//            updateNotification(songs[position])
        }
    }

    fun setNotificationControl(control: Boolean) {
        this.notificationControl = control
        if (songs == null) return
        val position = config.position
        if (position == -1) return
        updateNotification(songs!![position], true, true)
    }


    // mediaplayer 系列方法

    // 播放器是否已准备好
    private var mpPrepared = false

    private fun isMpLooping(): Boolean {
        return when {
            mpPrepared -> mp.isLooping
            else -> false
        }
    }

    private fun setMpLooping(looping: Boolean) {
        when {
            mpPrepared -> mp.isLooping = looping
        }
    }

    private fun isMpPlaying(): Boolean {
        return when {
            mpPrepared -> mp.isPlaying
            else -> false
        }
    }

    private fun getMpProgress(): Int {
        return when {
            mpPrepared -> mp.currentPosition
            else -> config.progress
        }
    }

    private fun mpSeekTo(progress: Int) {
        when {
            mpPrepared && progress <= mp.duration -> mp.seekTo(progress)
        }
    }

    private fun mpStart() {
        if (mpPrepared && !mp.isPlaying)
            mp.start()
    }

    private fun mpPause() {
        if (mpPrepared && mp.isPlaying) {
            mp.pause()
        }
    }






    class PlayBinder(private val service: PlayService) : Binder() {

        // 发送当前状态
        fun sendStatus() {
//            service.sendStatus()
            service.sendStatus()
        }


        /**
         * @newPlay 是否是重新播放
         */
        fun startPlaySong(songInfo: SongInfo, newPlay: Boolean) {
            service.startPlaySong(songInfo, newPlay)
        }


        fun setPlayProgress(progress: Int) {
            service.setPlayProgress(progress)
        }

        fun playOrPause() {
            service.playOrPause()
        }

        fun cutSong(next: Boolean) {
            service.cutSong(next)
        }

        fun cycle() {
            service.cycle()
        }

        fun random() {
            service.random()
        }

        fun stopWhenPause() {
            if (!service.isMpPlaying()) {
                service.stopSelf()
            }
        }

        fun setNotificationControl(control: Boolean) {
            service.setNotificationControl(control)
        }

    }
}