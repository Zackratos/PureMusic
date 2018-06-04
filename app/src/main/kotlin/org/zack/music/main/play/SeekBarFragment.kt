package org.zack.music.main.play

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import org.zack.music.BaseFragment
import org.zack.music.R
import org.zack.music.tools.RxBus
import org.zack.music.bean.SongInfo
import org.zack.music.config.ConfigHelper
import org.zack.music.event.*
import org.zack.music.tools.LogUtil

/**
 * @Author  Zackratos
 * @Data    18-5-18
 * @Email   869649339@qq.com
 */
abstract class SeekBarFragment: BaseFragment() {

    // 是否接收 progress 消息的开关
    protected var receiveProgress = true

    override fun initEventAndData() {

        val seekTran = ConfigHelper.getInstance().isSeekTran()
        onSeekBarChange(seekColor(seekTran))

        val statusDisposable = RxBus.getInstance().toObservable(Status::class.java)
                .subscribe {
                    onStatus(it)
                }
        addDisposable(statusDisposable)

        val playDisposable = RxBus.getInstance().toObservable(PlaySong::class.java)
                .subscribe {
                    onPlaySong(it.songInfo, it.newPlay)
                }
        addDisposable(playDisposable)

        val progressDisposable = RxBus.getInstance().toObservable(ProgressChange::class.java)
                .filter { receiveProgress }
                .doOnNext {
                    LogUtil.d("update", "receive")
                }
                .subscribe {
                    onProgressChange(it.progress)
                }
        addDisposable(progressDisposable)

        val seekDisposable = RxBus.getInstance().toObservable(SeekBarChange::class.java)
                .map { seekColor(it.tran) }
                .subscribe { onSeekBarChange(it) }

        addDisposable(seekDisposable)
    }

    @ColorInt
    private fun seekColor(tran: Boolean): Int {
/*        return ContextCompat.getColor(activity, when {
            tran -> android.R.color.transparent
            else -> R.color.half_transparent
        })*/
        return when {
            tran -> Color.TRANSPARENT
            else -> ContextCompat.getColor(activity, R.color.half_transparent)
        }
    }

    protected abstract fun onStatus(status: Status)

    protected abstract fun onPlaySong(songInfo: SongInfo, newPlay: Boolean)

    protected abstract fun onProgressChange(progress: Int)

    protected abstract fun onSeekBarChange(@ColorInt color: Int)

}